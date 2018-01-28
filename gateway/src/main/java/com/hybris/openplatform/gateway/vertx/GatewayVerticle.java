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
package com.hybris.openplatform.gateway.vertx;

import static java.util.Objects.isNull;
import static org.apache.commons.collections4.MapUtils.isNotEmpty;

import com.hybris.openplatform.bootstrap.constants.Constants;
import com.hybris.openplatform.bootstrap.messages.MessageAddresses;
import com.hybris.openplatform.bootstrap.messages.RestEndpointRegistration;
import com.hybris.openplatform.bootstrap.rxjava.vertx.MicroServiceVerticle;
import com.hybris.openplatform.gateway.services.BallancingStrategy;
import com.hybris.openplatform.stereotypes.VerticleComponent;

import java.util.Collection;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.function.Supplier;

import javax.annotation.Resource;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;

import io.vertx.core.Handler;
import io.vertx.core.eventbus.DeliveryOptions;
import io.vertx.core.json.Json;
import io.vertx.rxjava.core.MultiMap;
import io.vertx.rxjava.core.eventbus.Message;
import io.vertx.rxjava.core.http.HttpServerResponse;
import io.vertx.rxjava.ext.web.RoutingContext;


@VerticleComponent("gatewayVerticle")
public class GatewayVerticle extends MicroServiceVerticle
{
	private static final Logger LOGGER = LoggerFactory.getLogger(GatewayVerticle.class);
	private static final String CONTEXT_ROOT = "/gateway";

	private BallancingStrategy ballancingStrategy;

	@Override
	protected void registerVerticleEndPoints()
	{
		super.<String>rxRegisterConsumer(MessageAddresses.REGISTER_REST_END_POINT).handler(registerRestEndPointHandler());
	}

	private Handler<Message<String>> registerRestEndPointHandler()
	{
		return message -> {
			final String registrationMessage = message.body();
			final RestEndpointRegistration registration = Json.decodeValue(registrationMessage, RestEndpointRegistration.class);
			boolean registrationAdded = false;
			if (Objects.nonNull(registration))
			{
				final String originatorAddress = message.replyAddress();
				registration.setOriginatorAddress(originatorAddress);
				final String method = registration.getMethod();
				final String pattern = registration.getPattern();
				switch (method)
				{
					case "GET":
						registerGetRequestWithCors(registration,
								routeGetRequest(() -> ballancingStrategy.getNextRegisteredNode(method, pattern)));
						registrationAdded = ballancingStrategy.addNewRegistration(registration);
						break;
					case "POST":
						registerPostRequestWithCors(registration,
								routePostRequest(() -> ballancingStrategy.getNextRegisteredNode(method, pattern)));
						registrationAdded = ballancingStrategy.addNewRegistration(registration);
						break;
					case "HEAD":
						registerHeadRequestWithCors(registration,
								routeHeadRequest(() -> ballancingStrategy.getNextRegisteredNode(method, pattern)));
						registrationAdded = ballancingStrategy.addNewRegistration(registration);
						break;
					default:
						LOGGER.error("Request method {} is not supported", method);
						// TODO change the message
						message.reply("registration_failed for [" + registration.getPattern() + "]");
						return;
				}
			}
			if (registrationAdded)
			{
				registration.setStatus("active");
			}
			else
			{
				registration.setStatus("duplicated");
			}
			message.reply(Json.encode(registration));
		};
	}

	private Handler<RoutingContext> routePostRequest(final Supplier<RestEndpointRegistration> registrationSupplier)
	{
		return rc -> {
			final RestEndpointRegistration restEndpointRegistration = registrationSupplier.get();
			final Collection<String> requiredHeaders = restEndpointRegistration.getRequiredHeaders();
			final boolean requiredHeaderMissing = requiredHeaders.stream().anyMatch(h -> isNull(rc.request().getHeader(h)));
			if (!requiredHeaderMissing)
			{
				final DeliveryOptions deliveryOptions = new DeliveryOptions();
				if (!CollectionUtils.isEmpty(requiredHeaders))
				{
					requiredHeaders.forEach(h -> deliveryOptions.addHeader(h, rc.request().getHeader(h)));
				}
				final Map<String, String> pathParams = rc.pathParams();
				if(isNotEmpty(pathParams))
				{
					deliveryOptions.addHeader(Constants.MESSAGE_PATH_PARAMS_HEADER, Json.encode(pathParams));
				}
				LOGGER.debug("Forwarding POST request to eventBus address [{}]", restEndpointRegistration.getServiceAddress());
				rxSendMessage(restEndpointRegistration.getServiceAddress(), rc.getBodyAsString(Constants.DEFAULT_ENCODING),
						deliveryOptions)
						.doOnSuccess(responseMessage -> routeRequestReplySuccess(rc, responseMessage))
						.doOnError(error -> routeRequestReplyError(rc, error))
						.subscribe();
			}
			else
			{
				rc.response().setStatusCode(Constants.HTTP_STATUS_CODE_BAD_REQUEST)
						.end("Missing required headers in the request: " + requiredHeaders);
			}
		};
	}

