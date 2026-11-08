package com.cinesmart.payment.gateway;

/**
 * Strategy interface defining payment gateway operations.
 * Allows switching between MockPaymentGateway, Razorpay, Stripe, etc.,
 * without modifying domain booking services.
 */
public interface PaymentGateway {

    PaymentGatewayResponse processPayment(PaymentGatewayRequest request);

    RefundGatewayResponse processRefund(RefundGatewayRequest request);

    PaymentGatewayType getGatewayType();
}
