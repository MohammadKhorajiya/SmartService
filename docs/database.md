# Database Schema & Data Dictionary

## Schema Engine
- Database: PostgreSQL 16
- Migration Tool: Flyway
- Migration File: `backend/src/main/resources/db/migration/V1__initial_schema.sql`

## Key Tables
- `users`: User credentials, roles (`ADMIN`, `MANAGER`, `STAFF`, `TECHNICIAN`, `CUSTOMER`), BCrypt password hashes.
- `customers`: Customer profiles linked `1:1` with `users`.
- `technicians`: Technician profiles, specializations, availability status.
- `devices`: Registered devices linked `N:1` with `customers`.
- `service_requests`: Customer problem submissions.
- `repair_jobs`: Main work order table with `job_number`, status, priority, costs.
- `repair_job_status_history`: Audit tracking for status state machine changes.
- `diagnoses`: Technical findings linked `1:1` with `repair_jobs`.
- `parts`: Inventory catalog with `@Version` column for optimistic locking.
- `inventory_transactions`: Historical log of all stock receipts, reservations, and consumption.
- `inventory_reservations`: Reserved stock quantities for active repair jobs.
- `estimates` & `estimate_items`: Cost estimates sent to customers for approval.
- `invoices` & `invoice_items`: Billing invoices.
- `payments`: Verified gateway payment receipts.
- `notifications`: User in-app notification center.
- `audit_logs`: Operational audit trail.