	private Handler<RoutingContext> routeGetRequest(final Supplier<RestEndpointRegistration> registrationSupplier)
	{
		return rc -> {
			final RestEndpointRegistration restEndpointRegistration = registrationSupplier.get();

			final Collection<String> requiredHeaders = restEndpointRegistration.getRequiredHeaders();
			final boolean requiredHeaderMissing = requiredHeaders.stream().anyMatch(h -> isNull(rc.request().getHeader(h)));
			if (!requiredHeaderMissing)
			{
				final DeliveryOptions deliveryOptions = new DeliveryOptions();
				if (!CollectionUtils.isEmpty(requiredHeaders))
				{
					requiredHeaders.forEach(h -> deliveryOptions.addHeader(h, rc.request().getHeader(h)));
				}
				final Map<String, String> pathParams = rc.pathParams();
				rxSendMessage(restEndpointRegistration.getServiceAddress(), Json.encode(pathParams), deliveryOptions)
						.doOnSuccess(responseMessage -> routeRequestReplySuccess(rc, responseMessage))
						.doOnError(error -> routeRequestReplyError(rc, error)).subscribe();
			}
			else
			{
				rc.response().setStatusCode(Constants.HTTP_STATUS_CODE_BAD_REQUEST)
						.end("Missing required headers in the request: " + requiredHeaders);
			}
		};
	}

	private Handler<RoutingContext> routeHeadRequest(final Supplier<RestEndpointRegistration> registrationSupplier)
	{
		return rc -> {
			final RestEndpointRegistration restEndpointRegistration = registrationSupplier.get();
			final Collection<String> requiredHeaders = restEndpointRegistration.getRequiredHeaders();
			final boolean requiredHeaderMissing = requiredHeaders.stream().anyMatch(h -> isNull(rc.request().getHeader(h)));
			if (!requiredHeaderMissing)
			{
				final DeliveryOptions deliveryOptions = new DeliveryOptions();
				if (!CollectionUtils.isEmpty(requiredHeaders))
				{
					requiredHeaders.forEach(h -> deliveryOptions.addHeader(h, rc.request().getHeader(h)));
				}
				final Map<String, String> pathParams = rc.pathParams();
				rxSendMessage(restEndpointRegistration.getServiceAddress(), Json.encode(pathParams), deliveryOptions)
						.doOnSuccess(responseMessage -> routeRequestReplyHead(rc, responseMessage))
						.doOnError(error -> routeRequestReplyError(rc)).subscribe();
			}
			else
			{
				rc.response().setStatusCode(Constants.HTTP_STATUS_CODE_BAD_REQUEST).end();
			}
		};
	}

	private void routeRequestReplySuccess(final RoutingContext routingContext, final Message<String> responseMessage)
	{
		final MultiMap headers = responseMessage.headers();
		final Set<String> headerNames = headers.names();
		final HttpServerResponse httpServerResponse = routingContext.response();
		final MultiMap responseHeaders = httpServerResponse.headers();
		headerNames.stream().filter(hn -> !hn.equals(Constants.HTTP_RESPONSE_CODE_HEADER))
				.forEach(hn -> responseHeaders.add(hn, headers.get(hn)));
		final String messageStatusCode = headers.get(Constants.HTTP_RESPONSE_CODE_HEADER);
		if (!StringUtils.isEmpty(messageStatusCode))
		{
			httpServerResponse.setStatusCode(Integer.valueOf(messageStatusCode));
		}
		else
		{
			httpServerResponse.setStatusCode(Constants.HTTP_STATUS_CODE_SUCCESS);
		}
		final String body = responseMessage.body();
		if (StringUtils.isEmpty(body))
		{
			httpServerResponse.end();
		}
		else
		{
			httpServerResponse.end(body);
		}
	}

	private void routeRequestReplyHead(final RoutingContext routingContext, final Message<String> responseMessage)
	{
		final MultiMap headers = responseMessage.headers();
		final Set<String> headerNames = headers.names();
		final HttpServerResponse httpServerResponse = routingContext.response();
		final MultiMap responseHeaders = httpServerResponse.headers();
		headerNames.stream().filter(hn -> !hn.equals(Constants.HTTP_RESPONSE_CODE_HEADER))
				.forEach(hn -> responseHeaders.add(hn, headers.get(hn)));
		final String messageStatusCode = headers.get(Constants.HTTP_RESPONSE_CODE_HEADER);
		if (!StringUtils.isEmpty(messageStatusCode))
		{
			httpServerResponse.setStatusCode(Integer.valueOf(messageStatusCode));
		}
		else
		{
			httpServerResponse.setStatusCode(Constants.HTTP_STATUS_CODE_SUCCESS);
		}
		httpServerResponse.end();
	}

	private void routeRequestReplyError(final RoutingContext routingContext, final Throwable error)
	{
		routingContext.response().setStatusCode(Constants.HTTP_STATUS_CODE_BAD_REQUEST).end(Json.encodePrettily(error));
	}

	private void routeRequestReplyError(final RoutingContext routingContext)
	{

		routingContext.response().setStatusCode(Constants.HTTP_STATUS_CODE_BAD_REQUEST).end();
	}

	@Override
	protected String getRootContext()
	{
		return CONTEXT_ROOT;
	}

	@Resource
	public void setBallancingStrategy(final BallancingStrategy ballancingStrategy)
	{
		this.ballancingStrategy = ballancingStrategy;
	}
}
