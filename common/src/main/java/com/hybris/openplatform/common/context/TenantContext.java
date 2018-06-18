package com.hybris.openplatform.common.context;

import javax.annotation.PostConstruct;
import javax.annotation.Resource;

import org.springframework.core.env.Environment;
import org.springframework.stereotype.Component;


@Component
public class TenantContext
{
	private String name;
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

	@Resource
	public void setEnv(final Environment env)
	{
		this.env = env;
	}
}
