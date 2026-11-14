-- ============================================================================
-- CINESMART PHASE 6: ADVANCED ADMIN ANALYTICS DASHBOARD
-- Historical Analytics Seed Data (Multi-theatre, Time-series, Coupons, Refunds)
-- ============================================================================

-- 1. Ensure Multi-theatre Cinemas exist
INSERT INTO cinemas (id, name, city, address, contact_number)
VALUES 
  (2, 'CineSmart Nexus Mall', 'Bangalore', 'Koramangala 7th Block', '+91 80 4123 4567'),
  (3, 'CineSmart Phoenix Grand', 'Mumbai', 'Lower Parel West', '+91 22 6655 4433')
ON CONFLICT (id) DO UPDATE SET name = EXCLUDED.name, city = EXCLUDED.city;

SELECT setval('cinemas_id_seq', (SELECT MAX(id) FROM cinemas));

-- 2. Ensure Screens for new cinemas exist (Allowed ScreenType: STANDARD, IMAX_3D, DOLBY_ATMOS)
INSERT INTO screens (id, cinema_id, screen_number, screen_type, total_capacity)
VALUES
  (3, 2, 1, 'IMAX_3D', 60),
  (4, 2, 2, 'DOLBY_ATMOS', 50),
  (5, 3, 1, 'DOLBY_ATMOS', 48)
ON CONFLICT (id) DO UPDATE SET total_capacity = EXCLUDED.total_capacity;

SELECT setval('screens_id_seq', (SELECT MAX(id) FROM screens));

-- 3. Ensure Shows for movies across cinemas
INSERT INTO shows (id, movie_id, screen_id, start_time, end_time, base_price, status)
VALUES
  (101, 1, 3, NOW() - INTERVAL '10 days' + INTERVAL '14 hours', NOW() - INTERVAL '10 days' + INTERVAL '16 hours 45 mins', 250.00, 'SCHEDULED'),
  (102, 2, 3, NOW() - INTERVAL '7 days' + INTERVAL '18 hours', NOW() - INTERVAL '7 days' + INTERVAL '20 hours 30 mins', 280.00, 'SCHEDULED'),
  (103, 3, 4, NOW() - INTERVAL '5 days' + INTERVAL '19 hours', NOW() - INTERVAL '5 days' + INTERVAL '21 hours 30 mins', 300.00, 'SCHEDULED'),
  (104, 4, 5, NOW() - INTERVAL '3 days' + INTERVAL '20 hours', NOW() - INTERVAL '3 days' + INTERVAL '22 hours', 220.00, 'SCHEDULED'),
  (105, 5, 1, NOW() - INTERVAL '1 days' + INTERVAL '17 hours', NOW() - INTERVAL '1 days' + INTERVAL '19 hours', 240.00, 'SCHEDULED')
ON CONFLICT (id) DO NOTHING;

SELECT setval('shows_id_seq', (SELECT MAX(id) FROM shows));

-- 4. Seed Time-Series Historical Bookings spanning the last 30 days
INSERT INTO bookings (booking_reference, user_id, show_id, total_amount, original_amount, discount_amount, coupon_code, loyalty_points_redeemed, loyalty_discount_amount, status, created_at)
VALUES
  -- 28 days ago
  ('CS-HIST-28D-01', 4, 101, 500.00, 500.00, 0.00, NULL, 0, 0.00, 'CONFIRMED', NOW() - INTERVAL '28 days'),
  ('CS-HIST-28D-02', 3, 101, 250.00, 250.00, 0.00, NULL, 0, 0.00, 'CONFIRMED', NOW() - INTERVAL '28 days'),

  -- 21 days ago (Week 3)
  ('CS-HIST-21D-01', 4, 102, 560.00, 560.00, 0.00, NULL, 0, 0.00, 'CONFIRMED', NOW() - INTERVAL '21 days'),
  ('CS-HIST-21D-02', 5, 102, 280.00, 280.00, 0.00, NULL, 0, 0.00, 'CONFIRMED', NOW() - INTERVAL '21 days'),
  ('CS-HIST-21D-03', 3, 102, 280.00, 280.00, 0.00, NULL, 0, 0.00, 'CANCELLED', NOW() - INTERVAL '20 days'),

  -- 14 days ago (Week 2)
  ('CS-HIST-14D-01', 4, 103, 600.00, 600.00, 0.00, NULL, 0, 0.00, 'CONFIRMED', NOW() - INTERVAL '14 days'),
  ('CS-HIST-14D-02', 3, 103, 500.00, 600.00, 100.00, 'WELCOME100', 0, 0.00, 'CONFIRMED', NOW() - INTERVAL '13 days'),
  ('CS-HIST-14D-03', 5, 103, 300.00, 300.00, 0.00, NULL, 0, 0.00, 'CONFIRMED', NOW() - INTERVAL '12 days'),

  -- 7 days ago (Week 1)
  ('CS-HIST-07D-01', 4, 104, 440.00, 440.00, 0.00, NULL, 0, 0.00, 'CONFIRMED', NOW() - INTERVAL '7 days'),
  ('CS-HIST-07D-02', 3, 104, 390.00, 440.00, 50.00, 'CINE50', 0, 0.00, 'CONFIRMED', NOW() - INTERVAL '6 days'),
  ('CS-HIST-07D-03', 5, 104, 220.00, 220.00, 0.00, NULL, 0, 0.00, 'CANCELLED', NOW() - INTERVAL '5 days'),

  -- Recent days
  ('CS-HIST-03D-01', 4, 105, 480.00, 480.00, 0.00, NULL, 0, 0.00, 'CONFIRMED', NOW() - INTERVAL '3 days'),
  ('CS-HIST-02D-01', 3, 105, 430.00, 480.00, 0.00, NULL, 50, 50.00, 'CONFIRMED', NOW() - INTERVAL '2 days'),
  ('CS-HIST-01D-01', 4, 105, 720.00, 720.00, 0.00, NULL, 0, 0.00, 'CONFIRMED', NOW() - INTERVAL '1 days'),
  ('CS-HIST-TODAY-01', 3, 105, 240.00, 240.00, 0.00, NULL, 0, 0.00, 'CONFIRMED', NOW() - INTERVAL '2 hours')
ON CONFLICT (booking_reference) DO NOTHING;

-- 5. Record Coupon Usage records for coupons applied
INSERT INTO coupon_usages (coupon_id, user_id, booking_id, discount_applied, used_at)
SELECT c.id, b.user_id, b.id, b.discount_amount, b.created_at
FROM bookings b
JOIN coupons c ON UPPER(c.code) = UPPER(b.coupon_code)
WHERE b.coupon_code IS NOT NULL
ON CONFLICT DO NOTHING;

-- 6. Seed Refund Records for cancelled bookings
INSERT INTO refund_requests (refund_reference, booking_id, user_id, original_amount, cancellation_fee, refund_amount, refund_percentage, status, reason, requested_at, processed_at)
SELECT 
  'REF-HIST-' || b.id,
  b.id,
  b.user_id,
  b.total_amount,
  b.total_amount * 0.25,
  b.total_amount * 0.75,
  75.00,
  'COMPLETED',
  'Automated historical simulation',
  b.created_at + INTERVAL '2 hours',
  b.created_at + INTERVAL '3 hours'
FROM bookings b
WHERE b.status = 'CANCELLED' AND NOT EXISTS (
  SELECT 1 FROM refund_requests rr WHERE rr.booking_id = b.id
);
