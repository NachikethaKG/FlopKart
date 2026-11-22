# Phase 4.4: Verify Full Containerized E-Commerce Workflow via Docker Compose

## Overview
Phase 4.4 provides automated end-to-end smoke testing, verification workflows, and operational runbooks for the full containerized FlopKart microservices stack running under Docker Compose.

---

## Verification Architecture & Test Flow

```
[Smoke Test Runner] (Bash / PowerShell)
       │
       ├─► 1. Verify Eureka Registry (http://localhost:8761)
       │      └── Ensure API-GATEWAY + 6 Domain Microservices report UP
       │
       ├─► 2. User Authentication (POST /api/v1/auth/register & login)
       │      └── Gateway (8080) -> user-service (8081) -> flopkart_user_db -> Returns JWT
       │
       ├─► 3. Catalog Inspection (GET /api/v1/products)
       │      └── Gateway (8080) -> product-service (8082) -> flopkart_product_db
       │
       ├─► 4. Redis Cart Operations (POST & GET /api/v1/cart/items)
       │      └── Gateway (8080) -> cart-service (8083) -> Redis (6379)
       │
       └─► 5. Saga Order Placement & Verification (POST /api/v1/orders)
              ├── Gateway (8080) -> order-service (8084)
              │    ├── Feign -> product-service (reserve stock)
              │    ├── Feign -> cart-service (fetch cart)
              │    └── Kafka -> order-placed-topic
              ├── payment-service (8085) consumes event, processes payment
              │    └── Kafka -> payment-processed-topic
              └── order-service consumes payment event
                   ├── Updates status to CONFIRMED
                   └── Clears active Redis cart
```

---

## Test Automation Scripts

Two production-grade test runners are provided in the repository:

### 1. Bash Script (`scripts/smoke-test-docker.sh`)
Designed for Linux/macOS environments, Git Bash, WSL, and CI/CD pipelines:
```bash
chmod +x scripts/smoke-test-docker.sh
./scripts/smoke-test-docker.sh
```

### 2. PowerShell Script (`scripts/smoke-test-docker.ps1`)
Designed for native Windows PowerShell and Windows CI runners:
```powershell
.\scripts\smoke-test-docker.ps1
```

### Configurable Environment Overrides
Both scripts support overriding target URLs:
- `GATEWAY_URL` (default: `http://localhost:8080`)
- `EUREKA_URL` (default: `http://localhost:8761`)

Example:
```bash
GATEWAY_URL="http://192.168.1.100:8080" ./scripts/smoke-test-docker.sh
```

---

## Detailed Step-by-Step Verification Sequence

### Step 1: Service Discovery & Health Checks
- Polls `http://localhost:8761/actuator/health` until Eureka Server reports `{"status":"UP"}`.
- Inspects `/eureka/apps` to confirm the registration of all 7 client services:
  - `API-GATEWAY`
  - `USER-SERVICE`
  - `PRODUCT-SERVICE`
  - `CART-SERVICE`
  - `ORDER-SERVICE`
  - `PAYMENT-SERVICE`
  - `NOTIFICATION-SERVICE`

### Step 2: User Registration & JWT Authentication
- Generates a unique user identifier (`smoke_<random>@flopkart.com`).
- Calls `POST /api/v1/auth/register` through API Gateway.
- Calls `POST /api/v1/auth/login` to obtain an HMAC-SHA256 signed JWT token.
- Validates the response contains `success: true` and an authentic token string.

### Step 3: Product Catalog Browsing
- Queries `GET /api/v1/products?page=0&size=5` through API Gateway.
- If products are present, captures the first valid `productId`.
- If the catalog is empty, seeds an initial item (`Dockerized Headphones`, `$199.99`, stock `50`) via `POST /api/v1/products`.

### Step 4: Ephemeral Cart Sessions (Redis)
- Sends `POST /api/v1/cart/items` with Bearer token (`productId`, `quantity: 2`).
- Validates persistence in Redis by querying `GET /api/v1/cart`.
- Confirms `totalItems == 2` and calculated subtotal.

### Step 5: Distributed Saga Checkout (Kafka)
- Sends `POST /api/v1/orders` with shipping address.
- Confirms initial order generation in `PENDING` status.
- Waits 3 seconds for asynchronous event processing across Kafka topics:
  1. `order-placed-topic` emitted by `order-service`.
  2. `payment-service` processes payment and emits `payment-processed-topic`.
  3. `order-service` consumes outcome and updates order to `CONFIRMED`.
- Queries `GET /api/v1/orders/{orderNumber}` to verify final `CONFIRMED` status.
- Verifies that `GET /api/v1/cart` reports `totalItems: 0` (cart cleared post-checkout).

---

## Operational Troubleshooting & Runbook

### Inspecting Container Status
```bash
docker compose ps
```
All 12 containers should display `running (healthy)` or `running`.

### Viewing Service Logs
```bash
# View aggregated real-time logs
docker compose logs -f

# View logs for a specific service
docker compose logs -f api-gateway
docker compose logs -f order-service
docker compose logs -f payment-service
```

### Inspecting Databases
Connect to PostgreSQL container to verify per-service databases:
```bash
docker compose exec postgres psql -U postgres -l
```

Connect to a specific database (e.g. `flopkart_order_db`):
```bash
docker compose exec postgres psql -U postgres -d flopkart_order_db -c "SELECT order_number, status, total_amount FROM orders;"
```

### Inspecting Redis Cart Sessions
```bash
docker compose exec redis redis-cli keys "cart:*"
```

### Inspecting Kafka Topics & Consumer Groups
Open Kafka UI at `http://localhost:8090` or execute inside container:
```bash
docker compose exec kafka /opt/kafka/bin/kafka-topics.sh --bootstrap-server localhost:9092 --list
```

### Common Issues & Mitigations

| Symptom | Root Cause | Resolution |
| :--- | :--- | :--- |
| `503 Service Unavailable` on API Gateway | Microservices still registering with Eureka | Wait 30 seconds for heartbeat and Eureka cache refresh (`eureka.client.registry-fetch-interval-seconds`). |
| `Connection refused` on Docker Engine | Docker Desktop daemon stopped | Start Docker Desktop or execute `dockerd` service. |
| `409 Conflict` during Order Checkout | Insufficient inventory for product | Check stock with `GET /api/v1/products/{id}` or seed a fresh product with high stock quantity. |
| Order status remains `PENDING` | Kafka broker offline or consumer lag | Check `docker compose logs -f kafka` and verify `payment-service` consumer logs. |

---

## Acceptance Criteria Checklist
- [x] Automated smoke test scripts created in both Bash (`scripts/smoke-test-docker.sh`) and PowerShell (`scripts/smoke-test-docker.ps1`).
- [x] Full flow tested: Service discovery, JWT authentication, Catalog lookup, Redis Cart, and Kafka Saga checkout.
- [x] Troubleshooting, log inspection, and health validation commands documented.
- [x] Acceptance criteria verified for Phase 4.4 and Issue #17.
