-- CineSmart Development Seed Data
-- 5 Movies, Cinema 'CineSmart Central', 2 Screens, Physical Seats, Shows & ShowSeats

-- 1. Users (Passwords: Admin123!, Staff123!, Customer123!)
-- BCrypt encoded passwords (cost 10)
INSERT INTO users (email, password_hash, full_name, phone_number, role, is_active)
VALUES 
('admin@cinesmart.com', '$2a$10$e8wY1o9sJ4F6tBqKq.rZlOsZlJ8W7eN6X1cV9yD2uM0iK8mO5rB2a', 'System Administrator', '+1-555-0100', 'ROLE_ADMIN', TRUE),
('staff@cinesmart.com', '$2a$10$e8wY1o9sJ4F6tBqKq.rZlOsZlJ8W7eN6X1cV9yD2uM0iK8mO5rB2a', 'Gate Operations Staff', '+1-555-0101', 'ROLE_STAFF', TRUE),
('customer@cinesmart.com', '$2a$10$e8wY1o9sJ4F6tBqKq.rZlOsZlJ8W7eN6X1cV9yD2uM0iK8mO5rB2a', 'Jane Customer', '+1-555-0102', 'ROLE_CUSTOMER', TRUE)
ON CONFLICT (email) DO NOTHING;

-- 2. Movies (5 Distinctive Films)
INSERT INTO movies (id, title, synopsis, duration_minutes, language, genre, age_rating, posterUrl, is_active)
VALUES 
(1, 'Interstellar Odyssey', 'A heroic team of astronauts embarks on humanity''s greatest voyage through a newly discovered wormhole in deep space.', 169, 'English', 'Sci-Fi', 'PG-13', 'https://images.unsplash.com/photo-1534447677768-be436bb09401?w=800&q=80', TRUE),
(2, 'Dune: The Desert Prophecy', 'Paul Atreides unites with the Fremen on a spiritual quest of vengeance against conspirators who destroyed his family.', 166, 'English', 'Sci-Fi', 'PG-13', 'https://images.unsplash.com/photo-1509198397868-475647b2a1e5?w=800&q=80', TRUE),
(3, 'The Dark Knight Returns', 'Eight years after the Joker''s reign of chaos, Batman returns to defend Gotham City from the enigmatic masked terrorist Bane.', 164, 'English', 'Action', 'PG-13', 'https://images.unsplash.com/photo-1509281373149-e957c6296406?w=800&q=80', TRUE),
(4, 'Cyberpunk: Neon Horizon', 'In a neon-drenched metropolis, an outlaw mercenary takes on corporate syndicates to retrieve a stolen cybernetic bio-chip.', 142, 'English', 'Action', 'R', 'https://images.unsplash.com/photo-1518709268805-4e9042af9f23?w=800&q=80', TRUE),
(5, 'Spider-Man: Multiverse Rift', 'Teenager Miles Morales must unite with alternate universe web-slingers to defeat a reality-shattering extradimensional threat.', 140, 'English', 'Animation', 'PG', 'https://images.unsplash.com/photo-1635805737707-575885ab0820?w=800&q=80', TRUE)
ON CONFLICT (id) DO UPDATE SET title = EXCLUDED.title;

SELECT setval('movies_id_seq', 5);

-- 3. Cinema: CineSmart Central
INSERT INTO cinemas (id, name, city, address, contact_number)
VALUES (1, 'CineSmart Central', 'Metropolis', '100 Broadway Boulevard, Suite 500', '+1-555-9000')
ON CONFLICT (id) DO UPDATE SET name = EXCLUDED.name;

SELECT setval('cinemas_id_seq', 1);

-- 4. Screens: Screen 1 (IMAX 3D) and Screen 2 (Dolby Atmos)
INSERT INTO screens (id, cinema_id, screen_number, screen_type, total_capacity)
VALUES 
(1, 1, 1, 'IMAX_3D', 48),
(2, 1, 2, 'DOLBY_ATMOS', 48)
ON CONFLICT (cinema_id, screen_number) DO UPDATE SET total_capacity = EXCLUDED.total_capacity;

SELECT setval('screens_id_seq', 2);

