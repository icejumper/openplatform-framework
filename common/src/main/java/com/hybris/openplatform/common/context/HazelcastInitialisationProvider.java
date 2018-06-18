package com.hybris.openplatform.common.context;

import com.hybris.openplatform.common.application.config.hazelcast.HazelcastConfigProvider;

import java.util.Optional;

import javax.annotation.Resource;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;

import com.hazelcast.config.Config;
import com.hazelcast.config.GroupConfig;
import com.hazelcast.core.Hazelcast;
import com.hazelcast.core.HazelcastInstance;
import com.hazelcast.spi.properties.GroupProperty;


@Configuration
@ComponentScan({ "com.hybris.openplatform.common" })
public class HazelcastInitialisationProvider
{

	private static final Logger LOGGER = LoggerFactory.getLogger(HazelcastInitialisationProvider.class);

	private TenantContext tenantContext;
	private HazelcastConfigProvider hazelcastConfigProvider;


	@Bean
	@Qualifier("eventBusHazelcastInstance")
	public HazelcastInstance eventBusHazelcastInstance()
	{
		final Optional<Config> eventbusConfig = hazelcastConfigProvider.getEventbusConfig();

		if(eventbusConfig.isPresent())
		{
			final Config config = eventbusConfig.get();
			config.setProperty(GroupProperty.SHUTDOWNHOOK_ENABLED.getName(), "false");
			config.setProperty(GroupProperty.HEARTBEAT_INTERVAL_SECONDS.getName(), "10");

			final String tenantName = tenantContext.getName();
			final GroupConfig groupConfig = new GroupConfig("clusterGroup_" + tenantName, "");
			config.setGroupConfig(groupConfig);

			config.getNetworkConfig()
					//.setReuseAddress(true)
					.setPort(5701)
					.setPortAutoIncrement(true)
					.setPortCount(10);

			return Hazelcast.newHazelcastInstance(config);
		}
		else
		{
			LOGGER.error("Event bus hazelcast instance was not initialized properly: returning NULL");
			return null;
		}
	}

	@Resource
	public void setTenantContext(final TenantContext tenantContext)
	{
		this.tenantContext = tenantContext;
	}

	@Resource
	public void setHazelcastConfigProvider(final HazelcastConfigProvider hazelcastConfigProvider)
	{
		this.hazelcastConfigProvider = hazelcastConfigProvider;
	}
}
