package com.cinesmart;

import com.cinesmart.booking.entity.Booking;
import com.cinesmart.booking.entity.BookingStatus;
import com.cinesmart.booking.repository.BookingRepository;
import com.cinesmart.booking.service.BookingService;
import com.cinesmart.common.exception.BadRequestException;
import com.cinesmart.coupon.service.CouponService;
import com.cinesmart.loyalty.service.LoyaltyService;
import com.cinesmart.payment.dto.*;
import com.cinesmart.payment.entity.PaymentMethod;
import com.cinesmart.payment.entity.PaymentStatus;
import com.cinesmart.payment.entity.PaymentTransaction;
import com.cinesmart.payment.gateway.*;
import com.cinesmart.payment.repository.PaymentTransactionRepository;
import com.cinesmart.payment.service.PaymentService;
import com.cinesmart.ticket.service.TicketService;
import com.cinesmart.user.entity.User;
import com.cinesmart.user.entity.UserRole;
import com.cinesmart.user.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.access.AccessDeniedException;

import java.math.BigDecimal;
import java.util.Collections;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("PaymentService Comprehensive Unit Tests (Scenarios 1-10)")
class PaymentServiceTest {

    @Mock
    private PaymentTransactionRepository paymentTransactionRepository;
    @Mock
    private BookingRepository bookingRepository;
    @Mock
    private UserRepository userRepository;
    @Mock
    private PaymentGateway paymentGateway;
    @Mock
    private BookingService bookingService;
    @Mock
    private TicketService ticketService;
    @Mock
    private CouponService couponService;
    @Mock
    private LoyaltyService loyaltyService;

    @InjectMocks
    private PaymentService paymentService;

    private User testUser;
    private User otherUser;
    private Booking testBooking;

    @BeforeEach
    void setUp() {
        testUser = new User("customer@test.com", "hash", "Jane Customer", "+123", UserRole.ROLE_CUSTOMER);
        testUser.setId(1L);

        otherUser = new User("other@test.com", "hash", "Other User", "+456", UserRole.ROLE_CUSTOMER);
        otherUser.setId(2L);

        testBooking = new Booking("CS-2026-TESTREF", testUser, null, BigDecimal.valueOf(300.00));
        testBooking.setId(10L);
        testBooking.setStatus(BookingStatus.PENDING_PAYMENT);
    }

    @Test
    @DisplayName("Requirement 1 & 10: Payment transaction can be created and amount is strictly calculated server-side")
    void testPaymentTransactionCanBeCreatedServerSideAmount() {
        PaymentInitiateRequest request = new PaymentInitiateRequest(10L, "UPI");

        when(userRepository.findByEmail("customer@test.com")).thenReturn(Optional.of(testUser));
        when(bookingRepository.findById(10L)).thenReturn(Optional.of(testBooking));
        when(paymentTransactionRepository.save(any(PaymentTransaction.class))).thenAnswer(invocation -> {
            PaymentTransaction txn = invocation.getArgument(0);
            txn.setId(101L);
            return txn;
        });

        PaymentResponseDTO response = paymentService.initiatePayment("customer@test.com", request, false);

        assertNotNull(response);
        assertEquals(101L, response.getId());
        assertEquals(PaymentStatus.INITIATED, response.getStatus());
        // Verify server-side calculation: amount matches booking total (300.00)
        assertEquals(BigDecimal.valueOf(300.00), response.getAmount());
        assertEquals(PaymentMethod.UPI, response.getPaymentMethod());
        assertEquals("CS-2026-TESTREF", response.getBookingReference());

        verify(paymentTransactionRepository, times(1)).save(any(PaymentTransaction.class));
    }

    @Test
    @DisplayName("Requirement 2: Valid payment succeeds and transitions to SUCCESS")
    void testValidPaymentSucceeds() {
        PaymentInitiateRequest request = new PaymentInitiateRequest(10L, "CARD", "IDEM-ABC", true, null);

        when(userRepository.findByEmail("customer@test.com")).thenReturn(Optional.of(testUser));
        when(bookingRepository.findById(10L)).thenReturn(Optional.of(testBooking));
        when(paymentGateway.processPayment(any(PaymentGatewayRequest.class)))
                .thenReturn(PaymentGatewayResponse.success("GW-TXN-100", "Approved"));
        when(paymentTransactionRepository.save(any(PaymentTransaction.class))).thenAnswer(invocation -> {
            PaymentTransaction txn = invocation.getArgument(0);
            txn.setId(201L);
            return txn;
        });

        PaymentResponseDTO response = paymentService.initiatePayment("customer@test.com", request, true);

        assertNotNull(response);
        assertEquals(PaymentStatus.SUCCESS, response.getStatus());
        assertEquals("GW-TXN-100", response.getGatewayTxnId());
        verify(bookingService, times(1)).confirmBooking(10L);
    }

