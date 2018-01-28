package com.hybris.openplatform.common.service;

import java.util.Collection;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;

import com.hazelcast.core.IMap;


public interface StorageAwareService<T, K>
{
	Optional<T> storeItem(Function<Map<K, T>, T> itemFunction);

	Collection<T> getItems(Function<Map<K, T>, Collection<T>> itemFunction);

	Collection<T> getItemsByPredicate(Function<IMap<K, T>, Collection<T>> itemFunction);

	Optional<T> getItem(Function<Map<K, T>, T> itemFunction);

	Optional<T> getItemByPredicate(Function<IMap<K, T>, T> itemFunction);
}
