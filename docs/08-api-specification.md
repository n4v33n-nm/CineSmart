# CineSmart — Smart Movie Ticket Booking System
## Document 08: REST API Specifications

---

### Document Control
* **Document Version:** 1.0.0
* **API Style:** RESTful JSON over HTTPS
* **Error Standard:** RFC 7807 Problem Details for HTTP APIs
* **Phase:** Phase 1 — Interface Architecture & API Contracts

---

## 1. Global API Conventions

### 1.1 Base URL & Content Negotiation
* **Base URL:** `/api/v1`
* **Headers Required:**
  * `Content-Type: application/json`
  * `Accept: application/json`
  * `Authorization: Bearer <JWT>` (for protected endpoints)
  * `Idempotency-Key: <UUID>` (mandatory for mutating payment and hold requests)

### 1.2 Standard Error Response Format (RFC 7807)
All non-$2xx$ error responses return a standardized JSON structure:
```json
{
  "type": "https://cinesmart.app/errors/seat-conflict",
  "title": "Seat Hold Conflict",
  "status": 409,
  "detail": "One or more requested seats [E5, E6] are already held or booked by another customer.",
  "instance": "/api/v1/seat-holds",
  "timestamp": "2026-10-02T19:54:00Z"
}
```

---

## 2. Authentication & Profile Endpoints (`/api/v1/auth`)

### 2.1 Customer Registration
* **Method & Endpoint:** `POST /api/v1/auth/register`
* **Purpose:** Register a new customer account.
* **Auth Requirement:** Public
* **Request Body:**
```json
{
  "email": "customer@example.com",
  "password": "SecurePassword123!",
  "fullName": "Jane Doe",
  "phoneNumber": "+1-555-0199"
}
```
* **Success Response (`201 Created`):**
```json
{
  "userId": 101,
  "email": "customer@example.com",
  "fullName": "Jane Doe",
  "role": "ROLE_CUSTOMER",
  "createdAt": "2026-10-02T19:50:00Z"
}
```
* **Error Responses:** `400 Bad Request` (validation), `409 Conflict` (email already registered).

### 2.2 User Login
* **Method & Endpoint:** `POST /api/v1/auth/login`
* **Purpose:** Authenticate and obtain JWT token.
* **Auth Requirement:** Public
* **Request Body:**
```json
{
  "email": "customer@example.com",
  "password": "SecurePassword123!"
}
```
* **Success Response (`200 OK`):**
```json
{
  "accessToken": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
  "tokenType": "Bearer",
  "expiresInSeconds": 86400,
  "user": {
    "id": 101,
    "email": "customer@example.com",
    "fullName": "Jane Doe",
    "role": "ROLE_CUSTOMER"
  }
}
```
* **Error Responses:** `401 Unauthorized` (invalid credentials).

---

## 3. Catalog, Venues & Shows Endpoints (`/api/v1/catalog`)

### 3.1 Browse Movies
* **Method & Endpoint:** `GET /api/v1/movies`
* **Purpose:** List movies currently screening with query filters.
* **Auth Requirement:** Public
* **Query Parameters:** `genre` (string, optional), `language` (string, optional), `search` (string, optional).
* **Success Response (`200 OK`):**
```json
[
  {
    "id": 1,
    "title": "Interstellar Odyssey",
    "synopsis": "A voyage across the cosmos...",
    "durationMinutes": 169,
    "language": "English",
    "genre": "Sci-Fi",
    "ageRating": "PG-13",
    "posterUrl": "https://assets.cinesmart.app/posters/interstellar.jpg"
  }
]
```

### 3.2 View Screenings for a Movie
* **Method & Endpoint:** `GET /api/v1/shows`
* **Purpose:** List scheduled showtimes for a movie.
* **Auth Requirement:** Public
* **Query Parameters:** `movieId` (long, required), `date` (ISO date `YYYY-MM-DD`, required).
* **Success Response (`200 OK`):**
```json
[
  {
    "showId": 501,
    "cinemaName": "Grand Cineplex Downtown",
    "screenNumber": 1,
    "screenType": "IMAX_3D",
    "startTime": "2026-10-03T19:00:00Z",
    "endTime": "2026-10-03T21:49:00Z",
    "basePrice": 14.50,
    "availableSeatCount": 84,
    "status": "SCHEDULED"
  }
]
```

