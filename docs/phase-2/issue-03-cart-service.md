# Phase 2.3: Ephemeral Shopping Cart Service using Redis

## Overview
The `cart-service` manages ephemeral customer shopping carts using Redis in-memory storage. It provides high-throughput, sub-millisecond read and write latencies for item additions, updates, removals, and cart clearance upon checkout.

## Service Details
- **Module Name:** `cart-service`
- **Default Port:** `8083`
- **Storage:** Redis (`localhost:6379`)
- **Key Schema:** `cart:{userId}`
- **Default TTL:** 7 days (604,800 seconds)
- **Discovery:** Netflix Eureka Client (`CART-SERVICE`)

## Tech Stack
- Spring Boot 3.3.5
- Spring Data Redis & Jedis/Lettuce
- Jackson (with JSR310 Date/Time support & Polymorphic type handling)
- Spring Validation
- Actuator Metrics & Health

## Data Models
1. **`Cart`:**
   - Root aggregate stored in Redis under key `cart:{userId}`.
   - Fields: `userId`, `items` (List<CartItem>), `totalPrice`, `totalItems`, `updatedAt`.
   - Automatically recomputes cumulative price and item counts on mutation.
2. **`CartItem`:**
   - Fields: `productId`, `productName`, `price`, `quantity`, `imageUrl`, `itemTotal`.

## REST API Specification
All endpoints expect the downstream authenticated user ID in the `X-User-Id` request header (propagated by the API Gateway).

### 1. Retrieve Current Cart
- **URL:** `GET /api/v1/cart`
- **Header:** `X-User-Id: <userId>`
- **Response:**
```json
{
  "success": true,
  "status": 200,
  "message": "Cart retrieved successfully",
  "data": {
    "userId": "user-42",
    "items": [
      {
        "productId": 1,
        "productName": "Smartphone Ultra 5G",
        "price": 799.99,
        "quantity": 2,
        "itemTotal": 1599.98
      }
    ],
    "totalPrice": 1599.98,
    "totalItems": 2,
    "updatedAt": "2026-11-19T21:00:00.000"
  }
}
```

### 2. Add or Increment Item
- **URL:** `POST /api/v1/cart/items`
- **Header:** `X-User-Id: <userId>`
- **Request Body:**
```json
{
  "productId": 1,
  "productName": "Smartphone Ultra 5G",
  "price": 799.99,
  "quantity": 1,
  "imageUrl": "https://..."
}
```
- **Description:** Adds item or increments quantity if already present. Sets 7-day TTL on `cart:{userId}`.

### 3. Update Item Quantity
- **URL:** `PUT /api/v1/cart/items/{productId}`
- **Header:** `X-User-Id: <userId>`
- **Request Body:**
```json
{
  "quantity": 3
}
```
- **Description:** Sets exact item quantity. If `quantity <= 0`, item is removed from the cart.

### 4. Remove Item
- **URL:** `DELETE /api/v1/cart/items/{productId}`
- **Header:** `X-User-Id: <userId>`
- **Description:** Removes the specified product from the cart and recalculates totals.

### 5. Clear Cart (Checkout)
- **URL:** `DELETE /api/v1/cart`
- **Header:** `X-User-Id: <userId>`
- **Description:** Deletes the `cart:{userId}` key in Redis upon completed order placement or checkout.
