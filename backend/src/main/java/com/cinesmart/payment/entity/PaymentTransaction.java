package com.cinesmart.payment.entity;

import com.cinesmart.booking.entity.Booking;
import com.cinesmart.user.entity.User;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "payment_transactions", indexes = {
    @Index(name = "idx_payments_booking", columnList = "booking_id"),
    @Index(name = "idx_payments_status", columnList = "status"),
    @Index(name = "idx_payments_reference", columnList = "transaction_reference")
})
public class PaymentTransaction {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank
    @Column(name = "transaction_reference", nullable = false, unique = true, length = 64)
    private String transactionReference;

    @NotNull
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "booking_id", nullable = false)
    private Booking booking;

    @NotNull
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @NotBlank
    @Column(name = "idempotency_key", nullable = false, unique = true, length = 64)
    private String idempotencyKey;

    @Column(name = "gateway_txn_id", length = 100)
    private String gatewayTxnId;

    @NotNull
    @PositiveOrZero
    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal amount;

    @NotBlank
    @Column(nullable = false, length = 10)
    private String currency = "INR";

    @NotNull
    @Enumerated(EnumType.STRING)
    @Column(name = "payment_method", nullable = false, length = 30)
    private PaymentMethod paymentMethod;

    @NotNull
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private PaymentStatus status = PaymentStatus.INITIATED;

    @Column(name = "failure_reason", length = 255)
    private String failureReason;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt = LocalDateTime.now();

    @Column(name = "updated_at")
    private LocalDateTime updatedAt = LocalDateTime.now();

    public PaymentTransaction() {
    }

    public PaymentTransaction(String transactionReference,
                              Booking booking,
                              User user,
                              String idempotencyKey,
                              BigDecimal amount,
                              String currency,
                              PaymentMethod paymentMethod) {
        this.transactionReference = transactionReference;
        this.booking = booking;
        this.user = user;
        this.idempotencyKey = idempotencyKey;
        this.amount = amount;
        this.currency = (currency != null && !currency.trim().isEmpty()) ? currency : "INR";
        this.paymentMethod = paymentMethod;
        this.status = PaymentStatus.INITIATED;
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
    }

    public void transitionTo(PaymentStatus nextStatus) {
        if (!this.status.canTransitionTo(nextStatus)) {
            throw new IllegalStateException("Invalid payment state transition from " + this.status + " to " + nextStatus);
        }
        this.status = nextStatus;
        this.updatedAt = LocalDateTime.now();
    }

    public void markProcessing() {
        transitionTo(PaymentStatus.PROCESSING);
    }

    public void markSuccess(String gatewayTxnId) {
        transitionTo(PaymentStatus.SUCCESS);
        this.gatewayTxnId = gatewayTxnId;
    }

    public void markFailed(String reason) {
        transitionTo(PaymentStatus.FAILED);
        this.failureReason = reason;
    }

    public void markCancelled() {
        transitionTo(PaymentStatus.CANCELLED);
    }

    public void markRefunded(String refundGatewayRef) {
        transitionTo(PaymentStatus.REFUNDED);
        this.failureReason = "Refund processed. Ref: " + refundGatewayRef;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getTransactionReference() {
        return transactionReference;
    }

    public void setTransactionReference(String transactionReference) {
        this.transactionReference = transactionReference;
    }

    public Booking getBooking() {
        return booking;
    }

    public void setBooking(Booking booking) {
        this.booking = booking;
    }

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }

    public String getIdempotencyKey() {
        return idempotencyKey;
    }

    public void setIdempotencyKey(String idempotencyKey) {
        this.idempotencyKey = idempotencyKey;
    }

    public String getGatewayTxnId() {
        return gatewayTxnId;
    }

    public void setGatewayTxnId(String gatewayTxnId) {
        this.gatewayTxnId = gatewayTxnId;
    }

    public BigDecimal getAmount() {
        return amount;
    }

    public void setAmount(BigDecimal amount) {
        this.amount = amount;
    }

    public String getCurrency() {
        return currency;
    }

    public void setCurrency(String currency) {
        this.currency = currency;
    }

    public PaymentMethod getPaymentMethod() {
        return paymentMethod;
    }

    public void setPaymentMethod(PaymentMethod paymentMethod) {
        this.paymentMethod = paymentMethod;
    }

    public PaymentStatus getStatus() {
        return status;
    }

    public void setStatus(PaymentStatus status) {
        this.status = status;
    }

    public String getFailureReason() {
        return failureReason;
    }

    public void setFailureReason(String failureReason) {
        this.failureReason = failureReason;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }
}
