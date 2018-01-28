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
package com.hybris.openplatform.common.rxjava.handlers;


import com.hybris.openplatform.bootstrap.messages.RestEndpointRegistration;
import com.hybris.openplatform.bootstrap.rxjava.handlers.AsyncRegistrationHandlerProvider;

import javax.annotation.Resource;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.vertx.core.json.Json;
import io.vertx.rxjava.core.Vertx;


public abstract class AbstractRegistrationHandlerProvider implements AsyncRegistrationHandlerProvider
{
	private static final Logger LOGGER = LoggerFactory.getLogger(AbstractRegistrationHandlerProvider.class);

	private Vertx vertx;

	@Override
	public boolean processRegistration(final RestEndpointRegistration registration)
	{
		if("active".equals(registration.getStatus()))
		{
			LOGGER.info("Registration of {} succeeded!", Json.encodePrettily(registration));
		}
		return true;
	}

	@Resource
	public void setVertx(final Vertx vertx)
	{
		this.vertx = vertx;
	}

	protected Vertx getVertx()
	{
		return vertx;
	}
}
