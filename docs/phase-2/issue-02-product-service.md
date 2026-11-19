# Phase 2.2: Product Catalog Persistence and Filtering

## Overview
Upgrades the `product-service` from an in-memory placeholder into a robust catalog management service backed by PostgreSQL. The service supports full lifecycle product operations, category management, pagination, category filtering, and inventory stock checks.

## Service Details
- **Module Name:** `product-service`
- **Default Port:** `8082`
- **Database:** PostgreSQL (`flopkart_product_db`)
- **Discovery:** Netflix Eureka Client (`PRODUCT-SERVICE`)

## Tech Stack
- Spring Boot 3.3.5
- Spring Data JPA & Hibernate
- PostgreSQL JDBC Driver (H2 in-memory for testing)
- Spring Validation API
- Actuator for Health Metrics

## Domain Models
1. **Product (`Product`):**
   - Entity mapped to table `products`.
   - Fields: `id`, `name`, `description`, `price`, `stockQuantity`, `imageUrl`, `category` (ManyToOne), `active`, `createdAt`, `updatedAt`.
2. **Category (`Category`):**
   - Entity mapped to table `categories`.
   - Fields: `id`, `name`, `slug` (unique).

## Features & Endpoints

### 1. Paginated Product List with Category Filter
- **URL:** `GET /api/v1/products?page=0&size=10&category=electronics`
- **Description:** Returns paginated product records. Supports optional category filter by category name or slug.
- **Response Format:**
```json
{
  "success": true,
  "status": 200,
  "message": "Products retrieved successfully",
  "data": {
    "content": [
      {
        "id": 1,
        "name": "Smartphone Ultra 5G",
        "description": "Flagship smartphone with 6.7 inch display...",
        "price": 799.99,
        "stockQuantity": 50,
        "category": "Electronics",
        "imageUrl": "https://...",
        "active": true
      }
    ],
    "pageNumber": 0,
    "pageSize": 10,
    "totalElements": 1,
    "totalPages": 1,
    "first": true,
    "last": true,
    "empty": false
  }
}
```

### 2. Single Product Details
- **URL:** `GET /api/v1/products/{id}`
- **Description:** Retrieves full product information by product ID. Returns 404 if not found.

### 3. Create Product
- **URL:** `POST /api/v1/products`
- **Description:** Creates a new product. If the category does not exist, it is dynamically created.
- **Response (201 Created):**
```json
{
  "success": true,
  "status": 201,
  "message": "Product created successfully",
  "data": {
    "id": 7,
    "name": "Gaming Laptop",
    "price": 1299.99,
    "stockQuantity": 15,
    "category": "Computers"
  }
}
```

### 4. Available Stock Verification
- **URL:** `GET /api/v1/products/{id}/stock`
- **Description:** Returns current stock count and availability status.
- **Response:**
```json
{
  "success": true,
  "status": 200,
  "message": "Product stock retrieved successfully",
  "data": {
    "productId": 1,
    "stockQuantity": 50,
    "inStock": true
  }
}
```

## Data Seeding
`DataInitializer` runs on non-test profiles to prepopulate standard categories (Electronics, Fashion, Books, Home & Kitchen) and initial products for local development and demos.
