package com.hybris.openplatform.common.rxjava.vertx;

import com.hybris.openplatform.bootstrap.rxjava.vertx.MicroServiceVerticle;
import com.hybris.openplatform.bootstrap.vertx.CompositionVerticle;
import com.hybris.openplatform.common.deployment.VerticleDeployer;
import com.hybris.openplatform.stereotypes.MainVerticle;

import java.util.Objects;
import java.util.UUID;

import javax.annotation.Resource;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;

import io.vertx.core.Future;
import io.vertx.rxjava.core.http.HttpServer;
import io.vertx.rxjava.ext.web.Router;
import io.vertx.rxjava.ext.web.handler.CookieHandler;
import io.vertx.rxjava.ext.web.handler.SessionHandler;
import io.vertx.rxjava.ext.web.handler.StaticHandler;
import io.vertx.rxjava.ext.web.sstore.LocalSessionStore;
import io.vertx.servicediscovery.Record;
import rx.Single;


@MainVerticle(value = "mainMicroServiceRxVerticle")
public class MainMicroServiceVerticle extends MicroServiceVerticle implements CompositionVerticle
{
	private static final String HOST_NAME = "localhost";

	@Value("${vertx.http.port:0}")
	private int vertxPort;
	@Value("${vertx.microservice.name:#{null}}")
	private String micriserviceName;

	private VerticleDeployer verticleDeploymentService;

	private HttpServer httpServer;

	private static final Logger LOG = LoggerFactory.getLogger(MainMicroServiceVerticle.class);

	@Override
	public void start(final Future<Void> future) throws Exception
	{
		enableLocalSession(mainRxRouter);
		enableStaticHandler(mainRxRouter);

		mainRxRouter.get("/openplatform").handler(rc -> rc.response().end("Openplatform is alive"));

		httpServer = vertx.createHttpServer()
				.requestHandler(mainRxRouter::accept)
				.listen(vertxPort, ar -> {
					if (ar.succeeded())
					{
						LOG.info("MainVerticle Server started");
						publishGateway(future);
					}
					else
					{
						LOG.error("Cannot start the MainVerticle server: {}", ar.cause());
						future.fail(ar.cause());
					}
				});

	}

	@Override
	protected void registerVerticleEndPoints()
	{
		// any additional end points should be registered here
	}

	protected void enableStaticHandler(final Router router)
	{
		// Serve the static pages from directory 'static'
		router.route("/static/*").handler(StaticHandler.create().setCachingEnabled(false).setWebRoot("static"));
	}

	protected void enableLocalSession(final Router router)
	{
		router.route().handler(CookieHandler.create());
		router.route().handler(SessionHandler.create(
				LocalSessionStore.create(vertx, "shopping.user.session")));
	}

	protected void publishGateway(final Future<Void> future)
	{
		LOG.info("Publishing http service to http://{}:{}/", HOST_NAME, httpServer.actualPort());
		if(Objects.isNull(micriserviceName))
		{
			micriserviceName = "service-" + UUID.randomUUID().toString();
		}
		publishHttpEndpoint(micriserviceName, HOST_NAME, httpServer.actualPort())
			.doOnSuccess(r -> {
				LOG.info("{} (Rest endpoint) service published", micriserviceName);
				deploySubVerticles();
				future.complete();
			})
			.doOnError(t -> {
				LOG.error("Exception occured: {}", t);
				future.fail(t);
			}).subscribe();
	}

	public Single<Record> unpublishGateway()
	{
		LOG.info("Unpublishing active http service http://{}:{}/", HOST_NAME, httpServer.actualPort());
		return rxUnpublishHttpEndpoint(micriserviceName);
	}

	@Override
	public void deploySubVerticles()
	{
		verticleDeploymentService.preDeployVerticles();
		verticleDeploymentService.deployVerticles();
		verticleDeploymentService.postDeployVerticles();
	}

	@Resource
	public void setVerticleDeploymentService(final VerticleDeployer verticleDeploymentService)
	{
		this.verticleDeploymentService = verticleDeploymentService;
	}
}
