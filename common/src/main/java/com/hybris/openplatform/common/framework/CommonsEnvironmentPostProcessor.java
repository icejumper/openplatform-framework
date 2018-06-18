package com.hybris.openplatform.common.framework;

import com.hybris.openplatform.stereotypes.MicroserviceApplication;

import java.io.IOException;
import java.util.List;
import java.util.Objects;

import org.apache.commons.collections4.CollectionUtils;
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
		final ConfigurableEnvironment parent = new CommonsConfigurableEnvironment();
		final MutablePropertySources propertySources = parent.getPropertySources();

		final List<PropertySource<?>> loadCommonsFrameworkList = tryLoadingYamlPropertySource("commonsFramework", "yml", "yaml");
		final PropertySource<?> loadCommonsFramework;
		if (CollectionUtils.isNotEmpty(loadCommonsFrameworkList))
		{
			if (loadCommonsFrameworkList.size() == 1)
			{
				loadCommonsFramework = loadCommonsFrameworkList.get(0);
				propertySources.addLast(loadCommonsFramework);
			}
			else
			{
				throw new RuntimeException("Found more than one property configuration for commons framework: not supported");
			}
		}
		else
		{
			loadCommonsFramework = null;
		}

		String profile = null;
		final MicroserviceApplication microserviceApplicationAnn = springApplication.getMainApplicationClass()
				.getAnnotation(MicroserviceApplication.class);
		if (Objects.nonNull(microserviceApplicationAnn) && !StringUtils.isEmpty(microserviceApplicationAnn.profile()))
		{
			profile = microserviceApplicationAnn.profile();

			final List<PropertySource<?>> loadAppPropertiesSources = tryLoadingYamlPropertySource(profile, "yml", "yaml");
			if (Objects.nonNull(loadCommonsFramework) && CollectionUtils.isNotEmpty(loadAppPropertiesSources))
			{
				loadAppPropertiesSources.forEach(s -> propertySources.addBefore(loadCommonsFramework.getName(), s));
			}
		}
		configurableEnvironment.merge(parent);
	}

	private List<PropertySource<?>> tryLoadingYamlPropertySource(final String profile, final String... extensions)
	{
		final YamlPropertySourceLoader yamlPropertySourceLoader = new YamlPropertySourceLoader();
		Exception lastException = null;
		List<PropertySource<?>> propertySource = null;
		for (String ext : extensions)
		{
			try
			{
				final String resourcePath = "/application-" + profile + "." + ext;
				propertySource = yamlPropertySourceLoader
						.load("classpath:" + resourcePath, new ClassPathResource(resourcePath));
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
