package com.cinesmart.payment.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public class PaymentInitiateRequest {

    @NotNull(message = "Booking ID is required")
    private Long bookingId;

    @NotBlank(message = "Payment method is required (UPI, CARD, NET_BANKING, WALLET)")
    private String paymentMethod;

    private String idempotencyKey;

    private Boolean simulateSuccess = true;

    private String failureReason;

    public PaymentInitiateRequest() {
    }

    public PaymentInitiateRequest(Long bookingId, String paymentMethod) {
        this.bookingId = bookingId;
        this.paymentMethod = paymentMethod;
    }

    public PaymentInitiateRequest(Long bookingId, String paymentMethod, String idempotencyKey, Boolean simulateSuccess, String failureReason) {
        this.bookingId = bookingId;
        this.paymentMethod = paymentMethod;
        this.idempotencyKey = idempotencyKey;
        this.simulateSuccess = simulateSuccess;
        this.failureReason = failureReason;
    }

    public Long getBookingId() {
        return bookingId;
    }

    public void setBookingId(Long bookingId) {
        this.bookingId = bookingId;
    }

    public String getPaymentMethod() {
        return paymentMethod;
    }

    public void setPaymentMethod(String paymentMethod) {
        this.paymentMethod = paymentMethod;
    }

    public String getIdempotencyKey() {
        return idempotencyKey;
    }

    public void setIdempotencyKey(String idempotencyKey) {
        this.idempotencyKey = idempotencyKey;
    }

    public Boolean getSimulateSuccess() {
        return simulateSuccess;
    }

    public void setSimulateSuccess(Boolean simulateSuccess) {
        this.simulateSuccess = simulateSuccess;
    }

    public String getFailureReason() {
        return failureReason;
    }

    public void setFailureReason(String failureReason) {
        this.failureReason = failureReason;
    }
}
