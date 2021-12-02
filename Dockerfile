FROM openjdk:11.0.3-jre-slim

ARG spring_boot_jar

RUN test -n "$spring_boot_jar"

COPY $spring_boot_jar iexec-result-proxy.jar

# For Spring-Boot project, use the entrypoint
# below to reduce Tomcat startup time.
ENTRYPOINT exec java -Djava.security.egd=file:/dev/./urandom -jar iexec-result-proxy.jar
