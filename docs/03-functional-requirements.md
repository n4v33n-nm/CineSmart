# CineSmart — Smart Movie Ticket Booking System
## Document 03: Functional Requirements

---

### Document Control
* **Document Version:** 1.0.0
* **Priority Standard:** MoSCoW Framework (Must-Have, Should-Have, Could-Have / Optional)
* **Phase:** Phase 1 — Requirements Specification

---

## 1. Requirement Classification Framework

Functional requirements are prioritized according to the **MoSCoW** convention:
* **Must-Have (M):** Critical core functionality without which the system cannot function or be evaluated as a valid ticketing system.
* **Should-Have (S):** Essential capabilities representing CineSmart's distinctive features (Group Seating, Recovery Engine, Fair Waitlist) that elevate the system above a basic CRUD project.
* **Could-Have / Optional (C):** Value-add enhancements that enrich the demonstration if time permits, but whose absence does not compromise the core architecture.

---

## 2. Module 1: User Management & Authentication (FR-AUTH)

### FR-AUTH-01: Customer Registration
* **Priority:** Must-Have (M)
* **Description:** The system shall allow new customers to create an account by providing an email address, password, full name, and phone number.
* **Acceptance Criteria:**
  1. The system must validate email syntax and reject existing email registrations with HTTP 409 Conflict.
  2. Passwords must be hashed using BCrypt (work factor $\ge 10$) before database persistence.
  3. The default assigned role must be `ROLE_CUSTOMER`.

### FR-AUTH-02: User Authentication & JWT Issuance
* **Priority:** Must-Have (M)
* **Description:** Registered users (Customers, Staff, Administrators) shall authenticate using their registered email and password to receive a signed JSON Web Token (JWT).
* **Acceptance Criteria:**
  1. Valid credentials return an HTTP 200 containing a signed JWT token with expiry (e.g., 24 hours), user ID, and assigned roles.
  2. Invalid credentials return HTTP 401 Unauthorized without distinguishing whether email or password was incorrect.
  3. Subsequent authenticated API requests must include the JWT in the `Authorization: Bearer <token>` header.

### FR-AUTH-03: User Profile & Role-Based Access Control
* **Priority:** Must-Have (M)
* **Description:** The system shall restrict API endpoints based on user roles (`ROLE_CUSTOMER`, `ROLE_STAFF`, `ROLE_ADMIN`).
* **Acceptance Criteria:**
  1. Authenticated customers can view and update their own name, phone number, and booking history.
  2. Unauthorized attempts to access administrative endpoints return HTTP 403 Forbidden.

---

## 3. Module 2: Catalog, Venues & Show Scheduling (FR-CAT)

### FR-CAT-01: Movie Catalog Browsing & Search
* **Priority:** Must-Have (M)
* **Description:** Customers can browse currently screening and upcoming movies, with filtering by genre, language, title, and censor rating.
* **Acceptance Criteria:**
  1. System returns an active list of movies with poster URLs, synopsis, duration, language, genre, and age certification.
  2. Search queries support case-insensitive partial substring matching on movie titles.

### FR-CAT-02: Cinema, Screen & Showtime Discovery
* **Priority:** Must-Have (M)
* **Description:** Customers can view available cinemas, screens, and scheduled showtimes for a selected movie and date.
* **Acceptance Criteria:**
  1. Shows are grouped by Cinema and Screen, displaying show start time, end time, and audio/projection format (e.g., 2D, 3D, IMAX).
  2. Shows whose start time is in the past cannot be booked.

---

## 4. Module 3: Seat Map, Availability & Temporary Holds (FR-SEAT)

### FR-SEAT-01: Interactive Seat Map Display
* **Priority:** Must-Have (M)
* **Description:** The system shall render a graphical representation of the screen's seating layout for a specific showtime.
* **Acceptance Criteria:**
  1. The seat layout displays row identifiers (e.g., A-J), column numbers (e.g., 1-15), seat tiers (Standard, Premium, Recliner, Wheelchair Accessible), and unit prices.
  2. Each seat visually indicates its real-time status: `AVAILABLE`, `HELD`, `BOOKED`, or `BLOCKED` (maintenance).

### FR-SEAT-02: Concurrency-Safe Temporary Seat Holding
* **Priority:** Must-Have (M)
* **Description:** When a customer selects available seats and initiates checkout, the system shall place a temporary hold on those seats for a duration of 8 minutes (480 seconds).
* **Acceptance Criteria:**
  1. The hold operation must be atomic within a database transaction using row-level locking (`SELECT ... FOR UPDATE`) or atomic conditional update.
  2. If any selected seat is already `HELD` or `BOOKED`, the entire hold request must fail with HTTP 409 Conflict.
  3. A successful hold returns a unique `holdId` and a countdown expiration timestamp.
  4. While held, the seats appear as unavailable to all other concurrent users.

