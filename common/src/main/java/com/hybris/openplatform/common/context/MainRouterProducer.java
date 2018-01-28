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
package com.hybris.openplatform.common.context;

import javax.annotation.Resource;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

import io.vertx.core.Vertx;
import io.vertx.ext.web.Router;


@Component
@Profile("no-rx-vertx")
public class MainRouterProducer
{

	private Vertx microVertx;

	@Bean
	public Router mainRouter()
	{
		return Router.router(microVertx);
	}

	@Resource
	public void setMicroVertx(final Vertx microVertx)
	{
		this.microVertx = microVertx;
	}
}
