-- CineSmart PostgreSQL Database Schema
-- Phase 4: Booking Cancellation and Refund Management System

-- 1. Cancellation Policies Table (Configurable by Admin)
CREATE TABLE IF NOT EXISTS cancellation_policies (
    id BIGSERIAL PRIMARY KEY,
    policy_name VARCHAR(100) NOT NULL UNIQUE,
    description VARCHAR(255),
    min_hours_before_show INT NOT NULL DEFAULT 0,
    max_hours_before_show INT, -- NULL means unbounded (e.g. >24h)
    refund_percentage NUMERIC(5, 2) NOT NULL DEFAULT 100.00,
    cancellation_fee_percent NUMERIC(5, 2) NOT NULL DEFAULT 0.00,
    is_active BOOLEAN NOT NULL DEFAULT TRUE,
    display_order INT NOT NULL DEFAULT 0,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT chk_policy_hours CHECK (min_hours_before_show >= 0),
    CONSTRAINT chk_policy_refund CHECK (refund_percentage >= 0.00 AND refund_percentage <= 100.00),
    CONSTRAINT chk_policy_fee CHECK (cancellation_fee_percent >= 0.00 AND cancellation_fee_percent <= 100.00)
);

CREATE INDEX IF NOT EXISTS idx_cancellation_policies_active ON cancellation_policies(is_active, display_order);

-- 2. Refund Requests & Transactions Table
CREATE TABLE IF NOT EXISTS refund_requests (
    id BIGSERIAL PRIMARY KEY,
    refund_reference VARCHAR(64) NOT NULL UNIQUE,
    booking_id BIGINT NOT NULL REFERENCES bookings(id) ON DELETE RESTRICT,
    payment_id BIGINT REFERENCES payment_transactions(id) ON DELETE SET NULL,
    user_id BIGINT NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    policy_id BIGINT REFERENCES cancellation_policies(id) ON DELETE SET NULL,
    original_amount NUMERIC(10, 2) NOT NULL,
    cancellation_fee NUMERIC(10, 2) NOT NULL DEFAULT 0.00,
    refund_amount NUMERIC(10, 2) NOT NULL DEFAULT 0.00,
    refund_percentage NUMERIC(5, 2) NOT NULL DEFAULT 100.00,
    status VARCHAR(30) NOT NULL DEFAULT 'REQUESTED',
    reason VARCHAR(500),
    rejection_reason VARCHAR(500),
    admin_notes VARCHAR(500),
    gateway_refund_id VARCHAR(100),
    hours_before_show NUMERIC(6, 2),
    processed_by_user_id BIGINT REFERENCES users(id) ON DELETE SET NULL,
    requested_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    processed_at TIMESTAMP WITH TIME ZONE,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT chk_refund_status CHECK (status IN ('REQUESTED', 'APPROVED', 'PROCESSING', 'COMPLETED', 'FAILED', 'REJECTED')),
    CONSTRAINT chk_refund_amounts CHECK (original_amount >= 0 AND refund_amount >= 0 AND cancellation_fee >= 0)
);

CREATE INDEX IF NOT EXISTS idx_refund_requests_user ON refund_requests(user_id);
CREATE INDEX IF NOT EXISTS idx_refund_requests_booking ON refund_requests(booking_id);
CREATE INDEX IF NOT EXISTS idx_refund_requests_status ON refund_requests(status);
CREATE INDEX IF NOT EXISTS idx_refund_requests_ref ON refund_requests(refund_reference);

-- 3. Add Optional Cancellation Reason to Bookings
ALTER TABLE bookings ADD COLUMN IF NOT EXISTS cancellation_reason VARCHAR(500);

-- 4. Seed Standard Configurable Cancellation Policies
INSERT INTO cancellation_policies (policy_name, description, min_hours_before_show, max_hours_before_show, refund_percentage, cancellation_fee_percent, is_active, display_order)
VALUES
    ('Early Cancellation (>24h)', 'Full 100% refund when cancelled 24 hours or more before showtime', 24, NULL, 100.00, 0.00, TRUE, 1),
    ('Standard Window (6h - 24h)', '75% refund with 25% cancellation charge between 6 and 24 hours before showtime', 6, 24, 75.00, 25.00, TRUE, 2),
    ('Last-Minute Cancellation (<6h)', 'No refund (100% cancellation charge) within 6 hours of showtime', 0, 6, 0.00, 100.00, TRUE, 3)
ON CONFLICT (policy_name) DO NOTHING;

