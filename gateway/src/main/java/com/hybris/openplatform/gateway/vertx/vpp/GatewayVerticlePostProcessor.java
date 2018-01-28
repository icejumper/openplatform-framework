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
package com.hybris.openplatform.gateway.vertx.vpp;

import com.hybris.openplatform.bootstrap.messages.MessageAddresses;
import com.hybris.openplatform.bootstrap.rxjava.vertx.MicroServiceVerticle;
import com.hybris.openplatform.common.deployment.VerticlePostProcessor;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import com.google.common.collect.ImmutableMap;

import io.vertx.core.json.Json;


@Component
public class GatewayVerticlePostProcessor implements VerticlePostProcessor
{
	private static final Logger LOGGER = LoggerFactory.getLogger(GatewayVerticlePostProcessor.class);

	@Override
	public void beforeVerticleDeployment(final MicroServiceVerticle microServiceVerticle)
	{
		// nothing to do
	}

	@Override
	public void afterVerticleDeployment(final MicroServiceVerticle microServiceVerticle)
	{
		LOGGER.info("Broadcasting GATEWAY READY message...");
		microServiceVerticle
				.publishMessage(MessageAddresses.GATEWAY_READY, Json.encode(ImmutableMap.of("gateway-status", "ready")));
	}

}
