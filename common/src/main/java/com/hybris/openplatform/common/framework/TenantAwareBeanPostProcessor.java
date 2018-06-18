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
package com.hybris.openplatform.common.framework;

import static java.util.Objects.nonNull;

import com.hybris.openplatform.common.context.RequestTenantContext;
import com.hybris.openplatform.stereotypes.TenantAwareService;

import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.config.BeanPostProcessor;
import org.springframework.stereotype.Component;
import org.springframework.util.ClassUtils;
import org.springframework.util.ReflectionUtils;

import com.google.common.collect.Maps;
import com.google.common.collect.ObjectArrays;


/**
 * The bean post processor which creates the dynamic proxy around all methods of the class annotated with @TenantAwareService
 * In order to be compliant with the contract, the methods of the class must be duplicated - one without the tenantId, and
 * one - with tenantId as first argument
 */
@Component
public class TenantAwareBeanPostProcessor implements BeanPostProcessor
{
	private static final Logger LOGGER = LoggerFactory.getLogger(TenantAwareBeanPostProcessor.class);

	private Map<String, Object> tenantAwareBeans = Maps.newHashMap();
	private RequestTenantContext requestTenantContext;

	@Override
	public Object postProcessBeforeInitialization(final Object bean, final String beanName)
	{
		final TenantAwareService tenantAwareService = bean.getClass().getAnnotation(TenantAwareService.class);
		if (nonNull(tenantAwareService))
		{
			LOGGER.info("Adding the bean [{}] to a list of tenant-aware objects", beanName);
			tenantAwareBeans.put(beanName, bean);
		}
		if (bean instanceof RequestTenantContext)
		{
			requestTenantContext = (RequestTenantContext) bean;
		}

		return bean;
	}

	@Override
	public Object postProcessAfterInitialization(final Object bean, final String beanName)
	{
		if (tenantAwareBeans.containsKey(beanName))
		{
			LOGGER.info("Creating the tenant-aware proxy around the bean [{}]", beanName);
			return Proxy.newProxyInstance(bean.getClass().getClassLoader(), ClassUtils.getAllInterfaces(bean),
					(proxy, method, args) -> {
						final Class<?>[] parameterTypes = method.getParameterTypes().length > 0 ?
								ObjectArrays.concat(String.class, method.getParameterTypes()) :
								new Class<?>[] { String.class };
						Method tenantAwareMethod;
						try
						{
							tenantAwareMethod = ClassUtils.getMethod(bean.getClass(), method.getName(), parameterTypes);
						}
						catch (final Exception e)
						{
							LOGGER.warn(
									"The tenant-aware method {} with tenantId as first argument was not found, cannot execute the method in tenant-aware mode",
									method.getName());
							return ReflectionUtils.invokeMethod(method, bean, args);
						}
						if (nonNull(requestTenantContext))
						{
							final String currentTenant = requestTenantContext.getCurrentTenant();
							final Object[] newArgs = ObjectArrays.concat(currentTenant, args);
							return ReflectionUtils.invokeMethod(tenantAwareMethod, bean, newArgs);
						}
						else
						{
							LOGGER.warn(
									"The RequestTenantContext was not found in the system, cannot set up the tenant-aware context correctly");
							return ReflectionUtils.invokeMethod(method, bean, args);
						}
					});
		}
		return bean;
	}
}
