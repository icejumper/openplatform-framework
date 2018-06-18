FROM openjdk:8-jre-alpine

ENV SERVER_PORT 8085
EXPOSE $SERVER_PORT

# add ssl certificate
COPY target/classes/certificates/openplatform_trusted.jks /opt/security/

# add application jar
COPY target/zeppelins-gateway-0.1.jar /opt/

# add immutable dependencies
COPY target/libs/ /opt/libs

# add pseudomutable dependencies
COPY target/mutablelibs/ /opt/mutablelibs

## add the run script, make it executable
COPY target/classes/scripts/run.sh /opt/run.sh
RUN chmod +x /opt/run.sh

ENTRYPOINT [ "/bin/sh", "/opt/run.sh" ]

# for container debugging purposes only. Commend the ENTRYPOINT line and uncommend the line below
#CMD [ "/bin/sh", "/opt/run.sh" ]
