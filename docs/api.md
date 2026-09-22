# API Contract Documentation

OpenAPI / Swagger UI endpoint: `http://localhost:8080/swagger-ui.html`

## Auth Endpoints
- `POST /api/v1/auth/login`: Authenticate and receive JWT access + refresh tokens.
- `POST /api/v1/auth/register`: Customer account creation.
- `POST /api/v1/auth/refresh`: Exchange refresh token for new access token.
- `POST /api/v1/auth/logout`: Revoke active refresh token.

## Repair Jobs Endpoints
- `GET /api/v1/repair-jobs`: Paginate & filter jobs by status, technician, or customer.
- `POST /api/v1/repair-jobs`: Create repair job.
- `POST /api/v1/repair-jobs/{id}/assign-technician`: Assign technician (`ADMIN`/`MANAGER`).
- `PATCH /api/v1/repair-jobs/{id}/status`: Update job status (enforces state machine).
- `GET /api/v1/repair-jobs/{id}/history`: Retrieve status audit timeline.

## Estimate & Inventory Endpoints
- `POST /api/v1/estimates`: Generate estimate breakdown.
- `POST /api/v1/estimates/{id}/approval`: Approve/reject estimate & trigger stock reservation.
- `GET /api/v1/inventory/parts`: Search spare parts catalog.
- `POST /api/v1/inventory/parts`: Create part SKU.
- `POST /api/v1/inventory/adjust-stock`: Adjust stock quantity.

## Invoice & Payment Endpoints
- `POST /api/v1/invoices/job/{jobId}`: Auto-generate invoice from repair job.
- `POST /api/v1/payments/create-order/{invoiceId}`: Initialize payment gateway order.
- `POST /api/v1/payments/verify`: Server-side payment verification & stock consumption.
