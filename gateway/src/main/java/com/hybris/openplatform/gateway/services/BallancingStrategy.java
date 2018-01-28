package com.hybris.openplatform.gateway.services;

import com.hybris.openplatform.bootstrap.messages.RestEndpointRegistration;


public interface BallancingStrategy
{
	boolean addNewRegistration(RestEndpointRegistration registration);

	RestEndpointRegistration getNextRegisteredNode(String method, String pattern);

	default String getRegistrationEntryKey(String method, String pattern)
	{
		return method + "@" + pattern;
	}
}
