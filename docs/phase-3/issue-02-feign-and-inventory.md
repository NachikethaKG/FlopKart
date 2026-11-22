# Phase 3.2: Implement Inventory Reservation and Resilient OpenFeign Clients

## Overview
Phase 3.2 establishes resilient inter-service communication and atomic inventory management for the FlopKart microservices platform:
1. **Atomic Stock Reservation and Release**: Added transactional endpoints to `product-service` to reserve stock during checkout and release stock as a compensating action if payments fail.
2. **Scaffold `order-service`**: Initialized port `8084` with PostgreSQL (`flopkart_order_db`), Eureka client discovery, and Actuator monitoring.
3. **Resilient OpenFeign Clients**: Configured `ProductClient` and `CartClient` with Resilience4j circuit breaker fallback handlers to gracefully handle service degradation or downtime.

---

## Architecture & Implementation Details

### 1. Atomic Stock Reservation (`product-service`)
- **Endpoints**:
  - `POST /api/v1/products/reserve-stock`: Validates sufficient `stockQuantity` for all requested items using pessimistic locking (`SELECT ... FOR UPDATE`). If any requested quantity exceeds available stock, throws `InsufficientStockException` returning `409 Conflict`.
  - `POST /api/v1/products/release-stock`: Compensating endpoint that atomically restores stock quantities on payment failure or order cancellation.
- **DTOs (`common-dto`)**:
  - `StockItemDto`: Represents an item with `productId` and `quantity`.
  - `StockReservationRequest`: List of `StockItemDto` items to atomically reserve.
  - `StockReleaseRequest`: List of `StockItemDto` items and `orderNumber` to restore.

### 2. Scaffold `order-service`
- **Port**: `8084`
- **Database**: PostgreSQL (`flopkart_order_db`) with H2 in-memory profile for integration testing.
- **Dependencies**:
  - `spring-cloud-starter-openfeign`: Declarative REST client for inter-service RPC.
  - `spring-cloud-starter-circuitbreaker-resilience4j`: Fault tolerance and fallback handling.
  - `spring-kafka`: Messaging backbone for downstream Saga event orchestration.

### 3. OpenFeign Clients & Circuit Breakers
- **`ProductClient`**:
  - Points to `PRODUCT-SERVICE` via Eureka service discovery.
  - Methods: `getProductById`, `reserveStock`, `releaseStock`.
  - Fallback: `ProductClientFallback` intercepts failures and returns 503 Service Unavailable responses when `product-service` is unreachable or the circuit breaker is OPEN.
- **`CartClient`**:
  - Points to `CART-SERVICE` via Eureka service discovery.
  - Methods: `getCart`, `clearCart`.
  - Fallback: `CartClientFallback` provides graceful degradation when `cart-service` is unavailable.

---

## Verification & Acceptance Criteria
- [x] Reserving stock deducts inventory accurately within `@Transactional`.
- [x] Requesting more units than available returns HTTP `409 Conflict` with clear error diagnostics.
- [x] Compensating stock release restores product quantity.
- [x] `order-service` starts cleanly and registers `ProductClient` and `CartClient` with Resilience4j fallback handlers.
- [x] All unit and integration tests pass cleanly.
