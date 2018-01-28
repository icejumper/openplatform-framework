package com.hybris.openplatform.bootstrap.rxjava.handlers;

import com.hybris.openplatform.bootstrap.messages.RestEndpointRegistration;

import io.vertx.core.Handler;
import io.vertx.rxjava.core.Vertx;
import io.vertx.rxjava.core.eventbus.EventBus;
import io.vertx.rxjava.core.eventbus.Message;


public interface AsyncRegistrationHandlerProvider
{

	default void handle(final Vertx vertx, final RestEndpointRegistration registration)
	{
		if (processRegistration(registration))
		{
			final EventBus eventBus = vertx.eventBus();
			eventBus.consumer(registration.getServiceAddress(), messageHandler());
		}
	}

	default Handler<Message<String>> messageHandler()
	{
		return this::processMessage;
	}

	boolean processRegistration(final RestEndpointRegistration registration);

	void processMessage(Message<String> message);

}
