# CineSmart — Smart Movie Ticket Booking System
## Document 05: Use-Case Specifications

---

### Document Control
* **Document Version:** 1.0.0
* **Target Audience:** System Architects, Developers, QA Engineers, Academic Evaluators
* **Phase:** Phase 1 — Behavioral Modeling & Use-Case Analysis

---

## 1. Actor Identification and Responsibilities

```
+------------------------------------------------------------------------------------+
| ACTOR HIERARCHY & RESPONSIBILITY MATRIX                                           |
+----------------------+----------------------+--------------------------------------+
| Actor                | Type                 | Primary Responsibilities             |
+----------------------+----------------------+--------------------------------------+
| Customer             | Primary (Human)      | Browse movies/shows, view seat maps, |
|                      |                      | hold seats, pay, view history/tickets|
|                      |                      | cancel bookings, join waitlists.     |
+----------------------+----------------------+--------------------------------------+
| Group Organizer      | Primary (Specialized | Solicits smart group recommendations,|
|                      | Customer)            | coordinates group bookings, enters   |
|                      |                      | member details, initiates checkout.  |
+----------------------+----------------------+--------------------------------------+
| Administrator        | Primary (Human)      | Manages movie catalog, cinemas,      |
|                      |                      | screens, show schedules, pricing,    |
|                      |                      | and monitors operational reports.    |
+----------------------+----------------------+--------------------------------------+
| Cinema Staff         | Primary (Human)      | Validates digital ticket QR codes at |
|                      |                      | cinema entrance gates; checks entry. |
+----------------------+----------------------+--------------------------------------+
| Payment Gateway      | Supporting (External | Processes financial transactions,    |
|                      | Mock System)         | returns authorization codes/webhooks,|
|                      |                      | provides reconciliation status API.  |
+----------------------+----------------------+--------------------------------------+
| Notification Service | Supporting (Internal | Dispatches asynchronous email/in-app |
|                      | / External System)   | notices (hold expiry, waitlist offer,|
|                      |                      | booking confirmation, cancellation). |
+----------------------+----------------------+--------------------------------------+
```

---

## 2. Detailed Use-Case Specifications

Below are formal, comprehensive use-case specifications for the five core system workflows.

---

### Use Case UC-01: Browse Shows and Select Seats

* **Use Case ID:** UC-01
* **Use Case Name:** Browse Shows and Select Seats
* **Objective:** Enable a customer to find desired movie showtimes, inspect real-time seat availability on an interactive map, and select seats for purchase.
* **Primary Actor:** Customer
* **Secondary Actors:** None
* **Preconditions:**
  1. The movie catalog has active screenings scheduled by the Administrator.
  2. The customer has network access to the CineSmart web interface.

#### Main Success Flow:
1. Customer accesses the CineSmart home page and browses currently screening movies.
2. Customer filters movies by genre/language and selects a movie title.
3. System displays available cinemas, screen projection formats (2D, 3D, IMAX), and show dates/times.
4. Customer selects a specific showtime.
5. System queries the real-time inventory of `ShowSeat` records for that show and renders an interactive graphical seat matrix, color-coded by tier (Standard, Premium, Recliner, Wheelchair) and status (`AVAILABLE`, `HELD`, `BOOKED`).
6. Customer clicks on one or more `AVAILABLE` seats.
7. System highlights the selected seats, calculates the running total price including base tier pricing and estimated taxes, and activates the "Proceed to Checkout" button.
8. Customer clicks "Proceed to Checkout".
9. System transitions to the seat holding workflow (UC-03).

#### Alternative Flows:
* **AF-1.1 (Customer changes seat selection):** At step 6, customer clicks an already selected seat; system deselects it and recalculates the running price.
* **AF-1.2 (Customer prefers group recommendation):** At step 5, customer decides they want recommendations for a party of 4; customer clicks "Find Group Seats", branching into UC-02.

#### Exceptions:
* **EX-1.1 (Show is completely sold out):** At step 5, system determines all seats are `BOOKED` or `HELD`. System displays a "Sold Out" banner and provides a button: "Join Waitlist for this Show" (branches into UC-05).
* **EX-1.2 (Selected seat becomes held by another user during selection):** At step 8, another user's hold commits milliseconds prior; system displays a notification: *"Seat D8 was just held by another customer. Please choose an alternate seat."* and refreshes the seat map.

