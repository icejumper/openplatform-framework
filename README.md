# openplatform-framework
Springboot-based framework, designed to build microservices. Depends on Springboot 2.0, Vert.X 3, Hazelcast 3.6  

# Jump-start

- clone project in your local repo (<i>git clone https://github.com/icejumper/openplatform-framework.git</i>)
- <i>cd openplatform-framework</i>
- execute <i>mvn clean install</i>
- <i>cd gateway</i>
- execute <i>./runservice.sh</i>

# Building docker image for zeppelins-gateway

- make sure you run the docker machine (e.g. issue <i>docker-machine start</i> if you did not do so)
- run <i>eval $(docker-machine env)</i> to make sure you have the correct docker virtual machine environment variables
- run <i>mvn docker:build</i>. After the task finishes, you should see the image <i>myresource.azurecr.io/zeppelins-gateway:latest</i> in the docker images list (<i>docker images</i>)
- test, if the image runs correctly (e.g. by running <i>docker run -it myresource.azurecr.io/zeppelins-gateway:latest /bin/sh</i>)

# Deploying on Kubernetes cluster

- make sure you have the correct Kubernetes configuration (e.g. if using <i>minikube</i>, it is running and the docker repository environment is configured correctly)
- run <i>eval $(minikube docker-env)</i>
- run <i>mvn docker:build</i>. After the task finishes, you should see the image <i>zeppelins-gateway:0.1.0</i> in the docker images list (<i>docker images</i>)
- run <i>mvn fabric8:deploy -Pkubernetes</i>. This maven task will create the docker image, deploy it to associated kubernetes docker container and deploy a pod
- check the status of pod (<i>kubectl get po</i>) and running service (<i>kubectl get services</i>). You must see something like:

```bash
NAME                CLUSTER-IP      EXTERNAL-IP   PORT(S)    AGE
kubernetes          10.96.0.1       <none>        443/TCP    79d
zeppelins-gateway   10.100.236.39   <none>        8085/TCP   7m
```
  

# Introduction

The zeppelins openplatform framework helps creating the reactive microservices, based on
springboot 2.0 (https://spring.io/projects/spring-boot) and Vert.X toolkit (https://vertx.io/). 
The working and rather useful example of such microservice 
is a <i>zeppelins-gateway</i> application and is distrubuted as part of the framework. 
On the one hand side it is an application <i>per se</i>, whose role is to accept the "registration events"
from other services running in the same eventbus space and expose these services through secure https endpoints.
On the other hand side this application is a flexible validation and redirection gateway, accepting REST requests,
validating them and redirecting in the form of eventbus messages for further processing to a corresponding service.
    
 
