# CineSmart — Smart Movie Ticket Booking System
## Document 12: Assumptions, Architectural Trade-offs, and Open Decisions

---

### Document Control
* **Document Version:** 1.0.0
* **Target Audience:** College Project Mentor, Academic Evaluation Committee, Student Authors
* **Phase:** Phase 1 — Design Review & Pre-Implementation Sign-off

---

## 1. Architectural & Operational Assumptions

The CineSmart Phase 1 design operates under explicit engineering assumptions. These assumptions establish boundary conditions so the project remains defensible, coherent, and achievable within an academic semester.

### 1.1 Temporal & Synchronization Assumptions
* **Global Time Standard:** All database timestamps (`created_at`, `expires_at`, `start_time`) are stored in **UTC** (`TIMESTAMP WITH TIME ZONE`). The React client formats timestamps into the user's local browser timezone.
* **Clock Authority:** The backend application server is the sole authority for time. Client-side timers in the React SPA are purely visual countdown projections; any hold expiry or show cutoff is strictly evaluated by the server's injected `java.time.Clock`.

### 1.2 Inventory & Auditorium Model
* **Static Grid Geometries:** Screen seating maps are configured by administrators prior to show scheduling and remain static during active ticket sales. Mid-sale seat layout alterations are prohibited.
* **Show Lifetime:** A show's bookable window opens upon scheduling by an administrator and closes automatically at `show.start_time`. No online bookings are accepted once the movie screening has commenced.

### 1.3 Financial & Payment Environment
* **Deterministic Simulation:** Real financial merchant accounts (which require legal business entities, merchant banking keys, and PCI-DSS compliance audits) are explicitly superseded by an enterprise-grade **Mock Payment Provider Adapter**. This adapter mirrors real gateway behavior, supporting configurable latencies, webhook deliveries, and edge-case response codes (`SUCCESS`, `DECLINED`, `SOCKET_TIMEOUT`).

---

## 2. Architectural Trade-offs Analyzed

Every software design decision involves balancing competing qualities. Below is the explicit engineering rationale for CineSmart's key architectural choices:

```
+------------------------------------------------------------------------------------+
| KEY ARCHITECTURAL TRADE-OFFS                                                       |
+----------------------+--------------------+----------------------------------------+
| Decision Area        | Chosen Design      | Rejected Alternative & Rationale       |
+----------------------+--------------------+----------------------------------------+
| **Concurrency &**    | Pessimistic Write  | *Rejected:* Optimistic Locking alone.  |
| **Seat Holds**       | Locks (`SELECT FOR | Under heavy ticket release rushes,     |
|                      | UPDATE`) + Partial | optimistic locking causes massive retry|
|                      | Unique Index       | storms and user frustration. Locking   |
|                      |                    | serializes contention safely at the DB.|
+----------------------+--------------------+----------------------------------------+
| **System**           | Modular Monolith   | *Rejected:* Microservices Mesh.        |
| **Topology**         | (Spring Boot 3)    | Microservices introduce distributed    |
|                      |                    | transaction overhead (Saga/2PC) that   |
|                      |                    | distracts from core OOAD evaluation.   |
+----------------------+--------------------+----------------------------------------+
| **Event Messaging**  | Spring Application | *Rejected:* Apache Kafka / RabbitMQ.   |
|                      | Events (`@Async`)  | Spring internal application events     |
|                      |                    | provide clean decoupling without       |
|                      |                    | requiring external broker infrastructure|
+----------------------+--------------------+----------------------------------------+
| **Group Payment**    | Single Organizer   | *Rejected:* Split Payment Gateway.     |
| **Model**            | Payer with Member  | Multi-party payment escrow engines     |
|                      | Itinerary Sharing  | introduce extreme complexity unsuitable|
|                      |                    | for an undergraduate project timeline. |
+----------------------+--------------------+----------------------------------------+
```

---

## 3. Open Design Decisions for Mentor Review & Sign-Off

Before commencing Phase 2 (Implementation), the following four design decisions are submitted to the college project mentor for feedback or approval:

---

### Decision 01: Guest Checkout vs. Mandatory User Registration

* **Current Design Proposal:** **Mandatory Registration**. Customers must register and authenticate via JWT before holding seats or completing bookings.
* **Alternative Option:** Allow "Guest Checkout" using only an email address and phone number without setting a password.
* **Trade-off Analysis:** 
  * *Mandatory Registration* simplifies the waitlist queue, booking history lookups, and JWT role enforcement (`ROLE_CUSTOMER`).
  * *Guest Checkout* reduces checkout friction for casual moviegoers, but complicates secure booking cancellation and waitlist claim token ownership.
* **Recommendation:** Maintain Mandatory Registration to preserve clean OOAD security models.

---

