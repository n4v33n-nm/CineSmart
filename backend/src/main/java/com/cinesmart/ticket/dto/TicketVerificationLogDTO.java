package com.cinesmart.ticket.dto;

import com.cinesmart.ticket.entity.TicketVerificationLog;

import java.time.LocalDateTime;

public class TicketVerificationLogDTO {

    private Long id;
    private String ticketCode;
    private String result;
    private String failureReason;
    private String verifiedBy;
    private LocalDateTime attemptedAt;

    public TicketVerificationLogDTO() {}

    public static TicketVerificationLogDTO fromEntity(TicketVerificationLog log) {
        if (log == null) return null;
        TicketVerificationLogDTO dto = new TicketVerificationLogDTO();
        dto.setId(log.getId());
        dto.setTicketCode(log.getTicketCode());
        dto.setResult(log.getResult());
        dto.setFailureReason(log.getFailureReason());
        if (log.getVerifiedBy() != null) {
            dto.setVerifiedBy(log.getVerifiedBy().getFullName());
        }
        dto.setAttemptedAt(log.getAttemptedAt());
        return dto;
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

    public String getVerifiedBy() {
        return verifiedBy;
    }

    public void setVerifiedBy(String verifiedBy) {
        this.verifiedBy = verifiedBy;
    }

    public LocalDateTime getAttemptedAt() {
        return attemptedAt;
    }

    public void setAttemptedAt(LocalDateTime attemptedAt) {
        this.attemptedAt = attemptedAt;
    }
}

