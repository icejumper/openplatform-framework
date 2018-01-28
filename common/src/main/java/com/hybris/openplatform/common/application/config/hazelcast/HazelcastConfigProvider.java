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
package com.hybris.openplatform.common.application.config.hazelcast;

import com.hybris.openplatform.common.context.SpringProfileDiscovery;

import java.util.List;
import java.util.Optional;

import javax.annotation.Resource;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;

import com.hazelcast.config.Config;
import com.hazelcast.config.InMemoryXmlConfig;


@Component
public class HazelcastConfigProvider
{
	private static final Logger LOGGER = LoggerFactory.getLogger(HazelcastConfigProvider.class);

	private Environment env;
	private CustomHazelcastConfig customHazelcastConfig;
	private CustomEventbusHazelcastConfig customEventbusHazelcastConfig;
	private CustomHazelcastKubernetesConfig customHazelcastKubernetesConfig;
	private SpringProfileDiscovery springProfileDiscovery;

	public Config getConfig()
	{
		return new InMemoryXmlConfig(getInMemoryConfigXml());
	}

	public Optional<Config> getEventbusConfig()
	{
		final String inMemoryEventbusConfigXml = getInMemoryEventbusConfigXml();
		if(!StringUtils.isEmpty(inMemoryEventbusConfigXml))
		{
			return Optional.of(new InMemoryXmlConfig(inMemoryEventbusConfigXml));
		}
		return Optional.empty();
	}

	private String getInMemoryConfigXml()
	{
		final StringBuilder configStringBuilder = new StringBuilder("<?xml version=\"1.0\" encoding=\"UTF-8\"?>");

		configStringBuilder
				.append("<hazelcast ")
				.append("xsi:schemaLocation=\"http://www.hazelcast.com/schema/config hazelcast-config-3.6.xsd\"\n"
						+ "\t\t   xmlns=\"http://www.hazelcast.com/schema/config\"\n"
						+ "\t\t   xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\"").append(">");

		String datagridClusterConfig = env.getProperty("datagrid.hazelcast.configuration.default");
		if(springProfileDiscovery.isProfileKubernates())
		{
			datagridClusterConfig = env.getProperty("datagrid.hazelcast.configuration.kubernates");
		}
		LOGGER.info("Configuring hazelcast datagrid cluster with configuration {}", datagridClusterConfig);

		configStringBuilder
				.append("<import resource=\"")
				.append("classpath:")
				.append(datagridClusterConfig)
				.append("\"/>");

		final List<String> appConfigXmls = customHazelcastConfig.getSources();
		if (!CollectionUtils.isEmpty(appConfigXmls))
		{
			for (String appConfigXml : appConfigXmls)
			{
				configStringBuilder
						.append("<import resource=\"")
						.append("classpath:")
						.append(appConfigXml)
						.append("\"/>");
			}
		}

		if(springProfileDiscovery.isProfileKubernates())
		{
			final List<String> appKubernetesConfigXmls = customHazelcastKubernetesConfig.getSources();
			if (!CollectionUtils.isEmpty(appKubernetesConfigXmls))
			{
				for (String appKubernetesConfigXml : appKubernetesConfigXmls)
				{
					LOGGER.info("Adding hazelcast datagrid configuration {}", appKubernetesConfigXml);
					configStringBuilder
							.append("<import resource=\"")
							.append("classpath:")
							.append(appKubernetesConfigXml)
							.append("\"/>");
				}
			}
		}

		configStringBuilder.append("</hazelcast>");
		return configStringBuilder.toString();
	}

	private String getInMemoryEventbusConfigXml()
	{
		final StringBuilder configStringBuilder = new StringBuilder("<?xml version=\"1.0\" encoding=\"UTF-8\"?>");

		configStringBuilder
				.append("<hazelcast ")
				.append("xsi:schemaLocation=\"http://www.hazelcast.com/schema/config hazelcast-config-3.6.xsd\"\n"
						+ "\t\t   xmlns=\"http://www.hazelcast.com/schema/config\"\n"
						+ "\t\t   xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\"").append(">");

		String vertxEventbusClusterConfig = env.getProperty("vertx.eventbus.cluster.config.default");
		if(springProfileDiscovery.isProfileKubernates())
		{
			vertxEventbusClusterConfig = env.getProperty("vertx.eventbus.cluster.config.kubernates");
		}
		LOGGER.info("Configuring hazelcast cluster with configuration {}", vertxEventbusClusterConfig);

		configStringBuilder
				.append("<import resource=\"")
				.append("classpath:")
				.append(vertxEventbusClusterConfig)
				.append("\"/>");

		if(springProfileDiscovery.isProfileKubernates())
		{
			final List<String> appConfigXmls = customEventbusHazelcastConfig.getSources();
			if (!CollectionUtils.isEmpty(appConfigXmls))
			{
				for (String appConfigXml : appConfigXmls)
				{
					LOGGER.info("Adding hazelcast eventbus configuration {}", appConfigXml);
					configStringBuilder
							.append("<import resource=\"")
							.append("classpath:")
							.append(appConfigXml)
							.append("\"/>");
				}
			}
			else
			{
				return "";
			}
		}

		configStringBuilder.append("</hazelcast>");
		return configStringBuilder.toString();
	}

	@Resource
	public void setEnv(final Environment env)
	{
		this.env = env;
	}

	@Resource
	public void setCustomHazelcastConfig(final CustomHazelcastConfig customHazelcastConfig)
	{
		this.customHazelcastConfig = customHazelcastConfig;
	}

	@Resource
	public void setCustomEventbusHazelcastConfig(final CustomEventbusHazelcastConfig customEventbusHazelcastConfig)
	{
		this.customEventbusHazelcastConfig = customEventbusHazelcastConfig;
	}

	@Resource
	public void setCustomHazelcastKubernetesConfig(
			final CustomHazelcastKubernetesConfig customHazelcastKubernetesConfig)
	{
		this.customHazelcastKubernetesConfig = customHazelcastKubernetesConfig;
	}

	@Resource
	public void setSpringProfileDiscovery(final SpringProfileDiscovery springProfileDiscovery)
	{
		this.springProfileDiscovery = springProfileDiscovery;
	}
}
