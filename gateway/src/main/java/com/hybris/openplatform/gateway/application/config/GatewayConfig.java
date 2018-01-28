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
package com.hybris.openplatform.gateway.application.config;

import com.hybris.openplatform.gateway.persistence.RegistrationMapStore;

import javax.annotation.Resource;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration;
import org.springframework.boot.autoconfigure.orm.jpa.HibernateJpaAutoConfiguration;
import org.springframework.boot.orm.jpa.EntityScan;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.core.env.Environment;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

import com.hazelcast.config.MapConfig;
import com.hazelcast.config.MapStoreConfig;
import com.hazelcast.config.NearCacheConfig;


@Configuration
@Profile("persist")
@EnableJpaRepositories({ "com.hybris.openplatform.gateway.persistence.repositories" })
@EntityScan({ "com.hybris.openplatform.bootstrap.messages" })
@EnableAutoConfiguration
public class GatewayConfig
{

	private static final Logger LOGGER = LoggerFactory.getLogger(GatewayConfig.class);
	private Environment env;

	@Bean
	public MapConfig cartsMapConfig(final RegistrationMapStore registrationMapStore)
	{
		final MapConfig mapConfig = new MapConfig();
		mapConfig.setName(env.getProperty("storage.registrations.name"));
		mapConfig.setAsyncBackupCount(0);
		final MapStoreConfig mapStoreConfig = new MapStoreConfig();
		mapStoreConfig.setEnabled(true);
		mapStoreConfig.setWriteBatchSize(20);
		mapStoreConfig.setWriteDelaySeconds(1);
		mapStoreConfig.setClassName(RegistrationMapStore.class.getName());
		LOGGER.info("*** Setting {} as Registration Map Store", registrationMapStore);
		mapStoreConfig.setImplementation(registrationMapStore);
		// in the cluster let's use EAGER initial data loading
		mapStoreConfig.setInitialLoadMode(MapStoreConfig.InitialLoadMode.EAGER);

		final NearCacheConfig nearCacheConfig = new NearCacheConfig();
		nearCacheConfig.setMaxIdleSeconds(120).setTimeToLiveSeconds(300);
		mapConfig.setNearCacheConfig(nearCacheConfig);

		mapConfig.setMapStoreConfig(mapStoreConfig);
		return mapConfig;
	}

	@Resource
	public void setEnv(final Environment env)
	{
		this.env = env;
	}
}
