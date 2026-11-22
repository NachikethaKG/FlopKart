# Phase 3.3: Implement Order Service with Asynchronous Checkout and Saga Listener

## Overview
Phase 3.3 implements the core order management and Saga orchestration lifecycle in `order-service` (Port 8084). It connects `cart-service` and `product-service` via resilient OpenFeign clients and integrates with Apache Kafka to orchestrate the distributed checkout and payment flows asynchronously.

---

## Architectural Workflow & Implementation

### 1. Asynchronous Checkout (`POST /api/v1/orders`)
1. **Cart Lookup**: Fetches active items from `cart-service` via `CartClient.getCart(userId)`. If cart is empty, returns HTTP `400 Bad Request`.
2. **Stock Reservation**: Dispatches atomic reservation request to `product-service` via `ProductClient.reserveStock(...)`. If stock is insufficient, handles HTTP `409 Conflict`.
3. **Pending Order Persistence**: Generates unique `orderNumber` (`ORD-<UUID>`), calculates total amount, links line items, and persists `Order` in `PENDING` state in `flopkart_order_db`.
4. **Domain Event Emission**: Publishes strongly-typed `OrderPlacedEvent` to Kafka topic `order-placed-topic` containing `orderNumber`, `userId`, `userEmail`, `totalAmount`, and items list.
5. **Immediate Response**: Returns HTTP `201 Created` with `OrderDto` immediately to caller.

### 2. Query Endpoints
- `GET /api/v1/orders`: Returns descending chronological order history for the authenticated user (`X-User-Id`).
- `GET /api/v1/orders/{orderNumber}`: Retrieves individual order details and tracking status.

### 3. Saga Orchestration Listener (`payment-status-topic`)
- **Consumer**: `OrderPaymentConsumer` listens to `payment-status-topic` under consumer group `order-service-group`.
- **Payment Outcome Handlers**:
  - **`SUCCESS`**:
    - Transitions order status to `CONFIRMED`.
    - Invokes `CartClient.clearCart(userId)` to flush Redis shopping cart.
  - **`FAILED`**:
    - Transitions order status to `CANCELLED`.
    - Emits compensating `StockReleaseEvent` to `stock-release-topic` to release reserved inventory in `product-service`.

### 4. API Gateway Routing
Added route in `api-gateway`:
- Path: `/api/v1/orders/**`
- Target: `lb://ORDER-SERVICE`
- Global JWT filter passes authenticated user headers (`X-User-Id`, `X-User-Roles`, `X-User-Email`).

---

## Verification & Acceptance Criteria
- [x] Placing an order immediately returns HTTP 201 with status `PENDING`.
- [x] Strongly-typed `OrderPlacedEvent` is published to Kafka topic `order-placed-topic`.
- [x] Order history and tracking endpoints return accurate details.
- [x] Payment `SUCCESS` transitions order to `CONFIRMED` and flushes user cart.
- [x] Payment `FAILED` transitions order to `CANCELLED` and emits `StockReleaseEvent` to restore stock.
- [x] Gateway route forwards `/api/v1/orders/**` to `ORDER-SERVICE`.
