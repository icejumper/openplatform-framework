package com.hybris.openplatform.gateway.services;


import com.hybris.openplatform.bootstrap.messages.RestEndpointRegistration;

import java.util.List;
import java.util.Optional;


public interface GatewayService
{
	
	Optional<RestEndpointRegistration> getRegistration(final String pattern);

	List<RestEndpointRegistration> getRegistrations();

}
