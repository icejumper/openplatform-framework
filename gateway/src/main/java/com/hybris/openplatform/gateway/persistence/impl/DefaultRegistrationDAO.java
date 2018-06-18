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
package com.hybris.openplatform.gateway.persistence.impl;

import com.hybris.openplatform.bootstrap.constants.Constants;
import com.hybris.openplatform.bootstrap.messages.RestEndpointRegistration;
import com.hybris.openplatform.common.service.impl.DefaultStorageAwareService;
import com.hybris.openplatform.gateway.persistence.RegistrationDAO;

import java.util.Collection;
import java.util.Map;
import java.util.stream.Collectors;

import javax.annotation.Resource;

import org.springframework.core.env.Environment;
import org.springframework.stereotype.Component;


@Component
public class DefaultRegistrationDAO extends DefaultStorageAwareService<RestEndpointRegistration> implements RegistrationDAO
{

	private Environment env;

	@Override
	protected String getStorageName()
	{
		return env.getProperty(Constants.REGISTRATIONS_STORAGE_NAME_PROP);
	}

	@Override
	public RestEndpointRegistration saveRegistration(final RestEndpointRegistration registration)
	{
		return storeItem(r -> getRegistrationEntryKey(r.getMethod(), r.getPattern()), registration).orElse(null);
	}

	@Override
	public Collection<RestEndpointRegistration> getAllRegistrations()
	{
		return getItems(Map::values);
	}

	@Override
	public Collection<RestEndpointRegistration> getRegistrationsByPattern(final String pattern)
	{
		return getItems(regMap -> regMap.values().stream().filter(r -> r.getPattern().startsWith(pattern)).collect(
				Collectors.toList()));
	}

	@Resource
	public void setEnv(final Environment env)
	{
		this.env = env;
	}


}
