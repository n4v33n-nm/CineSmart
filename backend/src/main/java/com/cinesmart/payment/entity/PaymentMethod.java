package com.cinesmart.payment.entity;

import com.cinesmart.common.exception.BadRequestException;

public enum PaymentMethod {
    UPI,
    CARD,
    NET_BANKING,
    WALLET;

    public static PaymentMethod fromString(String value) {
        if (value == null || value.trim().isEmpty()) {
            throw new BadRequestException("Payment method is required", "INVALID_PAYMENT_METHOD");
        }
        try {
            return PaymentMethod.valueOf(value.trim().toUpperCase());
        } catch (IllegalArgumentException ex) {
            throw new BadRequestException("Unsupported payment method: " + value + ". Supported methods: UPI, CARD, NET_BANKING, WALLET", "INVALID_PAYMENT_METHOD");
        }
    }
}
