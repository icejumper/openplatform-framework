package com.hybris.openplatform.bootstrap.handlers;

import com.hybris.openplatform.bootstrap.messages.RestEndpointRegistration;

import io.vertx.core.AsyncResult;
import io.vertx.core.Handler;
import io.vertx.core.Vertx;
import io.vertx.core.eventbus.EventBus;
import io.vertx.core.eventbus.Message;


public interface AsyncRegistrationHandlerProvider
{

	default Handler<AsyncResult<Message<String>>> handle(final Vertx vertx, final RestEndpointRegistration registration)
	{
		return asyncResult -> {
			if (processRegistration(asyncResult, registration))
			{
				final EventBus eventBus = vertx.eventBus();
				eventBus.consumer(registration.getServiceAddress(), messageHandler());
			}
		};
	}

	default Handler<Message<String>> messageHandler()
	{
		return this::processMessage;
	}

	boolean processRegistration(AsyncResult<Message<String>> asyncResult, final RestEndpointRegistration registration);

	void processMessage(Message<String> message);

}
