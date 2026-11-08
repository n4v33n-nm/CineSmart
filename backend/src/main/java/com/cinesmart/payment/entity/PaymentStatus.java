package com.cinesmart.payment.entity;

public enum PaymentStatus {
    INITIATED,
    PROCESSING,
    SUCCESS,
    FAILED,
    CANCELLED,
    REFUNDED;

    public boolean canTransitionTo(PaymentStatus target) {
        if (target == null) {
            return false;
        }
        if (this == target) {
            return true; // Idempotent same-state
        }
        return switch (this) {
            case INITIATED -> target == PROCESSING || target == FAILED || target == CANCELLED;
            case PROCESSING -> target == SUCCESS || target == FAILED || target == CANCELLED;
            case SUCCESS -> target == REFUNDED;
            case FAILED, CANCELLED, REFUNDED -> false; // Terminal states
        };
    }
}
