package com.hybris.openplatform.common.service;

import java.util.Collection;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;
import java.util.function.Predicate;


public interface StorageAwareService<T, K>
{
	Optional<T> storeItem(Function<T, K> keyFunction, T item);

	Optional<T> storeItem(String tenantId, Function<T, K> keyFunction, T item);

	void storeItems(Function<T, K> keyFunction, Collection<T> itemsCollection);

	void storeItems(String tenantId, Function<T, K> keyFunction, Collection<T> itemsCollection);

	void storeItems(Function<T, K> keyFunction, Predicate<T> filter, Collection<T> itemsCollection);

	void storeItems(String tenantId, Function<T, K> keyFunction, Predicate<T> filter, Collection<T> itemsCollection);

	Collection<T> getItems(Function<Map<K, T>, Collection<T>> itemFunction);

	Collection<T> getItems(String tenantId, Function<Map<K, T>, Collection<T>> itemFunction);

	Map<K, T> getItemsMap(Predicate<K> keyFilter);

	Map<K, T> getItemsMap(String tenantId, Predicate<K> keyFilter);

	Optional<T> getItem(Function<Map<K, T>, T> itemFunction);

	Optional<T> getItem(String tenantId, Function<Map<K, T>, T> itemFunction);

}
