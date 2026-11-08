package com.cinesmart.payment.gateway;

import java.math.BigDecimal;

public class RefundGatewayRequest {

    private String transactionReference;
    private String gatewayTxnId;
    private BigDecimal amount;
    private String reason;

    public RefundGatewayRequest() {
    }

    public RefundGatewayRequest(String transactionReference, String gatewayTxnId, BigDecimal amount, String reason) {
        this.transactionReference = transactionReference;
        this.gatewayTxnId = gatewayTxnId;
        this.amount = amount;
        this.reason = reason;
    }

    public String getTransactionReference() {
        return transactionReference;
    }

    public void setTransactionReference(String transactionReference) {
        this.transactionReference = transactionReference;
    }

    public String getGatewayTxnId() {
        return gatewayTxnId;
    }

    public void setGatewayTxnId(String gatewayTxnId) {
        this.gatewayTxnId = gatewayTxnId;
    }

    public BigDecimal getAmount() {
        return amount;
    }

    public void setAmount(BigDecimal amount) {
        this.amount = amount;
    }

    public String getReason() {
        return reason;
    }

    public void setReason(String reason) {
        this.reason = reason;
    }
}
