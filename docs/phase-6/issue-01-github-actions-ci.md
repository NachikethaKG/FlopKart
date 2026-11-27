# Phase 6.1: GitHub Actions CI Workflow for Multi-Module Maven and React Builds

## Overview
Phase 6.1 establishes automated Continuous Integration (CI) for FlopKart across both the backend Spring Boot microservices and the React storefront frontend. Every `push` and `pull_request` targeting the `main` branch automatically triggers parallel GitHub Actions jobs ensuring zero regressions, complete compile verification, and production build artifact validation.

---

## Workflow Architecture (`.github/workflows/ci.yml`)

The CI workflow is defined in `.github/workflows/ci.yml` and consists of two parallel jobs:

### 1. `backend-ci` (Multi-Module Maven Pipeline)
* **Runner Environment:** `ubuntu-latest`
* **Java SDK:** Eclipse Temurin OpenJDK 21 (`actions/setup-java@v4`)
* **Dependency Caching:** Built-in Maven cache (`cache: maven`) leveraging `~/.m2/repository` key hashing on `pom.xml` files across the reactor.
* **Execution Command:**
  ```bash
  mvn clean test --batch-mode
  ```
* **Coverage Scope:**
  - `common-dto`
  - `eureka-server`
  - `api-gateway`
  - `user-service`
  - `product-service`
  - `cart-service`
  - `order-service`
  - `payment-service`
  - `notification-service`

### 2. `frontend-ci` (React / Vite Production Build)
* **Runner Environment:** `ubuntu-latest`
* **Node.js Runtime:** Node.js 20 LTS (`actions/setup-node@v4`)
* **Dependency Caching:** npm cache keyed on `frontend/package-lock.json`
* **Execution Commands:**
  ```bash
  npm ci
  npm run build
  ```
* **Verification Scope:**
  - Strict dependency installation via `package-lock.json` lockfile.
  - Full Vite production bundle compilation, asset hashing, and CSS processing via Tailwind CSS and PostCSS.

---

## Trigger Configuration

```yaml
on:
  push:
    branches: [ main ]
  pull_request:
    branches: [ main ]
```

---

## Acceptance Criteria Verification

| Requirement | Implementation Detail | Status |
| :--- | :--- | :--- |
| GitHub Actions workflow file | Created `.github/workflows/ci.yml` | Passed |
| Triggers on push & PR to `main` | Configured `on.push.branches: [main]` & `on.pull_request.branches: [main]` | Passed |
| Backend JDK 21 & Maven caching | Configured `actions/setup-java@v4` with `java-version: 21`, `distribution: temurin`, and `cache: maven` | Passed |
| Backend unit & integration test run | Executes `mvn clean test --batch-mode` across all reactor modules | Passed |
| Frontend Node.js 20 & npm cache | Configured `actions/setup-node@v4` with `node-version: 20` and `frontend/package-lock.json` cache | Passed |
| Frontend build verification | Executes `npm ci` and `npm run build` in `frontend/` directory | Passed |
