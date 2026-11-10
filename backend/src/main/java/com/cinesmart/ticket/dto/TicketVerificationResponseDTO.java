package com.cinesmart.ticket.dto;

import java.time.LocalDateTime;

public class TicketVerificationResponseDTO {

    private boolean valid;
    private String ticketCode;
    private String status; // VERIFIED_SUCCESSFULLY, ALREADY_USED, INVALID_CODE, INVALID_HASH, BOOKING_NOT_CONFIRMED, CANCELLED
    private String message;
    private TicketDTO ticket;
    private LocalDateTime verifiedAt;
    private String verifiedBy;

    public TicketVerificationResponseDTO() {}

    public TicketVerificationResponseDTO(boolean valid, String ticketCode, String status, String message, TicketDTO ticket, LocalDateTime verifiedAt, String verifiedBy) {
        this.valid = valid;
        this.ticketCode = ticketCode;
        this.status = status;
        this.message = message;
        this.ticket = ticket;
        this.verifiedAt = verifiedAt;
        this.verifiedBy = verifiedBy;
    }

    public boolean isValid() {
        return valid;
    }

    public void setValid(boolean valid) {
        this.valid = valid;
    }

    public String getTicketCode() {
        return ticketCode;
    }

    public void setTicketCode(String ticketCode) {
        this.ticketCode = ticketCode;
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

    public TicketDTO getTicket() {
        return ticket;
    }

    public void setTicket(TicketDTO ticket) {
        this.ticket = ticket;
    }

    public LocalDateTime getVerifiedAt() {
        return verifiedAt;
    }

    public void setVerifiedAt(LocalDateTime verifiedAt) {
        this.verifiedAt = verifiedAt;
    }

    public String getVerifiedBy() {
        return verifiedBy;
    }

    public void setVerifiedBy(String verifiedBy) {
        this.verifiedBy = verifiedBy;
    }
}

