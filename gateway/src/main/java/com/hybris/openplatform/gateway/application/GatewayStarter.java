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
