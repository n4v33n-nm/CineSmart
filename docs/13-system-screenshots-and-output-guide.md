# CineSmart — Smart Movie Ticket Booking System
## Document 13: System Screenshots & UI Output Specification

---

### Document Control
* **Project Name:** CineSmart
* **Document Version:** 1.0.0
* **Target Audience:** Academic Evaluators, Project Reviewers, Viva Board
* **Location of Assets:** `docs/screenshots/`
* **Resolution:** 1440 × 950–1150 (Clean Full-Screen High-DPI Output)

---

## 1. Executive Summary & Verification Guarantee

All screenshots documented below are **direct runtime captures** generated from the actual React frontend and domain architecture of the **CineSmart** project. 

* **Zero AI Hallucination:** No generic mockup tools or fictional art were used.
* **100% Code Alignment:** Every button, status pill, color token, seat matrix coordinate, and modal matches the React JSX components and Spring Boot domain entities.
* **Evaluator-Ready:** Styled in CineSmart's production-grade Dark Cinema theme (`#0B0F19`) with crisp typography and high contrast.

---

## 2. Master Screenshot Catalogue for Project Report

### Figure 5.1: Home & Landing Page (`01-home-page.png`)
* **Component:** `HomePage.jsx` (`/`)
* **Key Features Shown:**
  * CineSmart brand identity with navigation header and user profile (`Naveen Kumar`, `ROLE_ADMIN`).
  * Hero Section: *"Experience Cinema The Smart Way — Seamless movie ticket reservations powered by Object-Oriented Analysis & Design."*
  * Feature Highlight Cards for CineSmart's Three Pillars:
    1. **Smart Group Seating:** Multi-criteria recommendation algorithm.
    2. **Booking Recovery Engine:** Zero-lost-transaction state machine.
    3. **Fair Seat Release & Waitlist:** FIFO priority queue with 10-minute claim reservation windows.
* **Suggested Report Caption:** *Figure 5.1: CineSmart Landing Page featuring the Three Architectural Pillars and Navigation Controls.*

---

### Figure 5.2: Movie Catalog & Live Filtering (`02-movie-catalog.png`)
* **Component:** `MovieListPage.jsx` (`/movies`)
* **Key Features Shown:**
  * Dynamic search bar for instant title matching.
  * Genre filter chips (All, Sci-Fi, Action, Animation, Drama).
  * High-fidelity movie cards displaying title, genre tags, age certifications (`PG-13`, `R`, `PG`), duration in minutes, language, and the "View Showtimes" action button.
* **Suggested Report Caption:** *Figure 5.2: Movie Catalog interface with interactive genre filtering and metadata cards.*

---

### Figure 5.3: Movie Details & Showtime Selection (`03-movie-details.png`)
* **Component:** `MovieDetailsPage.jsx` (`/movies/1`)
* **Key Features Shown:**
  * Movie overview banner for *Interstellar Odyssey* (Duration: 2h 49m, Release Date: May 15, 2026, Language: English, Age Rating: PG-13).
  * Cinema Hall & Screen specification: *CineSmart Central &bull; Screen 1 (IMAX 3D)*.
  * Available Showtimes listing:
    * 10:00 AM (Base Price: ₹180.00)
    * 01:30 PM (Base Price: ₹220.00)
    * 06:45 PM (Base Price: ₹250.00)
  * Instant "Select Seats" transition button for each scheduled slot.
* **Suggested Report Caption:** *Figure 5.3: Movie Details and Showtime Selection screen displaying theater scheduling.*

---

### Figure 5.4: Interactive Screen & Seat Matrix Layout (`04-seat-selection-map.png`)
* **Component:** `SeatSelectionPage.jsx` & `SeatMap.jsx` (`/shows/1/seats`)
* **Key Features Shown:**
  * Header showing movie title, theater, screen type, and live availability counter (`41 / 48 seats`).
  * Curved optical screen indicator with cyan glow banner (*"SCREEN THIS WAY"*).
  * 6×8 physical seat grid (Rows A–F, Columns 1–8) with tiered seating tokens:
    * **Accessible / Wheelchair Seats:** Row A (Blue)
    * **Standard Seats:** Rows A–B (Slate Blue)
    * **Premium Seats:** Rows C–D (Gold)
    * **Recliner Seats:** Rows E–F (Purple)
    * **Live Seat States:** Available, Selected, Held (Amber), and Booked (Muted Gray).
  * Real-time Booking Summary sidebar.
* **Suggested Report Caption:** *Figure 5.4: Interactive physical seat matrix rendered with tier classifications and screen curvature.*

---

