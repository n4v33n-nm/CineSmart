-- CineSmart PostgreSQL Database Schema
-- Phase 1 & 2 Relational Foundations

DROP TABLE IF EXISTS audit_logs CASCADE;
DROP TABLE IF EXISTS tickets CASCADE;
DROP TABLE IF EXISTS group_members CASCADE;
DROP TABLE IF EXISTS group_bookings CASCADE;
DROP TABLE IF EXISTS payments CASCADE;
DROP TABLE IF EXISTS booking_seats CASCADE;
DROP TABLE IF EXISTS bookings CASCADE;
DROP TABLE IF EXISTS seat_holds CASCADE;
DROP TABLE IF EXISTS show_seats CASCADE;
DROP TABLE IF EXISTS shows CASCADE;
DROP TABLE IF EXISTS seats CASCADE;
DROP TABLE IF EXISTS screens CASCADE;
DROP TABLE IF EXISTS cinemas CASCADE;
DROP TABLE IF EXISTS movies CASCADE;
DROP TABLE IF EXISTS users CASCADE;

-- 1. Users Table
CREATE TABLE users (
    id BIGSERIAL PRIMARY KEY,
    email VARCHAR(150) NOT NULL UNIQUE,
    password_hash VARCHAR(255) NOT NULL,
    full_name VARCHAR(100) NOT NULL,
    phone_number VARCHAR(20),
    role VARCHAR(30) NOT NULL DEFAULT 'ROLE_CUSTOMER',
    is_active BOOLEAN NOT NULL DEFAULT TRUE,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT chk_user_role CHECK (role IN ('ROLE_CUSTOMER', 'ROLE_STAFF', 'ROLE_ADMIN'))
);

CREATE UNIQUE INDEX idx_users_email ON users(email);

-- 2. Movies Table
CREATE TABLE movies (
    id BIGSERIAL PRIMARY KEY,
    title VARCHAR(200) NOT NULL,
    synopsis TEXT,
    duration_minutes INT NOT NULL,
    language VARCHAR(50) NOT NULL,
    genre VARCHAR(50) NOT NULL,
    age_rating VARCHAR(10) NOT NULL,
    poster_url VARCHAR(500),
    is_active BOOLEAN NOT NULL DEFAULT TRUE,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT chk_movie_duration CHECK (duration_minutes > 0)
);

CREATE INDEX idx_movies_title ON movies(title);
CREATE INDEX idx_movies_genre ON movies(genre);

-- 3. Cinemas Table
CREATE TABLE cinemas (
    id BIGSERIAL PRIMARY KEY,
    name VARCHAR(150) NOT NULL,
    city VARCHAR(100) NOT NULL,
    address TEXT NOT NULL,
    contact_number VARCHAR(20)
);

-- 4. Screens Table
CREATE TABLE screens (
    id BIGSERIAL PRIMARY KEY,
    cinema_id BIGINT NOT NULL REFERENCES cinemas(id) ON DELETE CASCADE,
    screen_number INT NOT NULL,
    screen_type VARCHAR(30) NOT NULL DEFAULT 'STANDARD',
    total_capacity INT NOT NULL,
    CONSTRAINT uq_cinema_screen UNIQUE (cinema_id, screen_number),
    CONSTRAINT chk_screen_capacity CHECK (total_capacity > 0),
    CONSTRAINT chk_screen_type CHECK (screen_type IN ('STANDARD', 'IMAX_3D', 'DOLBY_ATMOS'))
);

-- 5. Seats (Physical Master Layout) Table
CREATE TABLE seats (
    id BIGSERIAL PRIMARY KEY,
    screen_id BIGINT NOT NULL REFERENCES screens(id) ON DELETE CASCADE,
    row_identifier VARCHAR(5) NOT NULL,
    column_number INT NOT NULL,
    seat_tier VARCHAR(30) NOT NULL DEFAULT 'STANDARD',
    grid_x INT NOT NULL,
    grid_y INT NOT NULL,
    is_active BOOLEAN NOT NULL DEFAULT TRUE,
    CONSTRAINT uq_screen_seat UNIQUE (screen_id, row_identifier, column_number),
    CONSTRAINT chk_seat_tier CHECK (seat_tier IN ('STANDARD', 'PREMIUM', 'RECLINER', 'ACCESSIBLE_WHEELCHAIR', 'ACCESSIBLE_COMPANION'))
);

-- 6. Shows Table
CREATE TABLE shows (
    id BIGSERIAL PRIMARY KEY,
    movie_id BIGINT NOT NULL REFERENCES movies(id) ON DELETE RESTRICT,
    screen_id BIGINT NOT NULL REFERENCES screens(id) ON DELETE RESTRICT,
    start_time TIMESTAMP WITH TIME ZONE NOT NULL,
    end_time TIMESTAMP WITH TIME ZONE NOT NULL,
    base_price NUMERIC(10, 2) NOT NULL,
    status VARCHAR(30) NOT NULL DEFAULT 'SCHEDULED',
    CONSTRAINT chk_show_time CHECK (end_time > start_time),
    CONSTRAINT chk_show_price CHECK (base_price >= 0),
    CONSTRAINT chk_show_status CHECK (status IN ('SCHEDULED', 'IN_PROGRESS', 'COMPLETED', 'CANCELLED'))
);

CREATE INDEX idx_shows_screen_time ON shows(screen_id, start_time);
CREATE INDEX idx_shows_movie_time ON shows(movie_id, start_time);

