# CineSmart — Smart Movie Ticket Booking System
## Document 01: Project Overview & Architectural Vision

---

### Document Control
* **Project Name:** CineSmart
* **Document Version:** 1.0.0 (Phase 1 — Analysis & Design Baseline)
* **Author / Architect:** Senior Software Architect & OOAD Specialist
* **Target Audience:** College Faculty Reviewers, Third-Year Computer Science Students, Academic Evaluators
* **Phase:** Phase 1 — Requirements Analysis, OOAD Modeling & System Architecture Specification

---

## 1. Executive Summary

**CineSmart** is a modern, full-stack movie ticket booking web application designed from the ground up using **Object-Oriented Analysis and Design (OOAD)** methodologies, robust domain-driven modeling, and industry-standard design patterns. Built upon a technology stack comprising **Java 17, Spring Boot 3, React 18, and PostgreSQL 15**, CineSmart bridges the gap between typical introductory CRUD tutorials and commercial-grade cinema reservation systems.

While contemporary ticketing platforms provide basic seat reservation capabilities, real-world ticket operations regularly suffer from three major operational friction points:
1. **Disjointed group seating**, where friends, families, or student groups struggle to manually coordinate adjacent seats across wide seat maps.
2. **Brittle payment lifecycles and orphan transactions**, where network dropped connections, payment timeouts, and temporary bank failures lead to unreleased seats or unacknowledged payments.
3. **Chaotic seat cancellations**, where released seats are randomly snapped up by bots or rapid clickers rather than being offered systematically and fairly to interested patrons on a structured waitlist.

CineSmart directly tackles these issues through three distinctive architectural subsystems without claiming unverified novelty or global uniqueness, but providing well-engineered, explainable, and academically verifiable solutions.

---

## 2. The Three Distinctive Pillars of CineSmart

```
+-----------------------------------------------------------------------------------+
|                                CINESMART PLATFORM                                 |
+-------------------------+-------------------------------+-------------------------+
|  1. Smart Group Seating |  2. Booking Recovery Engine   |  3. Fair Seat Release   |
|     & Coordination      |                               |      & Waitlist         |
+-------------------------+-------------------------------+-------------------------+
| - Contiguous clustering | - Idempotent callbacks        | - FIFO / Priority queue |
| - Adjacency evaluation  | - Active reconciliation       | - Timed claim windows   |
| - Fallback split-row    | - Auto-rollback & hold expire | - Strict show audit     |
| - Hard accessibility    | - Refund lifecycle tracking   | - Fair re-allocation    |
+-------------------------+-------------------------------+-------------------------+
```

### Pillar 1: Smart Group Seating and Coordination
* **The Problem:** In conventional reservation apps, a group coordinator must manually scan visual seat matrices, identify empty clusters of sufficient width, and repeatedly guess whether split-row seating (e.g., 3 seats in row F and 2 in row G directly in front) is viable.
* **CineSmart's Solution:** A modular strategy pattern (`SeatAllocationStrategy`) evaluates the visual screen layout. Given a requested party size $N$, viewing preferences (center, front, rear), and physical constraints (accessible companion seating), it computes optimal contiguous allocations. If contiguous blocks are unavailable, it produces scored and ranked alternative multi-cluster arrangements (e.g., $N/2 + N/2$ in adjacent rows with aligned column offsets) with deterministic tie-breaking.

### Pillar 2: Booking Recovery Engine
* **The Problem:** In distributed web applications, payment processing is inherently asynchronous and error-prone. A customer's account may be debited, but a browser crash or network timeout prevents the payment gateway callback from reaching the client app. In amateur projects, this causes either "phantom holds" (seats locked indefinitely) or premature seat release (causing double bookings when the customer's payment later reconciles).
* **CineSmart's Solution:** A formal state machine backed by Spring-managed database transactions, optimistic/pessimistic locking, idempotent webhook ingestion, and a scheduled reconciliation worker. Uncertain outcomes are tagged as `PAYMENT_PENDING_VERIFICATION` rather than failed, protecting the user's hold until automated verification with the payment provider succeeds or definitively fails.

