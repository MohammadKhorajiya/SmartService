# Smart Service — Service & Repair Management SaaS Platform

[![Java](https://img.shields.io/badge/Java-21-orange.svg)](https://oracle.com/java)
[![Spring Boot](https://img.shields.io/badge/Spring%20Boot-3.3.3-brightgreen.svg)](https://spring.io/projects/spring-boot)
[![React](https://img.shields.io/badge/React-18-blue.svg)](https://react.dev)
[![TypeScript](https://img.shields.io/badge/TypeScript-5.6-blue.svg)](https://www.typescriptlang.org)
[![PostgreSQL](https://img.shields.io/badge/PostgreSQL-16-blue.svg)](https://www.postgresql.org)
[![Docker](https://img.shields.io/badge/Docker-Enabled-blue.svg)](https://www.docker.com)

**Smart Service** is a production-grade, job-ready full-stack SaaS platform designed for modern electronics, mobile phone, laptop, and appliance repair businesses.

---

## 📁 Clean Repository Structure

```
d:\Anti-Gravity\
├── backend/                  # Java 21 / Spring Boot Backend Project
│   ├── pom.xml
│   └── src/main/java/com/smartservice/
├── frontend/                 # React 18 / TypeScript Frontend Project
│   ├── package.json
│   ├── vite.config.ts
│   └── src/
├── docs/                     # Full Technical Architecture & API Docs
│   ├── architecture.md
│   ├── database.md
│   ├── api.md
│   └── security.md
├── docker-compose.yml        # PostgreSQL 16 & Redis 7 Infrastructure
└── README.md
```

---

## 🚀 Quick Start (Local Setup)

### 1. Prerequisites
- **JDK 21** or later
- **Node.js 18+** & `npm`
- **Docker** & **Docker Compose**

### 2. Start Database & Redis via Docker
```bash
docker compose up -d
```
*Spawns PostgreSQL 16 on port 5432 and Redis 7 on port 6379.*

### 3. Run Backend Project (`backend/`)
```bash
cd backend
mvn spring-boot:run
```
*Backend API starts at `http://localhost:8080` (Swagger UI at `http://localhost:8080/swagger-ui.html`).*

### 4. Run Frontend Project (`frontend/`)
```bash
cd frontend
npm run dev
```
*Frontend SaaS application opens at `http://localhost:5173`.*

---

## 🔑 Demo Seed Credentials

| Role | Email | Password |
|---|---|---|
| **System Admin** | `admin@smartservice.com` | `Password@123` |
| **Service Manager** | `manager@smartservice.com` | `Password@123` |
| **Technician** | `tech.alex@smartservice.com` | `Password@123` |
| **Front Desk Staff** | `staff@smartservice.com` | `Password@123` |
| **Customer** | `customer.john@gmail.com` | `Password@123` |

---

## 🧪 Testing Execution

```bash
# Run backend JUnit tests
cd backend
mvn test

# Build frontend production bundle
cd frontend
npm run build
```