### 3.3 Fetch Interactive Seat Map
* **Method & Endpoint:** `GET /api/v1/shows/{showId}/seat-map`
* **Purpose:** Retrieve the full spatial seat grid with real-time seat availability.
* **Auth Requirement:** Public
* **Path Parameters:** `showId` (long, required).
* **Success Response (`200 OK`):**
```json
{
  "showId": 501,
  "screenName": "Screen 1 (IMAX)",
  "totalSeats": 120,
  "availableSeats": 84,
  "seats": [
    {
      "showSeatId": 1201,
      "row": "E",
      "number": 5,
      "tier": "PREMIUM",
      "price": 18.00,
      "gridX": 5,
      "gridY": 5,
      "status": "AVAILABLE"
    },
    {
      "showSeatId": 1202,
      "row": "E",
      "number": 6,
      "tier": "PREMIUM",
      "price": 18.00,
      "gridX": 6,
      "gridY": 5,
      "status": "HELD"
    }
  ]
}
```

---

## 4. Smart Group Seating & Recommendation Endpoints (`/api/v1/recommendations`)

### 4.1 Request Group Seating Recommendations
* **Method & Endpoint:** `POST /api/v1/recommendations/group-seats`
* **Purpose:** Compute optimal contiguous or split-row seat clusters for a group.
* **Auth Requirement:** Authenticated (`ROLE_CUSTOMER`)
* **Request Body:**
```json
{
  "showId": 501,
  "partySize": 5,
  "preferredTier": "PREMIUM",
  "requireAccessibility": false,
  "allowSplitRows": true
}
```
* **Success Response (`200 OK`):**
```json
{
  "showId": 501,
  "partySize": 5,
  "recommendations": [
    {
      "rank": 1,
      "arrangementType": "CONTIGUOUS_ROW",
      "score": 94,
      "description": "Row F, Seats 6 to 10 (Center Screen Premium)",
      "totalPrice": 90.00,
      "seats": [
        {"showSeatId": 1245, "row": "F", "number": 6, "tier": "PREMIUM", "price": 18.00},
        {"showSeatId": 1246, "row": "F", "number": 7, "tier": "PREMIUM", "price": 18.00},
        {"showSeatId": 1247, "row": "F", "number": 8, "tier": "PREMIUM", "price": 18.00},
        {"showSeatId": 1248, "row": "F", "number": 9, "tier": "PREMIUM", "price": 18.00},
        {"showSeatId": 1249, "row": "F", "number": 10, "tier": "PREMIUM", "price": 18.00}
      ]
    },
    {
      "rank": 2,
      "arrangementType": "ADJACENT_SPLIT_ROW",
      "score": 82,
      "description": "Row E (Seats 7-9) & Row F (Seats 7-8) - Aligned Vertically",
      "totalPrice": 90.00,
      "seats": [
        {"showSeatId": 1203, "row": "E", "number": 7, "tier": "PREMIUM", "price": 18.00},
        {"showSeatId": 1204, "row": "E", "number": 8, "tier": "PREMIUM", "price": 18.00},
        {"showSeatId": 1205, "row": "E", "number": 9, "tier": "PREMIUM", "price": 18.00},
        {"showSeatId": 1246, "row": "F", "number": 7, "tier": "PREMIUM", "price": 18.00},
        {"showSeatId": 1247, "row": "F", "number": 8, "tier": "PREMIUM", "price": 18.00}
      ]
    }
  ]
}
```
* **Error Responses:** `400 Bad Request` (party size out of 1-10 range), `404 Not Found` (show not found), `422 Unprocessable` (insufficient total available seats).

---

## 5. Seat Holds & Checkout Endpoints (`/api/v1/seat-holds`)

### 5.1 Acquire Temporary Seat Hold
* **Method & Endpoint:** `POST /api/v1/seat-holds`
* **Purpose:** Concurrency-safely lock selected seats for 8 minutes and initialize booking.
* **Auth Requirement:** Authenticated (`ROLE_CUSTOMER`)
* **Headers:** `Idempotency-Key: <UUID>`
* **Request Body:**
```json
{
  "showId": 501,
  "showSeatIds": [1245, 1246, 1247, 1248, 1249]
}
```
* **Success Response (`201 Created`):**
```json
{
  "holdId": 8801,
  "holdToken": "hld_98a7c2d8-4f1e-4b92-9a3b",
  "bookingId": 3042,
  "bookingReference": "CS-2026-3042",
  "totalAmount": 90.00,
  "expiresAt": "2026-10-02T20:02:00Z",
  "remainingSeconds": 480,
  "heldSeats": [
    {"showSeatId": 1245, "label": "F-6", "price": 18.00},
    {"showSeatId": 1246, "label": "F-7", "price": 18.00},
    {"showSeatId": 1247, "label": "F-8", "price": 18.00},
    {"showSeatId": 1248, "label": "F-9", "price": 18.00},
    {"showSeatId": 1249, "label": "F-10", "price": 18.00}
  ]
}
```
* **Error Responses:** `409 Conflict` (one or more seats already held/booked).

