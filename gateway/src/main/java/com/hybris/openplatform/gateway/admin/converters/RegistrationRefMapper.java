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
package com.hybris.openplatform.gateway.admin.converters;

import com.hybris.openplatform.bootstrap.messages.RestEndpointRegistration;
import com.hybris.openplatform.gateway.admin.model.RestEndpointRegistrationRef;

import org.mapstruct.Mapper;


@Mapper
public interface RegistrationRefMapper
{
	RestEndpointRegistrationRef fromRestEndpointRegistration(RestEndpointRegistration registration);

}
