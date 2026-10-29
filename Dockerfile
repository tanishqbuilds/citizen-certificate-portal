# syntax=docker/dockerfile:1
# GetDocs — Citizen Certificate Request Portal
#
# Build once, deploy many: the image packages the WAR that already passed the test suite
# (./mvnw verify, or the Jenkins pipeline) instead of recompiling it inside Docker.
#
#   ./mvnw -DskipTests package
#   docker build -t getdocs-ccrp:local --build-arg VERSION=$(./mvnw -q help:evaluate -Dexpression=project.version -DforceStdout) .
#   docker run -d -p 8086:8080 -e APP_OFFICER_PASSWORD=ChangeMe123 -v ccrp-data:/var/lib/ccrp getdocs-ccrp:local

ARG JAVA_IMAGE=eclipse-temurin:21-jre

# ---- 1. Build the Spring Boot WAR ----
FROM maven:3.9-eclipse-temurin-21 AS build
WORKDIR /build

COPY pom.xml .
COPY src ./src

RUN mvn -DskipTests clean package

# ---- 2. Split the executable WAR into Spring Boot layers ----
FROM ${JAVA_IMAGE} AS layers
WORKDIR /build

COPY --from=build /build/target/citizen-certificate-portal-*.war app.war

RUN java -Djarmode=tools -jar app.war extract \
    --layers --launcher --destination extracted


# ---- 3. Runtime image ----
FROM ${JAVA_IMAGE}

ARG VERSION=dev
ARG REVISION=unknown
ARG CREATED=unknown
LABEL org.opencontainers.image.title="GetDocs — Citizen Certificate Request Portal" \
      org.opencontainers.image.description="Apply for, review and issue government certificates online" \
      org.opencontainers.image.version="${VERSION}" \
      org.opencontainers.image.revision="${REVISION}" \
      org.opencontainers.image.created="${CREATED}" \
      org.opencontainers.image.source="https://github.com/tanishqbuilds/citizen-certificate-portal" \
      org.opencontainers.image.vendor="GetDocs (academic DevOps project)"

# Unprivileged user; the data directory (H2 database + uploaded documents) is the only writable path.
RUN groupadd --system --gid 10001 ccrp \
 && useradd --system --uid 10001 --gid ccrp --home-dir /app --shell /usr/sbin/nologin ccrp \
 && mkdir -p /var/lib/ccrp \
 && chown ccrp:ccrp /var/lib/ccrp

WORKDIR /app
# Least-changing layers first: a code change only rebuilds the small "application" layer.
COPY --from=layers /build/extracted/dependencies/ ./
COPY --from=layers /build/extracted/spring-boot-loader/ ./
COPY --from=layers /build/extracted/snapshot-dependencies/ ./
COPY --from=layers /build/extracted/application/ ./

ENV APP_DATA_DIR=/var/lib/ccrp \
    APP_ENVIRONMENT=docker \
    SERVER_PORT=8080 \
    JAVA_TOOL_OPTIONS="-XX:MaxRAMPercentage=75 -XX:+ExitOnOutOfMemoryError -Djava.security.egd=file:/dev/urandom"

USER ccrp
VOLUME ["/var/lib/ccrp"]
EXPOSE 8080

HEALTHCHECK --interval=15s --timeout=3s --start-period=45s --retries=3 \
  CMD curl -fsS http://localhost:8080/actuator/health/liveness || exit 1

# exec form: the JVM is PID 1 and receives SIGTERM, so `docker stop` triggers graceful shutdown.
ENTRYPOINT ["java", "org.springframework.boot.loader.launch.WarLauncher"]
