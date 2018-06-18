#!/bin/sh

java -Dspring.profiles.active=@orchestrator-profile@ -cp "/opt/libs/*:/opt/mutablelibs/*:/opt/zeppelins-gateway-0.1.jar" com.hybris.openplatform.gateway.application.GatewayStarter
