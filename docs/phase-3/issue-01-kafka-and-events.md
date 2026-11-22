# Phase 3.1: Local Kafka Infrastructure and Shared Event Contracts

## Overview
Establishes the distributed messaging and asynchronous event infrastructure for FlopKart. Defines immutable Java 21 event records in `common-dto` that serve as strongly typed contracts across all services coordinating the Saga workflow.

## Infrastructure Stack
- **Apache Kafka:** Confluent Community Platform 7.7.1 running in KRaft mode (no ZooKeeper dependency).
  - External Client Port: `9092`
  - Internal Docker Port: `29092`
  - Controller Port: `9093`
- **Kafka UI:** Web interface running on `http://localhost:8090` for inspecting topics, partitions, consumer groups, and messages.
- **Docker Compose:** Defined in [`docker-compose.infra.yml`](../../docker-compose.infra.yml), orchestrating Kafka, Kafka UI, PostgreSQL, and Redis.

## Event Contracts (`com.flopkart.common.event`)

### 1. `OrderItemDto`
Immutable record representing product item details in an order or stock event:
- `productId` (`Long`): Identifier of the item.
- `productName` (`String`): Display name of the item.
- `quantity` (`Integer`): Quantity purchased or reserved.
- `price` (`BigDecimal`): Unit price.

### 2. `OrderPlacedEvent`
Published by `order-service` to topic `order-placed-topic` when a customer places an order:
- `orderNumber` (`String`): Unique generated business order reference (e.g. `ORD-...`).
- `userId` (`String`): ID of the purchasing customer.
- `userEmail` (`String`): Email for notification delivery.
- `totalAmount` (`BigDecimal`): Cumulative order cost.
- `items` (`List<OrderItemDto>`): List of items included in the order.
- `timestamp` (`LocalDateTime`): Event creation timestamp.

### 3. `PaymentProcessedEvent`
Published by `payment-service` to topic `payment-status-topic` after attempting payment charge:
- `orderNumber` (`String`): Order reference.
- `paymentId` (`String`): Unique payment receipt identifier.
- `status` (`String`): `SUCCESS` or `FAILED`.
- `failureReason` (`String`): Optional description of failure reason.
- `timestamp` (`LocalDateTime`): Event creation timestamp.

### 4. `StockReleaseEvent`
Published by `order-service` to topic `stock-release-topic` when payment fails to trigger compensation:
- `orderNumber` (`String`): Order reference.
- `items` (`List<OrderItemDto>`): Inventory items to return to stock.
- `timestamp` (`LocalDateTime`): Event creation timestamp.

## Verification Commands
```bash
# Start local infrastructure
docker compose -f docker-compose.infra.yml up -d

# Build and install common-dto
mvn clean install -pl common-dto
```
