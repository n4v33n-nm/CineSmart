# CineSmart System Screenshots Catalog

This directory contains high-resolution (1440x900 @ 1.5x Retina DPI) screenshots of all major end-to-end features in the **CineSmart Movie Ticket Booking System**, ready for inclusion in academic, technical, or architecture project reports.

---

## Screenshot Mapping & Report Captions

| # | File Name | Route / View | Feature / Phase | Suggested Report Figure Caption |
|---|-----------|--------------|-----------------|---------------------------------|
| 1 | `01_HomePage.png` | `/` | Core UI | **Figure 1:** CineSmart Landing Page featuring responsive navigation, hero banner, and movie carousel. |
| 2 | `02_ExploreMoviesPage.png` | `/movies` | Core Catalog | **Figure 2:** Movie Discovery Catalog with real-time title search and interactive genre pill filters. |
| 3 | `03_MovieDetails_Showtimes.png` | `/movies/1` | Show Management | **Figure 3:** Movie Details View with synopsis, auditorium formats (IMAX 3D / Dolby Atmos), and showtime selection. |
| 4 | `04_RealTime_Seat_Locking.png` | `/shows/1/seats` | **Phase 1** | **Figure 4:** Interactive Seating Map with real-time 5-minute seat locking countdown and Smart Group Seating controls. |
| 5 | `05_Booking_Checkout_Coupons_Loyalty.png` | Checkout Modal | **Phase 3** | **Figure 5:** Checkout and Payment Modal demonstrating promotional coupon discount application (`WELCOME50`) and loyalty points redemption. |
| 6 | `06_Digital_Ticket_QRCode.png` | Digital Ticket Modal | **Phase 2** | **Figure 6:** Digital Cinema E-Pass containing encrypted turnstile QR code, ticket code, seat metadata, and PDF download button. |
| 7 | `07_Gate_Ticket_Scanner.png` | `/verify-tickets` | **Phase 2** | **Figure 7:** Theatre Gate Entry Scanner console with camera viewfinder, manual code validator, and real-time validation metrics. |
| 8 | `08_MyBookings_Reservations.png` | `/bookings` | Core / User | **Figure 8:** Customer Reservation Dashboard displaying confirmed bookings, seat details, and digital ticket access. |
| 9 | `09_Cancellation_Refund_Modal.png` | Cancellation Modal | **Phase 4** | **Figure 9:** Interactive Cancellation & Refund Preview showing dynamic policy evaluation (>24h window), fee deductions, and loyalty reversals. |
| 10 | `10_Refund_Tracking_Ledger.png` | `/bookings` (Refunds) | **Phase 4** | **Figure 10:** Customer Refund Tracking Ledger showing real-time refund statuses (`REQUESTED`, `APPROVED`, `COMPLETED`) and simulated gateway references. |
| 11 | `11_Loyalty_Rewards_Center.png` | `/rewards` | **Phase 3** | **Figure 11:** CineSmart Loyalty Rewards Club dashboard displaying membership tier, point balances, currency conversion rates, and transaction history. |
| 12 | `12_Admin_Movie_Management.png` | `/admin` (Movies) | Admin Console | **Figure 12:** Administrative Movie Catalog Management displaying active listings, runtime, certification, and CRUD actions. |
| 13 | `13_Admin_Show_Scheduling.png` | `/admin` (Shows) | Admin Console | **Figure 13:** Admin Show Scheduling interface with auditorium allocations, base pricing, and real-time seat inventory tracking. |
| 14 | `14_Admin_Coupon_Management.png` | `/admin` (Coupons) | **Phase 3** | **Figure 14:** Admin Promotional Offers & Coupon campaign manager with discount types, minimum spend thresholds, and usage caps. |
| 15 | `15_Admin_Refund_Management.png` | `/admin` (Refunds) | **Phase 4** | **Figure 15:** Admin Refund Management Console with financial KPI cards, status filters, and approve/process/reject workflows. |
| 16 | `16_Admin_Cancellation_Policies.png` | `/admin` (Policies) | **Phase 4** | **Figure 16:** Configurable Tiered Cancellation Policies table defining time window thresholds, refund percentages, and penalty fees. |

---

### Technical Specifications
- **Resolution:** 2160 × 1350 px (1440 × 900 CSS viewport @ 1.5x devicePixelRatio)
- **Format:** Lossless PNG
- **Environment:** Production-grade PostgreSQL database integration, real-time Spring Boot 3.3 REST APIs, and React 18 frontend.
