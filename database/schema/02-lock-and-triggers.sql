-- CineSmart Double-Booking Prevention & Seat Locking Extension

-- 1. Add locked_until and locked_by_id to show_seats
ALTER TABLE show_seats ADD COLUMN IF NOT EXISTS locked_until TIMESTAMP WITH TIME ZONE;
ALTER TABLE show_seats ADD COLUMN IF NOT EXISTS locked_by_id BIGINT REFERENCES users(id) ON DELETE SET NULL;

-- 2. Add expires_at to bookings
ALTER TABLE bookings ADD COLUMN IF NOT EXISTS expires_at TIMESTAMP WITH TIME ZONE;

-- 3. Update show_seats status check constraint to include 'LOCKED'
ALTER TABLE show_seats DROP CONSTRAINT IF EXISTS chk_show_seat_status;
ALTER TABLE show_seats DROP CONSTRAINT IF EXISTS show_seats_status_check;
ALTER TABLE show_seats ADD CONSTRAINT chk_show_seat_status CHECK (status IN ('AVAILABLE', 'LOCKED', 'HELD', 'BOOKED', 'BLOCKED', 'WAITLIST_OFFERED'));

-- 4. Create index on status and locked_until for lock cleanup performance
CREATE INDEX IF NOT EXISTS idx_show_seats_lock_lookup ON show_seats(status, locked_until);

-- 5. Concurrency & Integrity Trigger on booking_seats
-- Ensures a show_seat cannot be inserted into more than one active booking (CONFIRMED or PENDING_PAYMENT)
CREATE OR REPLACE FUNCTION check_seat_not_already_booked() RETURNS TRIGGER AS $$
BEGIN
    IF EXISTS (
        SELECT 1 FROM booking_seats bs
        JOIN bookings b ON bs.booking_id = b.id
        WHERE bs.show_seat_id = NEW.show_seat_id
          AND b.status IN ('CONFIRMED', 'PENDING_PAYMENT')
          AND b.id <> NEW.booking_id
    ) THEN
        RAISE EXCEPTION 'Double booking error: show_seat % is already reserved in another active booking', NEW.show_seat_id;
    END IF;
    RETURN NEW;
END;
$$ LANGUAGE plpgsql;

DROP TRIGGER IF EXISTS trg_prevent_double_booking ON booking_seats;
CREATE TRIGGER trg_prevent_double_booking
BEFORE INSERT ON booking_seats
FOR EACH ROW EXECUTE FUNCTION check_seat_not_already_booked();

