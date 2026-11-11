package com.cinesmart.refund.dto;

import jakarta.validation.constraints.Size;

public class BookingCancellationRequest {

    @Size(max = 500, message = "Reason cannot exceed 500 characters")
    private String reason;

    private Boolean simulateAutoProcess = true; // Auto execute simulated refund or hold for admin review

    public BookingCancellationRequest() {
    }

    public BookingCancellationRequest(String reason) {
        this.reason = reason;
    }

    public String getReason() {
        return reason;
    }

    public void setReason(String reason) {
        this.reason = reason;
    }

    public Boolean getSimulateAutoProcess() {
        return simulateAutoProcess;
    }

    public void setSimulateAutoProcess(Boolean simulateAutoProcess) {
        this.simulateAutoProcess = simulateAutoProcess;
    }
}

