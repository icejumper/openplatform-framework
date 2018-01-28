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
package com.hybris.openplatform.common.deployment.impl;

import com.hybris.openplatform.common.deployment.VerticleDeployer;
import com.hybris.openplatform.stereotypes.VerticleDeploymentService;


@VerticleDeploymentService("verticleDeploymentService")
public class DefaultVerticleDeployer implements VerticleDeployer
{

	@Override
	public void preDeployVerticles()
	{
		// empty
	}

	@Override
	public void deployVerticles()
	{
		// empty
	}

	@Override
	public void postDeployVerticles()
	{
		// empty
	}
}
