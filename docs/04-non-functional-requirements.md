# CineSmart — Smart Movie Ticket Booking System
## Document 04: Non-Functional Requirements

---

### Document Control
* **Document Version:** 1.0.0
* **Target Audience:** System Architects, Academic Reviewers, QA Engineers
* **Phase:** Phase 1 — Non-Functional Specifications & Design Targets

---

## 1. Classification & Measurement Philosophy

Non-functional requirements (NFRs) dictate *how well* the system executes its functional behaviors. In accordance with rigorous software engineering practices:
* All requirements are defined with **verifiable criteria**.
* No unfounded benchmark claims (such as "the system will handle millions of users") are made.
* All numerical performance metrics are explicitly categorized as **Design Targets** intended for verification during integration testing rather than unmeasured marketing claims.

---

## 2. Security and Access Control (NFR-SEC)

### NFR-SEC-01: Cryptographic Password Storage
* **Requirement:** User passwords must never be stored in plaintext or with reversible encryption.
* **Specification:** Passwords must be hashed using the **BCrypt** adaptive hashing algorithm with a minimum cost factor of 10 (`BCryptPasswordEncoder(10)`).
* **Verification:** Database inspection verifies hashes conform to standard `$2a$10$...` format; automated unit tests verify raw passwords fail direct string equality checks.

### NFR-SEC-02: Stateless Authentication via JWT
* **Requirement:** API interactions must rely on signed, stateless tokens to decouple user identity from server session storage.
* **Specification:** Authentication produces an HMAC-SHA256 (HS256) signed JSON Web Token (JWT) with standard claims (`sub`, `roles`, `iat`, `exp`). Expiration is set to 24 hours. Tokens must be transmitted via standard HTTP header `Authorization: Bearer <token>`.
* **Verification:** Tampered tokens, expired tokens, or tokens signed with invalid secret keys must return HTTP 401 Unauthorized within Spring Security filters.

### NFR-SEC-03: Role-Based Endpoint Protection
* **Requirement:** Strict principle of least privilege must govern API access.
* **Specification:** Endpoints are partitioned into public (`/api/v1/auth/**`, `/api/v1/movies/**`, `/api/v1/shows/**`), customer-only (`/api/v1/bookings/**`, `/api/v1/seat-holds/**`), staff-only (`/api/v1/staff/**`), and admin-only (`/api/v1/admin/**`).
* **Verification:** Automated tests verify that requests lacking required authorities receive HTTP 403 Forbidden.

### NFR-SEC-04: Protection Against Standard Web Vulnerabilities
* **Requirement:** The backend must resist common OWASP Top 10 vulnerabilities.
* **Specification:**
  1. **SQL Injection:** Prohibit string concatenation in SQL queries; use Spring Data JPA parameterized queries and Hibernate typed criteria.
  2. **Cross-Site Scripting (XSS):** React automatically sanitizes JSX string interpolation; API sets standard `Content-Type: application/json`.
  3. **CORS:** Restrict allowed Cross-Origin Resource Sharing origins strictly to the configured React client host (e.g., `http://localhost:3000` in development).

---

## 3. Data Integrity & Consistency (NFR-DATA)

### NFR-DATA-01: ACID Relational Guarantees
* **Requirement:** All state modifications involving ticket reservations, seat holds, and payments must execute under ACID transaction boundaries.
* **Specification:** Spring `@Transactional(isolation = Isolation.READ_COMMITTED)` encapsulates all service-layer write workflows. If any step fails (e.g., payment failure or seat already held), all changes must automatically roll back.
* **Verification:** Failure simulation tests verify no partial records (`Booking` without `BookingSeat`, or `Payment` without `Booking`) exist after an exception.

### NFR-DATA-02: Relational Integrity & Historical Immutability
* **Requirement:** Historical financial and reservation records must remain accurate regardless of catalog changes.
* **Specification:** Ticket prices are snapshotted into `booking_seats.unit_price` at booking creation. Soft deletion (`is_active = false`) is used for movies and shows to preserve referential integrity with past bookings.
* **Verification:** Updating a show's base price does not modify the calculated total or seat prices of past confirmed bookings.

---

## 4. Prevention of Double Booking & Concurrency Control (NFR-CONC)

### NFR-CONC-01: Strict Prevention of Double Booking
* **Requirement:** Under no circumstances shall two concurrent customers hold or book the same physical seat for the same showtime.
* **Specification:** 
  1. Multi-layered defense: Application logic checks availability within a transaction.
  2. Database row-level locking: Query show seats using pessimistic write locks (`SELECT ... FOR UPDATE` via `@Lock(LockModeType.PESSIMISTIC_WRITE)`).
  3. Database constraints: A partial unique index `idx_unique_active_hold_per_show_seat` on `seat_holds (show_seat_id) WHERE status = 'ACTIVE'` guarantees that PostgreSQL physically rejects duplicate active holds at the engine level.
* **Verification:** Concurrency stress test firing 50 simultaneous hold requests for the exact same seat must result in exactly 1 success (HTTP 201) and 49 conflict failures (HTTP 409).

### NFR-CONC-02: Optimistic Versioning for Show Seats
* **Requirement:** Detect concurrent status changes during seat map rendering and selection.
* **Specification:** `ShowSeat` entities include an integer `@Version` column. Updates increment the version number, triggering an `OptimisticLockException` if another thread committed an update concurrently.

---

## 5. Reliability, Recoverability & Idempotency (NFR-REL)

