package com.hybris.openplatform.common.rxjava.context;

import javax.annotation.Resource;

import org.springframework.context.annotation.Bean;
import org.springframework.stereotype.Component;

import io.vertx.rxjava.core.Vertx;
import io.vertx.rxjava.ext.web.Router;



@Component("mainRxRouterProducer")
public class MainRouterProducer
{
	private Vertx microRxVertx;

	@Bean
	public Router mainRxRouter()
	{
		return Router.router(microRxVertx);
	}

	@Resource
	public void setMicroVertx(final Vertx microVertx)
	{
		this.microRxVertx = microVertx;
	}
}
