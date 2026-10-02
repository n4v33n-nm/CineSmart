# CineSmart — Smart Movie Ticket Booking System
## Document 09: OOAD Principles and Design Patterns

---

### Document Control
* **Document Version:** 1.0.0
* **Target Audience:** Third-Year Computer Science Students, Academic Evaluators, Software Architects
* **Phase:** Phase 1 — Object-Oriented Analysis and Design (OOAD) Foundations

---

## 1. Core Object-Oriented Principles in CineSmart

CineSmart grounds its architecture in the fundamental tenets of Object-Oriented Analysis and Design (OOAD). Rather than treating classes as dumb data structs with getters and setters, CineSmart encapsulates behavior and state together.

```
+------------------------------------------------------------------------------------+
|                         OOAD CORE PILLARS IN CINESMART                             |
+----------------------+-------------------------------------------------------------+
| Principle            | Concrete CineSmart Implementation Example                   |
+----------------------+-------------------------------------------------------------+
| **Encapsulation**    | `ShowSeat` shields its internal `version` and `status` from |
|                      | external manipulation; state transitions occur strictly     |
|                      | through semantic methods: `hold()`, `book()`, `release()`.  |
|                      |                                                             |
| **Abstraction**      | `PaymentProvider` exposes clean contracts (`processPayment`)|
|                      | hiding network retries, HMAC headers, and JSON formatting.  |
|                      |                                                             |
| **Composition**      | `Screen` *has-a* collection of `Seat` objects; `Booking`    |
|                      | *has-a* list of `BookingSeat` line items and a `Payment`.   |
|                      |                                                             |
| **Polymorphism**     | Different allocation algorithms (`ContiguousSeatStrategy`,  |
|                      | `FlexibleGroupSeatStrategy`) are invoked interchangeably    |
|                      | via the common `SeatAllocationStrategy` interface.          |
|                      |                                                             |
| **Separation of**    | Controllers handle HTTP/REST; Services enforce transactions;|
| **Concerns**         | Repositories handle SQL; Domain models enforce invariants.  |
+----------------------+-------------------------------------------------------------+
```

### 1.1 Deep Dive: Composition Over Inheritance
A common beginner anti-pattern is creating inheritance hierarchies like `CustomerBooking extends Booking`, `GroupBooking extends Booking`, `VipBooking extends Booking`. This leads to rigid class explosion if a group booking also happens to be a VIP booking.
* **CineSmart's Approach:** Favor Composition. `Booking` represents the core monetary/seat transaction. `GroupBooking` *wraps* an existing `Booking` entity, adding collaborative coordination metadata (`shareableToken`, `groupMembers`). If a group is cancelled, the underlying booking lifecycle methods execute uniformly.

---

## 2. SOLID Design Principles Applied to CineSmart

### S — Single Responsibility Principle (SRP)
* *Rule:* A class should have one, and only one, reason to change.
* *Application:* 
  * `SeatHoldService` is solely responsible for temporary hold locks and TTL countdowns.
  * `TicketService` is solely responsible for generating cryptographically signed QR verification hashes.
  * `PaymentReconciliationService` is solely responsible for resolving ambiguous payments.
  * No "God Classes" exist.

### O — Open/Closed Principle (OCP)
* *Rule:* Software entities should be open for extension, but closed for modification.
* *Application:* When CineSmart needs to add a new seating heuristic (e.g., "Aisle-Preference Seating"), developers create `AislePreferenceSeatStrategy implements SeatAllocationStrategy` without altering a single line of existing code in `GroupSeatingService`.

### L — Liskov Substitution Principle (LSP)
* *Rule:* Subtypes must be substitutable for their base types without altering program correctness.
* *Application:* Any implementation of `SeatAllocationStrategy` (`ContiguousSeatStrategy`, `FlexibleGroupSeatStrategy`) accepts the same input parameters (`Show`, party size, tier) and guarantees the return of a valid `List<SeatRecommendation>` adhering to physical theater boundaries.

### I — Interface Segregation Principle (ISP)
* *Rule:* Clients should not be forced to depend upon interfaces that they do not use.
* *Application:* Rather than creating a gigantic `TicketingPlatformService` containing 50 methods, CineSmart defines narrow, cohesive interfaces: `SeatAllocationStrategy`, `PaymentProvider`, `BookingState`.

### D — Dependency Inversion Principle (DIP)
* *Rule:* High-level modules should not depend on low-level modules; both should depend on abstractions.
* *Application:* `BookingService` does not depend on a concrete `MockPaymentGateway` class; it depends on the `PaymentProvider` interface. Spring injects the appropriate bean at runtime.

---

## 3. Targeted Design Patterns

---

### 3.1 Strategy Pattern (Smart Seat Allocation Engine)

* **Design Pattern Type:** Behavioral
* **Context in CineSmart:** Allocating seats for a group requires different algorithmic approaches depending on group size, availability density, and customer constraints.
* **Why Applied:** Hardcoding `if-else` blocks inside `GroupSeatingService` creates tangled, unmaintainable code. Encapsulating each algorithm into its own strategy class isolates algorithmic complexity.