### NFR-REL-01: Idempotent Payment & Hold APIs
* **Requirement:** Duplicate network requests must not result in duplicate transactions or charges.
* **Specification:** Mutating endpoints (`/api/v1/payments/process`, `/api/v1/seat-holds`) require an `Idempotency-Key` header. Keys are persisted with transaction outcomes in a dedicated `idempotency_records` table with a 24-hour TTL.
* **Verification:** Submitting the same payment payload with the same key three consecutive times returns the cached original HTTP response without re-triggering payment provider logic.

### NFR-REL-02: Active Reconciliation for Uncertain Outcomes
* **Requirement:** Temporary communication drops between the server and payment gateway must not leave transactions permanently stranded.
* **Specification:** Any payment request that returns an ambiguous status or network timeout is flagged as `PAYMENT_PENDING_VERIFICATION`. A scheduled background worker (`@Scheduled(fixedDelay = 60000)`) queries the payment provider API to reconcile status within 3 minutes before any seat release is considered.
* **Verification:** Unit and integration tests simulate a 30-second gateway timeout, verifying that the hold remains active during reconciliation and confirms automatically upon delayed webhook arrival.

---

## 6. Maintainability and OOAD Principles (NFR-MAINT)

### NFR-MAINT-01: Clean Layered Architecture
* **Requirement:** The codebase must maintain strict separation of concerns and unidirectional dependencies: Presentation $\rightarrow$ Service $\rightarrow$ Repository $\rightarrow$ Database.
* **Specification:** Controllers must contain zero business logic or SQL queries; Repositories must contain zero HTTP logic. Domain models encapsulate business invariants.
* **Verification:** Code reviews and architectural linters verify no cyclic dependencies exist between packages.

### NFR-MAINT-02: Strategy Pattern for Extensible Allocation
* **Requirement:** The group seating engine must support new allocation strategies without modifying client services (Open-Closed Principle).
* **Specification:** Algorithms implement the common interface `SeatAllocationStrategy`. Spring automatically injects implementations (`ContiguousSeatStrategy`, `FlexibleGroupSeatStrategy`, `AccessibleSeatStrategy`) based on runtime request parameters.

---

## 7. Usability and Accessibility (NFR-USAB)

### NFR-USAB-01: Responsive Visual Seat Grid
* **Requirement:** The interactive seat map must be legible, intuitive, and navigable across standard screen resolutions (desktop $1920\times1080$, laptop $1366\times768$, and mobile $375\times812$).
* **Specification:** Visual layout uses standard color and icon cues (e.g., Green for Available, Amber for Held, Red/Grey for Booked, Blue for Selected, Wheelchair symbol for Accessible).
* **Verification:** Tested on major browsers (Chrome, Firefox, Edge, Safari) at standard viewports.

### NFR-USAB-02: Accessibility Standards (WCAG 2.1 AA Compliance)
* **Requirement:** The application must support keyboard navigation and adequate color contrast.
* **Specification:**
  1. Contrast ratio between seat text/icons and background must meet or exceed 4.5:1.
  2. All seat buttons must have descriptive `aria-label` attributes (e.g., `Row D Seat 7, Premium, Price $15.00, Available`).
  3. Interactive elements are reachable via standard keyboard `Tab` and `Arrow` keys.

---

## 8. Performance Design Targets (NFR-PERF)

> **Important Notice:** The following numerical targets are **Design Targets** formulated to guide architecture, caching, and database indexing. They represent expected performance under typical single-node college demonstration environments and are subject to verification during testing.

| Metric Identifier | Scope / Operation | Target Value (Design Target) | Condition / Context |
| :--- | :--- | :--- | :--- |
| **NFR-PERF-01** | Show & Movie Catalog Query | $\le 200\text{ ms}$ (p95) | Database populated with 100 movies, 20 screens, 500 shows; indexed queries. |
| **NFR-PERF-02** | Interactive Seat Map Fetch | $\le 300\text{ ms}$ (p95) | Screen capacity up to 300 seats; fetching real-time show-seat statuses. |
| **NFR-PERF-03** | Seat Hold Transaction Latency | $\le 500\text{ ms}$ (p95) | Under concurrent lock acquisition for up to 10 seats per hold. |
| **NFR-PERF-04** | Group Allocation Computation | $\le 250\text{ ms}$ (p95) | Evaluating screen layouts up to $20 \times 20$ grid for party sizes 2-10. |
| **NFR-PERF-05** | Concurrent User Load | 50 concurrent active users | Single-node deployment (Spring Boot + PostgreSQL on standard local hardware). |

---

## 9. Testing, Auditability and Traceability (NFR-TEST)

### NFR-TEST-01: Domain Logic Test Coverage Target
* **Requirement:** Critical business rules must be verified with automated unit tests.
* **Specification:** Target $\ge 80\%$ line and branch coverage for service classes (`GroupSeatingService`, `BookingRecoveryService`, `WaitlistService`, `BookingStateMachine`).
* **Verification:** JaCoCo test report generated during Maven `mvn test` build phase.

### NFR-TEST-02: Deterministic Time Simulation
* **Requirement:** Time-dependent logic (hold expiration, waitlist claim windows, show starting times) must be testable without waiting real-world minutes.
* **Specification:** System services inject a configurable `java.time.Clock` bean, enabling unit tests to advance time deterministically.

### NFR-TEST-03: Audit Trail for Seat Transitions & Gate Scans
* **Requirement:** Critical operational events must be recorded in an append-only audit log.
* **Specification:** An `audit_logs` table logs event timestamp, user ID, event type (`SEAT_HELD`, `SEAT_RELEASED`, `PAYMENT_RECONCILED`, `TICKET_SCANNED`), and metadata JSON.