#### Postconditions:
* **Success:** Desired seats are identified and passed to the temporary hold and payment workflow.
* **Failure:** No seats are reserved; seat map reflects current database availability.

---

### Use Case UC-02: Create a Group Booking and Receive Seat Recommendations

* **Use Case ID:** UC-02
* **Use Case Name:** Create a Group Booking and Receive Seat Recommendations
* **Objective:** Automatically recommend optimal contiguous or near-contiguous seating arrangements for a group of patrons based on size, tier, and accessibility constraints.
* **Primary Actor:** Group Organizer (Specialized Customer)
* **Secondary Actors:** Notification Service
* **Preconditions:**
  1. Group Organizer is authenticated with `ROLE_CUSTOMER`.
  2. The target showtime is in the future and has at least $N$ available seats.

#### Main Success Flow:
1. Organizer navigates to the show's seat selection page and clicks "Smart Group Booking".
2. System displays a group preference modal requesting:
   - Party size $N$ (range: 2 to 10)
   - Preferred seat tier (Standard, Premium, Recliner, Any)
   - Accessibility requirement checkbox (Wheelchair/Companion required)
   - Maximum allowed row splits (e.g., contiguous only vs. up to 2 adjacent rows)
3. Organizer specifies party size $N=5$, selects "Premium", unchecks accessibility, and clicks "Find Optimal Seats".
4. System invokes `GroupSeatingService`, passing constraints to `SeatAllocationStrategy`.
5. Strategy evaluates available seat sequences across all rows, scores each candidate block based on center proximity and tier match, and returns the top 3 ranked contiguous arrangements.
6. System renders the 3 recommendations with visual thumbnails and descriptive scores (e.g., *"Option 1: Row F, Seats 6-10 — Center Screen (Score: 94/100)"*).
7. Organizer selects Option 1.
8. System highlights those 5 seats on the main seat map and automatically transitions to the seat holding workflow (UC-03).

#### Alternative Flows:
* **AF-2.1 (No contiguous block exists; system generates split-cluster recommendations):**
  - At step 5, no single row has 5 contiguous available seats in the requested tier.
  - System invokes `FlexibleGroupSeatStrategy`.
  - Strategy partitions 5 into optimal sub-clusters (e.g., 3 seats in Row E [7-9] and 2 seats in Row F [7-8] directly behind).
  - System presents these split-row alternatives labeled *"Adjacent Row Seating"* with visual indicators of member proximity.
  - Organizer accepts the split-row recommendation and proceeds to Step 8.
* **AF-2.2 (Organizer adds member coordination details):**
  - After step 7, Organizer enters names and emails for the other 4 group members to generate an itinerary share link.

#### Exceptions:
* **EX-2.1 (Insufficient total available seats in the theater):** At step 5, the total available seats in the show is less than $N$. System notifies the user: *"Sorry, this show only has 3 seats remaining. You may join the waitlist or choose a different show."*
* **EX-2.2 (Wheelchair accessibility requested but no accessible companion slots exist):** System alerts: *"No wheelchair accessible clusters matching your party size are available for this show."*

#### Postconditions:
* **Success:** Optimal seat identifiers are selected and ready for reservation without manual seat hunting.
* **Failure:** No seats are locked; user retains option to manually select seats or join waitlist.

---

### Use Case UC-03: Hold Seats and Complete Payment

* **Use Case ID:** UC-03
* **Use Case Name:** Hold Seats and Complete Payment
* **Objective:** Acquire a temporary, concurrency-safe hold on chosen seats, collect payment via mock gateway, confirm the booking, and issue digital tickets.
* **Primary Actor:** Customer
* **Secondary Actors:** Payment Gateway, Notification Service
* **Preconditions:**
  1. Customer is authenticated and has selected 1 to 10 available seats.
  2. Target showtime is active.

