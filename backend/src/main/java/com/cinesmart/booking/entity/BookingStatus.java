package com.cinesmart.booking.entity;

public enum BookingStatus {
    PENDING_PAYMENT,
    PAYMENT_PENDING_VERIFICATION,
    CONFIRMED,
    CANCELLED,
    CANCELLED_BY_CINEMA,
    EXPIRED
}
