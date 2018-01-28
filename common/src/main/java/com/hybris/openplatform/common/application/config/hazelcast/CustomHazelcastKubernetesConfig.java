package com.hybris.openplatform.common.application.config.hazelcast;

import java.util.List;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

import com.google.common.collect.Lists;


@Configuration
@ConfigurationProperties(prefix = "datagrid.hazelcast.configuration.kubernetes.custom")
public class CustomHazelcastKubernetesConfig
{

	private List<String> sources = Lists.newArrayList();

	public List<String> getSources()
	{
		return sources;
	}
}
