package com.hybris.openplatform.gateway.services.impl;

import com.hybris.openplatform.bootstrap.messages.RestEndpointRegistration;
import com.hybris.openplatform.gateway.persistence.RegistrationDAO;
import com.hybris.openplatform.gateway.services.BallancingStrategy;

import java.util.LinkedList;
import java.util.Map;
import java.util.Objects;

import javax.annotation.Resource;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;

@Component
public class RoundRobinBallancingStrategy implements BallancingStrategy
{
	private static final Logger LOGGER = LoggerFactory.getLogger(RoundRobinBallancingStrategy.class);

	private Map<String, Integer> activeAddressTable = Maps.newHashMap();
	private Map<String, LinkedList<RestEndpointRegistration>> ballancingTable = Maps.newConcurrentMap();

	private RegistrationDAO registrationDAO;

	@Override
	public RestEndpointRegistration getNextRegisteredNode(final String method, final String pattern)
	{
		final String registrationEntryKey = registrationDAO.getRegistrationEntryKey(method, pattern);
		final LinkedList<RestEndpointRegistration> restEndpointRegistrationsQueue = ballancingTable.get(registrationEntryKey);
		Integer activeEntryIndex = activeAddressTable.get(registrationEntryKey);
		RestEndpointRegistration registration;
		if (Objects.isNull(activeEntryIndex) || activeEntryIndex >= (restEndpointRegistrationsQueue.size() - 1))
		{
			registration = restEndpointRegistrationsQueue.getFirst();
			activeAddressTable.put(registrationEntryKey, 1);
		}
		else
		{
			registration = restEndpointRegistrationsQueue.listIterator(activeEntryIndex).next();
			activeAddressTable.replace(registrationEntryKey, activeEntryIndex + 1);
		}
		LOGGER.debug("Using registration origAddress  {}", registration.getOriginatorAddress());
		return registration;
	}

	@Override
	public synchronized boolean addNewRegistration(final RestEndpointRegistration registration)
	{
		final String pattern = registration.getPattern();
		final String method = registration.getMethod();
		final String registrationEntryKey = registrationDAO.getRegistrationEntryKey(method, pattern);
		LinkedList<RestEndpointRegistration> restEndpointRegistrations = ballancingTable.get(registrationEntryKey);
		if(Objects.isNull(restEndpointRegistrations))
		{
			restEndpointRegistrations = Lists.newLinkedList();
			ballancingTable.put(registrationEntryKey, restEndpointRegistrations);
			registrationDAO.saveRegistration(registration);
		}
		if(restEndpointRegistrations.stream().noneMatch(r -> r.getServiceAddress().equals(registration.getServiceAddress())))
		{
			LOGGER.debug("Adding service forwarding address {}", registration.getServiceAddress());
			restEndpointRegistrations.addLast(registration);
			return true;
		}
		else
		{
			return false;
		}
	}

	@Resource
	public void setRegistrationDAO(final RegistrationDAO registrationDAO)
	{
		this.registrationDAO = registrationDAO;
	}
}
