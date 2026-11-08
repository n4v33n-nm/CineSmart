package com.cinesmart.payment.gateway;

import com.cinesmart.payment.entity.PaymentMethod;

import java.math.BigDecimal;

public class PaymentGatewayRequest {

    private String transactionReference;
    private BigDecimal amount;
    private String currency;
    private PaymentMethod paymentMethod;
    private String idempotencyKey;
    private Boolean simulateSuccess;
    private String failureReason;

    public PaymentGatewayRequest() {
    }

    public PaymentGatewayRequest(String transactionReference,
                                 BigDecimal amount,
                                 String currency,
                                 PaymentMethod paymentMethod,
                                 String idempotencyKey,
                                 Boolean simulateSuccess,
                                 String failureReason) {
        this.transactionReference = transactionReference;
        this.amount = amount;
        this.currency = currency;
        this.paymentMethod = paymentMethod;
        this.idempotencyKey = idempotencyKey;
        this.simulateSuccess = simulateSuccess;
        this.failureReason = failureReason;
    }

    public String getTransactionReference() {
        return transactionReference;
    }

    public void setTransactionReference(String transactionReference) {
        this.transactionReference = transactionReference;
    }

    public BigDecimal getAmount() {
        return amount;
    }

    public void setAmount(BigDecimal amount) {
        this.amount = amount;
    }

    public String getCurrency() {
        return currency;
    }

    public void setCurrency(String currency) {
        this.currency = currency;
    }

    public PaymentMethod getPaymentMethod() {
        return paymentMethod;
    }

    public void setPaymentMethod(PaymentMethod paymentMethod) {
        this.paymentMethod = paymentMethod;
    }

    public String getIdempotencyKey() {
        return idempotencyKey;
    }

    public void setIdempotencyKey(String idempotencyKey) {
        this.idempotencyKey = idempotencyKey;
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
