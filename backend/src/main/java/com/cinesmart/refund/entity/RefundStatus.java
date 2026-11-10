package com.cinesmart.refund.entity;

public enum RefundStatus {
    REQUESTED,
    APPROVED,
    PROCESSING,
    COMPLETED,
    FAILED,
    REJECTED;

    public boolean isTerminal() {
        return this == COMPLETED || this == REJECTED || this == FAILED;
    }
}