### Decision 02: Partial Waitlist Fulfillment vs. Exact Group Size Matching

* **Scenario:** A waitlisted customer requested a party size of 4 seats. Exactly 2 seats become available due to an expired hold.
* **Current Design Proposal:** **Skip & Match**. The system does *not* offer the 2 seats to the group of 4 (which would split their party or force them to decline). Instead, the system checks for the next waitlisted customer whose requested party size $\le 2$, or releases the seats if no matching size exists.
* **Alternative Option:** Offer the 2 seats to the group of 4 with a prompt: *"Only 2 seats are available. Do you wish to accept partial seating?"*
* **Trade-off Analysis:** Partial offering creates complex asynchronous dialogue and blocks the seats for 10 minutes while waiting for a response that is likely to be declined. Exact matching keeps queue progression fast, automated, and fair.
* **Recommendation:** Adopt Exact Matching (Skip and preserve queue position for larger groups).

---

### Decision 03: Refund Settlement Channel for Cancelled Shows

* **Scenario:** An administrator cancels a screening due to projector failure.
* **Current Design Proposal:** **Source Reversal Tracking**. The system logs an automated `Refund` record flagged as `REFUND_PENDING` linked to the original payment transaction ID, simulating a reversal to the customer's bank card.
* **Alternative Option:** Implement a "CineSmart In-App Wallet" where credits are stored for future bookings.
* **Trade-off Analysis:** In-app wallets require ledger accounting, balance transfer logic, and balance expiration policies. Direct refund tracking fulfills all academic requirements without unnecessary domain bloat.
* **Recommendation:** Retain Source Reversal Tracking.

---

### Decision 04: Seat Hold Duration (TTL Calibration)

* **Current Design Proposal:** **8 Minutes (480 seconds)** hold TTL, with a +3 minute extension granted solely during active payment reconciliation.
* **Discussion Point:** Should the TTL be reduced to 5 minutes (standard on high-traffic commercial flash-sales) or kept at 8 minutes?
* **Recommendation:** 8 minutes provides an ideal window during live academic project demonstrations, giving evaluators sufficient time to review screen transitions without artificial rush.

---

## 4. Phase 1 Sign-Off Checklist

```
+------------------------------------------------------------------------------------+
| PHASE 1 REVIEW CHECKLIST                                                           |
+---------------------------------------------------------------------+--------------+
| Item Description                                                    | Status       |
+---------------------------------------------------------------------+--------------+
| 1. Functional requirements prioritized and verifiable               | [X] Complete |
| 2. Non-functional design targets clearly labeled (not fake claims)  | [X] Complete |
| 3. Domain classes properly normalized (ShowSeat vs Seat vs Booking) | [X] Complete |
| 4. All 10 PlantUML source diagrams written with valid syntax        | [X] Complete |
| 5. Smart Group Seating algorithm formulated with 5 worked examples  | [X] Complete |
| 6. Relational database schema with partial unique indexes specified | [X] Complete |
| 7. Concurrency and recovery mechanisms mathematically sound         | [X] Complete |
| 8. Open decisions documented for mentor alignment                   | [X] Complete |
+---------------------------------------------------------------------+--------------+
```

---

## 5. Phase 2 Implementation Refinements & Minor Assumptions

During the Phase 2 implementation of the full-stack foundation, the following minimal, practical refinements were established to align with local development and demonstration workflows:

### 5.1 Authentication & Token Lifecycle
* **Decision:** Default JWT access token expiration is set to **24 hours (86,400,000 ms)** in `application.properties`.
* **Rationale:** Minimizes token expiry interruptions during academic demonstrations, live evaluation grading, and local development testing.

### 5.2 Test Isolation Strategy
* **Decision:** Dual database profile setup:
  * **Development & Production:** PostgreSQL via environment variables (`DB_URL`, `DB_USERNAME`, `DB_PASSWORD`) with `application-dev.properties`.
  * **Automated Unit & Integration Tests:** In-memory H2 database via `application-test.properties` configured for standard PostgreSQL dialect emulation.
* **Rationale:** Allows `mvn test` to execute autonomously and deterministically in any CI/CD environment without external database connectivity requirements.

### 5.3 Baseline Booking Transition (Pre-Payment Gateway)
* **Decision:** In Phase 2, `BookingService.createBooking()` validates seat availability using pessimistic locks (`SELECT ... FOR UPDATE`), creates a `Booking` record with status `CONFIRMED` or `PENDING_PAYMENT`, and transitions the corresponding `ShowSeat` entities to `BOOKED`.
* **Rationale:** Establishes the full end-to-end data flow (Home → Movies → Details → Seat Selection → Order Summary → Booking Confirmation) while cleanly leaving the advanced Payment Gateway Adapter, Hold TTL timers, and Booking Recovery Engine for subsequent phases.

