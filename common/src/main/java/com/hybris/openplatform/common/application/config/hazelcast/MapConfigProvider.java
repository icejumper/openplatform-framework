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
