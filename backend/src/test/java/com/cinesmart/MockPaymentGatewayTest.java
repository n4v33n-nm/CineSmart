package com.cinesmart;

import com.cinesmart.payment.entity.PaymentMethod;
import com.cinesmart.payment.gateway.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("MockPaymentGateway Strategy Unit Tests")
class MockPaymentGatewayTest {

    private MockPaymentGateway gateway;

    @BeforeEach
    void setUp() {
        gateway = new MockPaymentGateway();
    }

    @Test
    @DisplayName("Process payment successfully with valid request")
    void testProcessPaymentSuccess() {
        PaymentGatewayRequest request = new PaymentGatewayRequest(
                "TXN-001",
                BigDecimal.valueOf(250.00),
                "INR",
                PaymentMethod.UPI,
                "IDEM-1",
                true,
                null
        );

        PaymentGatewayResponse response = gateway.processPayment(request);

        assertTrue(response.isSuccess());
        assertNotNull(response.getGatewayTxnId());
        assertTrue(response.getGatewayTxnId().startsWith("MOCK-GW-"));
        assertEquals("Simulated payment captured successfully", response.getStatusMessage());
    }

    @Test
    @DisplayName("Process payment decline when simulateSuccess is false")
    void testProcessPaymentDecline() {
        PaymentGatewayRequest request = new PaymentGatewayRequest(
                "TXN-002",
                BigDecimal.valueOf(250.00),
                "INR",
                PaymentMethod.CARD,
                "IDEM-2",
                false,
                "Card declined by issuer"
        );

        PaymentGatewayResponse response = gateway.processPayment(request);

        assertFalse(response.isSuccess());
        assertNull(response.getGatewayTxnId());
        assertEquals("PAYMENT_DECLINED", response.getErrorCode());
        assertEquals("Card declined by issuer", response.getStatusMessage());
    }

    @Test
    @DisplayName("Process payment rejects non-positive amounts")
    void testProcessPaymentInvalidAmount() {
        PaymentGatewayRequest request = new PaymentGatewayRequest(
                "TXN-003",
                BigDecimal.ZERO,
                "INR",
                PaymentMethod.CARD,
                "IDEM-3",
                true,
                null
        );

        PaymentGatewayResponse response = gateway.processPayment(request);

        assertFalse(response.isSuccess());
        assertEquals("INVALID_AMOUNT", response.getErrorCode());
    }

    @Test
    @DisplayName("Process refund successfully")
    void testProcessRefundSuccess() {
        RefundGatewayRequest request = new RefundGatewayRequest(
                "TXN-001",
                "MOCK-GW-12345",
                BigDecimal.valueOf(250.00),
                "Customer requested cancellation"
        );

        RefundGatewayResponse response = gateway.processRefund(request);

        assertTrue(response.isSuccess());
        assertNotNull(response.getRefundReference());
        assertTrue(response.getRefundReference().startsWith("MOCK-REF-"));
    }

    @Test
    @DisplayName("Verify gateway type is MOCK")
    void testGatewayType() {
        assertEquals(PaymentGatewayType.MOCK, gateway.getGatewayType());
    }
}
