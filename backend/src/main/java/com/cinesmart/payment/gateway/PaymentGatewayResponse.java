package com.cinesmart.payment.gateway;

public class PaymentGatewayResponse {

    private boolean success;
    private String gatewayTxnId;
    private String statusMessage;
    private String errorCode;

    public PaymentGatewayResponse() {
    }

    public PaymentGatewayResponse(boolean success, String gatewayTxnId, String statusMessage, String errorCode) {
        this.success = success;
        this.gatewayTxnId = gatewayTxnId;
        this.statusMessage = statusMessage;
        this.errorCode = errorCode;
    }

    public static PaymentGatewayResponse success(String gatewayTxnId, String statusMessage) {
        return new PaymentGatewayResponse(true, gatewayTxnId, statusMessage, null);
    }

    public static PaymentGatewayResponse failure(String errorCode, String statusMessage) {
        return new PaymentGatewayResponse(false, null, statusMessage, errorCode);
    }

    public boolean isSuccess() {
        return success;
    }

    public void setSuccess(boolean success) {
        this.success = success;
    }

    public String getGatewayTxnId() {
        return gatewayTxnId;
    }

    public void setGatewayTxnId(String gatewayTxnId) {
        this.gatewayTxnId = gatewayTxnId;
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
