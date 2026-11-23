# Phase 5.6.3: Implement Product Stock Adjustment, Price Update, and Creation APIs

## Overview
Phase 5.6.3 enhances `product-service` (Port 8082) with robust catalog management endpoints allowing store managers to inspect inventory levels, adjust stock quantities atomically with delta or absolute values, update pricing with validation, create new catalog products, and safely deactivate or delete items.

---

## API Specifications

### 1. Create Catalog Item
* **Endpoint:** `POST /api/v1/products`
* **Authorization:** `MANAGER` role required via API Gateway
* **Request Body:**
  ```json
  {
    "name": "Mechanical Keyboard RGB",
    "description": "Hot-swappable mechanical switches with custom RGB backlighting.",
    "price": 129.99,
    "stockQuantity": 45,
    "category": "Electronics",
    "imageUrl": "https://images.unsplash.com/photo-1587829741301-dc798b83add3",
    "active": true
  }
  ```
* **Response (201 Created):**
  ```json
  {
    "success": true,
    "status": 201,
    "message": "Product created successfully",
    "data": {
      "id": 10,
      "name": "Mechanical Keyboard RGB",
      "price": 129.99,
      "stockQuantity": 45,
      "category": "Electronics",
      "active": true
    }
  }
  ```

---

### 2. Adjust Product Stock
* **Endpoint:** `PATCH /api/v1/products/{id}/stock`
* **Authorization:** `MANAGER` role required via API Gateway
* **Request Body Options:**
  * **Option A: Delta Adjustment (Positive for restock, negative for deduction):**
    ```json
    {
      "delta": 15
    }
    ```
  * **Option B: Absolute Quantity Overwrite:**
    ```json
    {
      "quantity": 60
    }
    ```
* **Validation & Error Handling:**
  * If `delta` causes `stockQuantity` to fall below `0`, or `quantity < 0`, returns HTTP `400 Bad Request`:
    ```json
    {
      "success": false,
      "status": 400,
      "message": "Stock quantity cannot drop below zero"
    }
    ```
* **Response (200 OK):** Returns updated `ProductDto` with persisted inventory level.

---

### 3. Update Product Price
* **Endpoint:** `PATCH /api/v1/products/{id}/price`
* **Authorization:** `MANAGER` role required via API Gateway
* **Request Body:**
  ```json
  {
    "price": 119.99
  }
  ```
* **Validation & Error Handling:**
  * Validates that `price > 0`. If `price <= 0` or missing, returns HTTP `400 Bad Request`.
* **Response (200 OK):** Returns updated `ProductDto` with persisted price and updated timestamp.

---

### 4. Delete / Deactivate Product
* **Endpoint:** `DELETE /api/v1/products/{id}`
* **Authorization:** `MANAGER` role required via API Gateway
* **Behavior:** Soft deactivates product (`active = false`), preserving transactional history in orders and payment records while removing the item from public catalog endpoints.
* **Response (200 OK):**
  ```json
  {
    "success": true,
    "status": 200,
    "message": "Product deleted successfully",
    "data": null
  }
  ```

---

## Acceptance Criteria Checklist
- [x] Implemented `UpdateStockRequest` and `UpdatePriceRequest` DTOs.
- [x] `PATCH /api/v1/products/{id}/stock` supports delta and absolute updates with boundary validation (`stock >= 0`).
- [x] `PATCH /api/v1/products/{id}/price` updates product price with validation (`price > 0`).
- [x] `DELETE /api/v1/products/{id}` safely deactivates catalog items.
- [x] Added `IllegalArgumentException` handling in `GlobalExceptionHandler` returning HTTP 400.
- [x] Comprehensive test coverage in `ProductServiceApplicationTests` (19 tests passing).