    @Test
    @DisplayName("Requirement 3: Invalid payment method is rejected")
    void testInvalidPaymentMethodRejected() {
        PaymentInitiateRequest request = new PaymentInitiateRequest(10L, "BITCOIN");

        when(userRepository.findByEmail("customer@test.com")).thenReturn(Optional.of(testUser));
        when(bookingRepository.findById(10L)).thenReturn(Optional.of(testBooking));

        BadRequestException ex = assertThrows(BadRequestException.class,
                () -> paymentService.initiatePayment("customer@test.com", request, false));

        assertEquals("INVALID_PAYMENT_METHOD", ex.getErrorCode());
        verify(paymentTransactionRepository, never()).save(any());
    }

    @Test
    @DisplayName("Requirement 4: Failed payment changes status correctly to FAILED and calls handlePaymentFailure")
    void testFailedPaymentChangesStatusCorrectly() {
        PaymentInitiateRequest request = new PaymentInitiateRequest(10L, "WALLET", "IDEM-FAIL", false, "Insufficient funds in wallet");

        when(userRepository.findByEmail("customer@test.com")).thenReturn(Optional.of(testUser));
        when(bookingRepository.findById(10L)).thenReturn(Optional.of(testBooking));
        when(paymentGateway.processPayment(any(PaymentGatewayRequest.class)))
                .thenReturn(PaymentGatewayResponse.failure("PAYMENT_DECLINED", "Insufficient funds in wallet"));
        when(paymentTransactionRepository.save(any(PaymentTransaction.class))).thenAnswer(invocation -> {
            PaymentTransaction txn = invocation.getArgument(0);
            txn.setId(301L);
            return txn;
        });

        PaymentResponseDTO response = paymentService.initiatePayment("customer@test.com", request, true);

        assertNotNull(response);
        assertEquals(PaymentStatus.FAILED, response.getStatus());
        assertEquals("Insufficient funds in wallet", response.getFailureReason());
        verify(bookingService, times(1)).handlePaymentFailure(10L, "Insufficient funds in wallet");
        verify(bookingService, never()).confirmBooking(anyLong());
    }

    @Test
    @DisplayName("Requirement 5: Duplicate payment is prevented (idempotency)")
    void testDuplicatePaymentPrevented() {
        PaymentTransaction existingSuccess = new PaymentTransaction(
                "TXN-EXISTING",
                testBooking,
                testUser,
                "IDEM-KEY-DUP",
                BigDecimal.valueOf(300.00),
                "INR",
                PaymentMethod.UPI
        );
        existingSuccess.setStatus(PaymentStatus.SUCCESS);

        when(userRepository.findByEmail("customer@test.com")).thenReturn(Optional.of(testUser));
        when(bookingRepository.findById(10L)).thenReturn(Optional.of(testBooking));
        when(paymentTransactionRepository.findByIdempotencyKey("IDEM-KEY-DUP")).thenReturn(Optional.of(existingSuccess));

        PaymentInitiateRequest request = new PaymentInitiateRequest(10L, "UPI", "IDEM-KEY-DUP", true, null);
        PaymentResponseDTO response = paymentService.initiatePayment("customer@test.com", request, true);

        // Should return existing success without re-charging or calling gateway
        assertEquals(PaymentStatus.SUCCESS, response.getStatus());
        verify(paymentGateway, never()).processPayment(any());
    }

    @Test
    @DisplayName("Requirement 6: Invalid payment state transition is rejected")
    void testInvalidPaymentStateTransitionRejected() {
        PaymentTransaction failedTxn = new PaymentTransaction(
                "TXN-FAILED",
                testBooking,
                testUser,
                "IDEM-FAIL",
                BigDecimal.valueOf(300.00),
                "INR",
                PaymentMethod.UPI
        );
        failedTxn.setStatus(PaymentStatus.FAILED);
        failedTxn.setId(401L);

        when(userRepository.findByEmail("customer@test.com")).thenReturn(Optional.of(testUser));
        when(paymentTransactionRepository.findById(401L)).thenReturn(Optional.of(failedTxn));

        IllegalStateException ex = assertThrows(IllegalStateException.class,
                () -> paymentService.processPayment(401L, "customer@test.com", new PaymentProcessRequest(true, null)));

        assertTrue(ex.getMessage().contains("Cannot process transaction in status FAILED"));
    }

