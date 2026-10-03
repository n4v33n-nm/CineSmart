# CineSmart — Smart Movie Ticket Booking System Using OOAD

[![Java](https://img.shields.io/badge/Java-17%20LTS-orange.svg)](https://www.oracle.com/java/)
[![Spring Boot](https://img.shields.io/badge/Spring%20Boot-3.3.4-brightgreen.svg)](https://spring.io/projects/spring-boot)
[![React](https://img.shields.io/badge/React-18.3.1-blue.svg)](https://react.dev/)
[![Vite](https://img.shields.io/badge/Vite-5.4-purple.svg)](https://vitejs.dev/)
[![PostgreSQL](https://img.shields.io/badge/PostgreSQL-15%2B-blue.svg)](https://www.postgresql.org/)
[![OOAD](https://img.shields.io/badge/Architecture-OOAD%20%26%20Design%20Patterns-purple.svg)]()
[![Tests](https://img.shields.io/badge/JUnit%205-42%20Passing-brightgreen.svg)]()

> **Phase 3 Complete:** Smart Group Seating and Intelligent Seat Allocation. Implemented interchangeable allocation strategies (Contiguous, Flexible Split-Row, Accessibility Gate), mathematical multi-factor scoring with deterministic tie-breaking, separation of recommendation from reservation, atomic pessimistic locking, and interactive React UI assistant. 42 passing tests. Fully runnable and verified.

---

## 1. Project Overview

**CineSmart** is a movie ticket reservation platform architected around **Object-Oriented Analysis and Design (OOAD)** principles and clean layered architecture. Built as a college capstone demonstration project, it models real-world cinema operations while preparing the infrastructure for three distinctive algorithmic engines:

1. **Smart Group Seating & Adjacency Coordination** (Phase 3)
2. **Booking Recovery Engine & Automated Reconciliation** (Phase 3)
3. **Fair Seat Release & FIFO Waitlist** (Phase 3)

---

## 2. Technology Stack

### Backend
* **Language:** Java 17 (LTS)
* **Framework:** Spring Boot 3.3.4
* **Web Layer:** Spring Web MVC (`@RestController`)
* **Persistence:** Spring Data JPA / Hibernate 6.x
* **Security:** Spring Security 6 with stateless JWT (`jjwt` 0.12.5) & BCrypt password hashing
* **Validation:** Jakarta Bean Validation (`@Valid`, `@NotBlank`, `@Email`, `@Positive`)
* **Database Driver:** PostgreSQL JDBC Driver (`org.postgresql:postgresql`)
* **Build Tool:** Apache Maven 3.9+
* **Testing:** JUnit 5, Mockito, Spring Boot Test, H2 In-Memory Database (test scope)

### Frontend
* **Library:** React 18.3.1 (Single-Page Application)
* **Build & Dev Server:** Vite 5.4
* **Routing:** React Router v6 (`react-router-dom`)
* **Icons:** Lucide React
* **Styling:** Vanilla CSS with HSL design system, dark cinema theme, glassmorphism, responsive grid
* **HTTP Client:** Fetch API with centralized bearer token injection and error unwrapping

### Database
* **Engine:** PostgreSQL 15+ (Production / Development)
* **DDL & Seed:** Complete DDL with check constraints, foreign keys, and partial unique indexes in `database/schema/` and `database/seed/`

---

## 3. Layered OOAD Architecture

```text
React Frontend (Vite SPA on :5173)
       │ HTTP / JSON (Bearer JWT)
       ▼
REST Controllers (@RestController /api/*)
       │ DTOs (Request / Response validation)
       ▼
Service Layer (@Service business logic & transactions)
       │ Domain Entities & Enums
       ▼
Repository Layer (Spring Data JPA with Pessimistic Locking)
       │ SQL / JDBC
       ▼
PostgreSQL Database (Local or Containerized on :5432)
```

### Core Domain Distinction: Physical Seat vs. ShowSeat vs. BookingSeat

CineSmart strictly enforces the OOAD domain distinction to avoid double-booking and data denormalization:

```text
+---------------------+          +----------------------+          +----------------------+
|     ScreenSeat      |          |       ShowSeat       |          |     BookingSeat      |
|     (Physical)      | 1      * |     (Temporal)       | 1      * |    (Financial)       |
|---------------------|--------->|----------------------|--------->|----------------------|
| Row A, Col 1        |          | Show: 7:00 PM        |          | Booking: CS-10293    |
| GridX: 0, GridY: 0  |          | Status: AVAILABLE    |          | Snapshot Price: $15  |
| Tier: STANDARD      |          | Price: $15.00        |          | Tier: STANDARD       |
+---------------------+          +----------------------+          +----------------------+
```

1. **`Seat`:** Represents an immutable physical seat in an auditorium. It has physical coordinates (`grid_x`, `grid_y`), row identifier, and tier.
2. **`ShowSeat`:** Represents the real-time availability and current price of that physical seat for a specific screening (`show_id`). Holds the active state (`AVAILABLE`, `HELD`, `BOOKED`, `BLOCKED`).
3. **`BookingSeat`:** An immutable transactional snapshot created upon booking confirmation. Preserves historical pricing even if show base prices fluctuate later.

---

## 4. Project Structure

```text
CineSmart/
│
├── backend/
│   ├── pom.xml
│   └── src/
│       ├── main/
│       │   ├── java/com/cinesmart/
│       │   │   ├── auth/          # JWT auth controllers, services, DTOs, UserDetails
│       │   │   ├── booking/       # Booking entity, repository, service, controller, DTOs
│       │   │   ├── cinema/        # Cinema & screen controllers, entity, repository, service
│       │   │   ├── common/        # GlobalExceptionHandler, ApiResponse<T>, custom exceptions
│       │   │   ├── config/        # SecurityConfig, CorsConfig, JwtTokenProvider, DataInitializer
│       │   │   ├── movie/         # Movie entity, repository, service, controller, DTOs
│       │   │   ├── screen/        # Screen entity, repository, DTOs
│       │   │   ├── seat/          # Seat & ShowSeat entities, repositories, seat-map DTOs
│       │   │   ├── show/          # Show entity, repository, service, controller, DTOs
│       │   │   ├── user/          # User entity, repository, UserRole enum
│       │   │   └── CineSmartApplication.java
│       │   └── resources/
│       │       ├── application.properties
│       │       ├── application-dev.properties
│       │       └── application-test.properties
│       └── test/java/com/cinesmart/
│           ├── BookingServiceTest.java
│           ├── BookingTest.java
│           ├── HealthControllerTest.java
│           ├── MovieRepositoryTest.java
│           ├── MovieServiceTest.java
│           ├── ShowSeatRepositoryTest.java
│           ├── ShowSeatTest.java
│           └── UserTest.java
│
├── frontend/
│   ├── package.json
│   ├── vite.config.js
│   ├── index.html
│   └── src/
│       ├── components/    # Navbar, Footer, SeatMap, Seat, MovieCard, MovieGrid, etc.
│       ├── context/       # AuthContext (login, register, logout, role checking)
│       ├── pages/         # Home, MovieList, MovieDetails, SeatSelection, Login, Register, MyBookings, Admin
│       ├── services/      # api.js, authService, movieService, showService, seatService, bookingService, adminService
│       ├── App.jsx        # Route definitions and ProtectedRoute guards
│       ├── index.css      # Design system tokens, glassmorphism, responsive utilities
│       └── main.jsx       # React DOM mount with AuthProvider and BrowserRouter
│
├── database/
│   ├── schema/
│   │   └── 01-schema.sql  # PostgreSQL DDL (12 normalized tables, constraints, indexes)
│   └── seed/
│       └── 01-seed-data.sql # Seed data (5 movies, cinema, 2 screens, 96 seats, 8 shows)
│
├── docs/                  # Phase 1 OOAD specifications & 10 PlantUML diagrams
│
└── README.md
```

---

## 5. Prerequisites

* **Java JDK:** 17 or higher (`java -version`)
* **Maven:** 3.8+ (`mvn -version`)
* **Node.js:** 18+ & npm (`node -v`, `npm -v`)
* **PostgreSQL:** 15+ (`psql --version`)

---

## 6. Database Setup & Environment Variables

### 1. Create PostgreSQL Database
```sql
CREATE DATABASE cinesmart;
```

### 2. Configure Environment Variables
You can export the environment variables or supply them in your local terminal:

```bash
# Windows PowerShell
$env:DB_URL="jdbc:postgresql://localhost:5432/cinesmart"
$env:DB_USERNAME="postgres"
$env:DB_PASSWORD="your_password"

# macOS / Linux Bash
export DB_URL="jdbc:postgresql://localhost:5432/cinesmart"
export DB_USERNAME="postgres"
export DB_PASSWORD="your_password"
```

*(Note: If you run without custom environment variables, `application.properties` defaults to `localhost:5432/cinesmart` with username `postgres`)*.

### 3. Automatic Seeding vs. Manual SQL Script
The Spring Boot backend includes an intelligent `DataInitializer` bean that automatically provisions sample movies, theaters, screens, 96 physical seats, and shows on first startup if the database is empty.

Alternatively, to manually execute the DDL and seeds:
```bash
psql -U postgres -d cinesmart -f database/schema/01-schema.sql
psql -U postgres -d cinesmart -f database/seed/01-seed-data.sql
```

---

## 7. How to Run

### Backend (Spring Boot)
Open a terminal in the `backend/` folder:

```bash
cd backend
mvn spring-boot:run
```

The backend server starts on:
👉 **`http://localhost:8080`**

Verify health endpoint:
```bash
curl http://localhost:8080/api/health
```
Response:
```json
{"status":"UP","application":"CineSmart"}
```

### Frontend (React + Vite)
Open a second terminal in the `frontend/` folder:

```bash
cd frontend
npm install
npm run dev
```

The frontend client launches on:
👉 **`http://localhost:5173`**

*(Requests made to `/api/*` are automatically proxied by Vite to `http://localhost:8080`)*.

---

## 8. Built-in Demo Accounts

For instant grading and demonstration, the database comes pre-seeded with the following roles:

| Role | Email | Password | Access Capabilities |
| :--- | :--- | :--- | :--- |
| **Customer** | `customer@cinesmart.com` | `password123` | Browse catalog, select seats, book tickets, view history |
| **Admin** | `admin@cinesmart.com` | `admin123` | Full Admin Console, create/deactivate movies, schedule/cancel shows |
| **Staff** | `staff@cinesmart.com` | `staff123` | Box-office desk and operational overrides |

*(Quick 1-click demo login buttons are provided on the Login page)*.

---

## 9. API Overview

All responses use the uniform envelope:
```json
{
  "success": true,
  "message": "Resource retrieved successfully",
  "data": { ... }
}
```

### Authentication
* `POST /api/auth/register` — Register a customer account
* `POST /api/auth/login` — Authenticate and receive JWT access token
* `GET /api/auth/me` — Retrieve current authenticated profile

### Movies
* `GET /api/movies` — List all active movies (optional filter by `genre` and `search`)
* `GET /api/movies/{id}` — Get single movie details with duration and certification
* `GET /api/movies/{id}/shows` — List all available showtimes for a specific movie

### Shows & Cinemas
* `GET /api/shows` — List active scheduled shows (optional `movieId` or `date` filter)
* `GET /api/shows/{id}` — Retrieve show details with screen information
* `GET /api/cinemas` — List cinemas and locations
* `GET /api/cinemas/{id}/screens` — List auditoriums belonging to a cinema

### Seats & Smart Group Seating
* `GET /api/shows/{showId}/seats` — Get live interactive seat map layout for a screening with `ShowSeat` availability states and tier pricing
* `POST /api/shows/{showId}/group-seating/recommendations` — Request intelligent group seating recommendations (partySize, preferredTier, preferredRow, allowSplitRows, requireAccessibility)
* `POST /api/shows/{showId}/recommendations` — Compatible alias endpoint for group seating recommendations
* `POST /api/recommendations/group-seats` — Global endpoint matching Phase 1 API specifications

### Bookings
* `POST /api/bookings` — Create a new ticket booking (`showId`, `showSeatIds`) with pessimistic concurrency locking
* `GET /api/bookings/my` — List all bookings for authenticated customer
* `GET /api/bookings/{id}` — Get detailed booking ticket by ID

### Admin Management (Guarded by `ROLE_ADMIN`)
* `POST /api/admin/movies` — Add new movie title
* `PUT /api/admin/movies/{id}` — Update movie metadata
* `DELETE /api/admin/movies/{id}` — Deactivate movie title
* `POST /api/admin/shows` — Schedule show & automatically provision real-time `ShowSeat` inventory
* `PUT /api/admin/shows/{id}/cancel` — Cancel scheduled show

---

## 10. Automated Testing

### Backend Test Suite
Run the backend test suite:
```bash
cd backend
mvn test
```

**Results:**
* **Tests executed:** 42 passing tests across 14 test classes
* **Failures:** 0
* **Errors:** 0
* **Coverage:**
  * **Phase 1 & 2 Foundations:** Domain Entity constraints (`UserTest`, `ShowSeatTest`, `BookingTest`), JPA Repositories (`MovieRepositoryTest`, `ShowSeatRepositoryTest`), Services (`MovieServiceTest`, `BookingServiceTest`), REST APIs (`HealthControllerTest`).
  * **Phase 3 Algorithmic Strategies:** Single-row sliding window (`ContiguousSeatAllocationStrategyTest`), multi-row vertical split allocation (`FlexibleSeatAllocationStrategyTest`), accessibility hard gates (`GroupSeatingServiceTest`).
  * **Phase 3 Preference Scoring & Determinism:** Center screen viewing angle, row depth sweet spots, tier concordance, split misalignment penalties, strict deterministic tie-breakers (`SeatScoringServiceTest`).
  * **Phase 3 Transactional & Concurrency Integrity:** Duplicate/invalid seat ID rejection, cross-show mismatches, atomic all-or-nothing rollback (`BookingValidationTest`), multi-threaded race condition double-booking prevention (`ConcurrencyBookingIntegrationTest`).
  * **Phase 3 REST Contracts:** Request validation and response DTO schemas (`GroupSeatingIntegrationTest`).

### Frontend Build Validation
```bash
cd frontend
cmd /c "npm run build"
```
**Results:**
* Production bundle compiled in `dist/` with 0 errors (1599 modules transformed, Vite build verified).

---

## 11. OOAD Concepts & Design Patterns

1. **Information Expert:** Entities encapsulate domain rules (e.g., `ShowSeat.isAvailable()`, `ShowSeat.markBooked()`, `Booking.calculateTotalAmount()`).
2. **Separation of Concerns:** Strict layer decoupling between Controller (DTO parsing) $\rightarrow$ Service (transaction & domain business logic) $\rightarrow$ Repository (data persistence).
3. **Repository Pattern:** Spring Data repositories abstract database access and queries without raw SQL leaks in business logic.
4. **Pessimistic Concurrency Locking:** `ShowSeatRepository.findAllByIdWithLock()` issues `SELECT ... FOR UPDATE` ensuring safe seat locking during booking creation.
5. **Data Transfer Object (DTO) Pattern:** Prevents over-posting, protects sensitive database fields (`password_hash`), and avoids Hibernate lazy-loading serialization issues.
6. **Strategy Pattern (Phase 3):** Interchangeable seat allocation algorithms implementing `SeatAllocationStrategy` (`ContiguousSeatAllocationStrategy`, `FlexibleSeatAllocationStrategy`, `AccessibleSeatAllocationStrategy`), dynamically orchestrated by `GroupSeatingService`.
7. **Single Responsibility Principle (SRP):** Complete separation between read-only advisory seat recommendation (`GroupSeatingService`) and transactional, atomic reservation (`BookingService`).

---

## 12. Roadmap & Implementation Phases

* [x] **Phase 1: OOAD Requirements Analysis and UML Design**
  * Use Cases, Functional Requirements, Database Schema, 10 PlantUML diagrams.
* [x] **Phase 2: Full-Stack Project Foundation and Core Infrastructure**
  * Spring Boot 3 + React 18 + PostgreSQL infrastructure, JWT auth, interactive seat map, order creation, admin CRUD, automated test suite.
* [x] **Phase 3: Smart Group Seating and Intelligent Seat Allocation**
  * Contiguous single-row sliding window algorithm ($\mathcal{O}(R \cdot C)$).
  * Flexible adjacent-split fallback algorithm ($K=2$) with vertical column alignment.
  * Certified accessibility hard constraints (wheelchair bay + companion pairs).
  * Mathematical multi-factor penalty scoring formula with documented weights.
  * 5-rule strict deterministic tie-breaker comparator.
  * Separation of recommendation (advisory) from reservation (atomic pessimistic locking).
  * REST API endpoints (`/api/shows/{showId}/group-seating/recommendations`).
  * Interactive React frontend widget (`SmartGroupSeating.jsx`) with real-time map preview.
  * Comprehensive test suite: 42 automated unit and integration tests passing.
  * Updated UML diagrams (`phase3-group-seating-class-diagram.puml`, `phase3-group-seating-sequence.puml`, `phase3-group-seating-activity.puml`).
