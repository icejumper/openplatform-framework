package com.hybris.openplatform.common.context;

public interface RequestTenantContext
{

	void setCurrentTenant(String tenantId);

	String getCurrentTenant();

	void removeCurrentTenant();

}
