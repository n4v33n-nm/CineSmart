# CineSmart — Smart Movie Ticket Booking System
## Document 11: Comprehensive Test Plan & Verification Matrix

---

### Document Control
* **Document Version:** 1.0.0
* **Testing Frameworks:** JUnit 5, Mockito, AssertJ, Spring MockMvc, Testcontainers (PostgreSQL)
* **Status:** Planned Test Specifications (Phase 1 Baseline — Pre-Implementation)
* **Phase:** Phase 1 — Quality Assurance & Verification Planning

---

## 1. Testing Strategy and Philosophy

> **Explicit Quality Assurance Notice:** In accordance with the academic guidelines of Phase 1, the test cases detailed in this document are **Planned Test Specifications**. They describe the exact test fixtures, inputs, and assertion criteria that will be implemented and executed in Phase 2. No claims of executed or passed tests are made prior to code implementation.

CineSmart organizes automated testing across a three-tier test pyramid:

```
          / \
         /   \
        / E2E \       Tier 3: End-to-End & Concurrency Stress Tests
       /------- \     (Multi-threaded race conditions, simulated payment gateways)
      / Integr.  \    Tier 2: Spring Boot Integration Tests (@SpringBootTest, Testcontainers)
     /------------\   (Database transactions, JPA constraints, Spring Security filters)
    /  Unit Tests  \  Tier 1: Fast Domain Unit Tests (JUnit 5, Mockito)
   /----------------\ (Algorithm scoring, state transitions, TTL math, Mock Clock)
```

---

## 2. Master Verification Matrix

The matrix below maps critical business scenarios to formal Test Specifications:

| Test ID | Scenario Name | Category | Scope / Target Component | Priority |
| :--- | :--- | :--- | :--- | :--- |
| **TC-BKG-01** | Normal Single-Seat Booking | Functional Happy Path | `SeatHoldService`, `PaymentService` | Critical (P0) |
| **TC-GRP-01** | Contiguous Group Seating Allocation | Algorithmic Happy Path | `ContiguousSeatStrategy` | Critical (P0) |
| **TC-GRP-02** | No Contiguous Seats — Split-Row Fallback | Algorithmic Fallback | `FlexibleGroupSeatStrategy` | High (P1) |
| **TC-GRP-03** | Insufficient Total Seats in Theater | Algorithmic Boundary | `GroupSeatingService` | High (P1) |
| **TC-HOLD-01** | Temporary Seat-Hold Expiration (TTL) | Concurrency / Temporal | `SeatHoldService`, Sweeper Worker | Critical (P0) |
| **TC-CONC-01**| Simultaneous Contention for Same Seat | Concurrency / ACID | Database `FOR UPDATE` & Partial Index | Critical (P0) |
| **TC-PAY-01** | Instant Payment Success | Financial Happy Path | `PaymentService`, `BookingStateMachine`| Critical (P0) |
| **TC-PAY-02** | Payment Decline with Remaining Hold | Financial Recovery | `PaymentService` (DECLINED scenario) | High (P1) |
| **TC-REC-01** | Gateway Timeout & Async Reconciliation | Recovery Engine | `PaymentReconciliationService` | Critical (P0) |
| **TC-IDEM-01**| Duplicate Payment Callback / Idempotency | Financial Integrity | Webhook Controller, Idempotency DB | Critical (P0) |
| **TC-ADM-01** | Show Cancellation & Mass Refund Trigger | Administrative Cascade | `AdminShowService`, `PaymentService` | High (P1) |
| **TC-WAIT-01**| Fair Waitlist Queueing & FIFO Dispatch | Fair Allocation | `WaitlistService`, Dispatch Worker | High (P1) |
| **TC-WAIT-02**| Waitlist 10-Minute Claim Expiry Cascade | Fair Allocation | `WaitlistService` | High (P1) |
| **TC-SEC-01** | Unauthorized Booking & Endpoint Access | Security / RBAC | Spring Security JWT Filter | Critical (P0) |
| **TC-STAT-01**| Invalid Booking State Transitions | Behavioral Invariants | `BookingState` Implementations | High (P1) |

---

## 3. Detailed Test Specifications

---

### TC-BKG-01: Normal Single-Seat Booking Flow
* **Test ID:** TC-BKG-01
* **Objective:** Verify standard end-to-end reservation of a single available seat.
* **Preconditions:** User `customer_1` authenticated; Show 101 has seat `E-5` in `AVAILABLE` status.
* **Test Inputs:** `showId = 101`, `seatIds = [505]`, mock payment `SIMULATE_SUCCESS`.
* **Execution Steps:**
  1. Submit `POST /api/v1/seat-holds` with seat `505`.
  2. Verify HTTP 201 Created and hold token returned.
  3. Submit `POST /api/v1/payments/process` with valid booking and hold IDs.
* **Expected Results:**
  1. `ShowSeat` status transitions: `AVAILABLE` $\rightarrow$ `HELD` $\rightarrow$ `BOOKED`.
  2. `Booking` status is `CONFIRMED`.
  3. Single `Ticket` issued with non-null `ticketCode` and `qrVerificationHash`.
* **Priority:** Critical (P0)

---

### TC-CONC-01: Simultaneous Attempts to Reserve the Same Seat (Race Condition)
* **Test ID:** TC-CONC-01
* **Objective:** Prove that multi-threaded concurrent requests for the exact same seat never result in a double booking.
* **Preconditions:** Show 101 has seat `E-7` in `AVAILABLE` status.
* **Test Inputs:** 50 concurrent threads simultaneously submitting hold requests for seat `E-7` using unique user IDs.
* **Execution Steps:**
  1. Initialize `CountDownLatch(50)` and `ExecutorService` with 50 threads.
  2. Release all 50 threads at the exact same millisecond against `POST /api/v1/seat-holds`.
  3. Await all thread completions and inspect database state.
