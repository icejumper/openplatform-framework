package com.hybris.openplatform.common.vertx;

import com.hybris.openplatform.bootstrap.vertx.CompositionVerticle;
import com.hybris.openplatform.bootstrap.vertx.MicroServiceVerticle;
import com.hybris.openplatform.common.deployment.VerticleDeployer;
import com.hybris.openplatform.stereotypes.MainVerticle;

import java.util.Objects;
import java.util.UUID;

import javax.annotation.Resource;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Profile;
import org.springframework.core.env.Environment;

import io.vertx.core.AsyncResult;
import io.vertx.core.Future;
import io.vertx.core.Handler;
import io.vertx.core.http.HttpServer;
import io.vertx.core.http.HttpServerOptions;
import io.vertx.core.net.JksOptions;
import io.vertx.ext.web.Router;
import io.vertx.ext.web.handler.CookieHandler;
import io.vertx.ext.web.handler.SessionHandler;
import io.vertx.ext.web.handler.StaticHandler;
import io.vertx.ext.web.sstore.LocalSessionStore;


@MainVerticle(value = "mainMicroServiceVerticle")
@Profile("no-rx-vertx")
public class MainMicroServiceVerticle extends MicroServiceVerticle implements CompositionVerticle
{
	private static final Logger LOG = LoggerFactory.getLogger(MainMicroServiceVerticle.class);
	private static final String HOST_NAME = "localhost";

	@Value("${vertx.http.port:0}")
	private int vertxPort;
	@Value("${vertx.microservice.name:#{null}}")
	private String micriserviceName;

	private VerticleDeployer verticleDeploymentService;

	private HttpServer httpServer;
	private Environment env;

	@Override
	public void start(final Future<Void> future) throws Exception
	{
		enableLocalSession(mainRouter);
		enableStaticHandler(mainRouter);

		mainRouter.get("/openplatform").handler(rc -> rc.response().end("Openplatform is alive"));

		final HttpServerOptions httpOpts = new HttpServerOptions();
		// Use a Java Keystore File
		final String certPath = env.getProperty("server.certificate_path", "");
		if (certPath.toLowerCase().endsWith("jks"))
		{
			httpOpts.setKeyStoreOptions(new JksOptions()
					.setPassword("password")
					.setPath(certPath));
			LOG.info("Setting up SSL for HTTP");
			httpOpts.setSsl(true);
		}

		httpServer = getVertx().createHttpServer(httpOpts)
				.requestHandler(mainRouter::accept)
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
		publishHttpEndpoint(micriserviceName, HOST_NAME, httpServer.actualPort(), ar -> {
			if (ar.failed())
			{
				LOG.error("Exception occured: {}", ar.cause());
				future.fail(ar.cause());
			}
			else
			{
				LOG.info("{} (Rest endpoint) service published : {}", micriserviceName, ar.succeeded());
				deploySubVerticles();
				future.complete();
			}
		});
	}

	public void unpublishGateway(final Handler<AsyncResult<Void>> handler)
	{
		LOG.info("Unpublishing active http service http://{}:{}/", HOST_NAME, httpServer.actualPort());
		unpublishHttpEndpoint(micriserviceName, handler);
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

	@Resource
	public void setEnv(final Environment env)
	{
		this.env = env;
	}
}