-- 7. ShowSeats (Per-Show Inventory) Table
CREATE TABLE show_seats (
    id BIGSERIAL PRIMARY KEY,
    show_id BIGINT NOT NULL REFERENCES shows(id) ON DELETE CASCADE,
    seat_id BIGINT NOT NULL REFERENCES seats(id) ON DELETE RESTRICT,
    status VARCHAR(30) NOT NULL DEFAULT 'AVAILABLE',
    price NUMERIC(10, 2) NOT NULL,
    version BIGINT NOT NULL DEFAULT 0,
    CONSTRAINT uq_show_seat UNIQUE (show_id, seat_id),
    CONSTRAINT chk_show_seat_price CHECK (price >= 0),
    CONSTRAINT chk_show_seat_status CHECK (status IN ('AVAILABLE', 'HELD', 'BOOKED', 'BLOCKED', 'WAITLIST_OFFERED'))
);

CREATE INDEX idx_show_seats_lookup ON show_seats(show_id, status);

-- 8. SeatHolds Table
CREATE TABLE seat_holds (
    id BIGSERIAL PRIMARY KEY,
    hold_token VARCHAR(64) NOT NULL UNIQUE,
    user_id BIGINT NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    show_id BIGINT NOT NULL REFERENCES shows(id) ON DELETE CASCADE,
    show_seat_id BIGINT NOT NULL REFERENCES show_seats(id) ON DELETE CASCADE,
    status VARCHAR(30) NOT NULL DEFAULT 'ACTIVE',
    created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    expires_at TIMESTAMP WITH TIME ZONE NOT NULL,
    CONSTRAINT chk_hold_status CHECK (status IN ('ACTIVE', 'EXPIRED', 'CONVERTED_TO_BOOKING', 'RELEASED'))
);

-- Partial Unique Index: Exactly one ACTIVE hold allowed per show_seat_id
CREATE UNIQUE INDEX idx_unique_active_hold_per_show_seat 
ON seat_holds (show_seat_id) 
WHERE status = 'ACTIVE';

-- 9. Bookings Table
CREATE TABLE bookings (
    id BIGSERIAL PRIMARY KEY,
    booking_reference VARCHAR(32) NOT NULL UNIQUE,
    user_id BIGINT NOT NULL REFERENCES users(id) ON DELETE RESTRICT,
    show_id BIGINT NOT NULL REFERENCES shows(id) ON DELETE RESTRICT,
    total_amount NUMERIC(10, 2) NOT NULL,
    status VARCHAR(30) NOT NULL DEFAULT 'PENDING_PAYMENT',
    created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    confirmed_at TIMESTAMP WITH TIME ZONE,
    CONSTRAINT chk_booking_amount CHECK (total_amount >= 0),
    CONSTRAINT chk_booking_status CHECK (status IN ('PENDING_PAYMENT', 'PAYMENT_PENDING_VERIFICATION', 'CONFIRMED', 'CANCELLED', 'CANCELLED_BY_CINEMA', 'EXPIRED'))
);

CREATE INDEX idx_bookings_user ON bookings(user_id);
CREATE INDEX idx_bookings_status ON bookings(status);

-- 10. BookingSeats Table (Line-Item Financial Snapshot)
CREATE TABLE booking_seats (
    id BIGSERIAL PRIMARY KEY,
    booking_id BIGINT NOT NULL REFERENCES bookings(id) ON DELETE CASCADE,
    show_seat_id BIGINT NOT NULL REFERENCES show_seats(id) ON DELETE RESTRICT,
    snapshot_price NUMERIC(10, 2) NOT NULL,
    seat_label VARCHAR(10) NOT NULL,
    tier_snapshot VARCHAR(30) NOT NULL,
    CONSTRAINT chk_snapshot_price CHECK (snapshot_price >= 0)
);

-- 11. Payments Table
CREATE TABLE payments (
    id BIGSERIAL PRIMARY KEY,
    booking_id BIGINT NOT NULL REFERENCES bookings(id) ON DELETE RESTRICT,
    idempotency_key VARCHAR(64) NOT NULL UNIQUE,
    gateway_txn_id VARCHAR(100),
    amount NUMERIC(10, 2) NOT NULL,
    payment_method VARCHAR(30) NOT NULL,
    status VARCHAR(30) NOT NULL DEFAULT 'PENDING',
    failure_reason VARCHAR(255),
    created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    reconciled_at TIMESTAMP WITH TIME ZONE,
    CONSTRAINT chk_payment_amount CHECK (amount > 0),
    CONSTRAINT chk_payment_status CHECK (status IN ('PENDING', 'SUCCESS', 'FAILED', 'PENDING_RECONCILIATION', 'REFUNDED'))
);

-- 12. Tickets Table
CREATE TABLE tickets (
    id BIGSERIAL PRIMARY KEY,
    booking_seat_id BIGINT NOT NULL UNIQUE REFERENCES booking_seats(id) ON DELETE RESTRICT,
    ticket_code VARCHAR(32) NOT NULL UNIQUE,
    qr_verification_hash VARCHAR(255) NOT NULL,
    status VARCHAR(20) NOT NULL DEFAULT 'ISSUED',
    issued_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    validated_at TIMESTAMP WITH TIME ZONE,
    validated_by BIGINT REFERENCES users(id),
    CONSTRAINT chk_ticket_status CHECK (status IN ('ISSUED', 'USED', 'VOIDED'))
);