* **Expected Results:**
  1. Exactly **1 request** receives HTTP 201 Created.
  2. Exactly **49 requests** receive HTTP 409 Conflict.
  3. In table `seat_holds`, exactly 1 record exists with `status = 'ACTIVE'`.
  4. In table `show_seats`, seat `E-7` has `status = 'HELD'` and optimistic `version = 1`.
* **Priority:** Critical (P0)

---

### TC-HOLD-01: Seat-Hold Expiration and Background Sweep
* **Test ID:** TC-HOLD-01
* **Objective:** Verify that held seats are automatically unlocked when the 8-minute TTL elapses without payment.
* **Preconditions:** Seat `F-8` held at $T_0$ with expiration at $T_0 + 8\text{ min}$.
* **Test Inputs:** Simulated clock advances to $T_0 + 8\text{ min} + 1\text{ sec}$.
* **Execution Steps:**
  1. Advance mock `java.time.Clock` by 481 seconds.
  2. Trigger scheduled worker `SeatHoldCleanupTask.sweepExpiredHolds()`.
  3. Query status of seat `F-8` and associated `SeatHold`.
* **Expected Results:**
  1. `SeatHold.status` transitions from `ACTIVE` to `EXPIRED`.
  2. `ShowSeat.status` transitions from `HELD` back to `AVAILABLE`.
  3. Attempting to submit payment with expired `holdId` returns HTTP 410 Gone.
* **Priority:** Critical (P0)

---

### TC-REC-01: Payment Gateway Timeout Followed by Successful Reconciliation
* **Test ID:** TC-REC-01
* **Objective:** Verify that an ambiguous payment outcome does not release seats prematurely and confirms automatically upon verification.
* **Preconditions:** User holds seats `D-1, D-2`; Gateway configured to simulate read timeout.
* **Test Inputs:** Payment request with `mockScenario = 'SIMULATE_TIMEOUT'`.
* **Execution Steps:**
  1. Client submits payment; gateway throws `SocketTimeoutException` after 15 seconds.
  2. Verify API returns HTTP 202 Accepted (`status = PAYMENT_PENDING_VERIFICATION`).
  3. Verify `SeatHold.expires_at` is extended by 3 minutes.
  4. Gateway mock status is flipped to `SUCCESS`.
  5. Trigger `PaymentReconciliationWorker.reconcilePendingPayments()`.
* **Expected Results:**
  1. Reconciler fetches status from gateway and identifies successful debit.
  2. `Payment.status` transitions to `SUCCESS`.
  3. `Booking.status` transitions to `CONFIRMED`.
  4. `ShowSeat` records transition to `BOOKED`.
  5. Tickets are issued; no duplicate hold or charge is generated.
* **Priority:** Critical (P0)

---

### TC-IDEM-01: Duplicate Payment Callback / Idempotency Token
* **Test ID:** TC-IDEM-01
* **Objective:** Ensure duplicate webhook deliveries or repeated customer clicks do not double-bill or issue duplicate tickets.
* **Preconditions:** Booking 201 has `Payment` in progress with idempotency key `"IDEM-ABC-123"`.
* **Test Inputs:** Three sequential identical HTTP POST requests with the same `Idempotency-Key: IDEM-ABC-123`.
* **Execution Steps:**
  1. Send Request 1 (completes successfully with HTTP 200).
  2. Send Request 2 with identical body and header.
  3. Send Request 3 with identical body and header.
* **Expected Results:**
  1. Requests 2 and 3 return cached HTTP 200 responses immediately.
  2. Payment provider adapter is invoked exactly **once**.
  3. Exactly one `Payment` record and one set of tickets exist in database.
* **Priority:** Critical (P0)

---

### TC-WAIT-01: Fair Waitlist Eligibility and Exclusive 10-Minute Claim Offer
* **Test ID:** TC-WAIT-01
* **Objective:** Verify FIFO dispatch and exclusive claim reservation when sold-out seats are cancelled.
* **Preconditions:** Show 101 sold out; User A joins waitlist at 10:00 (party size 2); User B joins at 10:05 (party size 2).
* **Test Inputs:** Existing customer cancels booking containing 2 adjacent seats in Row F.
* **Execution Steps:**
  1. Process cancellation for Show 101.
  2. `WaitlistDispatcher` processes released seats.
  3. Inspect waitlist statuses for User A and User B.
* **Expected Results:**
  1. User A (FIFO #1) receives offer: `WaitlistEntry.status = OFFERED`, `claimToken` generated, `offerExpiresAt = NOW() + 10 min`.
  2. User B remains in `WAITING` status (#1 in line).
  3. Released seats in `show_seats` transition to `WAITLIST_OFFERED` (not `AVAILABLE`), preventing public walk-up snatching.
* **Priority:** High (P1)

---

### TC-STAT-01: Prevention of Invalid State Transitions
* **Test ID:** TC-STAT-01
* **Objective:** Verify that `BookingStateMachine` rejects illegal operations in terminal states.
* **Preconditions:** Booking 301 is in terminal `CANCELLED` status.
* **Test Inputs:** Invoking `bookingService.confirmBooking(301)`.
* **Execution Steps:**
  1. Attempt to force-confirm an already cancelled booking.
* **Expected Results:**
  1. System throws `IllegalStateTransitionException`.
  2. Database rollback occurs; status remains `CANCELLED`.
* **Priority:** High (P1)
