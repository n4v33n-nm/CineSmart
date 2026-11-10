-- CineSmart Ticket Verification Logs Table
CREATE TABLE IF NOT EXISTS ticket_verification_logs (
    id BIGSERIAL PRIMARY KEY,
    ticket_code VARCHAR(64),
    scanned_hash VARCHAR(255),
    verified_by_id BIGINT REFERENCES users(id) ON DELETE SET NULL,
    result VARCHAR(30) NOT NULL,
    failure_reason VARCHAR(255),
    attempted_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX IF NOT EXISTS idx_ticket_verification_logs_result ON ticket_verification_logs(result);
CREATE INDEX IF NOT EXISTS idx_ticket_verification_logs_time ON ticket_verification_logs(attempted_at);

