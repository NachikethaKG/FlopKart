# Phase 5.2: Implement User Registration, Login UI, AuthContext, and Protected Routes

## Overview
Phase 5.2 implements user authentication and authorization on the frontend client, establishing `AuthContext` to persist stateless JWT credentials across page reloads and protect sensitive customer routes (`/cart`, `/orders`, `/checkout`).

---

## Architectural Flow

```
[User Form Action] (Register / Login)
         │
         ▼
[AuthContext.jsx] ───► POST /api/v1/auth/register or /login
         │               (Via Spring Cloud Gateway: Port 8080)
         │               (Forwarded to user-service: Port 8081)
         │
         ▼
[ApiResponse<AuthResponse>]
         │
         ├─► localStorage.setItem('token', token)
         ├─► localStorage.setItem('user', JSON.stringify(user))
         │
         ▼
[State Propagation]
         ├─► Navbar updates (User Greeting, Orders Link, Logout action)
         └─► ProtectedRoute allows access to /cart and /orders
```

---

## Key Components

### 1. AuthContext (`src/context/AuthContext.jsx`)
- State: `user`, `token`, `isAuthenticated`, `loading`.
- Restores active credentials from `localStorage` on initial mount.
- `login(email, password)`: invokes `POST /api/v1/auth/login`, unwraps payload, persists JWT token and user info, updates state.
- `register(name, email, password)`: invokes `POST /api/v1/auth/register`, creates user and logs in automatically.
- `logout()`: wipes `token` and `user` from `localStorage` and resets context state.

### 2. Login Page (`src/pages/LoginPage.jsx`)
- Email & password validation.
- Real-time submission spinner (`Loader2`).
- Dynamic error banners displaying backend validation/authentication failure messages.
- Remembers intended destination URL (`location.state?.from?.pathname`) and redirects upon successful login.

### 3. Register Page (`src/pages/RegisterPage.jsx`)
- Full name, email, password, and confirm-password fields.
- Client-side validation: checks matching passwords and minimum 6-character length.
- Graceful API error handling (e.g. duplicate email addresses).

### 4. ProtectedRoute Wrapper (`src/components/ProtectedRoute.jsx`)
- Intercepts route navigation to guarded resources (such as `/cart` and `/orders`).
- Displays a spinner while `loading` initial authentication status.
- Redirects unauthenticated visitors to `/login` with `state: { from: location }` to enable seamless post-login redirection.

### 5. Reactive Navbar (`src/components/Navbar.jsx`)
- Unauthenticated view: displays "Sign In" and "Get Started" call-to-action buttons.
- Authenticated view: displays user avatar, email/name tooltip, "My Orders" link, and a one-click "Sign Out" button.

---

## Acceptance Criteria Checklist
- [x] Created `AuthContext` managing `user`, `token`, `isAuthenticated`, `login()`, `register()`, and `logout()`.
- [x] Built `LoginPage` (`/login`) and `RegisterPage` (`/register`) with validation, loading states, and error alerts.
- [x] Implemented `ProtectedRoute` wrapper component redirecting unauthenticated users to `/login`.
- [x] Updated `Navbar` to dynamically reflect authentication state.
- [x] Documented implementation in `docs/phase-5/issue-02-auth-ui.md`.
