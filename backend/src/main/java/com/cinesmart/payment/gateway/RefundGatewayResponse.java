package com.cinesmart.payment.gateway;

public class RefundGatewayResponse {

    private boolean success;
    private String refundReference;
    private String statusMessage;
    private String errorCode;

    public RefundGatewayResponse() {
    }

    public RefundGatewayResponse(boolean success, String refundReference, String statusMessage, String errorCode) {
        this.success = success;
        this.refundReference = refundReference;
        this.statusMessage = statusMessage;
        this.errorCode = errorCode;
    }

    public static RefundGatewayResponse success(String refundReference, String statusMessage) {
        return new RefundGatewayResponse(true, refundReference, statusMessage, null);
    }

    public static RefundGatewayResponse failure(String errorCode, String statusMessage) {
        return new RefundGatewayResponse(false, null, statusMessage, errorCode);
    }

    public boolean isSuccess() {
        return success;
    }

    public void setSuccess(boolean success) {
        this.success = success;
    }

    public String getRefundReference() {
        return refundReference;
    }

    public void setRefundReference(String refundReference) {
        this.refundReference = refundReference;
    }

    public String getStatusMessage() {
        return statusMessage;
    }

    public void setStatusMessage(String statusMessage) {
        this.statusMessage = statusMessage;
    }

    public String getErrorCode() {
        return errorCode;
    }

    public void setErrorCode(String errorCode) {
        this.errorCode = errorCode;
    }
}
