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
