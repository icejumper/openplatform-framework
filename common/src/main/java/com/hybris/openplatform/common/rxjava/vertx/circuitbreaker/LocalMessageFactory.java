package com.hybris.openplatform.common.rxjava.vertx.circuitbreaker;

import io.vertx.core.eventbus.MessageCodec;
import io.vertx.core.eventbus.impl.MessageImpl;
import io.vertx.rxjava.core.eventbus.Message;


public class LocalMessageFactory
{

	public static <T> Message<T> createNewMessage(final String address, final T messageBody, final MessageCodec messageCodec)
	{
		return Message.newInstance(new MessageImpl(address,
				null, null, messageBody, messageCodec, false, null));
	}

}
