package com.hybris.openplatform.common.framework;

import static java.util.Objects.nonNull;

import com.hybris.openplatform.bootstrap.rxjava.vertx.MicroServiceVerticle;
import com.hybris.openplatform.common.deployment.VerticleDeployer;
import com.hybris.openplatform.common.deployment.VerticlePostProcessor;
import com.hybris.openplatform.stereotypes.MainVerticle;
import com.hybris.openplatform.stereotypes.VerticleComponent;
import com.hybris.openplatform.stereotypes.VerticleComponent.VerticleType;
import com.hybris.openplatform.stereotypes.VerticleDeploymentService;

import java.lang.reflect.Proxy;
import java.util.Collection;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.beans.factory.config.BeanPostProcessor;
import org.springframework.stereotype.Component;
import org.springframework.util.ClassUtils;
import org.springframework.util.CollectionUtils;
import org.springframework.util.ReflectionUtils;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;

import io.vertx.core.DeploymentOptions;
import io.vertx.core.Verticle;
import io.vertx.core.Vertx;
import io.vertx.core.spi.VerticleFactory;


@Component
public class MainVerticleBeanPostProcessor implements BeanPostProcessor
{
	private static final Logger LOG = LoggerFactory.getLogger(MainVerticleBeanPostProcessor.class);
	private static final String INVOKE_DEPLOY_SUB_VERTICLES = "deployVerticles";

	@Value("${vertx.deployment.instances:16}")
	private int numOfVerticleInstances;
	@Value("${vertx.deployment.worker:false}")
	private boolean isVerticleWorker;

	private Object verticleDeploymentService;
	private Object mainVerticle;
	private VerticleFactory verticleFactory;
	private Collection<VerticlePostProcessor> verticlePostProcessors = Lists.newArrayList();
	private Map<Verticle, VerticleComponent> subVerticles = Maps.newLinkedHashMap();

	@Override
	public Object postProcessBeforeInitialization(final Object bean, final String beanName)
	{
		final Class<?> beanClass = bean.getClass();
		final MainVerticle mainVerticleAnnotation = beanClass.getAnnotation(MainVerticle.class);
		final VerticleComponent verticleComponentAnnotation = beanClass.getAnnotation(VerticleComponent.class);
		final VerticleDeploymentService verticleDeploymentServiceAnnotation = beanClass
				.getAnnotation(VerticleDeploymentService.class);
		if (nonNull(mainVerticleAnnotation))
		{
			mainVerticle = bean;
		}
		else if (nonNull(verticleComponentAnnotation))
		{
			subVerticles.put((Verticle) bean, verticleComponentAnnotation);
		}
		else if (nonNull(verticleDeploymentServiceAnnotation))
		{
			verticleDeploymentService = bean;
		}
		else if (bean instanceof VerticleFactory)
		{
			verticleFactory = (VerticleFactory) bean;
		}
		else if (bean instanceof VerticlePostProcessor)
		{
			verticlePostProcessors.add((VerticlePostProcessor) bean);
		}
		return bean;
	}

	@Override
	public Object postProcessAfterInitialization(final Object bean, final String beanName)
	{
		try
		{
			if (bean instanceof VerticleDeployer && verticleDeploymentService instanceof VerticleDeployer && bean.equals(verticleDeploymentService))
			{
				return Proxy.newProxyInstance(bean.getClass().getClassLoader(), ClassUtils.getAllInterfaces(bean),
						(proxy, method, args) -> {
							if (INVOKE_DEPLOY_SUB_VERTICLES.equals(method.getName()))
							{
								internalDeployVerticles();
							}
							return ReflectionUtils.invokeMethod(method, bean, args);
						});
			}
		}
		catch (final Exception e)
		{
			LOG.warn("Failed processing the bean [{}]: {}", beanName, e);
		}
		return bean;
	}

	private void internalDeployVerticles()
	{
		LOG.info("Deploying sub-verticles....");
		final Vertx vertx = ((Verticle) mainVerticle).getVertx();
		if (!CollectionUtils.isEmpty(subVerticles))
		{
			LOG.info("Deploying {} sub-verticles....", subVerticles.size());
		}
		for (Map.Entry<Verticle, VerticleComponent> subVerticleEntry : subVerticles.entrySet())
		{
			final Verticle subVerticle = subVerticleEntry.getKey();
			LOG.info("Deploying verticle: {}", subVerticle);
			final DeploymentOptions deploymentOptions = new DeploymentOptions()
					.setWorker(isVerticleWorker)
					.setInstances(numOfVerticleInstances);
			final VerticleComponent verticleComponentAnnotation = subVerticleEntry.getValue();
			configureVerticle(deploymentOptions, verticleComponentAnnotation);
			String workerVerticleName = verticleFactory.prefix() + ":" + subVerticle.getClass().getName();
			verticlePostProcessors.forEach(vpp -> vpp.beforeVerticleDeployment((MicroServiceVerticle) subVerticle));
			vertx.deployVerticle(workerVerticleName, deploymentOptions, ar -> {
				if (ar.failed())
				{
					LOG.error("Failed to deploy verticle", ar.cause());
				}
				else
				{
					verticlePostProcessors.forEach(vpp -> vpp.afterVerticleDeployment((MicroServiceVerticle) subVerticle));
					String logMessageTemplate = "Verticle {} was successfully deployed in {} instances (as {} Verticle type). id=[{}]";
					LOG.info(logMessageTemplate, subVerticle.getClass().getName(), deploymentOptions.getInstances(),
							deploymentOptions.isWorker() ? "WORKER" : "STANDARD", ar.result());
				}
			});
		}
	}

	private void configureVerticle(final DeploymentOptions options, final VerticleComponent verticleComponentDeclaration)
	{
		final VerticleType verticleType = verticleComponentDeclaration.worker();
		final int verticleInstances = verticleComponentDeclaration.instances();
		if (!verticleType.equals(VerticleType.DEFAULT))
		{
			options.setWorker(verticleType.equals(VerticleType.WORKER));
		}
		if (verticleInstances > 0)
		{
			options.setInstances(verticleInstances);
		}
	}
}
