# Phase 5.6.4: Store Manager Inventory & Pricing Dashboard UI

## Overview
Phase 5.6.4 introduces a dedicated Store Manager portal at `/manager` within the FlopKart React single-page application. The dashboard provides authenticated managers with real-time inventory management tools, fast stock increments/decrements, inline price updating, category filtering, a product creation modal, and product removal capabilities.

---

## Key Features & User Interface

### 1. Manager Route Guard (`ManagerRoute.jsx`)
* Restricts access to the `/manager` route to authenticated users with `role === 'MANAGER'` or `role === 'ROLE_MANAGER'`.
* Unauthenticated visitors are redirected to `/login` preserving the return location (`state: { from: location }`).
* Non-manager authenticated users (`CUSTOMER`) are redirected back to the storefront home page (`/`).

### 2. Navigation Integration (`Navbar.jsx`)
* **Desktop Navigation:** Displays a styled `Manager Portal` button badge (with `LayoutDashboard` icon) adjacent to the Catalog navigation item when `isManager` is true.
* **User Dropdown:** Shows a `Manager Portal` navigation link inside the profile dropdown menu.
* **Mobile Drawer:** Exposes a full-width `Manager Portal` tap target in the mobile navigation menu.

### 3. Inventory Overview & KPI Metrics
* **Total Products:** Total number of active catalog items.
* **Low Stock Alerts:** Items with stock below 5 units flagged in amber/red badges for urgent restocking.
* **Out of Stock Count:** Zero-quantity items highlighted.
* **Total Inventory Value:** Aggregated valuation based on active stock $\times$ price.

### 4. Interactive Stock & Price Management Controls
* **Quick Stock Increments/Decrements:** $\pm 1$ buttons for fast stock adjustments dispatching `PATCH /api/v1/products/{id}/stock` with `{ "delta": 1 }` or `{ "delta": -1 }`.
* **Direct Stock Setting:** Direct quantity input modal/popover allowing exact quantity overrides (`{ "quantity": n }`).
* **Inline Price Editing:** Editable price field allowing instant price updates dispatched to `PATCH /api/v1/products/{id}/price` with validation to guarantee positive values.
* **Category Filtering & Search:** Rapidly filter inventory by category (Electronics, Wearables, Accessories, Home) or search by name.

### 5. Product Creation Modal
* Modal form allowing managers to introduce new catalog items directly:
  * Product Name
  * Description
  * Category (dropdown selector)
  * Price (INR/USD positive value)
  * Initial Stock Quantity
  * Image URL (with default placeholder fallback)
* Dispatches `POST /api/v1/products` with JWT authorization.
* Newly created products appear immediately in the manager grid and customer catalog.

### 6. Catalog Item Removal
* Confirmation modal to safely deactivate/delete items from the catalog via `DELETE /api/v1/products/{id}`.

---

## File Structure

* `frontend/src/pages/ManagerDashboardPage.jsx`: Main store manager portal with inventory table, quick adjustments, and add product modal.
* `frontend/src/components/ManagerRoute.jsx`: Route guard enforcing manager role permissions.
* `frontend/src/context/AuthContext.jsx`: Augmented with `isManager` and `role` extraction.
* `frontend/src/components/Navbar.jsx`: Manager navigation links for desktop, profile menu, and mobile drawer.
* `frontend/src/App.jsx`: `/manager` route registration.

---

## Acceptance Criteria Verification

| Test Scenario | Expected Outcome | Status |
| :--- | :--- | :--- |
| Login as `manager@flopkart.com` | `Manager Portal` badge appears in header nav and profile dropdown | Passed |
| Unauthenticated access to `/manager` | Redirects to `/login` | Passed |
| Regular customer access to `/manager` | Redirects to `/` (Forbidden access) | Passed |
| Quick stock $\pm 1$ adjustment | Sends `PATCH /api/v1/products/{id}/stock` and updates table | Passed |
| Price update | Sends `PATCH /api/v1/products/{id}/price` and verifies positive values | Passed |
| Add new product | Sends `POST /api/v1/products` and lists product in table | Passed |
| Delete product | Sends `DELETE /api/v1/products/{id}` and removes item | Passed |
