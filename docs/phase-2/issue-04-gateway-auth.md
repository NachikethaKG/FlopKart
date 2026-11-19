# Phase 2.4: Gateway JWT Authentication Filter and Route Security

## Overview
Secures the Spring Cloud API Gateway by intercepting inbound HTTP requests, validating signed JSON Web Tokens (JWT), and securely propagating identity context downstream to microservices via HTTP headers (`X-User-Id` and `X-User-Roles`).

## Gateway Specifications
- **Module Name:** `api-gateway`
- **Default Port:** `8080`
- **Tech Stack:** Spring Cloud Gateway (Reactive / WebFlux), JJWT 0.12.6, Netflix Eureka Discovery

## Route Configurations
The API Gateway manages routing for the core microservices:
1. **User Service:**
   - ID: `user-service-route`
   - Path Predicate: `/api/v1/auth/**`
   - Destination: `lb://USER-SERVICE`
2. **Product Service:**
   - ID: `product-service-route`
   - Path Predicate: `/api/v1/products/**`
   - Destination: `lb://PRODUCT-SERVICE`
3. **Cart Service:**
   - ID: `cart-service-route`
   - Path Predicate: `/api/v1/cart/**`
   - Destination: `lb://CART-SERVICE`

## Endpoint Whitelist (Unauthenticated Access)
The `AuthenticationFilter` evaluates incoming requests and bypasses authentication for:
- All CORS preflight requests (`OPTIONS` HTTP method)
- `/api/v1/auth/**` (e.g., registration and login)
- `GET /api/v1/products/**` (public catalog viewing)
- `/actuator/**` (health and readiness checks)

## Protected Endpoints & Token Verification
All other requests (including all `/api/v1/cart/**` endpoints and mutating product endpoints like `POST /api/v1/products`) require a valid Bearer token:
1. **Header Validation:** The request must contain an `Authorization` header prefixed with `Bearer `.
2. **Cryptographic Verification:** Signature is verified with HMAC-SHA256 using the configured secret key.
3. **Expiration Check:** Token expiration claim (`exp`) must be in the future.
4. **Header Propagation:**
   - `X-User-Id`: Extracted from token `userId` claim (or subject).
   - `X-User-Roles`: Extracted from token `role` claim.
   The request is mutated with these headers and forwarded downstream.
5. **Unauthorized Handling:**
   If the header is missing, invalid, expired, or malformed, the Gateway immediately returns `401 Unauthorized` with a standardized JSON payload:
   ```json
   {
     "success": false,
     "status": 401,
     "message": "Missing or invalid Authorization header"
   }
   ```
