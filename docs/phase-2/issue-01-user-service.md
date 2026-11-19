# Phase 2.1: User Service with Registration and JWT Authentication

## Overview
The `user-service` is the core identity and authentication microservice for the FlopKart platform. It manages customer registration, credential verification, and issues signed JSON Web Tokens (JWT) using HMAC-SHA256 for cross-service authentication.

## Service Details
- **Module Name:** `user-service`
- **Default Port:** `8081`
- **Database:** PostgreSQL (`flopkart_user_db`)
- **Discovery:** Netflix Eureka Client (`USER-SERVICE`)

## Tech Stack
- Spring Boot 3.3.5
- Spring Security 6 (Stateless, CSRF disabled)
- Spring Data JPA & Hibernate
- PostgreSQL Driver (with in-memory H2 for hermetic testing)
- JJWT 0.12.6 (`jjwt-api`, `jjwt-impl`, `jjwt-jackson`)
- BCrypt Password Encoder

## Architecture & Components
1. **User Entity (`User`):**
   - Stored in table `users` to prevent keyword collisions in PostgreSQL.
   - Fields: `id`, `name`, `email`, `password` (BCrypt hashed), `role`, `createdAt`.
2. **JWT Service (`JwtService`):**
   - Generates and verifies HMAC-SHA256 signed tokens using a configured 256-bit secret.
   - Claims include `userId`, `name`, `email`, and `role`.
3. **Security Configuration (`SecurityConfig`):**
   - Configures stateless session management (`SessionCreationPolicy.STATELESS`).
   - Disables CSRF for REST APIs.
   - Permits unauthenticated access to `/api/v1/auth/**` and `/actuator/**`.
4. **REST Endpoints (`AuthController`):**
   - `POST /api/v1/auth/register`: Hashes plain password using BCrypt and persists the new user. Returns user details and signed JWT wrapped in `ApiResponse`.
   - `POST /api/v1/auth/login`: Authenticates credentials using BCrypt comparison. Returns user metadata and signed JWT wrapped in `ApiResponse`.

## API Specification

### 1. Register User
- **URL:** `/api/v1/auth/register`
- **Method:** `POST`
- **Request Body:**
```json
{
  "name": "Jane Doe",
  "email": "jane@example.com",
  "password": "securepassword123"
}
```
- **Response (201 Created):**
```json
{
  "success": true,
  "status": 201,
  "message": "User registered successfully",
  "data": {
    "token": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
    "type": "Bearer",
    "user": {
      "id": 1,
      "name": "Jane Doe",
      "email": "jane@example.com",
      "role": "ROLE_USER",
      "createdAt": "2026-11-19T21:00:00.000"
    }
  },
  "timestamp": "2026-11-19T21:00:00.000"
}
```

### 2. Login User
- **URL:** `/api/v1/auth/login`
- **Method:** `POST`
- **Request Body:**
```json
{
  "email": "jane@example.com",
  "password": "securepassword123"
}
```
- **Response (200 OK):**
```json
{
  "success": true,
  "status": 200,
  "message": "Login successful",
  "data": {
    "token": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
    "type": "Bearer",
    "user": {
      "id": 1,
      "name": "Jane Doe",
      "email": "jane@example.com",
      "role": "ROLE_USER",
      "createdAt": "2026-11-19T21:00:00.000"
    }
  },
  "timestamp": "2026-11-19T21:00:00.000"
}
```
