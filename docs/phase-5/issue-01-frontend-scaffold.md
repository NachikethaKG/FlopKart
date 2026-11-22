# Phase 5.1: Scaffold React + Vite + Tailwind Storefront and Axios Gateway Client

## Overview
Phase 5.1 establishes the frontend client architecture for FlopKart, scaffolding a responsive Single Page Application (SPA) powered by React 18, Vite, Tailwind CSS, and Lucide Icons, integrated with the Spring Cloud API Gateway via a centralized Axios client.

---

## Architecture & Directory Structure

```
frontend/
├── index.html
├── package.json
├── vite.config.js
├── tailwind.config.js
├── postcss.config.js
├── .env.example
├── .env
├── src/
│   ├── main.jsx
│   ├── App.jsx
│   ├── index.css
│   ├── api/
│   │   └── axiosClient.js
│   └── components/
│       ├── Navbar.jsx
│       └── Footer.jsx
```

---

## Centralized Axios Client (`src/api/axiosClient.js`)

The application accesses all domain microservices exclusively through the Spring Cloud API Gateway on `http://localhost:8080/api/v1`.

### Key Features
1. **Dynamic Base URL:** Configured via `VITE_API_GATEWAY_URL` with a fallback to `http://localhost:8080/api/v1`.
2. **Request Interceptor:** Automatically extracts JWT token from `localStorage.getItem('token')` and appends `Authorization: Bearer <token>` to outbound requests.
3. **Response Interceptor:** Unwraps Spring Boot's standard `ApiResponse<T>` envelopes (`{ success, message, data, timestamp }`), making response data cleanly accessible to UI components.
4. **Global 401 Handling:** Automatically purges invalid or expired credentials from `localStorage` and routes the user to `/login` with preservation of the original destination URL.

---

## Application Shell Components

### 1. Responsive Navbar (`src/components/Navbar.jsx`)
- Modern brand logo with gradient styling.
- Desktop search bar triggering route queries.
- Real-time shopping cart badge reflecting item counts.
- Dynamic user profile / session dropdown (Sign In / Register for guests, Profile / Orders / Logout for authenticated users).
- Collapsible mobile navigation menu.

### 2. Modern Footer (`src/components/Footer.jsx`)
- Highlighting core microservice capabilities (Event-driven Saga, Kafka KRaft, Stateless JWT, Redis caching).
- Quick navigation links across categories and architecture specifications.

---

## Verification & Execution

```bash
# 1. Install dependencies
cd frontend
npm install

# 2. Build production assets
npm run build

# 3. Launch development server on http://localhost:5173
npm run dev
```

---

## Acceptance Criteria Checklist
- [x] Initialized Vite React project inside `frontend/` and configured Tailwind CSS.
- [x] Configured `.env.example` and `.env` with `VITE_API_GATEWAY_URL=http://localhost:8080/api/v1`.
- [x] Created `frontend/src/api/axiosClient.js` with Bearer token injection, `ApiResponse<T>` unwrapping, and 401 handling.
- [x] Built responsive application shell (`Navbar` and `Footer`).
- [x] Documented implementation in `docs/phase-5/issue-01-frontend-scaffold.md`.