### FR-SEAT-03: Automatic Seat Hold Expiration
* **Priority:** Must-Have (M)
* **Description:** Held seats that are not confirmed through successful payment within the 8-minute TTL must be automatically released back to the system.
* **Acceptance Criteria:**
  1. A background scheduled task sweeps expired holds every 30 seconds.
  2. In addition, an on-demand "lazy expiration" check evaluates hold timestamps during seat map fetches to prevent stale displays.
  3. Released seats transition from `HELD` to `WAITLIST_PENDING` (if waitlisted entries exist) or directly to `AVAILABLE`.

---

## 5. Module 4: Smart Group Seating & Recommendation Engine (FR-GRP)

### FR-GRP-01: Contiguous Group Seat Allocation
* **Priority:** Should-Have (S)
* **Description:** Given a group size $N$ ($2 \le N \le 10$), a show ID, and a preferred tier, the system shall compute and recommend the best contiguous seat block in a single row.
* **Acceptance Criteria:**
  1. Evaluates all valid contiguous sequences of length $N$ within available seats in each row.
  2. Ranks candidate blocks using a deterministic scoring function that rewards proximity to screen center and matches preferred tier.
  3. Returns the top 3 ranked contiguous seat recommendations to the client.

### FR-GRP-02: Alternative Seating Arrangements (Split Clusters)
* **Priority:** Should-Have (S)
* **Description:** If no single contiguous block of size $N$ is available, the system shall generate alternative split-cluster recommendations (e.g., $N/2 + N/2$ in adjacent rows or same row split across an aisle).
* **Acceptance Criteria:**
  1. The system partitions $N$ into minimal clusters (e.g., split into 2 clusters rather than 4).
  2. The recommendation algorithm prefers directly aligned column positions across adjacent rows (e.g., Row E seats 5-7 and Row F seats 5-7) to preserve group proximity.
  3. Every alternative recommendation is presented with a clear visual preview and an explanatory score badge (e.g., "Adjacent Rows - Aligned").

### FR-GRP-03: Accessibility Hard Constraints
* **Priority:** Must-Have (M)
* **Description:** Wheelchair-accessible seating must be enforced as a hard requirement and never assigned to non-accessible groups unless explicitly designated as companion seating.
* **Acceptance Criteria:**
  1. If an organizer requests accessible seating, only configurations containing certified wheelchair spaces and adjacent companion seats are returned.
  2. Non-accessible group requests must never automatically consume wheelchair spaces if standard seating exists.

### FR-GRP-04: Group Booking Entity & Member Coordination
* **Priority:** Could-Have (C)
* **Description:** An organizer can designate a booking as a "Group Booking", record member names/emails, and share an itinerary link.
* **Acceptance Criteria:**
  1. Group booking stores an organizer reference and a collection of member names/emails.
  2. Group members can view the read-only booking itinerary and seat assignments via a shared token link.

---

## 6. Module 5: Booking Lifecycle & Ticketing (FR-BKG)

### FR-BKG-01: Booking Creation & Snapshot Pricing
* **Priority:** Must-Have (M)
* **Description:** A booking record is initialized upon placing a seat hold, locking in the ticket prices at the time of reservation.
* **Acceptance Criteria:**
  1. A `Booking` record is created in status `PENDING_PAYMENT`, associated with the user, showtime, and specific `BookingSeat` line items.
  2. The unit price and applicable taxes/fees are copied into `BookingSeat` to prevent historical price mutation if catalog prices change later.

### FR-BKG-02: Booking Confirmation & Digital Ticket Generation
* **Priority:** Must-Have (M)
* **Description:** Upon verified payment receipt, the booking transitions to `CONFIRMED`, and digital tickets with cryptographically signed QR tokens are issued.
* **Acceptance Criteria:**
  1. Associated `ShowSeat` entities transition irreversibly from `HELD` to `BOOKED`.
  2. A digital `Ticket` record is created per seat, containing a unique alphanumeric ticket code and a verification hash (for QR generation).
  3. Confirmation notification is queued for the user.

### FR-BKG-03: Customer Booking Cancellation
* **Priority:** Should-Have (S)
* **Description:** Customers can cancel a confirmed booking up to 2 hours before the scheduled showtime.
* **Acceptance Criteria:**
  1. Cancellations requested $\ge 2$ hours prior to showtime transition booking status to `CANCELLED` and initiate a refund record.
  2. Associated seats are released and dispatched to the waitlist engine.
  3. Cancellation attempts $< 2$ hours before showtime are rejected with HTTP 422 Unprocessable Entity.

### FR-BKG-04: Customer Booking History
* **Priority:** Must-Have (M)
* **Description:** Authenticated customers can query their active and historical bookings.
* **Acceptance Criteria:**
  1. Returns paginated list of bookings sorted chronologically by show date.
  2. Includes movie details, theater, screen, seat numbers, total price, and booking status.

---

## 7. Module 6: Payment Processing, Recovery & Reconciliation (FR-PAY)

### FR-PAY-01: Mock Payment Processing
* **Priority:** Must-Have (M)
* **Description:** The system provides a mock payment provider simulating realistic payment outcomes: `SUCCESS`, `DECLINED`, and `NETWORK_TIMEOUT`.
* **Acceptance Criteria:**
  1. Client sends a payment submission request containing `holdId`, payment method details, and an `Idempotency-Key`.
  2. Immediate successful responses transition payment to `SUCCESS` and trigger booking confirmation.
  3. Immediate card declines transition payment to `FAILED` and release held seats immediately.

