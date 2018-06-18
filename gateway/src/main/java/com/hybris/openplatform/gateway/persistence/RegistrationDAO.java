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

import com.hybris.openplatform.bootstrap.messages.RestEndpointRegistration;

import java.util.Collection;


public interface RegistrationDAO
{

	RestEndpointRegistration saveRegistration(RestEndpointRegistration registration);

	Collection<RestEndpointRegistration> getAllRegistrations();

	Collection<RestEndpointRegistration> getRegistrationsByPattern(String pattern);

	default String getRegistrationEntryKey(String method, String pattern)
	{
		return method + "@" + pattern;
	}

}
