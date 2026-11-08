package com.cinesmart;

import com.cinesmart.booking.entity.Booking;
import com.cinesmart.payment.entity.PaymentMethod;
import com.cinesmart.payment.entity.PaymentStatus;
import com.cinesmart.payment.entity.PaymentTransaction;
import com.cinesmart.user.entity.User;
import com.cinesmart.user.entity.UserRole;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("PaymentTransaction Domain Entity & State Transition Unit Tests")
class PaymentTransactionTest {

    private PaymentTransaction transaction;
    private Booking booking;
    private User user;

    @BeforeEach
    void setUp() {
        user = new User("customer@test.com", "hash", "Jane Customer", "+123", UserRole.ROLE_CUSTOMER);
        user.setId(10L);

        booking = new Booking("CS-2026-TEST01", user, null, BigDecimal.valueOf(360.00));
        booking.setId(100L);

        transaction = new PaymentTransaction(
                "TXN-2026-0001",
                booking,
                user,
                "IDEM-KEY-001",
                BigDecimal.valueOf(360.00),
                "INR",
                PaymentMethod.UPI
        );
    }

    @Test
    @DisplayName("1. Payment transaction is created with initial INITIATED status and correct values")
    void testInitialState() {
        assertEquals(PaymentStatus.INITIATED, transaction.getStatus());
        assertEquals(BigDecimal.valueOf(360.00), transaction.getAmount());
        assertEquals("INR", transaction.getCurrency());
        assertEquals(PaymentMethod.UPI, transaction.getPaymentMethod());
        assertEquals("TXN-2026-0001", transaction.getTransactionReference());
        assertEquals("IDEM-KEY-001", transaction.getIdempotencyKey());
        assertNotNull(transaction.getCreatedAt());
    }

    @Test
    @DisplayName("2. Valid transition: INITIATED -> PROCESSING -> SUCCESS")
    void testHappyPathTransitions() {
        transaction.markProcessing();
        assertEquals(PaymentStatus.PROCESSING, transaction.getStatus());

        transaction.markSuccess("GW-TXN-12345");
        assertEquals(PaymentStatus.SUCCESS, transaction.getStatus());
        assertEquals("GW-TXN-12345", transaction.getGatewayTxnId());
    }

    @Test
    @DisplayName("3. Valid transition: INITIATED -> PROCESSING -> FAILED")
    void testFailedTransition() {
        transaction.markProcessing();
        transaction.markFailed("Insufficient balance");
        assertEquals(PaymentStatus.FAILED, transaction.getStatus());
        assertEquals("Insufficient balance", transaction.getFailureReason());
    }

    @Test
    @DisplayName("4. Valid transition: INITIATED -> CANCELLED")
    void testCancelledTransition() {
        transaction.markCancelled();
        assertEquals(PaymentStatus.CANCELLED, transaction.getStatus());
    }

    @Test
    @DisplayName("5. Valid transition: SUCCESS -> REFUNDED")
    void testRefundTransition() {
        transaction.markProcessing();
        transaction.markSuccess("GW-TXN-12345");
        transaction.markRefunded("REF-TXN-999");
        assertEquals(PaymentStatus.REFUNDED, transaction.getStatus());
        assertTrue(transaction.getFailureReason().contains("REF-TXN-999"));
    }

    @Test
    @DisplayName("6. Invalid transitions: FAILED -> REFUNDED is rejected")
    void testInvalidFailedToRefunded() {
        transaction.markFailed("Card expired");
        IllegalStateException ex = assertThrows(IllegalStateException.class, () -> transaction.markRefunded("REF-123"));
        assertTrue(ex.getMessage().contains("Invalid payment state transition"));
    }

    @Test
    @DisplayName("7. Invalid transitions: INITIATED -> REFUNDED is rejected")
    void testInvalidInitiatedToRefunded() {
        IllegalStateException ex = assertThrows(IllegalStateException.class, () -> transaction.markRefunded("REF-123"));
        assertTrue(ex.getMessage().contains("Invalid payment state transition"));
    }

    @Test
    @DisplayName("8. Invalid transitions: REFUNDED -> SUCCESS is rejected")
    void testInvalidRefundedToSuccess() {
        transaction.markProcessing();
        transaction.markSuccess("GW-1");
        transaction.markRefunded("REF-1");
        IllegalStateException ex = assertThrows(IllegalStateException.class, () -> transaction.markSuccess("GW-2"));
        assertTrue(ex.getMessage().contains("Invalid payment state transition"));
    }

    @Test
    @DisplayName("9. Invalid transitions: SUCCESS -> FAILED is rejected")
    void testInvalidSuccessToFailed() {
        transaction.markProcessing();
        transaction.markSuccess("GW-1");
        IllegalStateException ex = assertThrows(IllegalStateException.class, () -> transaction.markFailed("Late failure"));
        assertTrue(ex.getMessage().contains("Invalid payment state transition"));
    }

    @Test
    @DisplayName("10. PaymentMethod parsing validates supported methods and rejects unknown")
    void testPaymentMethodParsing() {
        assertEquals(PaymentMethod.UPI, PaymentMethod.fromString("UPI"));
        assertEquals(PaymentMethod.CARD, PaymentMethod.fromString("card"));
        assertEquals(PaymentMethod.NET_BANKING, PaymentMethod.fromString("NET_BANKING"));
        assertEquals(PaymentMethod.WALLET, PaymentMethod.fromString("wallet"));

        assertThrows(RuntimeException.class, () -> PaymentMethod.fromString("BITCOIN"));
        assertThrows(RuntimeException.class, () -> PaymentMethod.fromString(""));
        assertThrows(RuntimeException.class, () -> PaymentMethod.fromString(null));
    }
}