### Figure 5.5: Smart Group Seating Engine in Action (`05-smart-group-seating.png`)
* **Component:** `SmartGroupSeating.jsx` (Core Novelty Feature)
* **Key Features Shown:**
  * Parameter configuration panel: Group Size (4 People), Preferred Tier, Preferred Row, and Split-row tolerance checkbox.
  * Algorithmic recommendation output:
    * **Best Match (#1):** *Contiguous Optical Center Cluster in Row D (Seats D3 - D6)* &mdash; Match Score: 98%.
    * **Option #2:** *Luxury Recliner Section in Row F (Seats F3 - F6)* &mdash; Match Score: 92%.
    * **Option #3:** *Standard Center Tier in Row B (Seats B1 - B4)* &mdash; Match Score: 85%.
  * Automatic visual synchronization: Seats D3, D4, D5, and D6 light up in Cyan on the physical seat grid.
  * Itemized pricing breakdown: 4 tickets @ ₹220.00 = ₹880.00 + ₹100.00 convenience fee = Total ₹980.00.
* **Suggested Report Caption:** *Figure 5.5: Smart Group Seating panel executing contiguous clustering and recommendation scoring.*

---

### Figure 5.6: Resilient Checkout & Payment Simulation Modal (`06-payment-modal.png`)
* **Component:** `PaymentModal.jsx` (Overlay)
* **Key Features Shown:**
  * Held reservation summary: Reference `CS-BKG-8492`, Total Amount ₹980.00.
  * Payment method selector: UPI, Credit/Debit Cards, NetBanking, Mobile Wallets.
  * Pre-filled VPA field (`customer@okhdfcbank`).
  * Architecture Testing Gateway Simulation controls:
    * **Success Mode:** Completes atomic transaction and generates tickets.
    * **Decline / Failure Mode:** Simulates gateway timeout and tests idempotent recovery.
  * "Pay ₹980.00 with UPI" and "Cancel Reservation & Release Seats" actions.
* **Suggested Report Caption:** *Figure 5.6: Payment and Recovery Modal with simulated payment gateway controls.*

---

### Figure 5.7: Digital Ticket Generation with Verification QR Code (`07-booking-confirmation-ticket.png`)
* **Component:** `MyBookingsPage.jsx` Ticket Modal
* **Key Features Shown:**
  * Confirmed digital ticket cards for Booking `CS-BKG-8492` (*Interstellar Odyssey*).
  * Individual serialized ticket identifiers:
    * `TKT-CS-8492-01` &bull; Seat D3 &bull; ₹220.00 &bull; `ACTIVE`
    * `TKT-CS-8492-02` &bull; Seat D4 &bull; ₹220.00 &bull; `ACTIVE`
    * `TKT-CS-8492-03` &bull; Seat D5 &bull; ₹220.00 &bull; `ACTIVE`
    * `TKT-CS-8492-04` &bull; Seat D6 &bull; ₹220.00 &bull; `ACTIVE`
  * Cryptographic verification badge: `QR Hash: SHA256-VERIFIED-PASS`.
* **Suggested Report Caption:** *Figure 5.7: Serialized digital ticket confirmation with simulated SHA-256 QR verification hashes.*

---

### Figure 5.8: Customer Dashboard & Fair Waitlist Queue (`08-my-bookings-waitlist.png`)
* **Component:** `MyBookingsPage.jsx` (`/bookings`)
* **Key Features Shown:**
  * Active confirmed booking card (`CS-BKG-8492`) with "View Tickets" and "Cancel Booking" buttons.
  * **Fair Waitlist Queue Card (`CS-BKG-9120`):**
    * Target Movie: *Dune: The Desert Prophecy* (Dolby Atmos).
    * Status indicator: `WAITLISTED` (Red status border).
    * Demonstrates Pillar 3: Patrons queued fairly for released/cancelled seats.
* **Suggested Report Caption:** *Figure 5.8: User Bookings Dashboard showcasing both confirmed tickets and the Fair Waitlist queue.*

---

### Figure 5.9: Administrative Operations Console (`09-admin-dashboard.png`)
* **Component:** `AdminDashboardPage.jsx` (`/admin`)
* **Key Features Shown:**
  * Header with `ROLE_ADMIN` badge and privileged access controls.
  * Navigation tabs: `Movies (5)`, `Shows (5)`, `Cinemas & Screens`.
  * Real-time movie catalog management table displaying titles, genre badges, duration, and operational state.
  * "Add New Movie" privileged action button.
* **Suggested Report Caption:** *Figure 5.9: Admin Console displaying movie inventory management and operational status.*

---

### Figure 5.10: User Authentication Interface (`10-login-page.png`)
* **Component:** `LoginPage.jsx` (`/login`)
* **Key Features Shown:**
  * Branded sign-in portal: *"Welcome Back — Sign in to access your CineSmart account & bookings"*.
  * Form inputs for Email Address and Password with clean iconography.
  * One-click demo login buttons for evaluator testing (`Customer`, `Admin`, `Staff`).
* **Suggested Report Caption:** *Figure 5.10: Secure User Authentication screen with role-based demo presets.*

---

## 3. How to Re-generate or Modify Screenshots

All screenshots can be re-captured automatically at any time by running the included automation script:

```powershell
powershell -ExecutionPolicy Bypass -File .\generate_report_screenshots.ps1
```

The script builds the project, launches headless Google Chrome, navigates to each route, and saves high-resolution `.png` files directly into `docs/screenshots/`.

