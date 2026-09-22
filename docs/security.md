# Security Architecture & Data Isolation Specification

## 1. Authentication & JWT Security
- Standard Authorization header format: `Bearer <token>`
- HMAC-SHA256 signing key configured via `JWT_SECRET` environment variable
- Short-lived Access Tokens (24h) and secure Refresh Tokens (7d) stored in PostgreSQL with instant revocation capability upon logout
- Passwords hashed using **BCrypt** with salt factor 10

## 2. Role-Based Access Control (RBAC)
- Roles enforced via `@PreAuthorize("hasAnyRole(...)")` on Spring controllers:
  - `ADMIN`: Full platform access, user management, audit logs.
  - `MANAGER`: Technician assignment, inventory control, repair pipeline management.
  - `STAFF`: Service requests, job creation, customer invoicing.
  - `TECHNICIAN`: Restricted access to assigned repair jobs, diagnosis logging, and repair status updates.
  - `CUSTOMER`: Data-isolated access to own registered devices, service requests, estimates approval, and invoice payments.

## 3. Customer Data Isolation Safeguards
- Endpoints inspect `SecurityUtils.getCurrentUserId()` and `SecurityUtils.isCustomer()`
- Direct database query filters guarantee that a customer cannot view or modify another customer's repair job, device, or estimate. Attempting ID manipulation returns `403 FORBIDDEN`.

## 4. Payment Security & Server-Side Verification
- Payment success is NEVER trusted from frontend responses.
- `PaymentService.verifyAndRecordPayment` validates signatures and order IDs server-side before updating invoice status to `PAID` and triggering inventory consumption.
