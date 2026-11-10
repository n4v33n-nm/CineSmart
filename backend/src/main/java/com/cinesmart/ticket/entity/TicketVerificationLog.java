package com.cinesmart.ticket.entity;

import com.cinesmart.user.entity.User;
import jakarta.persistence.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "ticket_verification_logs", indexes = {
    @Index(name = "idx_ticket_verification_logs_result", columnList = "result"),
    @Index(name = "idx_ticket_verification_logs_time", columnList = "attempted_at")
})
public class TicketVerificationLog {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "ticket_code", length = 64)
    private String ticketCode;

    @Column(name = "scanned_hash", length = 255)
    private String scannedHash;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "verified_by_id")
    private User verifiedBy;

    @Column(nullable = false, length = 30)
    private String result; // SUCCESS, ALREADY_USED, INVALID_CODE, INVALID_HASH, BOOKING_NOT_CONFIRMED, CANCELLED

    @Column(name = "failure_reason", length = 255)
    private String failureReason;

    @Column(name = "attempted_at", nullable = false)
    private LocalDateTime attemptedAt = LocalDateTime.now();

    public TicketVerificationLog() {}

    public TicketVerificationLog(String ticketCode, String scannedHash, User verifiedBy, String result, String failureReason) {
        this.ticketCode = ticketCode;
        this.scannedHash = scannedHash;
        this.verifiedBy = verifiedBy;
        this.result = result;
        this.failureReason = failureReason;
        this.attemptedAt = LocalDateTime.now();
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getTicketCode() {
        return ticketCode;
    }

    public void setTicketCode(String ticketCode) {
        this.ticketCode = ticketCode;
    }

    public String getScannedHash() {
        return scannedHash;
    }

    public void setScannedHash(String scannedHash) {
        this.scannedHash = scannedHash;
    }

    public User getVerifiedBy() {
        return verifiedBy;
    }

    public void setVerifiedBy(User verifiedBy) {
        this.verifiedBy = verifiedBy;
    }

    public String getResult() {
        return result;
    }

    public void setResult(String result) {
        this.result = result;
    }

    public String getFailureReason() {
        return failureReason;
    }

    public void setFailureReason(String failureReason) {
        this.failureReason = failureReason;
    }

    public LocalDateTime getAttemptedAt() {
        return attemptedAt;
    }

    public void setAttemptedAt(LocalDateTime attemptedAt) {
        this.attemptedAt = attemptedAt;
    }
}
