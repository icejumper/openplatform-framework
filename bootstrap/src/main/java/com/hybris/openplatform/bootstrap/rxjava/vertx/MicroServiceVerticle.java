package com.hybris.openplatform.bootstrap.rxjava.vertx;

import com.hybris.openplatform.bootstrap.messages.MessageAddresses;
import com.hybris.openplatform.bootstrap.messages.RestEndpointRegistration;

import java.util.List;
import java.util.Objects;
import java.util.Set;

import javax.annotation.Resource;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import com.google.common.collect.Lists;
import com.google.common.collect.Sets;

import io.vertx.core.Handler;
import io.vertx.core.eventbus.DeliveryOptions;
import io.vertx.core.http.HttpMethod;
import io.vertx.core.json.Json;
import io.vertx.core.json.JsonObject;
import io.vertx.rxjava.core.AbstractVerticle;
import io.vertx.rxjava.core.Future;
import io.vertx.rxjava.core.Vertx;
import io.vertx.rxjava.core.eventbus.EventBus;
import io.vertx.rxjava.core.eventbus.Message;
import io.vertx.rxjava.core.eventbus.MessageConsumer;
import io.vertx.rxjava.ext.web.Router;
import io.vertx.rxjava.ext.web.RoutingContext;
import io.vertx.rxjava.ext.web.handler.BodyHandler;
import io.vertx.rxjava.ext.web.handler.CorsHandler;
import io.vertx.rxjava.servicediscovery.ServiceDiscovery;
import io.vertx.servicediscovery.Record;
import io.vertx.servicediscovery.ServiceDiscoveryOptions;
import io.vertx.servicediscovery.types.EventBusService;
import io.vertx.servicediscovery.types.HttpEndpoint;
import io.vertx.servicediscovery.types.MessageSource;
import rx.Single;


@Component
public abstract class MicroServiceVerticle extends AbstractVerticle
{

	private static final Logger LOG = LoggerFactory.getLogger(MicroServiceVerticle.class);
	private static final String DEFAULT_ROOT_CONTEXT = "/";

	protected Router mainRxRouter;
	protected Router router;
	private ServiceDiscovery discovery;
	private Set<Record> registeredRecords = Sets.newConcurrentHashSet();

	@Override
	public void start()
	{
		LOG.debug("Running verticle [{}] in the thread {}", this.getClass().getName(), Thread.currentThread().getName());
		discovery = ServiceDiscovery.create(vertx, new ServiceDiscoveryOptions().setBackendConfiguration(config()));
		// create sub-router only if the main router was initialized
		if (Objects.nonNull(mainRxRouter))
		{
			router = Router.router(vertx);
			LOG.debug("Mounting sub-router to {}", getRootContext());
			mainRxRouter.mountSubRouter(getRootContext(), router);
			router.route().handler(BodyHandler.create());

			router.get("/").handler(rc -> rc.response().end(getRootContext() + " microservice is alive"));
		}
		registerVerticleEndPoints();
	}

	protected abstract void registerVerticleEndPoints();

	public Single<Message<String>> rxRegisterService(final RestEndpointRegistration registration)
	{
		return rxSendMessage(MessageAddresses.REGISTER_REST_END_POINT, Json.encode(registration));
	}

	public Single<Record> publishHttpEndpoint(String name, String host, int port)
	{
		final Record record = HttpEndpoint.createRecord(name, host, port, "/");
		return rxPublish(record);
	}

	public Single<Record> rxUnpublishHttpEndpoint(final String name)
	{
		return getDiscovery().rxGetRecord(new JsonObject().put("name", name))
				.doOnSuccess(r -> doUnregisterHttpEndpoint(name, r))
				.doOnError(t -> {
					LOG.info("The record with name {} could not be found: nothing to unpublish. {}", name, t);
					Future.failedFuture(t);
				});
	}

	private void doUnregisterHttpEndpoint(final String name, final Record record)
	{
		getDiscovery().rxUnpublish(record.getRegistration())
				.doOnSuccess(aVoid -> LOG.info("The record with name {} was successfully unpublished", name))
				.doOnError(t -> {
					LOG.info("The record with name {} could not be unpublished: {}", name, t);
					Future.failedFuture(t);
				}).subscribe();
	}

	public Single<Record> publishMessageSource(String name, String address, Class<?> contentClass)
	{
		final Record record = MessageSource.createRecord(name, address, contentClass);
		return rxPublish(record);
	}

	public Single<Record> publishMessageSource(String name, String address)
	{
		final Record record = MessageSource.createRecord(name, address);
		return rxPublish(record);
	}

	public <T> void publishMessage(final String address, final T message)
	{
		final EventBus eventBus = vertx.eventBus();
		LOG.debug("Publishing message [{}] to address [{}]", message, address);
		eventBus.publish(address, message);
	}

	protected <T> Single<Message<T>> rxSendMessage(final String address, final T message)
	{
		final EventBus eventBus = vertx.eventBus();
		LOG.debug("Sending message [{}] to address [{}]", message, address);
		return eventBus.rxSend(address, message);
	}

	protected <T> Single<Message<T>> rxSendMessage(final String address, final T message, final DeliveryOptions deliveryOptions)
	{
		final EventBus eventBus = vertx.eventBus();
		LOG.debug("Sending message [{}] to address [{}]", message, address);
		return eventBus.rxSend(address, message, deliveryOptions);
	}

