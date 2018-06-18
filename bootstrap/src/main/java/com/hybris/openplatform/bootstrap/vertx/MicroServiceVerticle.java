package com.hybris.openplatform.bootstrap.vertx;

import com.hybris.openplatform.bootstrap.constants.Constants;
import com.hybris.openplatform.bootstrap.handlers.AsyncRegistrationHandlerProvider;
import com.hybris.openplatform.bootstrap.messages.MessageAddresses;
import com.hybris.openplatform.bootstrap.messages.RestEndpointRegistration;

import java.util.List;
import java.util.Objects;
import java.util.Set;

import javax.annotation.Resource;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import com.google.common.collect.Lists;
import com.google.common.collect.Sets;

import io.vertx.core.AbstractVerticle;
import io.vertx.core.AsyncResult;
import io.vertx.core.CompositeFuture;
import io.vertx.core.Future;
import io.vertx.core.Handler;
import io.vertx.core.eventbus.EventBus;
import io.vertx.core.eventbus.Message;
import io.vertx.core.json.Json;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.web.Router;
import io.vertx.ext.web.RoutingContext;
import io.vertx.ext.web.handler.BodyHandler;
import io.vertx.servicediscovery.Record;
import io.vertx.servicediscovery.ServiceDiscovery;
import io.vertx.servicediscovery.ServiceDiscoveryOptions;
import io.vertx.servicediscovery.types.EventBusService;
import io.vertx.servicediscovery.types.HttpEndpoint;
import io.vertx.servicediscovery.types.MessageSource;


@Component
public abstract class MicroServiceVerticle extends AbstractVerticle
{

	private static final Logger LOG = LoggerFactory.getLogger(MicroServiceVerticle.class);
	private static final String DEFAULT_ROOT_CONTEXT = "/";

	protected Router mainRouter;
	protected Router router;
	private ServiceDiscovery discovery;
	private Set<Record> registeredRecords = Sets.newConcurrentHashSet();

	@Override
	public void start()
	{
		LOG.info("Running verticle [{}] in the thread {}", this.getClass().getName(), Thread.currentThread().getName());
		discovery = ServiceDiscovery.create(vertx, new ServiceDiscoveryOptions().setBackendConfiguration(config()));
		// create sub-router only if the main router was initialized
		if (Objects.nonNull(mainRouter))
		{
			router = Router.router(vertx);
			LOG.info("Mounting sub-router to {}", getRootContext());
			mainRouter.mountSubRouter(getRootContext(), router);
			router.route().handler(BodyHandler.create());

			router.get("/").handler(rc -> rc.response().end(getRootContext() + " microservice is alive"));
		}
		registerVerticleEndPoints();
	}

	protected abstract void registerVerticleEndPoints();

	protected void registerService(final RestEndpointRegistration registration,
			final AsyncRegistrationHandlerProvider registrationHandlerProvider)
	{
		sendMessage(MessageAddresses.REGISTER_REST_END_POINT, Json.encode(registration),
				registrationHandlerProvider.handle(super.vertx, registration));
	}

	public void publishHttpEndpoint(String name, String host, int port, Handler<AsyncResult<Void>>
			completionHandler)
	{
		final Record record = HttpEndpoint.createRecord(name, host, port, "/");
		publish(record, completionHandler);
	}

	public void unpublishHttpEndpoint(String name, Handler<AsyncResult<Void>>
			completionHandler)
	{
		getDiscovery().getRecord(new JsonObject().put("name", name), ar -> {
			if (ar.succeeded())
			{
				final Record httpEndpointRecord = ar.result();
				getDiscovery().unpublish(httpEndpointRecord.getRegistration(), unpublishAr -> {
					if (unpublishAr.succeeded())
					{
						LOG.info("The record with name {} was successfully unpublished", name);
						completionHandler.handle(unpublishAr);
					}
					else
					{
						LOG.info("The record with name {} could not be unpublished: {}", name, unpublishAr.cause());
						Future.failedFuture(unpublishAr.cause());
					}
				});
			}
			else
			{
				LOG.info("The record with name {} could not be found: nothing to unpublish. {}", name, ar.cause());
				Future.failedFuture(ar.cause());
			}
		});
	}

	public void publishMessageSource(String name, String address, Class<?> contentClass, Handler<AsyncResult<Void>>
			completionHandler)
	{
		final Record record = MessageSource.createRecord(name, address, contentClass);
		publish(record, completionHandler);
	}

	public void publishMessageSource(String name, String address, Handler<AsyncResult<Void>>
			completionHandler)
	{
		final Record record = MessageSource.createRecord(name, address);
		publish(record, completionHandler);
	}

	protected void sendMessage(final String address, final String message,
			final Handler<AsyncResult<Message<String>>> replyHandler)
	{
		final EventBus eventBus = vertx.eventBus();
		LOG.debug("Sending message [{}] to address [{}]", message, address);
		eventBus.send(address, message, replyHandler);
	}

	protected void registerConsumer(final String address, final Handler<Message<String>> messageHandler)
	{
		final EventBus eventBus = vertx.eventBus();
		LOG.debug("Registering event bus consumer on address [{}]", address);
		eventBus.consumer(address, messageHandler);
	}

	public void publishEventBusService(String name, String address, Class<?> serviceClass, Handler<AsyncResult<Void>>
			completionHandler)
	{
		final Record record = EventBusService.createRecord(name, address, serviceClass);
		publish(record, completionHandler);
	}

	protected void publish(Record record, Handler<AsyncResult<Void>> completionHandler)
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

		discovery.publish(record, ar -> {
			if (ar.succeeded())
			{
				registeredRecords.add(record);
			}
			completionHandler.handle(ar.map((Void) null));
		});
	}

	public ServiceDiscovery getDiscovery()
	{
		return discovery;
	}

	protected void registerGetRequest(final String resourcePath, final Handler<RoutingContext> routingContextHandler)
	{
		router.get(resourcePath).handler(routingContextHandler);
	}

	protected void registerGetRequest(final String resourcePath, final String expectedContentType,
			final Handler<RoutingContext> routingContextHandler)
	{
		router.get(resourcePath).consumes(expectedContentType).handler(routingContextHandler);
	}

	protected void registerPostRequest(final String resourcePath, final Handler<RoutingContext> routingContextHandler)
	{
		router.post(resourcePath).consumes(Constants.HTTP_CONTENT_TYPE_APPLICATION_JSON).handler(routingContextHandler);
	}

	protected String getRootContext()
	{
		return DEFAULT_ROOT_CONTEXT;
	}

	@Override
	public void stop(Future<Void> future) throws Exception
	{
		final List<Future> futures = Lists.newArrayList();
		for (Record record : registeredRecords)
		{
			final Future<Void> unregistrationFuture = Future.future();
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
			final CompositeFuture composite = CompositeFuture.all(futures);
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

	protected Router getMainRouter()
	{
		return mainRouter;
	}

	@Resource
	public void setMainRouter(final Router mainRouter)
	{
		this.mainRouter = mainRouter;
	}
}
