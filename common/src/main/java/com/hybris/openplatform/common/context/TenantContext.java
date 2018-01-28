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
package com.hybris.openplatform.common.context;

import javax.annotation.PostConstruct;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Component;


@Component
public class TenantContext
{
	private String name;
	@Autowired
	private Environment env;

	public String getName()
	{
		return name;
	}

	@PostConstruct
	private void afterPropertiesSet()
	{
		name = env.getProperty("runtime.context.tenant", "master");
	}
}