#### Main Success Flow:
1. Customer submits a checkout request with selected seat IDs and an `Idempotency-Key`.
2. System opens a transactional boundary, acquires pessimistic write locks (`FOR UPDATE`) on the requested `ShowSeat` records, and verifies all are currently `AVAILABLE`.
3. System transitions seats to `HELD`, creates a `SeatHold` record with an 8-minute expiration timestamp, creates a `Booking` in status `PENDING_PAYMENT`, snapshots unit prices into `BookingSeat` line items, and commits the transaction.
4. System returns HTTP 201 Created containing `bookingId`, `holdId`, total amount, and an 8-minute countdown timer.
5. Customer views the checkout page displaying the active countdown timer and enters payment details (card number, CVV, expiry).
6. Customer submits payment before the countdown expires.
7. System sends an authorization request to the `PaymentGateway`.
8. `PaymentGateway` processes the charge and returns an immediate authorization success response (`status: SUCCESS, txnRef: "MOCK-TXN-98765"`).
9. System transitions `Payment` to `COMPLETED`, `Booking` to `CONFIRMED`, and `ShowSeat` records to `BOOKED`.
10. System generates digital `Ticket` records with signed verification tokens (for QR display) and queues a confirmation email via `NotificationService`.
11. System presents the confirmation screen to the customer with ticket details, seat numbers, and scannable QR codes.

#### Alternative Flows:
* **AF-3.1 (Customer cancels checkout voluntarily):**
  - At step 5, customer clicks "Cancel and Release Seats".
  - System releases the `SeatHold`, transitions `ShowSeat` records back to `AVAILABLE`, marks `Booking` as `CANCELLED`, and redirects to show catalog.

#### Exceptions:
* **EX-3.1 (Seat contention conflict during hold acquisition):** At step 2, one or more selected seats were locked by another transaction and are no longer `AVAILABLE`. System aborts hold, rolls back transaction, and returns HTTP 409 Conflict with detail: *"One or more seats have been selected by another customer."*
* **EX-3.2 (Hold expires before customer completes payment):** At step 6, customer attempts payment after 8 minutes have elapsed. System detects hold expiration, rejects payment submission with HTTP 410 Gone, releases seats, and marks booking `EXPIRED`.
* **EX-3.3 (Payment declined by gateway):** At step 8, gateway returns `status: DECLINED`. System transitions to Recovery Workflow (UC-04).
* **EX-3.4 (Gateway timeout / dropped connection):** At step 8, communication times out. System transitions to Recovery Workflow (UC-04).

#### Postconditions:
* **Success:** Booking is `CONFIRMED`, seats are permanently `BOOKED`, tickets are issued, payment is recorded.
* **Failure:** Seats are released back to `AVAILABLE` or waitlist; no orphan locks remain.

---

### Use Case UC-04: Recover a Booking After a Payment Failure or Timeout

* **Use Case ID:** UC-04
* **Use Case Name:** Recover a Booking After a Payment Failure or Timeout
* **Objective:** Safely manage payment interruptions, prevent premature seat releases during network dropouts, reconcile ambiguous payments, and provide customer recovery paths.
* **Primary Actor:** Customer
* **Secondary Actors:** Payment Gateway, Payment Reconciliation Engine (System), Notification Service
* **Preconditions:**
  1. A `Booking` in status `PENDING_PAYMENT` has experienced an ambiguous payment outcome or a verified card decline.

#### Main Success Flow (Scenario A: Ambiguous Gateway Timeout followed by Successful Reconciliation):
1. Customer submits payment in UC-03, but the network connection to the payment gateway times out after 15 seconds.
2. The client receives an HTTP 202 Accepted response: *"Payment outcome is currently being verified with your bank. Please do not re-submit."*
3. System transitions `Booking` to `PAYMENT_PENDING_VERIFICATION` and grants a 3-minute extension to the associated `SeatHold` so seats are not released while checking.
4. The background `PaymentReconciliationEngine` initiates a status query to `PaymentGateway.verifyTransaction(txnRef)`.
5. Payment Gateway responds: *"Transaction MOCK-TXN-98765 was successful; debited at 19:42:01"*.
6. Reconciliation Engine updates `Payment` to `COMPLETED`, transitions `Booking` to `CONFIRMED`, marks `ShowSeat` records as `BOOKED`, and issues tickets.
7. System pushes a websocket/notification update to the customer's browser; page updates to "Booking Confirmed!".

