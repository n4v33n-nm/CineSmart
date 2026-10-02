# CineSmart — Smart Movie Ticket Booking System
## Document 02: Problem Statement and Scope

---

### Document Control
* **Document Version:** 1.0.0
* **Target Audience:** Academic Review Panel, Student Developers
* **Phase:** Phase 1 — Requirements and System Modeling

---

## 1. Domain Context and Background

Commercial movie exhibition is a high-volume, time-critical retail industry characterized by sharp demand peaks, perishable inventory (an empty seat during a showtime represents zero recoverable value), and concurrent customer contention for prime seating locations. 

With the widespread adoption of online booking platforms, customers expect frictionless seat selection, real-time availability displays, and instantaneous booking confirmations. However, standard booking systems in the market and typical college demonstration projects frequently exhibit structural design flaws when subjected to edge cases:

1. **Static and Tedious Group Seat Selection:** When a group of 3 to 10 friends, colleagues, or family members attempts to book tickets, the group organizer is forced to manually pan across large seat maps, hunting for contiguous blocks. If a contiguous block is unavailable in their preferred tier, organizers must guess whether split-row seating (e.g., 3 seats in Row D and 3 seats directly behind in Row E) provides an acceptable viewing experience. In commercial apps, this results in high checkout abandonment.
2. **The "Payment Timeout Void" (Transaction Inconsistency):** Network connections on mobile devices and browsers are prone to dropouts. When a payment gateway takes 30 seconds to complete a banking handshake or when a customer closes their browser before the redirect completes, the reservation is often dropped as "failed" on the merchant server even though the customer's bank account was debited. Reversing this requires manual customer support intervention.
3. **The "First-to-Click" Cancellation Race:** When popular blockbuster showings sell out, high-demand seats occasionally become available due to cancellations or expired carts. In conventional platforms, these seats are tossed back into the general public pool without warning, creating an unfair race where automated scripts or random page refreshers claim the seats, leaving genuine patrons with no orderly recourse.

---

## 2. Core Problem Statement

> **How can an object-oriented, transactional cinema ticketing system provide automated, constraint-aware group seat recommendations, maintain robust ACID data consistency across uncertain payment lifecycles without orphan holds or double bookings, and manage fair, transparent seat redistribution for waitlisted patrons within an explainable and maintainable architecture?**

---

## 3. Project Scope Specification

To ensure a viable, academically rigorous, and defensible scope for a third-year computer science project, CineSmart defines strict boundaries between functional commitments and deliberate exclusions.

### 3.1 In-Scope Capabilities

```
+---------------------------------------------------------------------------------+
|                                 IN-SCOPE SCOPE                                  |
+---------------------------------------------------------------------------------+
| User Management:                                                                |
|   - Customer registration, login (JWT), password hashing (BCrypt), profiles     |
|   - Role-based authorization: ROLE_CUSTOMER, ROLE_STAFF, ROLE_ADMIN             |
|                                                                                 |
| Catalog & Show Browsing:                                                        |
|   - Search and browse movies by title, genre, language, and rating              |
|   - Cinema, screen, and showtime schedules with date/time filtering             |
|                                                                                 |
| Seating & Reservation Core:                                                     |
|   - Interactive visual seat matrix (Standard, Premium, Recliner, Wheelchair)    |
|   - Real-time seat status tracking: AVAILABLE, HELD, BOOKED, BLOCKED            |
|   - Concurrency-safe temporary seat holding with an 8-minute TTL                 |
|                                                                                 |
| Smart Group Seating Engine:                                                     |
|   - Party size (1-10), viewing tier preference, and accessibility constraints  |
|   - Automated contiguous block detection with center-screen proximity scoring   |
|   - Alternative split-row recommendation when contiguous blocks are unavailable |
|   - Scored, ranked arrangement suggestions with deterministic tie-breaking      |
|                                                                                 |
| Financial & Booking Recovery Engine:                                            |
|   - Mock payment integration simulating SUCCESS, VERIFIED_FAILURE, and TIMEOUT  |
|   - Idempotent payment webhook and callback processing                          |
|   - Background reconciliation worker resolving pending/ambiguous payments       |
|   - Automatic release of expired holds and full refund tracking for cancellations|
|                                                                                 |
| Fair Waitlist & Seat Reallocation:                                              |
|   - Join show-specific waitlist when showing is sold out or lacks desired seats |
|   - Priority ordering (timestamp FIFO and requested group size matching)       |
|   - Exclusive claim window (10 minutes) notification before public release      |
|                                                                                 |
| Cinema Administration & Validation:                                             |
|   - Admin CRUD for Movies, Cinemas, Screens, Seats, and Show schedules          |
|   - Cinema staff QR code scanner simulator for digital ticket entry validation  |
+---------------------------------------------------------------------------------+
```

