# Phase 6.3: Prepare Production Docker Compose, Reverse Proxy, and Dynamic CORS Config

## Overview
Phase 6.3 equips the FlopKart platform with production deployment manifests. It decouples production deployment from local Docker build contexts by configuring pre-built container image pulls from GitHub Container Registry (`ghcr.io`), introduces an automatic HTTPS reverse proxy using Caddy, provides a production secrets template (`.env.prod.example`), and parameterizes API Gateway CORS origins to support live frontend hosting (e.g., Vercel, Cloudflare Pages).

---

## Key Deliverables & Implementation

### 1. Dynamic CORS Configuration (`CorsConfig.java`)
* **Environment Variable:** `ALLOWED_ORIGINS` (defaults to `http://localhost:5173,http://localhost:3000`).
* **Implementation:** `api-gateway/src/main/java/com/flopkart/gateway/config/CorsConfig.java` injects `${ALLOWED_ORIGINS:${cors.allowed-origins:...}}` and dynamically splits comma-separated URLs into origin patterns.
* **Wildcard & Production URL Support:** Allows live Vercel deployments (e.g. `https://flopkart.vercel.app`, `https://*.vercel.app`) with full credentialed request support (`allowCredentials: true`, `maxAge: 3600L`).
* **Verification:** Unit test suite in `api-gateway/src/test/java/com/flopkart/gateway/config/CorsConfigTest.java` verifies preflight authorization for configured origins and 403 Forbidden rejection for unauthorized domains.

### 2. Production Docker Compose Manifest (`docker-compose.prod.yml`)
* **Image Sources:** Pulls pre-built images from `ghcr.io/${GITHUB_REPOSITORY_OWNER:-nachikethakg}/flopkart-<service>:${IMAGE_TAG:-latest}`.
* **No Local Build Overhead:** Deploys quickly on cloud VMs without requiring JDK, Maven, or build tools on the host.
* **Internal Network Security:** Internal microservice ports (e.g. databases, Kafka, user, order, cart, payment, product, notification) reside purely on `flopkart-prod-network` without exposing host ports.
* **Persistence:** Named volumes for PostgreSQL (`flopkart_prod_postgres_data`), Redis (`flopkart_prod_redis_data`), Kafka (`flopkart_prod_kafka_data`), and Caddy certificates (`caddy_data`, `caddy_config`).

### 3. Caddy Reverse Proxy & Automatic TLS (`deploy/Caddyfile`)
* **Service:** `caddy:2.8-alpine`
* **Ports:** Maps public HTTP `80` and HTTPS `443` to host.
* **Auto-TLS:** Automatic SSL/TLS certificate generation via Let's Encrypt / ZeroSSL using ACME protocol.
* **Proxy Target:** Routes reverse proxy traffic directly to `api-gateway:8080`.
* **Security Headers:** Enforces HSTS (`Strict-Transport-Security`), `X-Content-Type-Options: nosniff`, `X-Frame-Options: DENY`, and `Referrer-Policy: strict-origin-when-cross-origin`.

### 4. Production Secrets Template (`.env.prod.example`)
* Provides template for production environment variables:
  - `GITHUB_REPOSITORY_OWNER` & `IMAGE_TAG`
  - `DOMAIN_NAME` & `ACME_EMAIL`
  - `ALLOWED_ORIGINS` (Vercel / Cloudflare frontend URLs)
  - `JWT_SECRET` (Production 256-bit+ HMAC secret)
  - `POSTGRES_USER` & `POSTGRES_PASSWORD`

---

## Acceptance Criteria Verification

| Requirement | Implementation Detail | Status |
| :--- | :--- | :--- |
| Dynamic Gateway CORS | `CorsConfig.java` parameterizes allowed origins via `ALLOWED_ORIGINS` | Passed |
| Production Compose Manifest | `docker-compose.prod.yml` uses pre-built `ghcr.io` images | Passed |
| Compose Configuration Validation | `docker compose -f docker-compose.prod.yml config` passes validation | Passed |
| Caddy Reverse Proxy & TLS | `deploy/Caddyfile` with reverse proxy to `api-gateway:8080` and security headers | Passed |
| Production Environment Template | Created `.env.prod.example` with documented secret definitions | Passed |
