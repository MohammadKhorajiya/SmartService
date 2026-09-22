# System Architecture & Technical Specification

## Overview
Smart Service is designed as a **Modular Monolith** in Java 21 / Spring Boot 3.3.3 paired with a single-page React TypeScript application.

```
[ Customer / Staff Browser ]
          │
          ▼
[ React 18 + TS Frontend (Vite) ]
          │
          ▼ (HTTPS JSON REST API)
[ Spring Security + JWT Filter Layer ]
          │
          ▼
[ Domain Controllers & Services ]
          │
    ┌─────┴──────────────────┐
    ▼                        ▼
[ PostgreSQL 16 ]     [ Redis 7 Cache ]
```

## Core Modules & Boundaries
1. **`auth`**: Handles authentication, registration, refresh tokens, and password encoder.
2. **`customer`**: Customer profile management and account binding.
3. **`device`**: Multi-device ownership registry.
4. **`servicerequest`**: Problem intake and conversion to repair job.
5. **`repair`**: State machine validation, status timeline, and technician assignment.
6. **`diagnosis`**: Technical findings and recommended repair procedures.
7. **`estimate`**: Automated line-item breakdown, tax calculation, and customer approval.
8. **`inventory`**: SKU catalog, pessimistic/optimistic concurrency locks, and stock reservations.
9. **`invoice`**: Billing invoice generation and status updates.
10. **`payment`**: Gateway order creation and server-side payment verification.
11. **`analytics`**: Aggregated SaaS KPIs and pipeline statistics.
