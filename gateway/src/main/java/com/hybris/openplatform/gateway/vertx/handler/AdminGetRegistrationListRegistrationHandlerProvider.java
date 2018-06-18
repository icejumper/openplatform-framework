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
package com.hybris.openplatform.gateway.vertx.handler;

import com.hybris.openplatform.bootstrap.messages.RestEndpointRegistration;
import com.hybris.openplatform.common.rxjava.handlers.AbstractRegistrationHandlerProvider;
import com.hybris.openplatform.gateway.admin.converters.RegistrationRefMapper;
import com.hybris.openplatform.gateway.admin.model.RestEndpointRegistrationRef;
import com.hybris.openplatform.gateway.persistence.RegistrationDAO;

import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

import javax.annotation.Resource;

import org.apache.commons.collections4.CollectionUtils;
import org.mapstruct.factory.Mappers;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import io.vertx.core.json.Json;
import io.vertx.rxjava.core.eventbus.Message;


@Component
public class AdminGetRegistrationListRegistrationHandlerProvider extends AbstractRegistrationHandlerProvider
{
	private static final Logger LOG = LoggerFactory.getLogger(AdminGetRegistrationListRegistrationHandlerProvider.class);

	private RegistrationDAO registrationDAO;

	@Override
	public void processMessage(final Message<String> message)
	{
		final Collection<RestEndpointRegistration> allRegistrations = registrationDAO.getAllRegistrations();

		if (CollectionUtils.isNotEmpty(allRegistrations))
		{
			final RegistrationRefMapper registrationRefMapper = Mappers.getMapper(RegistrationRefMapper.class);
			final List<RestEndpointRegistrationRef> registrationRefs = allRegistrations.stream()
					.map(registrationRefMapper::fromRestEndpointRegistration).sorted(
							Comparator.comparing(RestEndpointRegistrationRef::getPattern)).collect(Collectors.toList());
			message.reply(Json.encodePrettily(registrationRefs));
		}
		else
		{
			message.reply(Json.encodePrettily(Collections.emptyList()));
		}
	}

	@Resource
	public void setRegistrationDAO(final RegistrationDAO registrationDAO)
	{
		this.registrationDAO = registrationDAO;
	}
}
