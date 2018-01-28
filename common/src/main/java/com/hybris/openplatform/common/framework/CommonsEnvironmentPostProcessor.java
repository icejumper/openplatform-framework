package com.hybris.openplatform.common.framework;

import com.hybris.openplatform.stereotypes.MicroserviceApplication;

import java.io.IOException;
import java.util.Objects;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.env.EnvironmentPostProcessor;
import org.springframework.boot.env.YamlPropertySourceLoader;
import org.springframework.core.env.ConfigurableEnvironment;
import org.springframework.core.env.MutablePropertySources;
import org.springframework.core.env.PropertySource;
import org.springframework.core.io.ClassPathResource;
import org.springframework.util.StringUtils;


public class CommonsEnvironmentPostProcessor implements EnvironmentPostProcessor
{
	@Override
	public void postProcessEnvironment(final ConfigurableEnvironment configurableEnvironment,
			final SpringApplication springApplication)
	{
		final MicroserviceApplication microserviceApplicationAnn = springApplication.getMainApplicationClass()
				.getAnnotation(MicroserviceApplication.class);

		if (Objects.nonNull(microserviceApplicationAnn) && !StringUtils.isEmpty(microserviceApplicationAnn.profile()))
		{
			final String profile = microserviceApplicationAnn.profile();

			final ConfigurableEnvironment parent = new CommonsConfigurableEnvironment();
			final MutablePropertySources propertySources = parent.getPropertySources();

			final PropertySource<?> load = tryLoadingYamlPropertySource(profile, "yml", "yaml");
			if(Objects.nonNull(load))
			{
				propertySources.addLast(load);
				configurableEnvironment.merge(parent);
			}
		}
	}

	private PropertySource<?> tryLoadingYamlPropertySource(final String profile, final String... extensions)
	{
		final YamlPropertySourceLoader yamlPropertySourceLoader = new YamlPropertySourceLoader();
		Exception lastException = null;
		PropertySource<?> propertySource = null;
		for (String ext : extensions)
		{
			try
			{
				final String resourcePath = "application-" + profile + "." + ext;
				propertySource = yamlPropertySourceLoader
						.load("classpath:/" + resourcePath, new ClassPathResource(resourcePath), null);
				return propertySource;
			}
			catch (RuntimeException | IOException e)
			{
				lastException = e;
			}
		}
		if (Objects.nonNull(lastException))
		{
			throw new IllegalStateException(lastException);
		}
		return null;
	}
}
