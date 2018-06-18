package com.hybris.openplatform.common.context.impl;

import com.hybris.openplatform.common.context.RequestTenantContext;

import org.springframework.stereotype.Component;


/**
 * Implementation of the {@link RequestTenantContext}, based on ThreadLocal
 */
@Component
public class DefaultRequestTenantContext implements RequestTenantContext
{

	private ThreadLocal<String> tenantIdThreadLocal = new ThreadLocal<>();

	@Override
	public void setCurrentTenant(final String tenantId)
	{
		tenantIdThreadLocal.set(tenantId);
	}

	@Override
	public String getCurrentTenant()
	{
		return tenantIdThreadLocal.get();
	}

	@Override
	public void removeCurrentTenant()
	{
		tenantIdThreadLocal.remove();
	}
}
