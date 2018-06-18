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
package com.hybris.openplatform.common.rxjava.vertx;

import com.hybris.openplatform.bootstrap.constants.Constants;
import com.hybris.openplatform.bootstrap.messages.MessageAddresses;
import com.hybris.openplatform.bootstrap.messages.RestEndpointRegistration;
import com.hybris.openplatform.bootstrap.rxjava.handlers.AsyncRegistrationHandlerProvider;
import com.hybris.openplatform.bootstrap.rxjava.vertx.MicroServiceVerticle;
import com.hybris.openplatform.common.rxjava.observers.OnErrorObserver;
import com.hybris.openplatform.stereotypes.SinglePurposeService;

import java.util.Map;
import java.util.Objects;
import java.util.function.Consumer;

import javax.annotation.Resource;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.env.Environment;

import com.google.common.collect.ImmutableSet;

import io.vertx.core.eventbus.ReplyException;
import io.vertx.core.eventbus.ReplyFailure;
import io.vertx.core.http.HttpMethod;
import io.vertx.core.json.Json;
import io.vertx.rxjava.core.Vertx;
import io.vertx.rxjava.core.eventbus.Message;
import rx.Observer;


public abstract class AbstractRegisterableServiceVerticle extends MicroServiceVerticle
{

	private static final Logger LOGGER = LoggerFactory.getLogger(AbstractRegisterableServiceVerticle.class);
	private static int REGISTER_RETRY_TIMEOUT = 5000;
	private Environment env;

	@Override
	protected void registerVerticleEndPoints()
	{
		registerServiceWithGateway();
		this.rxRegisterConsumer(MessageAddresses.GATEWAY_READY).handler(message -> {
			final Map<String, String> messageMap = Json.decodeValue((String) message.body(), Map.class);
			if ("ready".equals(messageMap.get("gateway-status")))
			{
				registerServiceWithGateway();
			}
		});
	}

	protected void registerServiceWithGateway()
	{
		final SinglePurposeService singlePurposeService = this.getClass().getAnnotation(SinglePurposeService.class);
		if (Objects.isNull(singlePurposeService))
		{
			throw new UnsupportedOperationException(
					"Implicit registration is allowed for single-purpose services only (see @SinglePurposeService)");
		}

		final String serviceId = singlePurposeService.serviceId();
		final String serviceDescription = singlePurposeService.serviceDescription();
		final HttpMethod method = singlePurposeService.method();
		final String endPointPattern = singlePurposeService.endPointPattern();

		final String serviceName = getEnv().getProperty("vertx.microservice.name") + "_" + serviceId;
		final String corsPattern = getEnv().getProperty("resources.security.cors.allowOriginPattern");
		final RestEndpointRegistration addToCartRegistration = new RestEndpointRegistration(getRootContext() + endPointPattern,
				method.name(), serviceName, serviceName + "_" + method.name(), null, serviceDescription, "new",
				ImmutableSet.of(Constants._TENANT_), corsPattern, getAcceptedContentType());
		final Vertx rxVertx = this.getRxVertx();
		final AsyncRegistrationHandlerProvider registrationHandlerProvider = getAsyncRegistrationHandlerProvider();
		this.rxRegisterService(addToCartRegistration)
				.doOnSuccess(responseMessage -> registrationHandlerProvider
						.handle(rxVertx, Json.decodeValue((String) responseMessage.body(), RestEndpointRegistration.class)))
				.doOnError(e -> tryToRegister(this, registrationHandlerProvider, addToCartRegistration))
				.subscribe(new OnErrorObserver<>(registrationErrorConsumer()));
	}

	protected void tryToRegister(final MicroServiceVerticle microServiceVerticle,
			final AsyncRegistrationHandlerProvider registrationHandlerProvider,
			final RestEndpointRegistration registration)
	{
		final Vertx rxVertx = microServiceVerticle.getRxVertx();
		final Observer<Message<String>> errorObserver = new OnErrorObserver<>(registrationErrorConsumer());
		rxVertx.setPeriodic(REGISTER_RETRY_TIMEOUT, id ->
				microServiceVerticle.rxRegisterService(registration)
						.doOnSuccess(responseMessage -> {
							LOGGER.info("Reply message: {}", responseMessage.body());
							registrationHandlerProvider.handle(rxVertx, registration);
							rxVertx.cancelTimer(id);
						})
						.doOnError(e -> LOGGER
								.info("No gateway found or gateway registration error. Will retry in {} msec", REGISTER_RETRY_TIMEOUT))
						.subscribe(errorObserver)
		);
	}

	protected AsyncRegistrationHandlerProvider getAsyncRegistrationHandlerProvider()
	{
		return null;
	}

	private String getAcceptedContentType()
	{
		return env.getProperty("vertx.http.contentType",
				com.hybris.openplatform.bootstrap.constants.Constants.HTTP_CONTENT_TYPE_APPLICATION_HYBRIS_VENDOR_JSON);
	}

	protected Consumer<Throwable> registrationErrorConsumer()
	{
		return error ->
		{
			if (error instanceof ReplyException)
			{
				final ReplyException replyException = (ReplyException) error;
				if (replyException.failureType().equals(ReplyFailure.NO_HANDLERS))
				{
					LOGGER.warn("Reply exception: {}", replyException.getMessage());
				}
				else
				{
					LOGGER.error("Reply exception: {}", replyException);
				}
			}
			else
			{
				LOGGER.error("General exception occurred: {}", error);
			}
		};
	}

	@Resource
	public void setEnv(final Environment env)
	{
		this.env = env;
	}

	protected Environment getEnv()
	{
		return env;
	}
}
