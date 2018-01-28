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
package com.hybris.openplatform.common.service.impl;

import com.hybris.openplatform.common.service.StorageAwareService;

import java.util.Collection;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;

import javax.annotation.Resource;

import com.hazelcast.core.HazelcastInstance;
import com.hazelcast.core.IMap;


public abstract class AbstractStorageAwareService<T, K> implements StorageAwareService<T, K>
{
	private HazelcastInstance dataHazelcastInstance;

	@Override
	public Optional<T> storeItem(final Function<Map<K, T>, T> itemFunction)
	{
		final Map<K, T> itemMap = dataHazelcastInstance.getMap(getStorageName());
		return Optional.ofNullable(itemFunction.apply(itemMap));
	}

	@Override
	public Collection<T> getItems(final Function<Map<K, T>, Collection<T>> itemFunction)
	{
		final Map<K, T> itemMap = dataHazelcastInstance.getMap(getStorageName());
		return itemFunction.apply(itemMap);
	}

	@Override
	public Collection<T> getItemsByPredicate(final Function<IMap<K, T>, Collection<T>> itemFunction)
	{
		final IMap<K, T> itemMap = dataHazelcastInstance.getMap(getStorageName());
		return itemFunction.apply(itemMap);
	}

	@Override
	public Optional<T> getItem(final Function<Map<K, T>, T> itemFunction)
	{
		final Map<K, T> itemMap = dataHazelcastInstance.getMap(getStorageName());
		return Optional.ofNullable(itemFunction.apply(itemMap));
	}

	@Override
	public Optional<T> getItemByPredicate(final Function<IMap<K, T>, T> itemFunction)
	{
		final IMap<K, T> itemMap = dataHazelcastInstance.getMap(getStorageName());
		return Optional.ofNullable(itemFunction.apply(itemMap));
	}

	protected abstract String getStorageName();

	@Resource
	public void setDataHazelcastInstance(final HazelcastInstance dataHazelcastInstance)
	{
		this.dataHazelcastInstance = dataHazelcastInstance;
	}
}
