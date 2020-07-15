FROM openjdk:11.0.3-jre-slim

ARG BUILD_VERSION

# the jar file is copied from "build/docker/"
# by the gradle plugin "docker"
COPY iexec-result-proxy-${BUILD_VERSION}.jar iexec-result-proxy.jar

# For Spring-Boot project, use the entrypoint
# below to reduce Tomcat startup time.
ENTRYPOINT exec java -Djava.security.egd=file:/dev/./urandom -jar iexec-result-proxy.jar
EXPOSE 18443
