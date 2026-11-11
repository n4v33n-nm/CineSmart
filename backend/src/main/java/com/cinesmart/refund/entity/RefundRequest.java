package com.cinesmart.refund.entity;

import com.cinesmart.booking.entity.Booking;
import com.cinesmart.payment.entity.PaymentTransaction;
import com.cinesmart.user.entity.User;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "refund_requests", indexes = {
    @Index(name = "idx_refund_requests_user", columnList = "user_id"),
    @Index(name = "idx_refund_requests_booking", columnList = "booking_id"),
    @Index(name = "idx_refund_requests_status", columnList = "status"),
    @Index(name = "idx_refund_requests_ref", columnList = "refund_reference")
})
public class RefundRequest {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank
    @Column(name = "refund_reference", nullable = false, unique = true, length = 64)
    private String refundReference;

    @NotNull
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "booking_id", nullable = false)
    private Booking booking;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "payment_id")
    private PaymentTransaction payment;

    @NotNull
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "policy_id")
    private CancellationPolicy policy;

    @NotNull
    @PositiveOrZero
    @Column(name = "original_amount", nullable = false, precision = 10, scale = 2)
    private BigDecimal originalAmount;

    @NotNull
    @PositiveOrZero
    @Column(name = "cancellation_fee", nullable = false, precision = 10, scale = 2)
    private BigDecimal cancellationFee = BigDecimal.ZERO;

    @NotNull
    @PositiveOrZero
    @Column(name = "refund_amount", nullable = false, precision = 10, scale = 2)
    private BigDecimal refundAmount = BigDecimal.ZERO;

    @NotNull
    @Column(name = "refund_percentage", nullable = false, precision = 5, scale = 2)
    private BigDecimal refundPercentage = new BigDecimal("100.00");

    @NotNull
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private RefundStatus status = RefundStatus.REQUESTED;

    @Column(length = 500)
    private String reason;

    @Column(name = "rejection_reason", length = 500)
    private String rejectionReason;

    @Column(name = "admin_notes", length = 500)
    private String adminNotes;

    @Column(name = "gateway_refund_id", length = 100)
    private String gatewayRefundId;

    @Column(name = "hours_before_show", precision = 6, scale = 2)
    private BigDecimal hoursBeforeShow;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "processed_by_user_id")
    private User processedByUser;

    @Column(name = "requested_at", nullable = false)
    private LocalDateTime requestedAt = LocalDateTime.now();

    @Column(name = "processed_at")
    private LocalDateTime processedAt;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt = LocalDateTime.now();

    @Column(name = "updated_at")
    private LocalDateTime updatedAt = LocalDateTime.now();

    public RefundRequest() {
    }

    public RefundRequest(String refundReference, Booking booking, PaymentTransaction payment,
                         User user, CancellationPolicy policy, BigDecimal originalAmount,
                         BigDecimal cancellationFee, BigDecimal refundAmount,
                         BigDecimal refundPercentage, BigDecimal hoursBeforeShow, String reason) {
        this.refundReference = refundReference;
        this.booking = booking;
        this.payment = payment;
        this.user = user;
        this.policy = policy;
        this.originalAmount = originalAmount != null ? originalAmount : BigDecimal.ZERO;
        this.cancellationFee = cancellationFee != null ? cancellationFee : BigDecimal.ZERO;
        this.refundAmount = refundAmount != null ? refundAmount : BigDecimal.ZERO;
        this.refundPercentage = refundPercentage != null ? refundPercentage : BigDecimal.ZERO;
        this.hoursBeforeShow = hoursBeforeShow;
        this.reason = reason;
        this.status = RefundStatus.REQUESTED;
        this.requestedAt = LocalDateTime.now();
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
    }

    @PreUpdate
    public void onUpdate() {
        this.updatedAt = LocalDateTime.now();
    }

    public void approve(User admin, String notes) {
        if (this.status != RefundStatus.REQUESTED) {
            throw new IllegalStateException("Only REQUESTED refunds can be approved. Current status: " + this.status);
        }
        this.status = RefundStatus.APPROVED;
        this.processedByUser = admin;
        this.adminNotes = notes;
        this.updatedAt = LocalDateTime.now();
    }

    public void markProcessing() {
        if (this.status != RefundStatus.APPROVED && this.status != RefundStatus.REQUESTED) {
            throw new IllegalStateException("Refund must be REQUESTED or APPROVED before processing. Current: " + this.status);
        }
        this.status = RefundStatus.PROCESSING;
        this.updatedAt = LocalDateTime.now();
    }

    public void markCompleted(String gatewayRefundId, User admin, String notes) {
        this.status = RefundStatus.COMPLETED;
        this.gatewayRefundId = gatewayRefundId;
        if (admin != null) {
            this.processedByUser = admin;
        }
        if (notes != null) {
            this.adminNotes = notes;
        }
        this.processedAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
    }

    public void markFailed(String reason, User admin) {
        this.status = RefundStatus.FAILED;
        this.adminNotes = reason;
        if (admin != null) {
            this.processedByUser = admin;
        }
        this.processedAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
    }

    public void reject(String rejectionReason, User admin) {
        if (this.status == RefundStatus.COMPLETED) {
            throw new IllegalStateException("Cannot reject an already completed refund.");
        }
        this.status = RefundStatus.REJECTED;
        this.rejectionReason = rejectionReason;
        this.processedByUser = admin;
        this.processedAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
    }

    // Getters and Setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getRefundReference() {
        return refundReference;
    }

    public void setRefundReference(String refundReference) {
        this.refundReference = refundReference;
    }

    public Booking getBooking() {
        return booking;
    }

    public void setBooking(Booking booking) {
        this.booking = booking;
    }

    public PaymentTransaction getPayment() {
        return payment;
    }

    public void setPayment(PaymentTransaction payment) {
        this.payment = payment;
    }

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }

    public CancellationPolicy getPolicy() {
        return policy;
    }

    public void setPolicy(CancellationPolicy policy) {
        this.policy = policy;
    }

    public BigDecimal getOriginalAmount() {
        return originalAmount;
    }

    public void setOriginalAmount(BigDecimal originalAmount) {
        this.originalAmount = originalAmount;
    }

    public BigDecimal getCancellationFee() {
        return cancellationFee;
    }

    public void setCancellationFee(BigDecimal cancellationFee) {
        this.cancellationFee = cancellationFee;
    }

    public BigDecimal getRefundAmount() {
        return refundAmount;
    }

    public void setRefundAmount(BigDecimal refundAmount) {
        this.refundAmount = refundAmount;
    }

    public BigDecimal getRefundPercentage() {
        return refundPercentage;
    }

    public void setRefundPercentage(BigDecimal refundPercentage) {
        this.refundPercentage = refundPercentage;
    }

    public RefundStatus getStatus() {
        return status;
    }

    public void setStatus(RefundStatus status) {
        this.status = status;
    }

    public String getReason() {
        return reason;
    }

    public void setReason(String reason) {
        this.reason = reason;
    }

    public String getRejectionReason() {
        return rejectionReason;
    }

    public void setRejectionReason(String rejectionReason) {
        this.rejectionReason = rejectionReason;
    }

    public String getAdminNotes() {
        return adminNotes;
    }

    public void setAdminNotes(String adminNotes) {
        this.adminNotes = adminNotes;
    }

    public String getGatewayRefundId() {
        return gatewayRefundId;
    }

    public void setGatewayRefundId(String gatewayRefundId) {
        this.gatewayRefundId = gatewayRefundId;
    }

    public BigDecimal getHoursBeforeShow() {
        return hoursBeforeShow;
    }

    public void setHoursBeforeShow(BigDecimal hoursBeforeShow) {
        this.hoursBeforeShow = hoursBeforeShow;
    }

    public User getProcessedByUser() {
        return processedByUser;
    }

    public void setProcessedByUser(User processedByUser) {
        this.processedByUser = processedByUser;
    }

    public LocalDateTime getRequestedAt() {
        return requestedAt;
    }

    public void setRequestedAt(LocalDateTime requestedAt) {
        this.requestedAt = requestedAt;
    }

    public LocalDateTime getProcessedAt() {
        return processedAt;
    }

    public void setProcessedAt(LocalDateTime processedAt) {
        this.processedAt = processedAt;
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