### 3.2 Out-of-Scope Capabilities (Deliberate Architectural Boundaries)

| Out-of-Scope Feature | Rationalization for Exclusion in College Capstone Project |
| :--- | :--- |
| **Real PCI-DSS Payment Gateway** | Real payment processing (e.g., live Stripe/Razorpay merchant accounts) requires live business registration, SSL compliance, and real currency risk. A high-fidelity Mock Payment Adapter simulating real latency, callbacks, and failure modes is architecturally superior for academic evaluation. |
| **Physical Turnstile Hardware** | CineSmart models the verification domain through a software Staff Ticket Validation interface (QR token verification) rather than integrating physical hardware gates. |
| **Dynamic Surge Pricing (ML)** | Complex machine-learning dynamic pricing algorithms divert focus from core OOAD principles. Seat pricing is determined deterministically by seat tier (e.g., Standard vs. VIP) and showtime slot. |
| **Native Mobile Applications** | Creating native iOS (Swift) and Android (Kotlin) apps would duplicate presentation logic. A fully responsive React Single Page Application (SPA) accessible on desktop and mobile browsers fulfills all requirements. |
| **Microservice Mesh Deployment** | Over-engineering the project into distributed microservices introduces DevOps complexity (distributed transactions, service discovery) that obscures core OOAD class design and relational integrity. CineSmart uses a modular monolithic Spring Boot service with clean hexagonal boundaries. |

---

## 4. Stakeholder Analysis

```
+-------------------------------------------------------------------------------+
| STAKEHOLDER MATRIX                                                            |
+--------------------+----------------------------------------------------------+
| Stakeholder        | Key Concerns & Goals                                     |
+--------------------+----------------------------------------------------------+
| Customer           | Intuitive seat selection, rapid booking, clear pricing,  |
|                    | transparent refund tracking, zero risk of double charge. |
|                    |                                                          |
| Group Organizer    | Effortless coordination of multi-person seating without  |
|                    | manual seat-by-seat searching or splitting friends apart.|
|                    |                                                          |
| Cinema Staff       | Fast, verifiable ticket scanning at cinema hall entrance;|
|                    | real-time detection of duplicate or fraudulent tickets.  |
|                    |                                                          |
| Cinema Admin       | Easy scheduling of movies, managing screen capacities,   |
|                    | monitoring seat occupancy and show revenue reports.      |
|                    |                                                          |
| Academic Evaluator | Clean OOAD modeling, design pattern justifications,     |
|                    | verifiable concurrency control, and academic integrity.  |
+--------------------+----------------------------------------------------------+
```

---

## 5. Architectural Distinction from Commercial Systems

CineSmart does not claim unverified global novelty. Instead, its distinctiveness stems from an **academically transparent, student-explainable, and mathematically verifiable implementation** of features that are typically hidden proprietary secrets in commercial platforms:

1. **White-Box Group Allocation:** Commercial engines often act as black boxes. CineSmart articulates a clear, parameterized scoring function combining Euclidean distance to screen center, row penalty weights, and cluster dispersion metrics with full tie-breaking transparency.
2. **Defensive Two-Phase Holding with Active Reconciliation:** Rather than relying on fragile client-side timers, CineSmart implements a server-authoritative state machine with database-level row locking (`FOR UPDATE`) and an asynchronous scheduled sweeper that reconciles unverified payment webhooks before releasing seats.
3. **Transparent Priority Waitlist Dispatching:** Instead of selling seats to whoever refreshes fastest, CineSmart implements an equitable, audit-logged queue that guarantees fairness through reserved time-bound claim invitations.
