package com.hybris.openplatform.common.rxjava.context;

import com.hybris.openplatform.common.context.SpringProfileDiscovery;

import java.net.Inet4Address;
import java.net.UnknownHostException;
import java.util.Objects;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import javax.annotation.Resource;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.stereotype.Component;

import com.hazelcast.core.HazelcastInstance;

import io.vertx.core.VertxOptions;
import io.vertx.rxjava.core.Vertx;
import io.vertx.spi.cluster.hazelcast.HazelcastClusterManager;


@Component
public class RxVertxProducer
{

	private static final Logger LOG = LoggerFactory.getLogger(RxVertxProducer.class);

	private HazelcastInstance eventBusHazelcastInstance;
	private SpringProfileDiscovery springProfileDiscovery;

	private Vertx microRxVertx;
	private VertxOptions vertxOptions;

	@PostConstruct
	void init() throws ExecutionException, InterruptedException, UnknownHostException
	{
		if(Objects.nonNull(eventBusHazelcastInstance))
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
			microRxVertx = future.get();
		}
		else
		{
			microRxVertx = Vertx.vertx();
		}
	}

	/**
	 * Exposes the clustered Vert.x instance.
	 * We must disable destroy method inference, otherwise Spring will call the {@link Vertx#close()} automatically.
	 */
	@Bean(destroyMethod = "")
	public Vertx microRxVertx()
	{
		return microRxVertx;
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
		microRxVertx.close(ar -> future.complete(null));
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
