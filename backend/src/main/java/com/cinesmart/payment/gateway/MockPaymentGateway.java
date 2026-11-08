package com.cinesmart.payment.gateway;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.UUID;

@Component
public class MockPaymentGateway implements PaymentGateway {

    private static final Logger log = LoggerFactory.getLogger(MockPaymentGateway.class);

    @Override
    public PaymentGatewayResponse processPayment(PaymentGatewayRequest request) {
        log.info("MockPaymentGateway processing payment for ref: {}, method: {}, amount: {} {}",
                request.getTransactionReference(), request.getPaymentMethod(), request.getAmount(), request.getCurrency());

        // Validate basic request integrity
        if (request.getAmount() == null || request.getAmount().compareTo(BigDecimal.ZERO) <= 0) {
            return PaymentGatewayResponse.failure("INVALID_AMOUNT", "Payment amount must be greater than zero");
        }

        if (request.getPaymentMethod() == null) {
            return PaymentGatewayResponse.failure("MISSING_PAYMENT_METHOD", "A valid payment method is required");
        }

        // Allow deterministic failure simulation for testing/demo
        if (Boolean.FALSE.equals(request.getSimulateSuccess())) {
            String reason = (request.getFailureReason() != null && !request.getFailureReason().trim().isEmpty())
                    ? request.getFailureReason()
                    : "Payment declined by simulated issuing bank (Insufficient funds / card limit)";
            log.warn("MockPaymentGateway simulated payment decline: {}", reason);
            return PaymentGatewayResponse.failure("PAYMENT_DECLINED", reason);
        }

        // Generate realistic simulated gateway transaction ID
        String gatewayTxnId = "MOCK-GW-" + UUID.randomUUID().toString().substring(0, 12).toUpperCase();
        log.info("MockPaymentGateway payment successful, gatewayTxnId: {}", gatewayTxnId);
        return PaymentGatewayResponse.success(gatewayTxnId, "Simulated payment captured successfully");
    }

    @Override
    public RefundGatewayResponse processRefund(RefundGatewayRequest request) {
        log.info("MockPaymentGateway processing refund for ref: {}, gatewayTxnId: {}, amount: {}",
                request.getTransactionReference(), request.getGatewayTxnId(), request.getAmount());

        if (request.getAmount() == null || request.getAmount().compareTo(BigDecimal.ZERO) <= 0) {
            return RefundGatewayResponse.failure("INVALID_REFUND_AMOUNT", "Refund amount must be positive");
        }

        String refundRef = "MOCK-REF-" + UUID.randomUUID().toString().substring(0, 12).toUpperCase();
        log.info("MockPaymentGateway refund successful, refundRef: {}", refundRef);
        return RefundGatewayResponse.success(refundRef, "Simulated refund reversed to original payment method successfully");
    }

    @Override
    public PaymentGatewayType getGatewayType() {
        return PaymentGatewayType.MOCK;
    }
}
