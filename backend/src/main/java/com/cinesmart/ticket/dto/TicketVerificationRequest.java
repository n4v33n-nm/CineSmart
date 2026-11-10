package com.cinesmart.ticket.dto;

import jakarta.validation.constraints.NotBlank;

public class TicketVerificationRequest {

    @NotBlank(message = "Ticket code cannot be blank")
    private String ticketCode;

    private String verificationHash;

    public TicketVerificationRequest() {}

    public TicketVerificationRequest(String ticketCode) {
        this.ticketCode = ticketCode;
    }

    public TicketVerificationRequest(String ticketCode, String verificationHash) {
        this.ticketCode = ticketCode;
        this.verificationHash = verificationHash;
    }

    public String getTicketCode() {
        return ticketCode;
    }

    public void setTicketCode(String ticketCode) {
        this.ticketCode = ticketCode;
    }

    public String getVerificationHash() {
        return verificationHash;
    }

    public void setVerificationHash(String verificationHash) {
        this.verificationHash = verificationHash;
    }
}
