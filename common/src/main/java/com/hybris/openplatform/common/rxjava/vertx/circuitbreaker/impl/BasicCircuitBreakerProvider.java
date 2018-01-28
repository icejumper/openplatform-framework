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
package com.hybris.openplatform.common.rxjava.vertx.circuitbreaker.impl;

import com.hybris.openplatform.common.rxjava.vertx.circuitbreaker.CircuitBreakerProvider;

import javax.annotation.Resource;

import org.springframework.core.env.Environment;
import org.springframework.stereotype.Component;

import io.vertx.circuitbreaker.CircuitBreakerOptions;
import io.vertx.core.Handler;
import io.vertx.rxjava.circuitbreaker.CircuitBreaker;
import io.vertx.rxjava.core.Vertx;


@Component
public class BasicCircuitBreakerProvider implements CircuitBreakerProvider
{
	private Vertx vertx;
	private Environment env;

	@Override
	public CircuitBreaker getBreaker(final String name, final Handler<Void> openHandler, final Handler<Void> closeHandler,
			final Handler<Void> halfOpenHandler)
	{
		return CircuitBreaker.create(name, vertx, new CircuitBreakerOptions()
				.setMaxFailures(env.getProperty("vertx.circuitbreaker.config.basic.maxfailures", Integer.class))
				.setTimeout(env.getProperty("vertx.circuitbreaker.config.basic.timeout", Integer.class))
				.setFallbackOnFailure(true)
				.setResetTimeout(env.getProperty("vertx.circuitbreaker.config.basic.resettimeout", Integer.class)))
				.openHandler(openHandler)
				.closeHandler(closeHandler)
				.halfOpenHandler(halfOpenHandler);
	}

	@Resource
	public void setVertx(final Vertx vertx)
	{
		this.vertx = vertx;
	}

	@Resource
	public void setEnv(final Environment env)
	{
		this.env = env;
	}
}
