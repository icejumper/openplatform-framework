FROM openjdk:8-jre-alpine

ENV SERVER_PORT 8085
EXPOSE $SERVER_PORT

COPY target/gateway-1.0-SNAPSHOT.jar /opt/

CMD [ "java", "-Dspring.profiles.active=kubernetes", "-jar", "/opt/gateway-1.0-SNAPSHOT.jar" ]
