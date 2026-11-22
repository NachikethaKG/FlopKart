# Phase 4.2: Configure Container-Ready Spring Profiles (application-docker.yml)

## Overview
Phase 4.2 configures dedicated `docker` Spring profiles across `eureka-server`, `api-gateway`, and all 6 domain microservices (`application-docker.yml`). This enables seamless internal service discovery and container networking via Docker's embedded DNS, while retaining `localhost` defaults for local standalone development.

---

## Configuration Details

### 1. Externalized Infrastructure Endpoints

| Configuration | Local Default (`application.yml`) | Docker Profile (`application-docker.yml`) |
| :--- | :--- | :--- |
| **Eureka Discovery** | `http://localhost:8761/eureka/` | `http://eureka-server:8761/eureka/` |
| **User DB** | `jdbc:postgresql://localhost:5432/flopkart_user_db` | `jdbc:postgresql://postgres:5432/flopkart_user_db` |
| **Product DB** | `jdbc:postgresql://localhost:5432/flopkart_product_db` | `jdbc:postgresql://postgres:5432/flopkart_product_db` |
| **Order DB** | `jdbc:postgresql://localhost:5432/flopkart_order_db` | `jdbc:postgresql://postgres:5432/flopkart_order_db` |
| **Payment DB** | `jdbc:postgresql://localhost:5432/flopkart_payment_db` | `jdbc:postgresql://postgres:5432/flopkart_payment_db` |
| **Redis Cache** | `host: localhost`, `port: 6379` | `host: redis`, `port: 6379` |
| **Kafka Cluster** | `bootstrap-servers: localhost:9092` | `bootstrap-servers: kafka:29092` |

### 2. Eureka Networking & Service Registration
- **Bridge Network Resolution**: All microservices configure `eureka.instance.prefer-ip-address: true` to avoid hostname registration issues within containerized subnets.
- **Eureka Server Hostname**: `eureka-server` sets `eureka.instance.hostname: eureka-server` and `prefer-ip-address: false` so peers resolve it by Docker service name.

### 3. Environment Variable Overrides & Safe Fallbacks
Every data source, Kafka bootstrap, Redis, and JWT secret supports environment variable override:
- `DB_URL`, `DB_USERNAME`, `DB_PASSWORD`
- `KAFKA_BOOTSTRAP_SERVERS`
- `REDIS_HOST`, `REDIS_PORT`
- `EUREKA_SERVER_URL`
- `JWT_SECRET`, `JWT_EXPIRATION`

---

## Verification & Acceptance Criteria
- [x] Dedicated `application-docker.yml` created for all 8 microservices.
- [x] Sibling containers resolve infrastructure using Docker internal DNS (`postgres`, `redis`, `kafka`, `eureka-server`).
- [x] `SPRING_PROFILES_ACTIVE=docker` baked into image default environment with dynamic override capability.
- [x] Sensitive parameters retain local defaults while remaining fully customizable via environment variables.
