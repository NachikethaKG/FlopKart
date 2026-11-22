# Phase 3.4: Implement Idempotent Payment Service with Kafka Consumer/Producer

## Overview
Phase 3.4 implements the asynchronous, event-driven `payment-service` (Port 8085) for the FlopKart platform. The service listens for order checkout events via Apache Kafka, safely charges payments while enforcing strict idempotency against duplicate delivery, simulates payment gateway authorization, and publishes downstream payment outcome events for the distributed Saga workflow.

---

## Architecture & Implementation Details

### 1. Payment Entity (`flopkart_payment_db`)
- **Table**: `payments`
- **Fields**:
  - `id`: Auto-generated primary key (Long).
  - `paymentReference`: Unique tracking reference (`PAY-<UUID>`).
  - `orderNumber`: Unique constraint to prevent duplicate transactions for the same order.
  - `userId`: User identifier associated with the order.
  - `amount`: Transaction amount.
  - `status`: Payment outcome (`SUCCESS` or `FAILED`).
  - `failureReason`: Failure description when status is `FAILED`.
  - `processedAt`: Audit timestamp of execution.

### 2. Kafka Message Processing & Idempotency
- **Input Topic**: `order-placed-topic` (Consumer Group: `payment-service-group`)
- **Idempotency Guard**:
  - Before initiating payment processing, `paymentRepository.findByOrderNumber(orderNumber)` checks if a payment already exists.
  - If a record is present (from a message replay or network retry), processing is safely bypassed without duplicate charging or duplicate database inserts.
- **Gateway Simulation**:
  - Validates business rules or configurable simulation flags (e.g. amount thresholds, test triggers).
  - Persists payment status to PostgreSQL.
- **Output Topic**: `payment-status-topic`
  - Emits strongly typed `PaymentProcessedEvent` (`orderNumber`, `paymentId`, `status` [SUCCESS/FAILED], `failureReason`, `timestamp`).

### 3. REST API
- `GET /api/v1/payments/order/{orderNumber}`: Retrieves payment receipt and status for a given order.
- `GET /api/v1/payments/status`: Microservice health and status endpoint.

---

## Verification & Acceptance Criteria
- [x] `OrderPlacedEvent` consumed from `order-placed-topic` persists a payment record and emits `PaymentProcessedEvent`.
- [x] Replaying duplicate `OrderPlacedEvent` messages does not create duplicate payment records.
- [x] Configurable failure simulation correctly handles rejected payments and emits `PaymentProcessedEvent` with status `FAILED`.
- [x] Payment lookup endpoint returns accurate details and HTTP 404 for nonexistent orders.