### Pillar 3: Fair Seat Release and Waitlist Management
* **The Problem:** When high-demand movie shows sell out, cancellations inevitably occur (e.g., plans change, payment holds expire). In standard platforms, released seats return instantly to the open pool, rewarding automated scripts or lucky page refreshers.
* **CineSmart's Solution:** A show-specific priority queue (`WaitlistEntry`) tracks interested patrons. When held seats expire or a booking is cancelled, seats are held under a private `WAITLIST_OFFERED` status, and a time-bound claim window (e.g., 10 minutes) is extended exclusively to the next eligible waitlisted customer before general public release.

---

## 3. Technology Stack & Justification

| Technology Layer | Selected Tool / Framework | Justification & Architectural Relevance |
| :--- | :--- | :--- |
| **Frontend** | React 18, HTML5, Vanilla CSS, JavaScript (ES6+) | Single Page Application (SPA) offering componentized state management for dynamic seat grid rendering, countdown hold timers, and intuitive responsive user interfaces without heavy CSS framework lock-in. |
| **Backend** | Java 17 (LTS), Spring Boot 3.x | Enterprise-grade type safety, robust concurrency management, rich ecosystem for REST API construction, declarative transaction management, and first-class OOP support. |
| **Database** | PostgreSQL 15+ | Relational integrity, ACID compliance, row-level locking (`SELECT ... FOR UPDATE`), check constraints, partial indexes for active seat holds, and battle-tested concurrent transaction handling. |
| **Persistence** | Spring Data JPA / Hibernate ORM | Object-Relational Mapping (ORM) translating domain entities to database relations, repository pattern abstraction, optimistic locking (`@Version`), and automatic schema generation. |
| **Security** | Spring Security 6 with JWT | Stateless authentication, Role-Based Access Control (`ROLE_CUSTOMER`, `ROLE_ADMIN`, `ROLE_STAFF`), password hashing using BCrypt, and secure REST endpoints. |
| **Build & Dependencies** | Apache Maven 3.8+ | Standardized lifecycle management, reproducible builds, and dependency configuration via declarative `pom.xml`. |
| **Testing** | JUnit 5, Mockito, AssertJ | Comprehensive unit testing of domain services, isolation of external dependencies (e.g., payment gateway mocks), and parameterized algorithm verification. |
| **Version Control** | Git & GitHub | Distributed version control, branch protection, pull request reviews, and issue tracking. |

---

## 4. Architectural Paradigm & System Principles

CineSmart adheres strictly to a **Clean Layered Architecture** with unidirectional dependency flow:

```
+-------------------------------------------------------------+
|               Presentation Layer (React SPA)                |
+-------------------------------------------------------------+
                              | JSON / HTTPS (REST API)
                              v
+-------------------------------------------------------------+
|           API / Controller Layer (Spring Web MVC)           |
+-------------------------------------------------------------+
                              | DTOs / Method Calls
                              v
+-------------------------------------------------------------+
|         Business / Service Layer (Spring Services)          |
|  - Allocation Strategies    - Recovery & Reconciliation     |
|  - Booking Lifecycle Engine - Waitlist Priority Dispatcher  |
+-------------------------------------------------------------+
                              | Entities / Repository Calls
                              v
+-------------------------------------------------------------+
|       Data Access Layer (Spring Data JPA Repositories)      |
+-------------------------------------------------------------+
                              | SQL / ACID Transactions
                              v
+-------------------------------------------------------------+
|              Database Layer (PostgreSQL 15+)                |
+-------------------------------------------------------------+
```

