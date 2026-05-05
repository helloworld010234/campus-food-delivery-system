# =============================================================
# Sky Take Out - Backend Dockerfile (PROPOSED, not yet validated)
#
# Build context expectation:
#   docker build -f core/backend/deploy/Dockerfile core/backend
#
# Notes:
#   * Multi-stage build keeps the final image small and excludes
#     the Maven cache and build tooling.
#   * The runtime image runs as non-root user `sky`.
#   * HEALTHCHECK probes /internal/health (HealthController).
#     Spring Boot Actuator is NOT on the classpath as of 2026-05-05.
#   * No secrets are baked in; everything comes from environment
#     variables set by docker-compose or the orchestrator.
# =============================================================

# ---- Stage 1: Build ----
FROM maven:3.9-eclipse-temurin-17-alpine AS builder
WORKDIR /build

# Copy parent + module POMs first so dependency resolution caches well.
COPY pom.xml .
COPY sky-common/pom.xml sky-common/
COPY sky-pojo/pom.xml sky-pojo/
COPY sky-server/pom.xml sky-server/

# Pre-download dependencies (best effort; will resolve again with sources).
RUN mvn -B -pl sky-server -am dependency:go-offline || true

# Copy sources and build.
COPY sky-common/src sky-common/src
COPY sky-pojo/src sky-pojo/src
COPY sky-server/src sky-server/src

RUN mvn -B -pl sky-server -am clean package -DskipTests \
    && cp sky-server/target/sky-server.jar /build/app.jar

# ---- Stage 2: Runtime ----
FROM eclipse-temurin:17-jre-alpine
WORKDIR /app

RUN addgroup -S sky && adduser -S sky -G sky \
    && mkdir -p /var/log/sky \
    && chown -R sky:sky /var/log/sky

COPY --from=builder /build/app.jar /app/app.jar
RUN chown -R sky:sky /app

USER sky
EXPOSE 8080

# Health check uses the project-provided endpoint.
# Adjust to /actuator/health if Actuator is added later.
HEALTHCHECK --interval=30s --timeout=5s --start-period=60s --retries=3 \
    CMD wget -qO- http://localhost:8080/internal/health || exit 1

ENTRYPOINT ["java", \
    "-XX:+ExitOnOutOfMemoryError", \
    "-Djava.security.egd=file:/dev/./urandom", \
    "-jar", "/app/app.jar"]
