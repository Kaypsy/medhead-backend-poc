# Multi-stage Dockerfile for MedHead Emergency Allocation
# Stage 1: Build with Maven
FROM maven:3.9.9-eclipse-temurin-17 AS builder

WORKDIR /app

# Cache dependencies first
COPY pom.xml ./
RUN mvn -q -e -DskipTests dependency:go-offline

# Copy sources and build
COPY src ./src
RUN mvn -q -e -DskipTests package

# Stage 2: Runtime with OpenJDK 17 slim
FROM openjdk:17-slim AS runtime

LABEL org.opencontainers.image.title="MedHead Emergency Allocation" \
      org.opencontainers.image.description="Spring Boot service for emergency hospital bed allocation" \
      org.opencontainers.image.source="https://example.com/medhead" \
      org.opencontainers.image.licenses="MIT"

# Install minimal tools (curl for healthcheck)
RUN apt-get update \
    && apt-get install -y --no-install-recommends curl \
    && rm -rf /var/lib/apt/lists/*

WORKDIR /app

# Copy fat jar from builder stage
COPY --from=builder /app/target/*.jar /app/app.jar

# Expose default port
EXPOSE 8080

# Healthcheck uses Spring Boot Actuator. The app uses server.servlet.context-path=/api
# So health endpoint is /api/actuator/health
HEALTHCHECK --interval=30s --timeout=5s --retries=5 CMD \
  curl -fsS http://localhost:8080/api/actuator/health | grep '"status":"UP"' || exit 1

# JVM options can be overridden at runtime by JAVA_OPTS env var
ENV JAVA_OPTS="-XX:+UseContainerSupport -XX:MaxRAMPercentage=75.0 -XX:InitialRAMPercentage=50.0 -Djava.security.egd=file:/dev/./urandom"

# Default Spring profile can be overridden (docker/prod)
ENV SPRING_PROFILES_ACTIVE=docker

ENTRYPOINT ["sh", "-c", "exec java $JAVA_OPTS -jar /app/app.jar"]
