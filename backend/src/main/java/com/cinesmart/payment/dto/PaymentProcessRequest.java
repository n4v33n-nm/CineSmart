package com.cinesmart.payment.dto;

public class PaymentProcessRequest {

    private Boolean simulateSuccess = true;
    private String failureReason;

    public PaymentProcessRequest() {
    }

    public PaymentProcessRequest(Boolean simulateSuccess, String failureReason) {
        this.simulateSuccess = simulateSuccess;
        this.failureReason = failureReason;
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
