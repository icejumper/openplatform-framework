#!/bin/sh

java -Dspring.profiles.active=default -cp "target/libs/*:target/mutablelibs/*:target/zeppelins-gateway-0.1.jar" com.hybris.openplatform.gateway.application.GatewayStarter
