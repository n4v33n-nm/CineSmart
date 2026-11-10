-- CineSmart PostgreSQL Database Schema
-- Phase 3: Offers, Coupons and Loyalty Rewards System

-- 1. Coupons Table
CREATE TABLE IF NOT EXISTS coupons (
    id BIGSERIAL PRIMARY KEY,
    code VARCHAR(50) NOT NULL UNIQUE,
    description VARCHAR(255),
    discount_type VARCHAR(20) NOT NULL, -- 'PERCENTAGE', 'FIXED_AMOUNT'
    discount_value NUMERIC(10, 2) NOT NULL,
    min_booking_amount NUMERIC(10, 2) NOT NULL DEFAULT 0.00,
    max_discount_amount NUMERIC(10, 2),
    valid_from TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    valid_until TIMESTAMP WITH TIME ZONE NOT NULL,
    usage_limit INT NOT NULL DEFAULT 100,
    per_user_limit INT NOT NULL DEFAULT 1,
    times_used INT NOT NULL DEFAULT 0,
    is_active BOOLEAN NOT NULL DEFAULT TRUE,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT chk_discount_type CHECK (discount_type IN ('PERCENTAGE', 'FIXED_AMOUNT')),
    CONSTRAINT chk_discount_value CHECK (discount_value > 0),
    CONSTRAINT chk_min_booking CHECK (min_booking_amount >= 0)
);

CREATE INDEX IF NOT EXISTS idx_coupons_code ON coupons(code);
CREATE INDEX IF NOT EXISTS idx_coupons_active ON coupons(is_active, valid_from, valid_until);

-- 2. Coupon Usages Table
CREATE TABLE IF NOT EXISTS coupon_usages (
    id BIGSERIAL PRIMARY KEY,
    coupon_id BIGINT NOT NULL REFERENCES coupons(id) ON DELETE CASCADE,
    user_id BIGINT NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    booking_id BIGINT NOT NULL REFERENCES bookings(id) ON DELETE CASCADE,
    discount_applied NUMERIC(10, 2) NOT NULL,
    used_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT uq_coupon_booking UNIQUE (coupon_id, booking_id)
);

CREATE INDEX IF NOT EXISTS idx_coupon_usages_user ON coupon_usages(coupon_id, user_id);

-- 3. Loyalty Accounts Table
CREATE TABLE IF NOT EXISTS loyalty_accounts (
    id BIGSERIAL PRIMARY KEY,
    user_id BIGINT NOT NULL UNIQUE REFERENCES users(id) ON DELETE CASCADE,
    points_balance INT NOT NULL DEFAULT 0,
    lifetime_points_earned INT NOT NULL DEFAULT 0,
    lifetime_points_redeemed INT NOT NULL DEFAULT 0,
    updated_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT chk_points_balance CHECK (points_balance >= 0)
);

CREATE UNIQUE INDEX IF NOT EXISTS idx_loyalty_accounts_user ON loyalty_accounts(user_id);

-- 4. Loyalty Transactions Table
CREATE TABLE IF NOT EXISTS loyalty_transactions (
    id BIGSERIAL PRIMARY KEY,
    user_id BIGINT NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    booking_id BIGINT REFERENCES bookings(id) ON DELETE SET NULL,
    points INT NOT NULL,
    transaction_type VARCHAR(30) NOT NULL, -- 'EARNED', 'REDEEMED', 'REVERSED'
    description VARCHAR(255) NOT NULL,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT chk_loyalty_txn_type CHECK (transaction_type IN ('EARNED', 'REDEEMED', 'REVERSED'))
);

CREATE INDEX IF NOT EXISTS idx_loyalty_txn_user ON loyalty_transactions(user_id, created_at DESC);
CREATE INDEX IF NOT EXISTS idx_loyalty_txn_booking ON loyalty_transactions(booking_id);

-- 5. Add discount/coupon/loyalty columns to bookings table
ALTER TABLE bookings ADD COLUMN IF NOT EXISTS original_amount NUMERIC(10, 2);
ALTER TABLE bookings ADD COLUMN IF NOT EXISTS discount_amount NUMERIC(10, 2) DEFAULT 0.00;
ALTER TABLE bookings ADD COLUMN IF NOT EXISTS coupon_code VARCHAR(50);
ALTER TABLE bookings ADD COLUMN IF NOT EXISTS loyalty_points_redeemed INT DEFAULT 0;
ALTER TABLE bookings ADD COLUMN IF NOT EXISTS loyalty_discount_amount NUMERIC(10, 2) DEFAULT 0.00;

