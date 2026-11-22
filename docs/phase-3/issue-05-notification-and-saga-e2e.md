# Phase 3.5: Implement Notification Service and Verify End-to-End Saga Flow

## Overview
Phase 3.5 completes the distributed Saga transaction and event-driven architecture for the FlopKart platform. It introduces `notification-service` (Port 8086) for domain event receipts, binds `product-service` to Kafka for compensating stock releases, and verifies the full end-to-end checkout and rollback flows.

---

## Architectural Workflow & Distributed Saga

```
[Client / Gateway]
       │
       ▼ POST /api/v1/orders
[order-service] ──(Feign RPC)──► [cart-service] (fetch active cart)
       │
       ├──(Feign RPC)──────────► [product-service] (pessimistic lock reserve stock)
       │
       ├──(Persist PENDING)
       │
       ▼ (Publish: order-placed-topic)
       ├───► [payment-service] (Consumer: idempotency check + gateway charge)
       │            │
       │            ▼ (Publish: payment-status-topic [SUCCESS / FAILED])
       │            ├─────────────────────────────────────────┐
       │            ▼                                         ▼
       │     [order-service]                           [notification-service]
       │       - On SUCCESS: CONFIRMED                  - Log formatted receipt
       │         (Clear cart via CartClient)            - Email / SMS dispatch
       │       - On FAILED: CANCELLED
       │         (Publish: stock-release-topic)
       │                        │
       │                        ▼
       │               [product-service]
       │                 - Compensating transaction
       │                 - Atomically restores inventory
       │
       └───► [notification-service] (Log order creation receipt)
```

---

## Key Components Implemented

### 1. `notification-service` (Port 8086)
- **Consumer**: `NotificationKafkaConsumer` listening to `order-placed-topic` and `payment-status-topic` under consumer group `notification-group`.
- **Dispatcher**: `LoggingNotificationDispatcher` generates structured, formatted receipts:
  - Order creation receipt (`PENDING PAYMENT`)
  - Payment confirmation receipt (`CONFIRMED`)
  - Cancellation notice with reason (`CANCELLED`)

### 2. Compensating Stock Release (`product-service`)
- **Consumer**: `StockReleaseConsumer` consumes `stock-release-topic` under consumer group `product-service-group`.
- **Action**: Restores inventory atomically in PostgreSQL using `productService.releaseStock(request)`.

---

## Verification & Acceptance Criteria
- [x] Successful checkout transitions order from `PENDING` -> `CONFIRMED`, flushes Redis cart, and logs payment confirmation receipt.
- [x] Simulated payment failure transitions order from `PENDING` -> `CANCELLED`, restores product stock in PostgreSQL via compensating Kafka event, and logs cancellation notice.
- [x] `notification-service` status endpoint returns HTTP 200 `UP` on port 8086.
- [x] Complete multi-module reactor builds cleanly (`mvn clean test`).
