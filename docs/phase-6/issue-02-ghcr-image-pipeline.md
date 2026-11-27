# Phase 6.2: Automated Docker Image Build and Push to GitHub Container Registry (GHCR)

## Overview
Phase 6.2 configures an automated Continuous Delivery (CD) workflow that builds production-ready Docker container images for all 8 FlopKart backend microservices on merges or pushes to `main`, publishing each container image to the GitHub Container Registry (`ghcr.io`).

---

## Workflow Architecture (`.github/workflows/cd-images.yml`)

The pipeline runs as a GitHub Actions matrix workflow:

### 1. Matrix Strategy (8 Parallel Microservices)
The matrix job executes concurrently across 8 independent runners:
- `eureka-server` (`./eureka-server/Dockerfile`)
- `api-gateway` (`./api-gateway/Dockerfile`)
- `user-service` (`./user-service/Dockerfile`)
- `product-service` (`./product-service/Dockerfile`)
- `cart-service` (`./cart-service/Dockerfile`)
- `order-service` (`./order-service/Dockerfile`)
- `payment-service` (`./payment-service/Dockerfile`)
- `notification-service` (`./notification-service/Dockerfile`)

### 2. Registry Authentication
* **Registry:** `ghcr.io`
* **Authentication Action:** `docker/login-action@v3`
* **Credentials:** `${{ secrets.GITHUB_TOKEN }}` with `packages: write` permission.

### 3. Lowercase Tag Sanitization
To conform to Docker registry naming standards (RFC 1123 / OCI specifications), image namespaces are converted to lowercase:
* `ghcr.io/<owner>/flopkart-<service>:latest`
* `ghcr.io/<owner>/flopkart-<service>:<sha>`

### 4. Layer Caching with Buildx
Leverages Docker Buildx (`docker/setup-buildx-action@v3` and `docker/build-push-action@v6`) with GitHub Actions cache backend (`type=gha`) scoped per service to accelerate subsequent builds.

---

## Trigger Configuration

```yaml
on:
  push:
    branches: [ main ]
  workflow_dispatch:
```

---

## Acceptance Criteria Verification

| Requirement | Implementation Detail | Status |
| :--- | :--- | :--- |
| CD workflow manifest | Created `.github/workflows/cd-images.yml` | Passed |
| Automatic build on merge to `main` | Configured `on.push.branches: [main]` and `workflow_dispatch` | Passed |
| GHCR Authentication | Configured `docker/login-action@v3` using `secrets.GITHUB_TOKEN` | Passed |
| Packages write permission | Added top-level `permissions: packages: write` | Passed |
| Matrix of all 8 backend services | Defined strategy matrix with all 8 services | Passed |
| Multi-tagging (`latest` & `sha`) | Generates both `:latest` and `:${{ github.sha }}` tags | Passed |
| Context and Dockerfile paths | Set `context: .` and `file: ./${{ matrix.service }}/Dockerfile` | Passed |
