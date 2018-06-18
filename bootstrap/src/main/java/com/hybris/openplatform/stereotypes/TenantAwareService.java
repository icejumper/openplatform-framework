package com.hybris.openplatform.stereotypes;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

import org.springframework.stereotype.Service;


@Service
@Retention(RetentionPolicy.RUNTIME)
public @interface TenantAwareService
{
	
}
