# Phase 4.3: Build Unified docker-compose.yml with Health Checks and DB Init Scripts

## Overview
Phase 4.3 introduces a unified, production-ready `docker-compose.yml` orchestrating all infrastructure containers and 8 Spring Boot microservices with explicit health checks and dependency-ordered initialization.

---

## Architectural Stack Overview

```
                                 [Client Traffic]
                                        │
                                        ▼ Port 8080
                                ┌────────────────┐
                                │  api-gateway   │
                                └───────┬────────┘
                                        │
                    ┌───────────────────┼───────────────────┐
                    ▼                   ▼                   ▼
            ┌──────────────┐    ┌──────────────┐    ┌──────────────┐
            │ user-service │    │product-service│    │ cart-service │
            │  Port 8081   │    │  Port 8082   │    │  Port 8083   │
            └──────┬───────┘    └──────┬───────┘    └──────┬───────┘
                   │                   │                   │
                   ▼                   ▼                   ▼
             [PostgreSQL]        [PostgreSQL]           [Redis]
         (flopkart_user_db)  (flopkart_product_db)  (Session Cache)
                   │                   │
                   └──────────┬────────┘
                              ▼ (Kafka Topics)
                 ┌─────────────────────────┐
                 │  order-service (8084)   │
                 │  payment-service (8085) │
                 │notification-service(8086│
                 └─────────────────────────┘
```

---

## Key Components

### 1. Database Provisioning (`docker/postgres/init-databases.sql`)
PostgreSQL container automatically executes `/docker-entrypoint-initdb.d/init-databases.sql` on first boot, provisioning individual databases for each service:
- `flopkart_user_db`
- `flopkart_product_db`
- `flopkart_order_db`
- `flopkart_payment_db`

### 2. Dependency Ordering & Health Checks

| Container | Healthcheck Test | Depends On | Condition |
| :--- | :--- | :--- | :--- |
| **`postgres`** | `pg_isready -U postgres` | - | - |
| **`redis`** | `redis-cli ping` | - | - |
| **`kafka`** | `nc -z localhost 9092` | - | - |
| **`kafka-ui`** | - | `kafka` | `service_healthy` |
| **`eureka-server`** | `wget -q --spider http://localhost:8761/actuator/health` | - | - |
| **`api-gateway`** | - | `eureka-server` | `service_healthy` |
| **`user-service`** | - | `postgres`, `eureka-server` | `service_healthy` |
| **`product-service`** | - | `postgres`, `kafka`, `eureka-server` | `service_healthy` |
| **`cart-service`** | - | `redis`, `eureka-server` | `service_healthy` |
| **`order-service`** | - | `postgres`, `kafka`, `eureka-server` | `service_healthy` |
| **`payment-service`** | - | `postgres`, `kafka`, `eureka-server` | `service_healthy` |
| **`notification-service`** | - | `kafka`, `eureka-server` | `service_healthy` |

### 3. Unified Environment Config (`.env.example`)
Provides documentation and local defaults for ports, passwords, JWT secrets, and simulation toggles.

---

## Verification & Acceptance Criteria
- [x] Database initialization script provisions all 4 databases automatically.
- [x] Unified `docker-compose.yml` connects all 12 services via `flopkart-network`.
- [x] Named persistent volumes preserve state across container restarts (`postgres_data`, `redis_data`, `kafka_data`).
- [x] Microservices depend on healthy infrastructure before starting, preventing crash-loops.
- [x] `.env.example` provides complete configuration documentation.