```
       +---------------------------------------------+
       |         <<interface>>                       |
       |    SeatAllocationStrategy                   |
       +---------------------------------------------+
       | + recommendSeats(show, partySize, tier)     |
       +---------------------------------------------+
                              ^
                              |
         +--------------------+--------------------+
         |                                         |
+--------------------------+             +--------------------------+
|  ContiguousSeatStrategy  |             | FlexibleGroupSeatStrategy|
+--------------------------+             +--------------------------+
| Finds contiguous 1-row   |             | Partitions group into    |
| blocks nearest to center |             | aligned adjacent rows    |
+--------------------------+             +--------------------------+
```

* **Participating Components:**
  1. `SeatAllocationStrategy` (Strategy Interface): Defines the uniform recommendation contract.
  2. `ContiguousSeatStrategy` (Concrete Strategy 1): Searches exclusively for contiguous single-row blocks; evaluates Euclidean distance to screen center.
  3. `FlexibleGroupSeatStrategy` (Concrete Strategy 2): Executed when contiguous blocks fail; breaks group into $N/2 + N/2$ sub-clusters across adjacent rows.
  4. `AccessibleSeatStrategy` (Concrete Strategy 3): Enforces strict wheelchair and companion seating physical spatial adjacency.
  5. `GroupSeatingService` (Context): Selects and delegates to the appropriate strategy.

---

### 3.2 State Pattern (Booking Lifecycle Management)

* **Design Pattern Type:** Behavioral
* **Context in CineSmart:** A booking progresses through complex, mutually exclusive states (`PENDING_PAYMENT`, `PAYMENT_PENDING_VERIFICATION`, `CONFIRMED`, `CANCELLED`, `EXPIRED`). Each state responds differently to events like payment callbacks, cancellations, or timer expiries.
* **Why Applied:** Eliminates error-prone conditional branching (e.g., `if (status == CONFIRMED && !isExpired)`) scattered across services. Each state class encapsulates valid transitions and actions.

```
+---------------------------------------------------------------------------------+
|                                <<interface>>                                    |
|                                 BookingState                                    |
+---------------------------------------------------------------------------------+
| + handlePaymentSuccess(context: BookingContext, payment: Payment): void         |
| + handlePaymentFailure(context: BookingContext, payment: Payment): void         |
| + cancel(context: BookingContext): void                                         |
| + expire(context: BookingContext): void                                         |
+---------------------------------------------------------------------------------+
       ^                      ^                      ^                      ^
       |                      |                      |                      |
+--------------+      +----------------+      +--------------+      +--------------+
|PendingPayment|      |PendingVerificat|      |ConfirmedState|      |CancelledState|
|    State     |      |    ionState    |      |              |      |              |
+--------------+      +----------------+      +--------------+      +--------------+
```

* **Behavioral Encapsulation:**
  * In `PendingPaymentState`: calling `cancel()` releases held seats immediately.
  * In `ConfirmedState`: calling `cancel()` checks the 2-hour showtime policy and triggers a financial refund.
  * In `CancelledState`: calling `handlePaymentSuccess()` throws an `IllegalStateException` preventing accidental double ticket issuance.

---

### 3.3 Observer / Domain Event Pattern (Decoupled Side Effects)

* **Design Pattern Type:** Behavioral
* **Context in CineSmart:** When a booking is confirmed or cancelled, multiple decoupled side effects must occur:
  * Customer must receive an email notification.
  * Real-time seat inventory must update.
  * If cancelled, the Waitlist Engine must be alerted to evaluate waiting patrons.
* **Why Applied:** Directly calling `NotificationService` and `WaitlistService` inside `BookingService` creates tight coupling. Using Spring's Application Event mechanism decouples publishers from listeners.

```
[BookingService] -- publishes --> (BookingConfirmedEvent)
                                          |
          +-------------------------------+-------------------------------+
          |                                                               |
          v                                                               v
[EmailNotificationListener]                                     [TicketIssuanceListener]
  - Formats HTML confirmation                                     - Creates QR tokens
  - Asynchronously sends email                                    - Persists Ticket rows
```

* **Spring Implementation:**
  * Publisher calls: `applicationEventPublisher.publishEvent(new BookingConfirmedEvent(bookingId));`
  * Listeners annotated with `@Async` and `@EventListener` execute without blocking the main database transaction.

---

### 3.4 Adapter Pattern (Payment Gateway Integration)

* **Design Pattern Type:** Structural
* **Context in CineSmart:** The application must interact with payment gateways without coupling the domain logic to any proprietary third-party SDK.
* **Why Applied:** In a college demonstration, simulating payment timeouts and drops is essential. An adapter allows seamless swapping between a `MockPaymentProviderAdapter` and a future live payment gateway (like Stripe or Razorpay) without altering `PaymentService`.

```
[PaymentService] ---> (PaymentProvider Interface)
                             ^
                             | implements
               [MockPaymentProviderAdapter] ---> [Simulated Gateway REST/Worker]
```

---

### 3.5 Repository Pattern (Data Access Decoupling)

* **Design Pattern Type:** Architectural / Structural
* **Context in CineSmart:** Reading and persisting entities (`ShowSeat`, `Booking`, `Payment`) across PostgreSQL database relations.
* **Why Applied:** Provided natively by **Spring Data JPA** (`JpaRepository`). It completely abstracts raw JDBC connection management, SQL generation, and result set mapping, providing type-safe methods (`findByShowIdAndStatus`, `saveAndFlush`) while supporting custom JPQL and native SQL queries with pessimistic locking (`@Lock(LockModeType.PESSIMISTIC_WRITE)`).
