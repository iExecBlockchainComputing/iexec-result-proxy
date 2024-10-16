FROM eclipse-temurin:11.0.24_8-jre-focal

ARG jar

RUN test -n "$jar"

RUN apt-get update \
    && apt-get install -y --no-install-recommends curl \
    && rm -rf /var/lib/apt/lists/*

RUN groupadd --system appuser \
    && useradd -g appuser -s /sbin/nologin -c "Docker image user" appuser

WORKDIR /app
COPY $jar iexec-result-proxy.jar
RUN mkdir /data
RUN chown -R appuser:appuser /app /data

USER appuser
ENTRYPOINT [ "java", "-Djava.security.egd=file:/dev/./urandom", "-jar", "iexec-result-proxy.jar" ]