### 5.2 Release Seat Hold Voluntarily
* **Method & Endpoint:** `DELETE /api/v1/seat-holds/{holdId}`
* **Purpose:** Immediately release held seats back to the pool if customer cancels checkout.
* **Auth Requirement:** Authenticated (`ROLE_CUSTOMER`)
* **Success Response (`200 OK`):**
```json
{
  "message": "Seat hold released successfully.",
  "holdId": 8801,
  "releasedSeatsCount": 5
}
```

---

## 6. Payment & Recovery Endpoints (`/api/v1/payments`)

### 6.1 Process Checkout Payment
* **Method & Endpoint:** `POST /api/v1/payments/process`
* **Purpose:** Charge customer via mock gateway and confirm booking.
* **Auth Requirement:** Authenticated (`ROLE_CUSTOMER`)
* **Headers:** `Idempotency-Key: <UUID>`
* **Request Body:**
```json
{
  "bookingId": 3042,
  "holdId": 8801,
  "paymentMethod": "CARD",
  "mockScenario": "SIMULATE_SUCCESS"
}
```
*(Note: `mockScenario` supports: `SIMULATE_SUCCESS`, `SIMULATE_DECLINE`, `SIMULATE_TIMEOUT`)*
* **Success Response (`200 OK` — Instant Success):**
```json
{
  "paymentId": 9921,
  "status": "SUCCESS",
  "gatewayTransactionId": "MOCK-TXN-88219",
  "bookingReference": "CS-2026-3042",
  "bookingStatus": "CONFIRMED",
  "confirmedAt": "2026-10-02T19:55:00Z",
  "tickets": [
    {
      "ticketId": 701,
      "seatLabel": "F-6",
      "ticketCode": "TKT-CS-701-F6",
      "qrHash": "a8f3b92c4e1d70a..."
    }
  ]
}
```
* **Ambiguous / Recovery Response (`202 Accepted` — Timeout / Verification Pending):**
```json
{
  "paymentId": 9922,
  "status": "PENDING_RECONCILIATION",
  "bookingStatus": "PAYMENT_PENDING_VERIFICATION",
  "message": "Payment processing is taking longer than expected. Verification in progress.",
  "retryCheckIntervalSeconds": 15
}
```
* **Error Responses:** `402 Payment Required` (card declined; retry possible), `410 Gone` (hold expired).

### 6.2 Payment Webhook Ingestion
* **Method & Endpoint:** `POST /api/v1/payments/webhook`
* **Purpose:** Ingest asynchronous status notifications from payment gateway.
* **Auth Requirement:** Webhook HMAC Signature verification header (`X-Gateway-Signature`).
* **Request Body:**
```json
{
  "eventId": "evt_00192837",
  "gatewayTxnId": "MOCK-TXN-88219",
  "status": "SUCCESS",
  "amount": 90.00,
  "timestamp": "2026-10-02T19:55:01Z"
}
```
* **Success Response (`200 OK`):**
```json
{
  "received": true,
  "idempotentHandled": true
}
```

---

## 7. Bookings & Cancellation Endpoints (`/api/v1/bookings`)

### 7.1 View Customer Booking History
* **Method & Endpoint:** `GET /api/v1/bookings`
* **Purpose:** Retrieve active and past bookings for authenticated user.
* **Auth Requirement:** Authenticated (`ROLE_CUSTOMER`)
* **Success Response (`200 OK`):**
```json
[
  {
    "bookingId": 3042,
    "bookingReference": "CS-2026-3042",
    "movieTitle": "Interstellar Odyssey",
    "cinemaName": "Grand Cineplex Downtown",
    "screenName": "Screen 1 (IMAX)",
    "showTime": "2026-10-03T19:00:00Z",
    "status": "CONFIRMED",
    "totalAmount": 90.00,
    "seatLabels": ["F-6", "F-7", "F-8", "F-9", "F-10"],
    "createdAt": "2026-10-02T19:50:00Z"
  }
]
```

