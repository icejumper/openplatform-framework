package com.hybris.openplatform.common.service.impl;

import static java.util.Objects.isNull;
import static java.util.Objects.nonNull;
import static org.apache.commons.lang3.StringUtils.isNotEmpty;

import com.hybris.openplatform.common.service.StorageAwareService;

import java.util.Collection;
import java.util.Collections;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import javax.annotation.Resource;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.collect.Maps;
import com.hazelcast.core.HazelcastInstance;


public abstract class AbstractStorageAwareService<T, K> implements StorageAwareService<T, K>
{

	private static final Logger LOGGER = LoggerFactory.getLogger(AbstractStorageAwareService.class);
	private HazelcastInstance dataHazelcastInstance;

	@Override
	public Optional<T> storeItem(final Function<T, K> keyFunction, final T item)
	{
		final Map<K, T> itemMap = getItemMap();
		final K itemKey = keyFunction.apply(item);
		if (itemMap.containsKey(itemKey))
		{
			itemMap.replace(itemKey, item);
		}
		else
		{
			itemMap.put(itemKey, item);
		}
		return Optional.ofNullable(itemMap.get(itemKey));
	}

	@Override
	public Optional<T> storeItem(final String tenantId, final Function<T, K> keyFunction, final T item)
	{
		Map<K, T> itemMap = getItemMap(tenantId);
		if(isNull(itemMap))
		{
			itemMap = createNewRepositoryMap(tenantId, getStorageName());
		}
		final K itemKey = keyFunction.apply(item);
		if (itemMap.containsKey(itemKey))
		{
			itemMap.replace(itemKey, item);
		}
		else
		{
			itemMap.put(itemKey, item);
		}
		updateStorage(tenantId, itemMap);
		return Optional.ofNullable(itemMap.get(itemKey));
	}

	@Override
	public void storeItems(final Function<T, K> keyFunction, final Collection<T> itemsCollection)
	{
		storeItems(keyFunction, item -> true, itemsCollection);
	}

	@Override
	public void storeItems(final String tenantId, final Function<T, K> keyFunction, final Collection<T> itemsCollection)
	{
		storeItems(tenantId, keyFunction, item -> true, itemsCollection);
	}

	@Override
	public void storeItems(final Function<T, K> keyFunction, final Predicate<T> filter, final Collection<T> itemsCollection)
	{
		final Map<K, T> itemMap = getItemMap();
		for (T item : itemsCollection)
		{
			final K itemKey = keyFunction.apply(item);
			if (itemMap.containsKey(itemKey))
			{
				final T storedItem = itemMap.get(itemKey);
				if (filter.test(storedItem))
				{
					itemMap.replace(itemKey, item);
				}
			}
			else
			{
				itemMap.put(itemKey, item);
			}
		}
	}

	@Override
	public void storeItems(final String tenantId, final Function<T, K> keyFunction, final Predicate<T> filter,
			final Collection<T> itemsCollection)
	{
		Map<K, T> itemMap = getItemMap(tenantId);
		if(Objects.isNull(itemMap))
		{
			itemMap = createNewRepositoryMap(tenantId, getStorageName());
		}
		for (T item : itemsCollection)
		{
			final K itemKey = keyFunction.apply(item);
			if (itemMap.containsKey(itemKey))
			{
				final T storedItem = itemMap.get(itemKey);
				if (filter.test(storedItem))
				{
					itemMap.replace(itemKey, item);
				}
			}
			else
			{
				itemMap.put(itemKey, item);
			}
		}
		updateStorage(tenantId, itemMap);
	}

	@Override
	public Collection<T> getItems(final Function<Map<K, T>, Collection<T>> itemFunction)
	{
		final Map<K, T> itemMap = getItemMap();
		return itemFunction.apply(itemMap);
	}

	@Override
	public Collection<T> getItems(final String tenantId, final Function<Map<K, T>, Collection<T>> itemFunction)
	{
		final Map<K, T> itemMap = getItemMap(tenantId);
		return nonNull(itemMap)? itemFunction.apply(itemMap): Collections.emptyList();
	}

	@Override
	public Map<K, T> getItemsMap(final Predicate<K> keyFilter)
	{
		final Map<K, T> itemMap = getItemMap();
		return itemMap.keySet().stream().filter(keyFilter).collect(Collectors.toMap(Function.identity(), itemMap::get));
	}

	@Override
	public Map<K, T> getItemsMap(final String tenantId, final Predicate<K> keyFilter)
	{
		final Map<K, T> itemMap = getItemMap(tenantId);
		if(isNull(itemMap))
		{
			return null;
		}
		return itemMap.keySet().stream().filter(keyFilter).collect(Collectors.toMap(Function.identity(), itemMap::get));
	}

	@Override
	public Optional<T> getItem(final Function<Map<K, T>, T> itemFunction)
	{
		final Map<K, T> itemMap = getItemMap();
		return Optional.ofNullable(itemFunction.apply(itemMap));
	}

	@Override
	public Optional<T> getItem(final String tenantId, final Function<Map<K, T>, T> itemFunction)
	{
		final Map<K, T> itemMap = getItemMap(tenantId);
		return Optional.ofNullable(nonNull(itemMap)? itemFunction.apply(itemMap): null);
	}

	protected Map<K, T> getItemMap(final String tenantId)
	{
		if ( isNotEmpty(tenantId))
		{
			final Map<String, Map<K, T>> storages = dataHazelcastInstance.getMap(tenantId);
			return storages.get(getStorageName());
		}
		LOGGER.info("For this method the tenantId should be provided: tenantId [{}]", tenantId);
		return Maps.newHashMap();
	}

	protected Map<K, T> getItemMap()
	{
		return dataHazelcastInstance.getMap(getStorageName());
	}

	protected abstract String getStorageName();

	private Map<K, T> createNewRepositoryMap(final String tenantId, final String storageName)
	{
		final Map<String, Map<K,T>> storageMap = dataHazelcastInstance.getMap(tenantId);
		if(isNull(storageMap.get(storageName)))
		{
			final Map<K, T> newStorage = Maps.newHashMap();
			storageMap.put(storageName, newStorage);
		}
		return storageMap.get(storageName);
	}

	private void updateStorage(final String tenantId, final Map<K, T> itemMap)
	{
		final Map<String, Map<K, T>> storages = dataHazelcastInstance.getMap(tenantId);
		final Map<K, T> oldValue = storages.get(tenantId);
		final String storageName = getStorageName();
		if(Objects.nonNull(oldValue))
		{
			storages.replace(storageName, oldValue, itemMap);
		}
		else
		{
			storages.put(storageName, itemMap);
		}
	}

	@Resource
	public void setDataHazelcastInstance(final HazelcastInstance dataHazelcastInstance)
	{
		this.dataHazelcastInstance = dataHazelcastInstance;
	}

	public HazelcastInstance getDataHazelcastInstance()
	{
		return dataHazelcastInstance;
	}
}
