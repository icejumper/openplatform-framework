package com.hybris.openplatform.common.deployment;

import com.hybris.openplatform.bootstrap.rxjava.vertx.MicroServiceVerticle;


public interface VerticlePostProcessor
{
	void beforeVerticleDeployment(MicroServiceVerticle microServiceVerticle);

	void afterVerticleDeployment(MicroServiceVerticle microServiceVerticle);
	
}