    @Test
    @DisplayName("Requirement 7: Refund succeeds for an eligible successful payment")
    void testRefundSucceedsForEligiblePayment() {
        PaymentTransaction successTxn = new PaymentTransaction(
                "TXN-SUCCESS",
                testBooking,
                testUser,
                "IDEM-REFUND",
                BigDecimal.valueOf(300.00),
                "INR",
                PaymentMethod.UPI
        );
        successTxn.setId(501L);
        successTxn.setStatus(PaymentStatus.SUCCESS);
        successTxn.setGatewayTxnId("MOCK-GW-123");

        when(userRepository.findByEmail("customer@test.com")).thenReturn(Optional.of(testUser));
        when(paymentTransactionRepository.findById(501L)).thenReturn(Optional.of(successTxn));
        when(paymentGateway.processRefund(any(RefundGatewayRequest.class)))
                .thenReturn(RefundGatewayResponse.success("MOCK-REF-777", "Refunded successfully"));

        RefundResponseDTO refundResponse = paymentService.refundPayment(501L, "customer@test.com", new RefundRequestDTO("Customer cancellation"));

        assertNotNull(refundResponse);
        assertEquals(PaymentStatus.REFUNDED.name(), refundResponse.getStatus());
        assertEquals("MOCK-REF-777", refundResponse.getRefundReference());
        assertEquals(PaymentStatus.REFUNDED, successTxn.getStatus());
        verify(bookingService, times(1)).cancelBooking(10L, "customer@test.com");
    }

    @Test
    @DisplayName("Requirement 8: Duplicate refund is prevented")
    void testDuplicateRefundPrevented() {
        PaymentTransaction refundedTxn = new PaymentTransaction(
                "TXN-ALREADY-REFUNDED",
                testBooking,
                testUser,
                "IDEM-REF",
                BigDecimal.valueOf(300.00),
                "INR",
                PaymentMethod.UPI
        );
        refundedTxn.setId(601L);
        refundedTxn.setStatus(PaymentStatus.REFUNDED);

        when(userRepository.findByEmail("customer@test.com")).thenReturn(Optional.of(testUser));
        when(paymentTransactionRepository.findById(601L)).thenReturn(Optional.of(refundedTxn));

        BadRequestException ex = assertThrows(BadRequestException.class,
                () -> paymentService.refundPayment(601L, "customer@test.com", new RefundRequestDTO("Duplicate attempt")));

        assertEquals("ALREADY_REFUNDED", ex.getErrorCode());
        verify(paymentGateway, never()).processRefund(any());
    }

    @Test
    @DisplayName("Requirement 9: Failed payment cannot be refunded")
    void testFailedPaymentCannotBeRefunded() {
        PaymentTransaction failedTxn = new PaymentTransaction(
                "TXN-FAILED",
                testBooking,
                testUser,
                "IDEM-FAIL",
                BigDecimal.valueOf(300.00),
                "INR",
                PaymentMethod.CARD
        );
        failedTxn.setId(701L);
        failedTxn.setStatus(PaymentStatus.FAILED);

        when(userRepository.findByEmail("customer@test.com")).thenReturn(Optional.of(testUser));
        when(paymentTransactionRepository.findById(701L)).thenReturn(Optional.of(failedTxn));

        BadRequestException ex = assertThrows(BadRequestException.class,
                () -> paymentService.refundPayment(701L, "customer@test.com", new RefundRequestDTO("Try refund")));

        assertEquals("INVALID_REFUND_STATE", ex.getErrorCode());
        verify(paymentGateway, never()).processRefund(any());
    }

    @Test
    @DisplayName("Requirement 16: Unauthorized users cannot access another user's payment transaction")
    void testUnauthorizedUserAccessDenied() {
        PaymentTransaction userTxn = new PaymentTransaction(
                "TXN-SECURE",
                testBooking,
                testUser,
                "IDEM-SEC",
                BigDecimal.valueOf(300.00),
                "INR",
                PaymentMethod.UPI
        );
        userTxn.setId(801L);

        when(userRepository.findByEmail("other@test.com")).thenReturn(Optional.of(otherUser));
        when(paymentTransactionRepository.findById(801L)).thenReturn(Optional.of(userTxn));

        assertThrows(AccessDeniedException.class,
                () -> paymentService.getPaymentById(801L, "other@test.com"));
    }
}
