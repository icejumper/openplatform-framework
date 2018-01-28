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
package com.hybris.openplatform.gateway.application;

import com.hybris.openplatform.common.rxjava.application.AbstractOpenPlatformMicroservices;
import com.hybris.openplatform.stereotypes.MicroserviceApplication;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration;
import org.springframework.boot.autoconfigure.orm.jpa.HibernateJpaAutoConfiguration;
import org.springframework.context.annotation.ComponentScan;



@ComponentScan({ "com.hybris.openplatform.gateway" })
@MicroserviceApplication(profile = "gateway")
@EnableAutoConfiguration(exclude = { DataSourceAutoConfiguration.class, HibernateJpaAutoConfiguration.class })
public class GatewayStarter extends AbstractOpenPlatformMicroservices
{

	public static void main(String[] args)
	{
		SpringApplication.run(GatewayStarter.class, args);
	}

	@Override
	protected String serviceName()
	{
		return "apiGateway";
	}
}
