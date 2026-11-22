# Phase 5.5: Implement Checkout Workflow and Live Order Status Tracking UI

## Overview
Phase 5.5 completes the customer purchasing journey on the React frontend, providing an end-to-end checkout submission flow, asynchronous Kafka Saga event polling, and an order history dashboard with live state updates.

---

## Distributed Saga Checkout & Tracking Architecture

```
[Customer on /checkout]
           │
           ├─► Submits Shipping Address & Reviews Cart
           │
           ▼
[POST /api/v1/orders] ───► [Spring Cloud Gateway (8080)]
                                  │
                                  ▼
                         [order-service (8084)]
                                  │
            ┌─────────────────────┴─────────────────────┐
            ▼                                           ▼
[Reserves Inventory]                           [Publishes to Kafka]
  (via Feign Client:                             (Topic: order-placed-topic)
   product-service)                                     │
                                                        ▼
                                             [payment-service (8085)]
                                                        │
                                                        ▼
                                             [Publishes to Kafka]
                                              (Topic: payment-processed-topic)
                                                        │
                                                        ▼
                                             [order-service (8084)]
                                              └── Status: PENDING -> CONFIRMED
                                                  Clears Redis Cart

[UI on /orders/:orderNumber]
  └── Polls GET /api/v1/orders/{orderNumber} every 2 seconds until status !== PENDING.
```

---

## Key Components

### 1. Checkout Page (`src/pages/CheckoutPage.jsx`)
- Routed at `/checkout` (guarded by `ProtectedRoute`).
- Itemized cart breakdown and live subtotal/total summaries.
- Comprehensive shipping address form (Street Address, City, State/Province, Postal Code, Country).
- On submission: calls `POST /api/v1/orders`, triggers `refreshCart()` to clear client and Redis state, and routes to `/orders/:orderNumber`.

### 2. Live Order Tracking View (`src/pages/OrderDetailPage.jsx`)
- Routed at `/orders/:orderNumber`.
- Real-time status polling: polls `GET /api/v1/orders/{orderNumber}` every 2 seconds while `status === 'PENDING'`, automatically halting once the status transitions to `CONFIRMED` or `CANCELLED`.
- 4-Stage Saga Progress Tracker:
  1. **Order Placed:** Initial persistence in `order-service`.
  2. **Inventory Reserved:** Atomic reservation confirmed via `product-service`.
  3. **Payment Event:** Kafka consumer processes payment in `payment-service`.
  4. **Order Confirmed:** Final status persisted and confirmed.
- Color-coded badges and itemized line-item pricing table.

### 3. Order History Dashboard (`src/pages/OrdersPage.jsx`)
- Routed at `/orders`.
- Retrieves all orders for the authenticated user via `GET /api/v1/orders`.
- Displays order reference numbers, dates, status badges, shipping destinations, and direct links to live tracking views.
- Clean empty-state view for new users with a "Start Shopping" button.

---

## Acceptance Criteria Checklist
- [x] Built `CheckoutPage` (`/checkout`) with cart summary, shipping address form, and `POST /api/v1/orders` submission.
- [x] Refreshed `CartContext` and redirected to `/orders/:orderNumber` on order creation.
- [x] Implemented status polling (every 2s while `PENDING`) visualizing live Kafka Saga progression to `CONFIRMED`.
- [x] Built `OrdersPage` (`/orders`) displaying complete user order history with color-coded status badges and itemized lists.
- [x] Documented implementation in `docs/phase-5/issue-05-checkout-and-orders-ui.md`.