	protected <T> MessageConsumer<T> rxRegisterConsumer(final String address)
	{
		final EventBus eventBus = vertx.eventBus();
		LOG.debug("Registering event bus consumer on address [{}]", address);
		return eventBus.consumer(address);
	}

	protected <T> MessageConsumer<T> rxRegisterLocalConsumer(final String address)
	{
		final EventBus eventBus = vertx.eventBus();
		LOG.debug("Registering event bus local consumer on address [{}]", address);
		return eventBus.localConsumer(address);
	}

	public Single<Record> publishEventBusService(String name, String address, Class<?> serviceClass)
	{
		final Record record = EventBusService.createRecord(name, address, serviceClass);
		return rxPublish(record);
	}

	protected Single<Record> rxPublish(Record record)
	{
		if (discovery == null)
		{
			try
			{
				start();
			}
			catch (Exception e)
			{
				throw new IllegalStateException("Cannot create discovery service");
			}
		}

		return discovery.rxPublish(record).doOnSuccess(r -> registeredRecords.add(r));
	}

	public ServiceDiscovery getDiscovery()
	{
		return discovery;
	}

	protected void registerGetRequestWithCors(final RestEndpointRegistration restEndpointRegistration,
			final Handler<RoutingContext> routingContextHandler)
	{
		addCorsHandlerIfAllowed(HttpMethod.GET, restEndpointRegistration.getAccessControlAllowOrigin());
		router.get(restEndpointRegistration.getPattern()).consumes(restEndpointRegistration.getAcceptedContentType())
				.handler(routingContextHandler);
	}

	protected void registerGetRequest(final RestEndpointRegistration restEndpointRegistration,
			final Handler<RoutingContext> routingContextHandler)
	{
		router.get(restEndpointRegistration.getPattern()).consumes(restEndpointRegistration.getAcceptedContentType())
				.handler(routingContextHandler);
	}

	protected void registerGetRequest(final String resourcePath, final String expectedContentType,
			final Handler<RoutingContext> routingContextHandler)
	{
		router.get(resourcePath).consumes(expectedContentType).handler(routingContextHandler);
	}

	protected void registerPostRequestWithCors(final RestEndpointRegistration restEndpointRegistration,
			final Handler<RoutingContext> routingContextHandler)
	{
		addCorsHandlerIfAllowed(HttpMethod.POST, restEndpointRegistration.getAccessControlAllowOrigin());
		router.post(restEndpointRegistration.getPattern()).consumes(restEndpointRegistration.getAcceptedContentType())
				.handler(routingContextHandler);
	}

	protected void registerPostRequest(final RestEndpointRegistration restEndpointRegistration,
			final Handler<RoutingContext> routingContextHandler)
	{
		router.post(restEndpointRegistration.getPattern()).consumes(restEndpointRegistration.getAcceptedContentType())
				.handler(routingContextHandler);
	}

	protected void registerHeadRequestWithCors(final RestEndpointRegistration restEndpointRegistration,
			final Handler<RoutingContext> routingContextHandler)
	{
		addCorsHandlerIfAllowed(HttpMethod.HEAD, restEndpointRegistration.getAccessControlAllowOrigin());
		router.head(restEndpointRegistration.getPattern()).consumes(restEndpointRegistration.getAcceptedContentType())
				.handler(routingContextHandler);
	}

	private void addCorsHandlerIfAllowed(final HttpMethod httpMethod, final String allowedOriginPattern)
	{
		CorsHandler corsHandler;
		if (!StringUtils.isEmpty(allowedOriginPattern))
		{
			corsHandler = CorsHandler.create(allowedOriginPattern)
					.allowedMethod(httpMethod)
					.allowedMethod(HttpMethod.OPTIONS)
					.allowedHeader("X-PINGARUNER")
					.allowedHeader("Content-Type");
			router.route().handler(corsHandler);
		}
	}

	protected String getRootContext()
	{
		return DEFAULT_ROOT_CONTEXT;
	}

	public Vertx getRxVertx()
	{
		return vertx;
	}

	@Override
	public void stop(io.vertx.core.Future<Void> future) throws Exception
	{
		final List<io.vertx.core.Future> futures = Lists.newArrayList();
		for (Record record : registeredRecords)
		{
			final io.vertx.core.Future<Void> unregistrationFuture = io.vertx.core.Future.future();
			futures.add(unregistrationFuture);
			unregistrationFuture.setHandler(ar -> {
				if (ar.succeeded())
				{
					LOG.info("Unregistering record: {}", record.toJson().encodePrettily());
				}
				else if (ar.failed())
				{
					LOG.error("Error occured while unregistering record: {}", ar.cause());
				}
			});
			getDiscovery().unpublish(record.getRegistration(), unregistrationFuture);
		}

		if (futures.isEmpty())
		{
			getDiscovery().close();
			future.complete();
		}
		else
		{
			final io.vertx.core.CompositeFuture composite = io.vertx.core.CompositeFuture.all(futures);
			composite.setHandler(ar -> {
				getDiscovery().close();
				if (ar.failed())
				{
					future.fail(ar.cause());
				}
				else
				{
					future.complete();
				}
			});
		}
	}

	protected Router getMainRxRouter()
	{
		return mainRxRouter;
	}

	@Resource
	public void setMainRxRouter(final Router mainRxRouter)
	{
		this.mainRxRouter = mainRxRouter;
	}
}