-- 5. Physical Seats (Rows A through F, Columns 1 to 8 per Screen)
-- Screen 1 Seats
INSERT INTO seats (screen_id, row_identifier, column_number, seat_tier, grid_x, grid_y, is_active)
SELECT 
    1 as screen_id,
    chr(65 + r) as row_identifier,
    c as column_number,
    CASE 
        WHEN r = 0 AND c IN (1, 2) THEN 'ACCESSIBLE_WHEELCHAIR'
        WHEN r = 0 AND c IN (3, 4) THEN 'ACCESSIBLE_COMPANION'
        WHEN r IN (0, 1) THEN 'STANDARD'
        WHEN r IN (2, 3) THEN 'PREMIUM'
        ELSE 'RECLINER'
    END as seat_tier,
    c as grid_x,
    r + 1 as grid_y,
    TRUE as is_active
FROM generate_series(0, 5) r
CROSS JOIN generate_series(1, 8) c
ON CONFLICT (screen_id, row_identifier, column_number) DO NOTHING;

-- Screen 2 Seats
INSERT INTO seats (screen_id, row_identifier, column_number, seat_tier, grid_x, grid_y, is_active)
SELECT 
    2 as screen_id,
    chr(65 + r) as row_identifier,
    c as column_number,
    CASE 
        WHEN r = 0 AND c IN (1, 2) THEN 'ACCESSIBLE_WHEELCHAIR'
        WHEN r = 0 AND c IN (3, 4) THEN 'ACCESSIBLE_COMPANION'
        WHEN r IN (0, 1) THEN 'STANDARD'
        WHEN r IN (2, 3) THEN 'PREMIUM'
        ELSE 'RECLINER'
    END as seat_tier,
    c as grid_x,
    r + 1 as grid_y,
    TRUE as is_active
FROM generate_series(0, 5) r
CROSS JOIN generate_series(1, 8) c
ON CONFLICT (screen_id, row_identifier, column_number) DO NOTHING;

-- 6. Shows (Multiple Showtimes: 10:00 AM, 1:00 PM, 4:00 PM, 7:00 PM, 10:00 PM)
INSERT INTO shows (id, movie_id, screen_id, start_time, end_time, base_price, status)
VALUES 
(1, 1, 1, CURRENT_DATE + TIME '10:00:00', CURRENT_DATE + TIME '12:49:00', 14.50, 'SCHEDULED'),
(2, 1, 1, CURRENT_DATE + TIME '13:00:00', CURRENT_DATE + TIME '15:49:00', 16.50, 'SCHEDULED'),
(3, 1, 1, CURRENT_DATE + TIME '19:00:00', CURRENT_DATE + TIME '21:49:00', 18.00, 'SCHEDULED'),
(4, 2, 2, CURRENT_DATE + TIME '13:00:00', CURRENT_DATE + TIME '15:46:00', 15.00, 'SCHEDULED'),
(5, 2, 2, CURRENT_DATE + TIME '16:00:00', CURRENT_DATE + TIME '18:46:00', 17.00, 'SCHEDULED'),
(6, 3, 2, CURRENT_DATE + TIME '19:00:00', CURRENT_DATE + TIME '21:44:00', 18.00, 'SCHEDULED'),
(7, 4, 1, CURRENT_DATE + TIME '22:00:00', CURRENT_DATE + TIME '00:22:00' + INTERVAL '1 day', 16.00, 'SCHEDULED'),
(8, 5, 2, CURRENT_DATE + TIME '10:00:00', CURRENT_DATE + TIME '12:20:00', 12.50, 'SCHEDULED')
ON CONFLICT (id) DO UPDATE SET base_price = EXCLUDED.base_price;

SELECT setval('shows_id_seq', 8);

-- 7. ShowSeats Generation (Instantiates a ShowSeat for every physical seat in the screen)
INSERT INTO show_seats (show_id, seat_id, status, price, version)
SELECT 
    sh.id as show_id,
    s.id as seat_id,
    'AVAILABLE' as status,
    CASE 
        WHEN s.seat_tier = 'STANDARD' THEN sh.base_price
        WHEN s.seat_tier = 'PREMIUM' THEN sh.base_price + 3.00
        WHEN s.seat_tier = 'RECLINER' THEN sh.base_price + 6.00
        ELSE sh.base_price
    END as price,
    0 as version
FROM shows sh
JOIN screens scr ON sh.screen_id = scr.id
JOIN seats s ON s.screen_id = scr.id
ON CONFLICT (show_id, seat_id) DO NOTHING;
