package com.cinesmart.payment.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public class RefundResponseDTO {

    private Long paymentId;
    private String transactionReference;
    private Long bookingId;
    private String bookingReference;
    private BigDecimal refundedAmount;
    private String refundReference;
    private String status;
    private String message;
    private LocalDateTime refundedAt;

    public RefundResponseDTO() {
    }

    public RefundResponseDTO(Long paymentId,
                             String transactionReference,
                             Long bookingId,
                             String bookingReference,
                             BigDecimal refundedAmount,
                             String refundReference,
                             String status,
                             String message,
                             LocalDateTime refundedAt) {
        this.paymentId = paymentId;
        this.transactionReference = transactionReference;
        this.bookingId = bookingId;
        this.bookingReference = bookingReference;
        this.refundedAmount = refundedAmount;
        this.refundReference = refundReference;
        this.status = status;
        this.message = message;
        this.refundedAt = refundedAt;
    }

    public Long getPaymentId() {
        return paymentId;
    }

    public void setPaymentId(Long paymentId) {
        this.paymentId = paymentId;
    }

    public String getTransactionReference() {
        return transactionReference;
    }

    public void setTransactionReference(String transactionReference) {
        this.transactionReference = transactionReference;
    }

    public Long getBookingId() {
        return bookingId;
    }

    public void setBookingId(Long bookingId) {
        this.bookingId = bookingId;
    }

    public String getBookingReference() {
        return bookingReference;
    }

    public void setBookingReference(String bookingReference) {
        this.bookingReference = bookingReference;
    }

    public BigDecimal getRefundedAmount() {
        return refundedAmount;
    }

    public void setRefundedAmount(BigDecimal refundedAmount) {
        this.refundedAmount = refundedAmount;
    }

    public String getRefundReference() {
        return refundReference;
    }

    public void setRefundReference(String refundReference) {
        this.refundReference = refundReference;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public LocalDateTime getRefundedAt() {
        return refundedAt;
    }

    public void setRefundedAt(LocalDateTime refundedAt) {
        this.refundedAt = refundedAt;
    }
}
