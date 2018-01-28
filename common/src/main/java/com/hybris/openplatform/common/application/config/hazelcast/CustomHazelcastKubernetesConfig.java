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
