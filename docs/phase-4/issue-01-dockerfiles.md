# Phase 4.1: Create Multi-Stage Dockerfiles and .dockerignore for All Microservices

## Overview
Phase 4.1 establishes containerization standards for the FlopKart microservices platform by introducing optimized, production-ready multi-stage Dockerfiles across all 8 microservices and an optimized root `.dockerignore`.

---

## Technical Specifications

### 1. Multi-Stage Build Architecture
Each microservice uses a two-stage build pattern:
- **Builder Stage (`eclipse-temurin:21-jdk-alpine`)**:
  - Installs Maven on lightweight Alpine Linux (`apk add --no-cache maven`).
  - Copies root `pom.xml` and child module POMs to satisfy reactor dependency resolution.
  - Copies shared `common-dto` source and the specific target microservice source.
  - Builds the deployable Spring Boot executable JAR with `mvn clean package -pl <service> -am -DskipTests`.
- **Runtime Stage (`eclipse-temurin:21-jre-alpine`)**:
  - Based on lightweight Alpine JRE (~140 MB total image size, well below the 250 MB ceiling).
  - Creates a dedicated non-root user and group (`flopkart:flopkart`).
  - Copies only the compiled executable JAR from the builder stage.
  - Enforces container-aware JVM flags:
    `-XX:+UseContainerSupport -XX:MaxRAMPercentage=75.0`
  - Runs with least-privilege security (`USER flopkart`).

### 2. Services Containerized

| Service | Exposed Port | Base Image (Runtime) | User |
| :--- | :---: | :--- | :--- |
| **`eureka-server`** | 8761 | `eclipse-temurin:21-jre-alpine` | `flopkart` |
| **`api-gateway`** | 8080 | `eclipse-temurin:21-jre-alpine` | `flopkart` |
| **`user-service`** | 8081 | `eclipse-temurin:21-jre-alpine` | `flopkart` |
| **`product-service`** | 8082 | `eclipse-temurin:21-jre-alpine` | `flopkart` |
| **`cart-service`** | 8083 | `eclipse-temurin:21-jre-alpine` | `flopkart` |
| **`order-service`** | 8084 | `eclipse-temurin:21-jre-alpine` | `flopkart` |
| **`payment-service`** | 8085 | `eclipse-temurin:21-jre-alpine` | `flopkart` |
| **`notification-service`** | 8086 | `eclipse-temurin:21-jre-alpine` | `flopkart` |

### 3. `.dockerignore` Exclusions
The root `.dockerignore` strips local build artifacts and IDE files:
- Version control metadata (`.git`, `.gitignore`)
- IDE metadata (`.idea`, `*.iml`, `.vscode`)
- Build outputs (`**/target`)
- Documentation and frontend assets (`docs/`, `frontend/`)
- Local environment files and compose definitions (`docker-compose*.yml`, `.env*`)

---

## Verification & Acceptance Criteria
- [x] Multi-stage `Dockerfile` definitions created for all 8 microservices.
- [x] Every image uses `eclipse-temurin:21-jre-alpine` (< 250 MB footprint).
- [x] Container executes as a non-root user (`flopkart`).
- [x] Container-aware memory allocation configured (`-XX:+UseContainerSupport -XX:MaxRAMPercentage=75.0`).
- [x] Root `.dockerignore` created and excludes unnecessary directories.
