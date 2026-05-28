# Dynamic Price Manager

A Spring Boot web application for a beauty salon: online booking, admin panel (services, staff, reservations), and **dynamic pricing** that adjusts appointment prices based on demand, time windows, and last-minute rules.

> **Note:** This is a **student / demo** project. Default passwords are intentional for easy local setup — do not deploy it publicly on the internet without hardening.

## Table of Contents
* [General Information](#general-information)
* [Technologies Used](#technologies-used)
* [Architecture](#architecture)
* [Features](#features)
* [Setup & Usage](#setup--usage)
* [Default Accounts](#default-accounts)
* [REST API](#rest-api)
* [Testing](#testing)
* [Author](#author)

## General Information

The main goals of this project are:

- **Salon operations:** Manage services, employees, working hours, and reservations from an admin UI.
- **Client self-service:** Register, book appointments, view and cancel upcoming visits, and update profile details.
- **Dynamic pricing:** Apply configurable rules (weekday + time window, busy/quiet demand thresholds, last-minute discount) and show a live price quote when the client picks a slot.
- **Data integrity:** Prevent double-booking and overlapping slots per employee; store the final quoted price on each reservation.

## Technologies Used

- **Java 21** — core language
- **Spring Boot 4** — application framework (Web MVC, Security, Data JPA, Validation)
- **Thymeleaf** — server-rendered admin and client UI
- **PostgreSQL** — production-like database (local via Docker)
- **H2** — in-memory database for automated tests
- **Docker Compose** — PostgreSQL and pgAdmin for local development
- **Maven** — build and dependencies
- **JUnit 5 & MockMvc** — unit and web integration tests
- **JaCoCo** — code coverage (80% line threshold on main code)

## Architecture

The application is a classic three-tier monolith: browser UI and JSON booking API served by one Spring Boot instance, with PostgreSQL as the data store.

```text
          ┌──────────────┐
 browser ─┤ Spring Boot  ├── Thymeleaf (admin + client) + REST (/api/booking)
          └──────┬───────┘
                 │
           ┌─────▼──────┐
           │ PostgreSQL │
           └────────────┘
```

**Security:** role-based access — `ADMIN` for `/admin/**`, `CLIENT` for `/client/**`; form login with CSRF on POST forms; booking API requires authentication.

## Features

- **Admin dashboard:** overview and navigation to management screens.
- **Services:** CRUD for salon services (name, price, duration, description).
- **Employees:** CRUD, specialization, working hours, assignment of offered services.
- **Reservations:** list with filters (employee, status, date range); confirm pending bookings.
- **Dynamic pricing:** global settings (lookback weeks, busy/quiet thresholds, last-minute window), CRUD for pricing rules, admin price simulator.
- **Client area:** browse services, book a slot (date picker + priced time slots), list reservations, cancel future visits, edit profile.
- **Registration:** open client sign-up (`/register`); new users get the `CLIENT` role only.
- **Demo seed (optional):** profile `demo-seed` fills the database with sample services, staff, clients, and a dense calendar — see [Setup & Usage](#setup--usage).

## Setup & Usage

### Prerequisites

- **JDK 21**
- **Maven 3.9+**
- **Docker** (for PostgreSQL via Compose)

### 1. Start the database

From the project root:

```bash
docker compose up -d
```

PostgreSQL listens on `localhost:5432` (user `admin`, password `adminpass`, database `dynamic_pricing`). pgAdmin is available at [http://localhost:8081](http://localhost:8081) (`admin@admin.com` / `adminpass`).

### 2. Run the application

**macOS / Linux**

```bash
mvn spring-boot:run
```

**Windows (PowerShell or CMD)**

```powershell
mvn spring-boot:run
```

Or use the Maven wrapper: `./mvnw` (macOS/Linux) / `mvnw.cmd` (Windows).

Default URL: [http://localhost:8080](http://localhost:8080) — you will be redirected to `/login`.

Optional: override datasource settings with environment variables:

- `SPRING_DATASOURCE_URL`
- `SPRING_DATASOURCE_USERNAME`
- `SPRING_DATASOURCE_PASSWORD`

### 3. (Optional) Load demo salon data

Starts a **one-off** Spring Boot batch run with profile `demo-seed` (10 services, 5 employees, 10 client accounts, hundreds of reservations). The database must already be running (step 1).

**The main app from step 2 may keep running on port 8080** — seed does not start a web server (`application-demo-seed.properties` sets `spring.main.web-application-type=none`), so there is no port conflict.

Use a **second** terminal. The process exits on its own after `Salon simulation seed complete` (or stop with `Ctrl+C`). Re-runs are idempotent (skipped if employees with marker `__SALON_SIM__` already exist).

**macOS / Linux**

```bash
./scripts/seed-demo.sh
```

Or without the script:

```bash
mvn spring-boot:run -Dspring-boot.run.profiles=demo-seed
```

**Windows (PowerShell)**

```powershell
mvn spring-boot:run "-Dspring-boot.run.profiles=demo-seed"
```

**Windows (CMD)**

```cmd
mvn spring-boot:run -Dspring-boot.run.profiles=demo-seed
```

With the Maven wrapper:

```powershell
.\mvnw.cmd spring-boot:run "-Dspring-boot.run.profiles=demo-seed"
```

**Windows (Git Bash)** — same as macOS/Linux:

```bash
./scripts/seed-demo.sh
```

## Default Accounts

Created automatically on first startup by `DataInitializer` (if the accounts do not exist yet):

| Role   | Email               | Password |
|--------|---------------------|----------|
| Admin  | `admin@system.com`  | `admin`  |
| Client | `client@system.com` | `client` |

**Demo seed clients** (only after demo seed in step 3): `simulation.client01@local.test` … `simulation.client10@local.test` — password `demo123`.

## REST API

Authenticated JSON endpoints used by the booking UI (`fetch` with session cookie + CSRF header):

| Method | Path | Purpose |
| :--- | :--- | :--- |
| `GET` | `/api/booking/available-slots` | Available times for an employee, date, and service, each with dynamic price. Query: `employeeId`, `date` (`yyyy-MM-dd`), `serviceId`. |
| `GET` | `/api/booking/price-quote` | Price breakdown for one slot. Query: `serviceId`, `date`, `time` (`HH:mm`). |

Admin pricing simulator (authenticated as `ADMIN`):

| Method | Path | Purpose |
| :--- | :--- | :--- |
| `GET` | `/admin/pricing/quote` | Same quote JSON as above for testing rules. Query: `serviceId`, `date`, `time`. |

Main **web routes** (HTML):

| Path | Role | Purpose |
| :--- | :--- | :--- |
| `/login`, `/register` | public | Sign in / sign up |
| `/admin`, `/admin/services`, `/admin/employees`, `/admin/reservations`, `/admin/pricing` | `ADMIN` | Management UI |
| `/client`, `/client/book`, `/client/reservations`, `/client/profile` | `CLIENT` | Client UI |

## Testing

The project includes unit tests, service integration tests, and MockMvc web tests.

```bash
mvn verify
```

This runs all tests and enforces JaCoCo **line coverage ≥ 80%** on application code (DTOs, entities, and demo initializers are excluded). Reports: `target/site/jacoco/index.html`.

## Author

Project developed by **Jakub Podsadowski**.
