package com.hybris.openplatform.gateway.vertx;

import com.hybris.openplatform.bootstrap.rxjava.handlers.AsyncRegistrationHandlerProvider;
import com.hybris.openplatform.gateway.vertx.messages.Messages;
import com.hybris.openplatform.stereotypes.SinglePurposeService;
import com.hybris.openplatform.stereotypes.VerticleComponent;

import javax.annotation.Resource;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.vertx.core.http.HttpMethod;


@VerticleComponent(value = "adminGetRegistrationListVerticle", instances = 1)
@SinglePurposeService(serviceId = "admin_getregistrationlist", method = HttpMethod.GET, endPointPattern = "/endpoints/list", serviceDescription = "GET list of registered endpoints")
public class AdminGetRegistrationListVerticle extends AbstractRegisterableServiceVerticle
{
	private static final Logger LOG = LoggerFactory.getLogger(AdminGetRegistrationListVerticle.class);

	private static final String CONTEXT_ROOT = "/admin";

	private AsyncRegistrationHandlerProvider adminGetPromotionsListRegistrationHandlerProvider;

	@Override
	protected void registerVerticleEndPoints()
	{
		super.registerVerticleEndPoints();
		super.<String>rxRegisterLocalConsumer(Messages.ADMIN_GET_REGISTRATIONS_LIST)
				.handler(getAsyncRegistrationHandlerProvider().messageHandler());
	}

	@Override
	protected String getRootContext()
	{
		return CONTEXT_ROOT;
	}

	@Override
	protected AsyncRegistrationHandlerProvider getAsyncRegistrationHandlerProvider()
	{
		return adminGetPromotionsListRegistrationHandlerProvider;
	}

	@Resource
	public void setAdminGetPromotionsListRegistrationHandlerProvider(
			final AsyncRegistrationHandlerProvider adminGetPromotionsListRegistrationHandlerProvider)
	{
		this.adminGetPromotionsListRegistrationHandlerProvider = adminGetPromotionsListRegistrationHandlerProvider;
	}
}
