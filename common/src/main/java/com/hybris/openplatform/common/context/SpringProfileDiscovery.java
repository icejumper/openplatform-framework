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

import static java.util.Arrays.stream;
import static java.util.Objects.isNull;
import static java.util.Objects.nonNull;

import javax.annotation.Resource;

import org.springframework.core.env.Environment;
import org.springframework.stereotype.Component;


@Component
public class SpringProfileDiscovery
{
	private Environment env;

	public boolean isProfileDefault()
	{
	   return isNull(env.getActiveProfiles()) || stream(env.getActiveProfiles()).anyMatch(p -> p.equals("default"));
	}

	public boolean isProfileKubernates()
	{
		return nonNull(env.getActiveProfiles()) && stream(env.getActiveProfiles()).anyMatch(p -> p.equals("kubernetes"));
	}

	@Resource
	public void setEnv(final Environment env)
	{
		this.env = env;
	}
}