#### Alternative Flow (Scenario B: Verified Card Decline with Retry Opportunity):
1. In UC-03, the payment gateway returns a definitive failure (`status: DECLINED, reason: INSUFFICIENT_FUNDS`).
2. System updates `Payment` record to `FAILED`.
3. System checks remaining hold time: 4 minutes remaining on the 8-minute hold.
4. System displays an error banner: *"Payment was declined by your bank. Your seats are held for another 4 minutes. Please try an alternative payment method."*
5. Customer selects "Pay with Alternate Card" and enters new payment details.
6. Payment succeeds; booking proceeds to confirmation (UC-03 step 9).

#### Exceptions:
* **EX-4.1 (Reconciliation verifies payment definitively failed after timeout):**
  - Gateway responds to reconciliation query with `status: FAILED`.
  - System releases `SeatHold`, marks `Booking` as `FAILED`, transitions seats to `AVAILABLE` (or dispatches to waitlist), and notifies customer.
* **EX-4.2 (Payment succeeds after seats were erroneously released due to system crash):**
  - Rare edge-case recovery: If delayed webhook arrives after seats were claimed by waitlist, system flags transaction as `OVERBOOKING_EXCEPTION_REQUIRES_REFUND`.
  - System immediately triggers an automated 100% refund via gateway and logs an alert for administrative auditing.

#### Postconditions:
* **Success:** Booking is either verified and confirmed or cleanly aborted with seats released. No customer is charged without receiving tickets or an automated refund.

---

### Use Case UC-05: Join a Waitlist and Receive a Seat Offer

* **Use Case ID:** UC-05
* **Use Case Name:** Join a Waitlist and Receive a Seat Offer
* **Objective:** Allow a customer to register for sold-out showings and receive a guaranteed, time-limited exclusive reservation offer when seats become available.
* **Primary Actor:** Customer
* **Secondary Actors:** Notification Service, Waitlist Dispatcher Engine (System)
* **Preconditions:**
  1. The target show has no available contiguous seats matching the customer's desired party size.
  2. Customer is authenticated.

#### Main Success Flow:
1. Customer attempts to view or book a sold-out show and clicks "Join Waitlist".
2. System prompts for party size (e.g., 2 seats), preferred tier, and notification preference (Email/SMS).
3. System creates a `WaitlistEntry` with status `WAITING`, recording the current timestamp, and assigns a queue position (e.g., #2 in line).
4. Sometime later, another patron cancels their 2-seat booking for that show (or an active seat hold expires).
5. The cancellation event triggers the `WaitlistDispatcherEngine`.
6. Dispatcher inspects active waitlist entries in strict chronological (FIFO) order, filtering for matching party sizes $\le$ released cluster size.
7. Customer's entry is selected. System transitions the 2 seats to status `WAITLIST_OFFERED` (preventing public booking) and creates an exclusive claim window expiring in 10 minutes.
8. System updates `WaitlistEntry` to status `OFFERED` and dispatches an immediate notification to the customer with an exclusive claim URL.
9. Customer opens the link within 10 minutes, clicks "Claim & Proceed to Checkout".
10. System converts the waitlist offer into an active `SeatHold` and redirects to the payment screen (UC-03).
11. `WaitlistEntry` transitions to `FULFILLED`.

#### Alternative Flows:
* **AF-5.1 (Customer declines or ignores offer; offer cascades to next in line):**
  - At step 8, 10 minutes elapse without the customer claiming the offer.
  - The scheduled sweeper marks the customer's `WaitlistEntry` as `EXPIRED`.
  - Dispatcher evaluates the next waitlist entry in queue and issues a new 10-minute claim offer.
* **AF-5.2 (No further waitlist entries exist):**
  - If no eligible waitlist patrons remain, the seats transition to `AVAILABLE` for public booking.

#### Exceptions:
* **EX-5.1 (Customer attempts to claim after the 10-minute window has expired):** Customer clicks claim link at minute 11. System displays: *"This claim offer has expired and has been offered to the next patron."*
* **EX-5.2 (Showtime starts within 30 minutes):** Waitlist auto-dispatching halts 30 minutes prior to showtime; any newly released seats go directly to general public walk-up sale.

#### Postconditions:
* **Success:** Waitlisted customer gains exclusive access to newly freed seats without unfair public competition.
* **Failure:** Seats cascade cleanly to the next eligible patron or general public inventory.
