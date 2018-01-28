package com.hybris.openplatform.common.deployment;

public interface VerticleDeployer 
{
	 void preDeployVerticles();

	 void deployVerticles();

	 void postDeployVerticles();
}
