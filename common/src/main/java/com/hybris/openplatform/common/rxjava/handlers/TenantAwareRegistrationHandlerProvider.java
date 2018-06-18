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
package com.hybris.openplatform.common.rxjava.handlers;

import com.hybris.openplatform.bootstrap.constants.Constants;
import com.hybris.openplatform.common.context.RequestTenantContext;

import java.util.Objects;

import javax.annotation.Resource;

import io.vertx.rxjava.core.eventbus.Message;


/**
 * message handler with tenant-awareness logic
 */
public abstract class TenantAwareRegistrationHandlerProvider extends AbstractRegistrationHandlerProvider
{
	private RequestTenantContext requestTenantContext;

	@Override
	public void processMessage(final Message<String> message)
	{
		final String tenantId = message.headers().get(Constants._TENANT_);
		if (Objects.nonNull(tenantId))
		{
			requestTenantContext.setCurrentTenant(tenantId);
			processMessageWithTenant(message);
			requestTenantContext.removeCurrentTenant();
		}
	}

	protected abstract void processMessageWithTenant(final Message<String> message);

	@Resource
	public void setRequestTenantContext(final RequestTenantContext requestTenantContext)
	{
		this.requestTenantContext = requestTenantContext;
	}
}
