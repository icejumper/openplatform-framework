package com.hybris.openplatform.common.rxjava.vertx.circuitbreaker;

import io.vertx.core.Handler;
import io.vertx.rxjava.circuitbreaker.CircuitBreaker;


public interface CircuitBreakerProvider
{

	CircuitBreaker getBreaker(String name, Handler<Void> openHandler, Handler<Void> closeHandler, Handler<Void> halfOpenHandler);

}
