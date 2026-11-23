# Phase 5.6.2: Add MANAGER Role Support in User Service and Gateway Route Protection

## Overview
Phase 5.6.2 introduces Role-Based Access Control (RBAC) across the FlopKart architecture, distinguishing between standard customers (`CUSTOMER`/`ROLE_USER`) and store managers (`MANAGER`). It seeds a default Store Manager account on startup, propagates role claims via HMAC-SHA256 JWTs, passes roles downstream to microservices via `X-User-Roles` headers, and enforces strict authorization at the API Gateway protecting product catalog mutations.

---

## Role-Based Architecture & Security Flow

```
[Store Manager or Customer Client]
              │
              ├─► POST /api/v1/auth/login { "email": "manager@flopkart.com", "password": "..." }
              │
              ▼
    [user-service (8081)]
              │
              ├─► Seeds default manager: manager@flopkart.com / Manager@123 (role: MANAGER)
              ├─► Generates JWT with claims: { userId, name, email, role: "MANAGER" }
              └─► Returns AuthResponse { token, user: { id, name, email, role } }
              │
              ▼
[Request to /api/v1/products/**] ───► [Spring Cloud Gateway (8080)]
                                               │
                                               ▼
                                  [AuthenticationFilter.java]
                                               │
               ┌───────────────────────────────┴───────────────────────────────┐
               ▼                                                               ▼
        [GET Request]                                            [POST / PUT / PATCH / DELETE]
               │                                                               │
               ▼                                                               ▼
       (Publicly Allowed)                                         [Validate JWT & Check Role]
                                                                               │
                                               ┌───────────────────────────────┴───────────────────────────────┐
                                               ▼                                                               ▼
                                       [role === 'MANAGER']                                          [role !== 'MANAGER']
                                               │                                                               │
                                               ▼                                                               ▼
                                      (Passes with Headers)                                           HTTP 403 Forbidden
                                   - X-User-Id: <userId>                                     {"success":false,"status":403,
                                   - X-User-Roles: MANAGER                                    "message":"Access denied: MANAGER
                                   - X-User-Email: <email>                                    role required"}
                                               │
                                               ▼
                                    [product-service (8082)]
```

---

## Key Changes Implemented

### 1. User Entity & Service Enhancements (`user-service`)
- `User.java`: Supports role strings (`CUSTOMER`, `ROLE_USER`, `MANAGER`).
- `RegisterRequest.java`: Supports optional `role` parameter during registration while preserving default `ROLE_USER` fallback.
- `AuthService.java`: Resolves user role, encrypts credentials, and maps role into `AuthResponse` and `UserResponse`.
- `UserServiceApplication.java`: Added `seedManagerUser` `CommandLineRunner` bean that creates or updates `manager@flopkart.com` / `Manager@123` with role `MANAGER` if absent.

### 2. API Gateway Role Authorization (`api-gateway`)
- `AuthenticationFilter.java`:
  - Extracts JWT token claims (`userId`, `role`, `email`).
  - Implemented `isManager(role)` verifying role normalization against `MANAGER` and `ROLE_MANAGER`.
  - Enforces HTTP `403 Forbidden` for any non-GET request targeting `/api/v1/products/**` without `MANAGER` credentials.
  - Forwarded `X-User-Roles` header downstream for service-level auditing.
- `AuthenticationFilterTest.java`: Added comprehensive unit tests validating `403 Forbidden` rejection on non-manager tokens and successful throughput for `MANAGER` tokens.

---

## Acceptance Criteria Checklist
- [x] Default manager seeded automatically on startup: `manager@flopkart.com` / `Manager@123`.
- [x] JWT contains `role: "MANAGER"` claim upon successful manager authentication.
- [x] Standard customer tokens attempting catalog mutations (`POST`, `PUT`, `PATCH`, `DELETE`) receive `403 Forbidden`.
- [x] `GET /api/v1/products/**` remains accessible to unauthenticated public visitors and customers.
- [x] Store Manager tokens successfully pass through the gateway to `product-service`.
