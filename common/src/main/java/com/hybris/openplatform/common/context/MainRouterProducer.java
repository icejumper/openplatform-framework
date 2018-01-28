package com.hybris.openplatform.common.context;

import javax.annotation.Resource;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

import io.vertx.core.Vertx;
import io.vertx.ext.web.Router;


@Component
@Profile("no-rx-vertx")
public class MainRouterProducer
{

	private Vertx microVertx;

	@Bean
	public Router mainRouter()
	{
		return Router.router(microVertx);
	}

	@Resource
	public void setMicroVertx(final Vertx microVertx)
	{
		this.microVertx = microVertx;
	}
}