### 7.2 Cancel Confirmed Booking
* **Method & Endpoint:** `POST /api/v1/bookings/{bookingId}/cancel`
* **Purpose:** Cancel a booking ($\ge 2$ hours before showtime) and initiate refund.
* **Auth Requirement:** Authenticated (`ROLE_CUSTOMER`)
* **Success Response (`200 OK`):**
```json
{
  "bookingId": 3042,
  "status": "CANCELLED",
  "refundAmount": 90.00,
  "refundStatus": "REFUND_PENDING",
  "releasedSeatsCount": 5,
  "message": "Booking cancelled. Refund of $90.00 initiated."
}
```
* **Error Responses:** `422 Unprocessable Entity` (attempted cancellation within 2 hours of showtime).

---

## 8. Waitlist Endpoints (`/api/v1/waitlists`)

### 8.1 Register on Show Waitlist
* **Method & Endpoint:** `POST /api/v1/waitlists`
* **Purpose:** Join queue for sold-out showtime.
* **Auth Requirement:** Authenticated (`ROLE_CUSTOMER`)
* **Request Body:**
```json
{
  "showId": 501,
  "partySize": 2,
  "preferredTier": "PREMIUM"
}
```
* **Success Response (`201 Created`):**
```json
{
  "waitlistId": 401,
  "showId": 501,
  "queuePosition": 3,
  "status": "WAITING",
  "message": "You are #3 on the waitlist. You will receive an offer if seats are released."
}
```

### 8.2 Claim Waitlist Offer
* **Method & Endpoint:** `POST /api/v1/waitlists/claim`
* **Purpose:** Exercise exclusive 10-minute seat offer.
* **Auth Requirement:** Authenticated (`ROLE_CUSTOMER`)
* **Request Body:**
```json
{
  "claimToken": "clm_8f29d10e-94c3-4e81-a3f1"
}
```
* **Success Response (`200 OK`):**
```json
{
  "message": "Offer claimed! Seats reserved for checkout.",
  "holdId": 8904,
  "bookingId": 3055,
  "expiresAt": "2026-10-02T20:10:00Z"
}
```
* **Error Responses:** `410 Gone` (10-minute claim window expired).

---

## 9. Staff & Admin Endpoints (`/api/v1/staff`, `/api/v1/admin`)

### 9.1 Validate Ticket QR Code (Staff)
* **Method & Endpoint:** `POST /api/v1/staff/tickets/validate`
* **Purpose:** Validate customer digital ticket at cinema hall entrance.
* **Auth Requirement:** Authenticated (`ROLE_STAFF`)
* **Request Body:**
```json
{
  "ticketCode": "TKT-CS-701-F6",
  "qrHash": "a8f3b92c4e1d70a..."
}
```
* **Success Response (`200 OK` — Valid Entry):**
```json
{
  "validationStatus": "VALID",
  "ticketId": 701,
  "movieTitle": "Interstellar Odyssey",
  "screenName": "Screen 1 (IMAX)",
  "seatLabel": "F-6",
  "patronName": "Jane Doe",
  "scannedAt": "2026-10-03T18:45:10Z"
}
```
* **Error / Duplicate Response (`409 Conflict` — Already Scanned):**
```json
{
  "validationStatus": "INVALID_ALREADY_USED",
  "ticketId": 701,
  "previouslyScannedAt": "2026-10-03T18:42:01Z",
  "message": "Warning: Ticket already marked as USED!"
}
```

### 9.2 Emergency Show Cancellation (Admin)
* **Method & Endpoint:** `POST /api/v1/admin/shows/{showId}/cancel`
* **Purpose:** Cancel entire show due to technical failure; triggers auto-refunds.
* **Auth Requirement:** Authenticated (`ROLE_ADMIN`)
* **Request Body:**
```json
{
  "cancellationReason": "Projector optical failure in Screen 1"
}
```
* **Success Response (`200 OK`):**
```json
{
  "showId": 501,
  "showStatus": "CANCELLED",
  "affectedBookingsCount": 42,
  "totalRefundAmount": 3480.00,
  "message": "Show cancelled. 42 bookings refunded and customers alerted."
}
```
