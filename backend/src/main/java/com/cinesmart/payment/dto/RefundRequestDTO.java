package com.cinesmart.payment.dto;

public class RefundRequestDTO {

    private String reason;

    public RefundRequestDTO() {
    }

    public RefundRequestDTO(String reason) {
        this.reason = reason;
    }

    public String getReason() {
        return reason;
    }

    public void setReason(String reason) {
        this.reason = reason;
    }
}
