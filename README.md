# FlopKart Cloud E-Commerce Platform

[![CI Pipeline](https://github.com/NachikethaKG/FlopKart/actions/workflows/ci.yml/badge.svg)](https://github.com/NachikethaKG/FlopKart/actions/workflows/ci.yml)
[![Docker Images GHCR](https://github.com/NachikethaKG/FlopKart/actions/workflows/cd-images.yml/badge.svg)](https://github.com/NachikethaKG/FlopKart/actions/workflows/cd-images.yml)
[![Java Version](https://img.shields.io/badge/Java-21%20LTS-orange.svg)](https://adoptium.net/)
[![Spring Boot](https://img.shields.io/badge/Spring%20Boot-3.3.5-brightgreen.svg)](https://spring.io/projects/spring-boot)
[![React](https://img.shields.io/badge/React-18.3-blue.svg)](https://react.dev/)
[![Tailwind CSS](https://img.shields.io/badge/Tailwind-3.4-38bdf8.svg)](https://tailwindcss.com/)

**FlopKart** is an enterprise-grade cloud-native e-commerce microservices platform. Built with Java 21, Spring Boot 3, and Spring Cloud, it demonstrates an event-driven architecture with Apache Kafka, distributed caching with Redis, transactional PostgreSQL databases, reactive API Gateway routing, and a modern React 18 storefront with a dedicated Store Manager inventory and pricing portal.

---

## System Architecture

```text
                               +----------------------------------------+
                               |     React Storefront SPA (Vite 6)      |
                               |    Customer UI & Manager Portal        |
                               +-------------------+--------------------+
                                                   |
                                                   | HTTPS / JSON
                                                   v
                               +-------------------+--------------------+
                               |     Caddy Auto-HTTPS Reverse Proxy     |
                               |             (Port 80 / 443)            |
                               +-------------------+--------------------+
                                                   |
                                                   v
                      +----------------------------+----------------------------+
                      |         Spring Cloud API Gateway (Port 8080)            |
                      |  - JWT Authentication & RBAC (CUSTOMER vs MANAGER)      |
                      |  - Dynamic CORS Filter & Rate Limiting                  |
                      +-------------+-----------------------------+-------------+
                                    |                             |
                       Service Reg  |                             | Proxy Route
                                    v                             v
                      +-------------+-------------+  +------------+-------------+
                      |   Netflix Eureka Server   |  |     Domain Microservices |
                      |        (Port 8761)        |  |                          |
                      +---------------------------+  +----+---+---+---+---+---+--+
                                                          |   |   |   |   |   |
         +------------------------------------------------+   |   |   |   |   +------------------------------------+
         |                                                    |   |   |   |                                        |
         v                                                    v   v   v   v                                        v
+------------------+  +------------------+  +---------------+  +------------------+  +------------------+  +-------------------+
|   User Service   |  | Product Service  |  | Cart Service  |  |  Order Service   |  | Payment Service  |  |Notification Svc   |
|   (Port 8081)    |  |   (Port 8082)    |  |  (Port 8083)  |  |   (Port 8084)    |  |   (Port 8085)    |  |    (Port 8086)    |
| Auth & Profiles  |  | Catalog & Stock  |  | Redis Session |  | Saga Orchestrator|  | Transaction Flow |  | Event Consumer    |
+--------+---------+  +--------+---------+  +-------+-------+  +--------+---------+  +--------+---------+  +---------+---------+
         |                     |                    |                   |                     |                      |
         v                     v                    v                   v                     v                      |
  [PostgreSQL]          [PostgreSQL]             [Redis]         [PostgreSQL]          [PostgreSQL]                  |
                               |                                        |                     |                      |
                               +---------------------+                  |                     |                      |
                                                     |                  v                     v                      v
                                                     |      +-----------+---------------------+----------------------+
                                                     +----->|                 Apache Kafka (KRaft)                  |
                                                            |       Topics: order-placed, payment-processed, etc.   |
                                                            +-------------------------------------------------------+
```

---

## Microservices Matrix

| Microservice | Internal Port | Technology Stack | Primary Responsibility |
| :--- | :--- | :--- | :--- |
| **API Gateway** | `8080` | Spring Cloud Gateway, JJWT | Reactive routing, JWT validation, RBAC, dynamic CORS |
| **Eureka Server** | `8761` | Spring Cloud Netflix Eureka | Dynamic service registration and heartbeat health checks |
| **User Service** | `8081` | Spring Boot 3, Spring Security, JPA | User registration, authentication, JWT issuing, manager seeding |
| **Product Service** | `8082` | Spring Boot 3, JPA, PostgreSQL | Product catalog, inventory stock adjustments, price updates |
| **Cart Service** | `8083` | Spring Boot 3, Spring Data Redis | High-speed in-memory shopping cart operations |
| **Order Service** | `8084` | Spring Boot 3, JPA, Kafka, Saga | Order lifecycle management and transactional Saga coordination |
| **Payment Service** | `8085` | Spring Boot 3, JPA, Kafka | Simulated payment processing and compensation events |
| **Notification Service** | `8086` | Spring Boot 3, Spring Kafka | Event listener logging customer order/payment notifications |
| **Frontend Client** | `5173` | React 18, Vite 6, Tailwind CSS | Storefront UI, shopping cart, live order tracking, Manager Portal |

---

## Quickstart Guide

### 1. Prerequisites
* **Docker & Docker Compose** (Docker Desktop on Windows/macOS or Docker Engine on Linux)
* **Java 21 LTS** & **Maven 3.9+** (optional, for local IDE development)
* **Node.js 20 LTS** & **npm** (optional, for local frontend development)

### 2. One-Click Launcher (Windows)
* Double-click or drag **`start.bat`** into your terminal. It checks Docker, initializes `.env`, launches all backend services, and boots the React dev server.
* To cleanly stop all containers and processes, run **`stop.bat`**.

### 3. Docker Compose (Cross-Platform)
```bash
# Clone the repository
git clone https://github.com/NachikethaKG/FlopKart.git
cd FlopKart

# Initialize environment file
cp .env.example .env

# Start backend microservices and databases
docker compose up -d

# Verify container health
docker compose ps
```

Access Points:
* **Storefront UI:** `http://localhost:5173`
* **API Gateway:** `http://localhost:8080`
* **Eureka Dashboard:** `http://localhost:8761`
* **Kafka UI:** `http://localhost:8090`

---

## Demo Credentials

### 1. Store Manager Account
* **Email:** `manager@flopkart.com`
* **Password:** `Manager@123`
* **Capabilities:** Full access to `/manager` Store Manager Inventory & Pricing Dashboard. Adjust product stock ($\pm 1$ or direct set), update prices inline, create new items, and remove items.

### 2. Customer Account
* **Email:** `smoke_24155@flopkart.com` *(or create your own via Sign Up)*
* **Password:** `Password123!`
* **Capabilities:** Browse catalog, filter by category/search, add items to cart, checkout, and monitor order Saga status.

---

## Production Deployment (Cloud)

FlopKart provides production-ready deployment manifests pulling pre-built container images from GitHub Container Registry:

```bash
# 1. On your Linux VM (Ubuntu 24.04):
cp .env.prod.example .env
nano .env   # Configure your DOMAIN_NAME and ALLOWED_ORIGINS

# 2. Launch with Caddy Reverse Proxy & Auto-TLS:
docker compose -f docker-compose.prod.yml up -d
```
See the full runbook in [docs/phase-6/issue-04-cloud-deployment.md](docs/phase-6/issue-04-cloud-deployment.md).

---

## Running Automated Smoke Tests

A comprehensive PowerShell smoke test script executes full end-to-end user journeys (registration $\to$ auth $\to$ catalog browsing $\to$ cart $\to$ order placement):

```powershell
powershell -ExecutionPolicy Bypass -File .\scripts\smoke-test-docker.ps1
```

---

## License
Distributed under the Apache 2.0 License.
