# Phase 5.4: Implement Shopping Cart Context and Interactive Cart Management UI

## Overview
Phase 5.4 implements client-side state synchronization with the Redis-backed `cart-service`, exposing seamless add-to-cart actions, quantity adjustments, item removals, live subtotal updates, and an interactive `/cart` management view.

---

## Architecture & Data Flow

```
[UI Components: ProductCard / ProductDetailPage / CartPage]
                          │
                          ▼
                  [CartContext.jsx]
                          │
                          ▼
            [Spring Cloud API Gateway (Port 8080)]
            (Injects X-User-Id from JWT Authorization header)
                          │
                          ▼
               [cart-service (Port 8083)]
                          │
                          ▼
             [Redis 7 (Session Cache: 6379)]
                 └── Key: "cart:<userId>"
```

---

## Key Components

### 1. Cart Context (`src/context/CartContext.jsx`)
- **State Model:** Tracks `cart` object (`items`, `totalItems`, `totalPrice`, `updatedAt`), `loading`, and temporary `toast` messages.
- **Session Restorations:** Invokes `GET /api/v1/cart` on user login; purges cart state on logout.
- **Operations:**
  - `addItem(product, quantity)`: Dispatches `POST /api/v1/cart/items` with payload `{ productId, productName, price, quantity, imageUrl }`.
  - `updateQuantity(productId, quantity)`: Dispatches `PUT /api/v1/cart/items/{productId}` with `{ quantity }` (automatically removes item if quantity <= 0).
  - `removeItem(productId)`: Dispatches `DELETE /api/v1/cart/items/{productId}`.
  - `clearCart()`: Dispatches `DELETE /api/v1/cart`.
  - `toast`: Triggers visual toast notifications on every cart action with automatic 3-second auto-dismiss.

### 2. Live Navbar Badge Integration (`src/components/Navbar.jsx`)
- Dynamically derives item count from `useCart().totalItemCount`.
- Updates synchronously across the entire app whenever products are added, modified, or cleared.

### 3. Interactive Cart Page (`src/pages/CartPage.jsx`)
- Routed at `/cart` and protected via `ProtectedRoute`.
- Displays itemized rows with image thumbnails, titles, unit prices, `+`/`-` steppers, line totals, and trash can remove buttons.
- Order Summary card detailing subtotal, complimentary shipping, and calculated grand total.
- "Proceed to Checkout" button routing to `/checkout`.
- Clean empty cart illustration with a "Start Shopping" call-to-action when empty.

### 4. Toast Notification Banner (`src/components/Toast.jsx`)
- Non-blocking slide-in alert providing immediate tactile feedback on user actions.

---

## Acceptance Criteria Checklist
- [x] Created `CartContext` fetching authenticated user's cart and exposing `addItem`, `updateQuantity`, `removeItem`, `clearCart`, and `totalItemCount`.
- [x] Wired live `totalItemCount` badge into `Navbar` cart icon.
- [x] Built `CartPage` (`/cart`) with itemized product rows, stepper controls, remove buttons, subtotal summary, and checkout CTA.
- [x] Implemented animated toast notifications for cart updates.
- [x] Documented implementation in `docs/phase-5/issue-04-shopping-cart-ui.md`.
