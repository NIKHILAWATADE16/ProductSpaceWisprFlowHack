# Finance Coach

A modular-monolith personal finance coach built with React, Vite, Spring Boot, PostgreSQL, and JPA.

## Current status

**Phase 1 — project setup** is implemented:

- Spring Boot 3.4 API project targeting Java 21
- PostgreSQL Docker Compose service
- JPA entities for users, profiles, transactions, goals, budgets, and subscriptions
- Monetary values modeled with `BigDecimal`
- React/Vite frontend shell with responsive dark SaaS styling
- Environment-variable based backend configuration

The API and business endpoints are intentionally added in later phases so each phase can be verified independently.

## Prerequisites

- Java 21+
- Maven 3.9+
- Node.js 20+
- Docker Desktop (for PostgreSQL)

## Run Phase 1

1. Copy `.env.example` to `.env` and adjust values if needed. Spring Boot reads the defaults in `application.yml`; export variables in your shell when overriding them.
2. Start PostgreSQL:

   ```powershell
   docker compose up -d postgres
   ```

3. Build and start the backend:

   ```powershell
   cd backend
   mvn clean verify
   mvn spring-boot:run
   ```

4. In another terminal, install and start the frontend:

   ```powershell
   cd frontend
   npm install
   npm run dev
   ```

5. Open http://localhost:5173.

## Database

Hibernate creates/updates the Phase 1 schema with `DDL_AUTO=update`. For production, use a versioned migration tool and set `DDL_AUTO=validate`.

The local PostgreSQL data is stored in the `financecoach-postgres-data` Docker volume.

## Planned phases

1. Backend APIs, authentication, validation, and calculations
2. Dashboard API integration and charts
3. Transaction CRUD, imports, categorization, and correction rules
4. Goals, budgets, and simulations
5. AI coach with deterministic fallback
6. Reports and subscriptions
7. Automated tests, seed data, and final demonstration guide
