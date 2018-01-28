package com.hybris.openplatform.common.context;

import java.net.Inet4Address;
import java.net.UnknownHostException;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import javax.annotation.Resource;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

import com.hazelcast.core.HazelcastInstance;

import io.vertx.core.Vertx;
import io.vertx.core.VertxOptions;
import io.vertx.spi.cluster.hazelcast.HazelcastClusterManager;


@Component
@Profile("no-rx-vertx")
public class VertxProducer
{

	private static final Logger LOG = LoggerFactory.getLogger(VertxProducer.class);

	private HazelcastInstance eventBusHazelcastInstance;
	private SpringProfileDiscovery springProfileDiscovery;

	private Vertx microVertx;
	private VertxOptions vertxOptions;

	@PostConstruct
	void init() throws ExecutionException, InterruptedException, UnknownHostException
	{
		final HazelcastClusterManager clusterManager = new HazelcastClusterManager(eventBusHazelcastInstance);

		vertxOptions = new VertxOptions();
		final VertxOptions options = vertxOptions.setClusterManager(clusterManager);
		if (springProfileDiscovery.isProfileKubernates())
		{
			final String ipAddress = Inet4Address.getLocalHost().getHostAddress();
			options.setClusterHost(ipAddress);
		}
		final CompletableFuture<Vertx> future = new CompletableFuture<>();
		Vertx.clusteredVertx(options, ar -> {
			if (ar.succeeded())
			{
				LOG.info("Successfully finished cluster configuratin for Vert.X using hazelcast cluster group {}",
						eventBusHazelcastInstance.getConfig().getGroupConfig().getName());
				future.complete(ar.result());
			}
			else
			{
				future.completeExceptionally(ar.cause());
			}
		});
		microVertx = future.get();
	}

	/**
	 * Exposes the clustered Vert.x instance.
	 * We must disable destroy method inference, otherwise Spring will call the {@link Vertx#close()} automatically.
	 */
	@Bean(destroyMethod = "")
	public Vertx microVertx()
	{
		return microVertx;
	}

	@Bean
	public VertxOptions vertxOptions()
	{
		return vertxOptions;
	}

	@PreDestroy
	void close() throws ExecutionException, InterruptedException
	{
		CompletableFuture<Void> future = new CompletableFuture<>();
		microVertx.close(ar -> future.complete(null));
		future.get();
	}

	@Resource
	public void setSpringProfileDiscovery(final SpringProfileDiscovery springProfileDiscovery)
	{
		this.springProfileDiscovery = springProfileDiscovery;
	}

	@Resource
	public void setEventBusHazelcastInstance(final HazelcastInstance eventBusHazelcastInstance)
	{
		this.eventBusHazelcastInstance = eventBusHazelcastInstance;
	}
}
