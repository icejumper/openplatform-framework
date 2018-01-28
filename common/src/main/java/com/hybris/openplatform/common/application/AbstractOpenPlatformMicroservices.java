/*
 * [y] hybris Platform
 *
 * Copyright (c) 2017 SAP SE or an SAP affiliate company.  All rights reserved.
 *
 * This software is the confidential and proprietary information of SAP
 * ("Confidential Information"). You shall not disclose such Confidential
 * Information and shall use it only in accordance with the terms of the
 * license agreement you entered into with SAP.
 */
package com.hybris.openplatform.common.application;

import com.hybris.openplatform.common.application.config.hazelcast.HazelcastConfigProvider;
import com.hybris.openplatform.common.application.config.hazelcast.MapConfigProvider;
import com.hybris.openplatform.common.context.SpringVerticleFactory;
import com.hybris.openplatform.common.vertx.MainMicroServiceVerticle;

import java.util.Objects;

import javax.annotation.PreDestroy;
import javax.annotation.Resource;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.event.EventListener;
import org.springframework.core.env.Environment;

import com.hazelcast.config.Config;
import com.hazelcast.config.GroupConfig;
import com.hazelcast.core.Hazelcast;
import com.hazelcast.core.HazelcastInstance;
import com.hazelcast.spi.properties.GroupProperty;

import io.vertx.core.DeploymentOptions;
import io.vertx.core.Vertx;


@SpringBootApplication
@ComponentScan({ "com.hybris.openplatform.common" })
public abstract class AbstractOpenPlatformMicroservices
{
	private static final Logger LOG = LoggerFactory.getLogger(AbstractOpenPlatformMicroservices.class);

	private MainMicroServiceVerticle mainMicroServiceVerticle;
	private Vertx vertx;
	private SpringVerticleFactory verticleFactory;
	private Environment environment;
	private HazelcastConfigProvider hazelcastConfigProvider;
	private MapConfigProvider mapConfigProvider;

	@Bean
	@Qualifier("dataHazelcastInstance")
	public HazelcastInstance dataHazelcastInstance()
	{
		final Config config = hazelcastConfigProvider.getConfig();

		config.setProperty(GroupProperty.SHUTDOWNHOOK_ENABLED.getName(), "false");
		config.setProperty(GroupProperty.HEARTBEAT_INTERVAL_SECONDS.getName(), "10");

		if (Objects.nonNull(mapConfigProvider))
		{
			mapConfigProvider.getMapConfigList().stream().peek(c -> LOG.info("*** Adding map configuration: {}", c))
					.forEach(config::addMapConfig);
		}
		else
		{
			LOG.error("*** No map configurations found!");
		}

		final GroupConfig groupConfig = new GroupConfig("clusterGroup_" + serviceName(), "");
		config.setGroupConfig(groupConfig);

		config.getNetworkConfig().setReuseAddress(true).setPort(6801).setPortAutoIncrement(true).setPortCount(100);

		return Hazelcast.newHazelcastInstance(config);
	}


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

	protected Environment getEnvironment()
	{
		return environment;
	}

	@Resource
	public void setEnvironment(final Environment environment)
	{
		this.environment = environment;
	}

	@Resource
	public void setVerticleFactory(final SpringVerticleFactory verticleFactory)
	{
		this.verticleFactory = verticleFactory;
	}

	@Resource
	public void setVertx(final Vertx vertx)
	{
		this.vertx = vertx;
	}

	@Resource
	public void setMainMicroServiceVerticle(final MainMicroServiceVerticle mainMicroServiceVerticle)
	{
		this.mainMicroServiceVerticle = mainMicroServiceVerticle;
	}

	@Resource
	public void setHazelcastConfigProvider(final HazelcastConfigProvider hazelcastConfigProvider)
	{
		this.hazelcastConfigProvider = hazelcastConfigProvider;
	}

	@Resource
	public void setMapConfigProvider(final MapConfigProvider mapConfigProvider)
	{
		this.mapConfigProvider = mapConfigProvider;
	}

	protected abstract String serviceName();
}