### FR-PAY-02: Idempotent Webhook & Duplicate Callback Ingestion
* **Priority:** Must-Have (M)
* **Description:** The payment webhook endpoint must safely handle duplicate delivery of gateway callbacks.
* **Acceptance Criteria:**
  1. The system checks `Payment.gatewayTransactionId` or idempotency token.
  2. If a callback for an already `COMPLETED` payment is received, the system returns HTTP 200 OK without re-executing ticket issuance or database mutations.

### FR-PAY-03: Uncertain Payment Recovery & Reconciliation Engine
* **Priority:** Should-Have (S)
* **Description:** When a payment attempt times out or results in an ambiguous status, the system must not assume failure and release seats prematurely.
* **Acceptance Criteria:**
  1. Ambiguous transactions enter status `PAYMENT_PENDING_VERIFICATION`, extending the seat hold by an additional grace period (e.g., 3 minutes).
  2. A background `PaymentReconciliationService` queries the mock payment provider API every 60 seconds for terminal status.
  3. If verified as `SUCCESS`, the booking is confirmed. If verified as `FAILED`, the hold is released.

### FR-PAY-04: Show Cancellation & Bulk Refund Tracking
* **Priority:** Should-Have (S)
* **Description:** In the event an administrator cancels an entire show (e.g., technical failure), all confirmed bookings are cancelled and refunds recorded.
* **Acceptance Criteria:**
  1. All confirmed bookings for the show transition to `CANCELLED_BY_CINEMA`.
  2. Automatic `Refund` records in status `REFUND_PENDING` are generated for the full paid amount.
  3. All seats are transitioned to `BLOCKED`.

---

## 8. Module 7: Fair Seat Release & Waitlist Management (FR-WAIT)

### FR-WAIT-01: Waitlist Registration
* **Priority:** Should-Have (S)
* **Description:** When a desired showtime is sold out or has insufficient contiguous seats, customers can join a waitlist by specifying required party size and tier.
* **Acceptance Criteria:**
  1. Creates a `WaitlistEntry` with status `WAITING` and record timestamp.
  2. A customer cannot join the waitlist multiple times for the exact same showtime.

### FR-WAIT-02: Seat Release Trigger & Fair Eligibility Matching
* **Priority:** Should-Have (S)
* **Description:** When seats are released due to booking cancellation or expired hold, the waitlist engine determines the next eligible patron.
* **Acceptance Criteria:**
  1. Evaluates waitlist entries in strict First-In, First-Out (FIFO) chronological order.
  2. Matches candidate entries whose requested party size $\le$ the number of released adjacent seats.
  3. Skips entries that do not fit the available seat cluster to prevent blocking smaller or larger matching parties.

### FR-WAIT-03: Exclusive Seat Claim Window
* **Priority:** Should-Have (S)
* **Description:** The matched waitlist customer is granted a private 10-minute claim reservation window.
* **Acceptance Criteria:**
  1. Seats transition to `WAITLIST_OFFERED` status, inaccessible to the general public.
  2. Notification is dispatched with a unique claim link and countdown expiration.
  3. If the customer does not claim within 10 minutes, the entry transitions to `EXPIRED`, and the offer advances to the next eligible waitlisted customer.
  4. If no waitlist entries match, seats are released to `AVAILABLE` for the general public.

---

## 9. Module 8: Administration & Staff Validation (FR-ADM)

### FR-ADM-01: Movie & Venue Management (CRUD)
* **Priority:** Must-Have (M)
* **Description:** Administrators (`ROLE_ADMIN`) can manage movies, cinemas, screens, and seat layouts.
* **Acceptance Criteria:**
  1. Admin can create, read, update, and soft-delete movie catalog entries.
  2. Admin can configure cinema screens and define seat layouts (row, column, tier, accessibility flag).

### FR-ADM-02: Show Scheduling & Pricing Configuration
* **Priority:** Must-Have (M)
* **Description:** Administrators can schedule movie showtimes on screens and assign base pricing per tier.
* **Acceptance Criteria:**
  1. Prevents overlapping show schedules on the same screen (validates start and end times including buffer cleaning intervals).
  2. Automatically instantiates `ShowSeat` inventory matching screen configuration upon show creation.

### FR-ADM-03: Cinema Staff QR Ticket Validation
* **Priority:** Must-Have (M)
* **Description:** Cinema gate staff (`ROLE_STAFF`) can validate digital tickets by scanning or entering the ticket QR verification code.
* **Acceptance Criteria:**
  1. Validates that the ticket exists, belongs to a show starting today, and is in status `ISSUED`.
  2. Upon valid scan, ticket status changes to `USED`, recording timestamp and validating staff ID.
  3. Repeated scanning of an already `USED` ticket returns an immediate alert: `TICKET_ALREADY_USED` with previous scan timestamp.
