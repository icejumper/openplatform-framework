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
