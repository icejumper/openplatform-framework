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

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

import com.google.common.collect.Lists;
import com.hazelcast.config.MapConfig;


@Component
public class MapConfigProvider
{

	private List<MapConfig> mapConfigList = Lists.newArrayList();

	public List<MapConfig> getMapConfigList()
	{
		return mapConfigList;
	}

	@Autowired(required = false)
	@Profile("persist")
	public void setMapConfigList(final List<MapConfig> mapConfigList)
	{
		this.mapConfigList = mapConfigList;
	}
}
