package com.hybris.openplatform.common.application;

import com.hybris.openplatform.common.context.SpringVerticleFactory;
import com.hybris.openplatform.common.vertx.MainMicroServiceVerticle;

import javax.annotation.PreDestroy;
import javax.annotation.Resource;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.event.EventListener;

import io.vertx.core.DeploymentOptions;
import io.vertx.core.Vertx;


@SpringBootApplication
@ComponentScan({ "com.hybris.openplatform.common" })
public abstract class BasicAbstractOpenPlatformMicroservices
{
	private static final Logger LOG = LoggerFactory.getLogger(BasicAbstractOpenPlatformMicroservices.class);

	private MainMicroServiceVerticle mainMicroServiceVerticle;
	private Vertx vertx;
	private SpringVerticleFactory verticleFactory;

	@PreDestroy
	public void preDestroy()
	{
		vertx.close(ar1 -> {
			LOG.info("Shutting down gracefully Hazelcast instance");
			// make the HZC instance gracefully shutdown here
		});
	}

	/**
	 * Deploy verticles when the Spring application is ready.
	 */
	@EventListener
	void deployVerticles(ApplicationReadyEvent event)
	{
		vertx.registerVerticleFactory(verticleFactory);
		final DeploymentOptions deploymentOptions = new DeploymentOptions()
				.setWorker(true)
				.setInstances(1);
		vertx.deployVerticle(mainMicroServiceVerticle, deploymentOptions);
	}

	@Resource
	public void setMainMicroServiceVerticle(final MainMicroServiceVerticle mainMicroServiceVerticle)
	{
		this.mainMicroServiceVerticle = mainMicroServiceVerticle;
	}

	@Resource
	public void setVertx(final Vertx vertx)
	{
		this.vertx = vertx;
	}

	@Resource
	public void setVerticleFactory(final SpringVerticleFactory verticleFactory)
	{
		this.verticleFactory = verticleFactory;
	}
}
