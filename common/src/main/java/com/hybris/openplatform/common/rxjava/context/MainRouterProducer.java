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
package com.hybris.openplatform.common.rxjava.context;

import javax.annotation.Resource;

import org.springframework.context.annotation.Bean;
import org.springframework.stereotype.Component;

import io.vertx.rxjava.core.Vertx;
import io.vertx.rxjava.ext.web.Router;



@Component("mainRxRouterProducer")
public class MainRouterProducer
{
	private Vertx microRxVertx;

	@Bean
	public Router mainRxRouter()
	{
		return Router.router(microRxVertx);
	}

	@Resource
	public void setMicroVertx(final Vertx microVertx)
	{
		this.microRxVertx = microVertx;
	}
}
