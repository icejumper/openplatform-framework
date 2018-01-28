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
package com.hybris.openplatform.gateway.persistence;

import static java.util.Objects.isNull;
import static java.util.Objects.nonNull;
import static java.util.stream.Collectors.toSet;

import com.hybris.openplatform.bootstrap.messages.RestEndpointRegistration;
import com.hybris.openplatform.gateway.persistence.repositories.RegistrationRepository;

import java.util.Collection;
import java.util.Collections;
import java.util.Map;
import java.util.Objects;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

import javax.annotation.Resource;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

import com.google.common.collect.Maps;
import com.hazelcast.core.MapStore;


/**
 * Hazlecast MapStrore implementation for Cart
 */
@Component
@Profile("persist")
public class RegistrationMapStore implements MapStore<String, RestEndpointRegistration>
{

	private static Logger LOGGER = LoggerFactory.getLogger(RestEndpointRegistration.class);

	private RegistrationRepository registrationRepository;

	@Override
	public void store(final String pattern, final RestEndpointRegistration registration)
	{
		final RestEndpointRegistration savedReg = registrationRepository.findByPattern(pattern);
		if (Objects.nonNull(savedReg))
		{
			registration.setPattern(registration.getPattern());
		}
		LOGGER.info("Store registration (store): {}", registration);
		registrationRepository.save(registration);
	}

	@Override
	public void storeAll(final Map<String, RestEndpointRegistration> registrationMap)
	{
		final Iterable<String> patternSet = registrationMap.keySet();
		LOGGER.info("Storing ALL registrations (storeAll) with patterns: {}", patternSet);
		final Iterable<RestEndpointRegistration> storedRegistrations = registrationRepository.findAllByPatterns(patternSet);
		if (nonNull(storedRegistrations) && storedRegistrations.iterator().hasNext())
		{
			final Map<String, RestEndpointRegistration> storedRegistrationMap = Maps.newHashMap();
			for (RestEndpointRegistration registration : storedRegistrations)
			{
				storedRegistrationMap.put(registration.getPattern(), registration);
			}
			for (final String pattern : patternSet)
			{
				if (storedRegistrationMap.containsKey(pattern))
				{
					final RestEndpointRegistration storedRegistration = storedRegistrationMap.get(pattern);
					final RestEndpointRegistration registration = registrationMap.get(pattern);
					registration.setPattern(storedRegistration.getPattern());
				}
			}
		}
		final Iterable<RestEndpointRegistration> registrationsToSave = registrationMap.entrySet().stream().map(Map.Entry::getValue)
				.collect(toSet());
		LOGGER.info("Store registrations (storeAll): {}", registrationsToSave);
		registrationRepository.save(registrationsToSave);
	}


	@Override
	public void delete(final String pattern)
	{
		LOGGER.info("Deleting registration (delete): {}", pattern);
		final RestEndpointRegistration registration = registrationRepository.findByPattern(pattern);
		if (nonNull(registration))
		{
			registrationRepository.delete(registration);
		}
	}

	@Override
	public void deleteAll(final Collection<String> patterns)
	{
		final Iterable<RestEndpointRegistration> allRegistrations = registrationRepository.findAllByPatterns(patterns);
		LOGGER.info("Deleting ALL registrations (deleteAll): {}", allRegistrations);
		if (nonNull(allRegistrations) && allRegistrations.iterator().hasNext())
		{
			registrationRepository.delete(allRegistrations);
		}
	}

	@Override
	public RestEndpointRegistration load(final String pattern)
	{
		LOGGER.info("Loading cart by pattern (load): {}", pattern);
		return registrationRepository.findByPattern(pattern);
	}

	@Override
	public Map<String, RestEndpointRegistration> loadAll(final Collection<String> patterns)
	{
		final Iterable<RestEndpointRegistration> allRegistrations = registrationRepository.findAllByPatterns(patterns);
		LOGGER.info("Loading ALL registrations (loadAll): {}", allRegistrations);
		if (isNull(allRegistrations) || !allRegistrations.iterator().hasNext())
		{
			return Maps.newHashMap();
		}
		return StreamSupport.stream(allRegistrations.spliterator(), false)
				.collect(Collectors.toMap(RestEndpointRegistration::getPattern, Function.identity()));
	}

	@Override
	public Iterable<String> loadAllKeys()
	{
		final Iterable<RestEndpointRegistration> allRegistrations = registrationRepository.findAll();
		LOGGER.info("Loading registrations: {}", allRegistrations);
		if (isNull(allRegistrations) || !allRegistrations.iterator().hasNext())
		{
			return Collections.emptyList();
		}
		return StreamSupport.stream(allRegistrations.spliterator(), false).filter(r -> nonNull(r.getPattern()))
				.map(RestEndpointRegistration::getPattern)
				.collect(toSet());
	}

	@Resource
	public void setRegistrationRepository(final RegistrationRepository registrationRepository)
	{
		this.registrationRepository = registrationRepository;
	}
}
