FROM openjdk:11.0.3-jre-slim

ARG jar

RUN test -n "$jar"

COPY $jar iexec-result-proxy.jar

# For Spring-Boot project, use the entrypoint
# below to reduce Tomcat startup time.
ENTRYPOINT exec java -Djava.security.egd=file:/dev/./urandom -jar iexec-result-proxy.jar
