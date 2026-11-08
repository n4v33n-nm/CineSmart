package com.cinesmart.show.entity;

public enum ShowSeatStatus {
    AVAILABLE,
    LOCKED,
    HELD,
    BOOKED,
    BLOCKED,
    WAITLIST_OFFERED;

    public boolean isTemporarilyLocked() {
        return this == LOCKED || this == HELD;
    }

    public boolean isUnavailable() {
        return this == LOCKED || this == HELD || this == BOOKED || this == BLOCKED;
    }
}
