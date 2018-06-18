package com.hybris.openplatform.util;

import com.hybris.openplatform.bootstrap.util.DiscoveryUtils;

import java.util.Optional;

import org.assertj.core.api.Assertions;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;


public class DiscoveryUtilsTest
{
	private DiscoveryUtils discoveryUtils;

	@Before
	public void setUp()
	{
		discoveryUtils = new DiscoveryUtils();
		discoveryUtils = Mockito.spy(discoveryUtils);
	}

	@Test
	public void testHostNameConversion1()
	{
		Mockito.when(discoveryUtils.getHostName()).thenReturn(Optional.of("k8s-agent-32434E-4"));
		final String normalizedMasterNodeName = discoveryUtils.resolveName("${master}:9092",
				"(.+)-(agent)-(.+)-(.+):(\\d+)", "$1", "master", "$3", "0", "$5");
		Assertions.assertThat(normalizedMasterNodeName).isEqualTo("k8s-master-32434E-0:9092");
	}

	@Test
	public void testHostNameConversion2()
	{
		Mockito.when(discoveryUtils.getHostName()).thenReturn(Optional.of("k8s-agent-32434E-4"));
		final String normalizedMasterNodeName = discoveryUtils.resolveName("${master}:9092",
				"(.+)-(agent)-(.+)-(.+):(\\d+)", "SomethingLong", "master", "$3", "0", "$5");
		Assertions.assertThat(normalizedMasterNodeName).isEqualTo("SomethingLong-master-32434E-0:9092");
	}

	@Test
	public void testHostNameConversionWrongNumberOfSubstitutions()
	{
		Mockito.when(discoveryUtils.getHostName()).thenReturn(Optional.of("k8s-agent-32434E-4"));
		final String normalizedMasterNodeName = discoveryUtils.resolveName("${master}:9092",
				"(.+)-(agent)-(.+)-(.+):(\\d+)", "SomethingLong", "master", "$3", "0");
		Assertions.assertThat(normalizedMasterNodeName).isEqualTo("master:9092");
	}
}
