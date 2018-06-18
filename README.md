# openplatform-framework
Springboot-based framework, designed to build microservices. Depends on Springboot 2.0, Vert.X 3, Hazelcast 3.6  

# Jump-start

- clone project in your local repo (<i>git clone https://github.com/icejumper/openplatform-framework.git</i>)
- <i>cd openplatform-framework</i>
- execute <i>mvn clean install</i>
- <i>cd gateway</i>
- execute <i>./runservice.sh</i>

# Introduction

The zeppelins openplatform framework helps creating the reactive microservices, based on
springboot 2.0 (https://spring.io/projects/spring-boot) and Vert.X toolkit (https://vertx.io/). 
The working and rather useful example of such microservice 
is a <i>zeppelins-gateway</i> application and is distrubuted as part of the framework. 
On the one hand side it is an application <i>per se</i>, whose role is to accept the "registration events"
from other services running in the same eventbus space and expose these services through secure https endpoints.
On the other hand side this application is a flexible validation and redirection gateway, accepting REST requests,
validating them and redirecting in the form of eventbus messages for further processing to a corresponding service.
    
 