### Core Design Guidelines Followed:
1. **Separation of Concerns:** Controllers handle HTTP protocols and input validation; Services coordinate business logic, transactions, and algorithms; Repositories manage SQL persistence; Domain entities encapsulate state and business invariants.
2. **Defensive Concurrency:** Never trust application-layer memory alone to prevent double bookings. All seat reservations are fortified through transactional database constraints, row locks, and optimistic versioning.
3. **Idempotency by Design:** All mutating operations (such as payment processing and seat holding) require idempotency keys to ensure network retries do not generate duplicated charges or conflicting bookings.
4. **Academically Rigorous yet Scope-Realistic:** The architecture is designed specifically for an upper-level undergraduate college project. It avoids unnecessary distributed microservice overhead (such as Kafka or Kubernetes clusters) while fully demonstrating advanced object-oriented design patterns (Strategy, State, Observer, Repository, Adapter).

---

## 5. Phase 1 Documentation Map

The analysis and design specifications are partitioned across 12 focused artifacts in the `docs/` folder:

* **[01-project-overview.md](file:///c:/Users/navee/OneDrive/Desktop/CineSmart/docs/01-project-overview.md):** Executive introduction, distinctive feature overview, stack justification, and design philosophy.
* **[02-problem-statement-and-scope.md](file:///c:/Users/navee/OneDrive/Desktop/CineSmart/docs/02-problem-statement-and-scope.md):** Formal domain problem definition, stakeholder pain points, in-scope requirements, and out-of-scope boundaries.
* **[03-functional-requirements.md](file:///c:/Users/navee/OneDrive/Desktop/CineSmart/docs/03-functional-requirements.md):** Categorized requirement catalog with Requirement IDs, priorities (MoSCoW), descriptions, and verifiable acceptance criteria.
* **[04-non-functional-requirements.md](file:///c:/Users/navee/OneDrive/Desktop/CineSmart/docs/04-non-functional-requirements.md):** Testable requirements covering security, data integrity, anti-double-booking concurrency, maintainability, and realistic design targets.
* **[05-use-case-specifications.md](file:///c:/Users/navee/OneDrive/Desktop/CineSmart/docs/05-use-case-specifications.md):** Complete specifications for key workflows (browse, group recommendations, seat hold & pay, recovery, waitlist).
* **[06-uml-diagrams/](file:///c:/Users/navee/OneDrive/Desktop/CineSmart/docs/06-uml-diagrams/):** Ten standalone, editable PlantUML (`.puml`) diagram source files covering structural, behavioral, and deployment perspectives.
* **[07-database-design.md](file:///c:/Users/navee/OneDrive/Desktop/CineSmart/docs/07-database-design.md):** Entity-Relationship design, PostgreSQL table schemas, primary/foreign keys, check constraints, partial unique indexes, and concurrency mechanisms.
* **[08-api-specification.md](file:///c:/Users/navee/OneDrive/Desktop/CineSmart/docs/08-api-specification.md):** REST API endpoints, HTTP verbs, payload schemas, security roles, and RFC 7807 error responses.
* **[09-design-patterns.md](file:///c:/Users/navee/OneDrive/Desktop/CineSmart/docs/09-design-patterns.md):** Concrete OOAD principles (SOLID) and design pattern implementations (Strategy, State, Observer, Adapter, Repository).
* **[10-algorithm-design.md](file:///c:/Users/navee/OneDrive/Desktop/CineSmart/docs/10-algorithm-design.md):** Smart Group Seating algorithm, input/output schemas, constraint models, scoring formulae, tie-breakers, pseudocode, and 5 visual worked examples.
* **[11-test-plan.md](file:///c:/Users/navee/OneDrive/Desktop/CineSmart/docs/11-test-plan.md):** Planned test suite matrix covering happy paths, edge cases, race conditions, expired holds, and recovery reconciliation.
* **[12-assumptions-and-open-decisions.md](file:///c:/Users/navee/OneDrive/Desktop/CineSmart/docs/12-assumptions-and-open-decisions.md):** Explicit system assumptions, architectural trade-offs, and open decision points requiring academic mentor confirmation.
