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
package com.hybris.openplatform.common.handlers;


import com.hybris.openplatform.bootstrap.handlers.AsyncRegistrationHandlerProvider;
import com.hybris.openplatform.bootstrap.messages.RestEndpointRegistration;

import javax.annotation.Resource;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.vertx.core.AsyncResult;
import io.vertx.core.Vertx;
import io.vertx.core.eventbus.Message;
import io.vertx.core.json.Json;


public abstract class AbstractRegistrationHandlerProvider implements AsyncRegistrationHandlerProvider
{
	private static final Logger LOGGER = LoggerFactory.getLogger(AbstractRegistrationHandlerProvider.class);

	private Vertx vertx;

	@Override
	public boolean processRegistration(final AsyncResult<Message<String>> asyncResult, final RestEndpointRegistration registration)
	{
		if (asyncResult.succeeded())
		{
			if (asyncResult.result().body().equals("registration_ok"))
			{
				LOGGER.info("Registration of {} succeeded!", Json.encodePrettily(registration));
				return true;
			}
			else
			{
				LOGGER.error("Registration of {} failed!", Json.encodePrettily(registration));
			}
		}
		else
		{
			LOGGER.error("Registration of {} failed with cause {}!", Json.encodePrettily(registration), asyncResult.cause());
		}
		return false;
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
