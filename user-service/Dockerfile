# Stage 1: Builder stage
FROM eclipse-temurin:21-jdk-alpine AS builder
WORKDIR /workspace

RUN apk add --no-cache maven

# Copy POMs for reactor resolution
COPY pom.xml .
COPY common-dto/pom.xml common-dto/
COPY eureka-server/pom.xml eureka-server/
COPY api-gateway/pom.xml api-gateway/
COPY product-service/pom.xml product-service/
COPY user-service/pom.xml user-service/
COPY cart-service/pom.xml cart-service/
COPY order-service/pom.xml order-service/
COPY payment-service/pom.xml payment-service/
COPY notification-service/pom.xml notification-service/

# Copy sources for common-dto and user-service
COPY common-dto/src common-dto/src
COPY user-service/src user-service/src

# Build executable jar
RUN mvn clean package -pl user-service -am -DskipTests

# Stage 2: Runtime stage
FROM eclipse-temurin:21-jre-alpine
WORKDIR /app

# Run as non-root user
RUN addgroup -S flopkart && adduser -S flopkart -G flopkart

COPY --from=builder --chown=flopkart:flopkart /workspace/user-service/target/*.jar app.jar

USER flopkart

EXPOSE 8081

ENV SPRING_PROFILES_ACTIVE=docker
ENV JAVA_OPTS="-XX:+UseContainerSupport -XX:MaxRAMPercentage=75.0"

ENTRYPOINT ["sh", "-c", "java $JAVA_OPTS -jar app.jar"]
