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
