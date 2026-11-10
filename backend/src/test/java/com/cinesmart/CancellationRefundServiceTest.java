package com.cinesmart;

import com.cinesmart.booking.entity.Booking;
import com.cinesmart.booking.entity.BookingSeat;
import com.cinesmart.booking.entity.BookingStatus;
import com.cinesmart.booking.repository.BookingRepository;
import com.cinesmart.cinema.entity.Cinema;
import com.cinesmart.cinema.entity.ScreenType;
import com.cinesmart.common.exception.BadRequestException;
import com.cinesmart.coupon.service.CouponService;
import com.cinesmart.loyalty.entity.LoyaltyTransaction;
import com.cinesmart.loyalty.entity.LoyaltyTransactionType;
import com.cinesmart.loyalty.repository.LoyaltyTransactionRepository;
import com.cinesmart.loyalty.service.LoyaltyService;
import com.cinesmart.movie.entity.Movie;
import com.cinesmart.payment.entity.PaymentMethod;
import com.cinesmart.payment.entity.PaymentStatus;
import com.cinesmart.payment.entity.PaymentTransaction;
import com.cinesmart.payment.gateway.PaymentGateway;
import com.cinesmart.payment.gateway.RefundGatewayRequest;
import com.cinesmart.payment.gateway.RefundGatewayResponse;
import com.cinesmart.payment.repository.PaymentTransactionRepository;
import com.cinesmart.refund.dto.BookingCancellationRequest;
import com.cinesmart.refund.dto.CancellationPreviewResponseDTO;
import com.cinesmart.refund.dto.RefundAdminActionRequest;
import com.cinesmart.refund.dto.RefundRequestDTO;
import com.cinesmart.refund.entity.CancellationPolicy;
import com.cinesmart.refund.entity.RefundRequest;
import com.cinesmart.refund.entity.RefundStatus;
import com.cinesmart.refund.repository.CancellationPolicyRepository;
import com.cinesmart.refund.repository.RefundRequestRepository;
import com.cinesmart.refund.service.CancellationPolicyService;
import com.cinesmart.refund.service.RefundService;
import com.cinesmart.screen.entity.Screen;
import com.cinesmart.seat.entity.Seat;
import com.cinesmart.seat.entity.SeatTier;
import com.cinesmart.show.entity.Show;
import com.cinesmart.show.entity.ShowSeat;
import com.cinesmart.show.entity.ShowSeatStatus;
import com.cinesmart.show.repository.ShowSeatRepository;
import com.cinesmart.ticket.entity.Ticket;
import com.cinesmart.ticket.entity.TicketStatus;
import com.cinesmart.ticket.repository.TicketRepository;
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
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
@DisplayName("Cancellation and Refund Management Unit Tests")
class CancellationRefundServiceTest {

    @Mock
    private RefundRequestRepository refundRequestRepository;

    @Mock
    private CancellationPolicyRepository cancellationPolicyRepository;

    @Mock
    private BookingRepository bookingRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private PaymentTransactionRepository paymentTransactionRepository;

    @Mock
    private PaymentGateway paymentGateway;

    @Mock
    private TicketRepository ticketRepository;

    @Mock
    private TicketService ticketService;

    @Mock
    private ShowSeatRepository showSeatRepository;

    @Mock
    private CouponService couponService;

    @Mock
    private LoyaltyService loyaltyService;

    @Mock
    private LoyaltyTransactionRepository loyaltyTransactionRepository;

    private CancellationPolicyService cancellationPolicyService;
    private RefundService refundService;

    private User customer;
    private User admin;
    private Movie movie;
    private Cinema cinema;
    private Screen screen;
    private Show futureShow;
    private Show pastShow;
    private Booking confirmedBooking;
    private PaymentTransaction paymentTxn;

    private CancellationPolicy policyEarly;
    private CancellationPolicy policyStandard;
    private CancellationPolicy policyLate;

    @BeforeEach
    void setUp() {
        cancellationPolicyService = new CancellationPolicyService(cancellationPolicyRepository);
        refundService = new RefundService(
                refundRequestRepository,
                cancellationPolicyService,
                bookingRepository,
                userRepository,
                paymentTransactionRepository,
                paymentGateway,
                ticketRepository,
                ticketService,
                showSeatRepository,
                couponService,
                loyaltyService,
                loyaltyTransactionRepository
        );

        customer = new User("customer@test.com", "hash", "Test Customer", "+919876543210", UserRole.ROLE_CUSTOMER);
        customer.setId(10L);

        admin = new User("admin@test.com", "hash", "Admin Manager", "+919876543211", UserRole.ROLE_ADMIN);
        admin.setId(1L);

        movie = new Movie("Inception", "Mind-bending thriller", 148, "English", "Sci-Fi", "PG-13", "poster.jpg");
        movie.setId(100L);

        cinema = new Cinema("CineSmart Central", "Bangalore", "Downtown", "+919999999999");
        cinema.setId(1L);

        screen = new Screen(cinema, 1, ScreenType.IMAX_3D, 100);
        screen.setId(1L);

        // Future show in 48 hours (>24h)
        futureShow = new Show(movie, screen, LocalDateTime.now().plusHours(48), LocalDateTime.now().plusHours(51), BigDecimal.valueOf(250.00));
        futureShow.setId(500L);

        // Past show
        pastShow = new Show(movie, screen, LocalDateTime.now().minusHours(2), LocalDateTime.now().plusHours(1), BigDecimal.valueOf(250.00));
        pastShow.setId(501L);

        confirmedBooking = new Booking("CS-BKG-TEST001", customer, futureShow, BigDecimal.valueOf(500.00));
        confirmedBooking.setId(1001L);
        confirmedBooking.confirm();

        Seat s1 = new Seat(screen, "A", 1, SeatTier.STANDARD, 0, 0);
        ShowSeat ss1 = new ShowSeat(futureShow, s1, BigDecimal.valueOf(250.00));
        ss1.setStatus(ShowSeatStatus.BOOKED);

        Seat s2 = new Seat(screen, "A", 2, SeatTier.STANDARD, 0, 1);
        ShowSeat ss2 = new ShowSeat(futureShow, s2, BigDecimal.valueOf(250.00));
        ss2.setStatus(ShowSeatStatus.BOOKED);

        BookingSeat bs1 = new BookingSeat(confirmedBooking, ss1, BigDecimal.valueOf(250.00), "A-1", "STANDARD");
        BookingSeat bs2 = new BookingSeat(confirmedBooking, ss2, BigDecimal.valueOf(250.00), "A-2", "STANDARD");
        confirmedBooking.addBookingSeat(bs1);
        confirmedBooking.addBookingSeat(bs2);

        paymentTxn = new PaymentTransaction("TXN-TEST-123", confirmedBooking, customer, "IDEMP-TEST-001", BigDecimal.valueOf(500.00), "INR", PaymentMethod.CARD);
        paymentTxn.setId(2001L);
        paymentTxn.markProcessing();
        paymentTxn.markSuccess("GW-SUCCESS-123");

        // Policies: >24h = 100%, 6-24h = 75%, <6h = 0%
        policyEarly = new CancellationPolicy("Early (>24h)", "100% refund", 24, null, BigDecimal.valueOf(100.00), BigDecimal.ZERO, 1);
        policyEarly.setId(1L);

        policyStandard = new CancellationPolicy("Standard (6-24h)", "75% refund", 6, 24, BigDecimal.valueOf(75.00), BigDecimal.valueOf(25.00), 2);
        policyStandard.setId(2L);

        policyLate = new CancellationPolicy("Late (<6h)", "0% refund", 0, 6, BigDecimal.ZERO, BigDecimal.valueOf(100.00), 3);
        policyLate.setId(3L);

        when(cancellationPolicyRepository.findActivePoliciesForEvaluation())
                .thenReturn(List.of(policyEarly, policyStandard, policyLate));

        when(userRepository.findByEmail("customer@test.com")).thenReturn(Optional.of(customer));
        when(userRepository.findByEmail("admin@test.com")).thenReturn(Optional.of(admin));
        when(bookingRepository.findById(1001L)).thenReturn(Optional.of(confirmedBooking));
        when(paymentTransactionRepository.findByBookingIdAndStatus(1001L, PaymentStatus.SUCCESS)).thenReturn(Optional.of(paymentTxn));
    }

    @Test
    @DisplayName("Should match Early Policy (100% refund) when cancellation is more than 24 hours before show")
    void testPolicyMatchingEarly() {
        CancellationPolicy matched = cancellationPolicyService.findMatchingPolicy(30.0);
        assertNotNull(matched);
        assertEquals(BigDecimal.valueOf(100.00), matched.getRefundPercentage());
        assertEquals(BigDecimal.ZERO, matched.getCancellationFeePercent());
    }

    @Test
    @DisplayName("Should match Standard Policy (75% refund) when cancellation is between 6 and 24 hours before show")
    void testPolicyMatchingStandard() {
        CancellationPolicy matched = cancellationPolicyService.findMatchingPolicy(12.0);
        assertNotNull(matched);
        assertEquals(BigDecimal.valueOf(75.00), matched.getRefundPercentage());
        assertEquals(BigDecimal.valueOf(25.00), matched.getCancellationFeePercent());
    }

    @Test
    @DisplayName("Should match Late Policy (0% refund) when cancellation is within 6 hours of show")
    void testPolicyMatchingLate() {
        CancellationPolicy matched = cancellationPolicyService.findMatchingPolicy(3.5);
        assertNotNull(matched);
        assertEquals(BigDecimal.ZERO, matched.getRefundPercentage());
        assertEquals(BigDecimal.valueOf(100.00), matched.getCancellationFeePercent());
    }

    @Test
    @DisplayName("Preview cancellation should calculate 100% refund for show in 48 hours")
    void testPreviewCancellationEligible() {
        when(ticketRepository.findByBookingId(1001L)).thenReturn(Collections.emptyList());

        CancellationPreviewResponseDTO preview = refundService.previewCancellation(1001L, "customer@test.com");
        assertTrue(preview.isEligible());
        assertEquals(0, preview.getPaidAmount().compareTo(BigDecimal.valueOf(500.00)));
        assertEquals(0, preview.getRefundPercentage().compareTo(BigDecimal.valueOf(100.00)));
        assertEquals(0, preview.getCancellationFee().compareTo(BigDecimal.ZERO));
        assertEquals(0, preview.getEstimatedRefundAmount().compareTo(new BigDecimal("500.00")));
    }

    @Test
    @DisplayName("Preview cancellation should reject when show has already started")
    void testPreviewCancellationShowStarted() {
        Booking pastBooking = new Booking("CS-PAST-01", customer, pastShow, BigDecimal.valueOf(500.00));
        pastBooking.setId(1002L);
        pastBooking.confirm();
        when(bookingRepository.findById(1002L)).thenReturn(Optional.of(pastBooking));

        CancellationPreviewResponseDTO preview = refundService.previewCancellation(1002L, "customer@test.com");
        assertFalse(preview.isEligible());
        assertTrue(preview.getIneligibilityReason().contains("already started"));
    }

    @Test
    @DisplayName("Preview cancellation should reject when ticket has already been used for entry")
    void testPreviewCancellationTicketUsed() {
        Ticket usedTicket = new Ticket();
        usedTicket.setStatus(TicketStatus.USED);
        when(ticketRepository.findByBookingId(1001L)).thenReturn(List.of(usedTicket));

        CancellationPreviewResponseDTO preview = refundService.previewCancellation(1001L, "customer@test.com");
        assertFalse(preview.isEligible());
        assertTrue(preview.getIneligibilityReason().contains("already been scanned"));
    }

    @Test
    @DisplayName("Cancel booking with auto simulated refund should transition refund request to COMPLETED")
    void testCancelBookingWithAutoRefund() {
        when(ticketRepository.findByBookingId(1001L)).thenReturn(Collections.emptyList());
        when(refundRequestRepository.existsByBookingIdAndStatusNotIn(eq(1001L), any())).thenReturn(false);

        RefundGatewayResponse mockGwResp = RefundGatewayResponse.success("MOCK-REF-XYZ789", "Simulated refund successful");
        when(paymentGateway.processRefund(any(RefundGatewayRequest.class))).thenReturn(mockGwResp);

        when(refundRequestRepository.save(any(RefundRequest.class))).thenAnswer(invocation -> invocation.getArgument(0));

        BookingCancellationRequest req = new BookingCancellationRequest("Travel plans changed");
        req.setSimulateAutoProcess(true);

        RefundRequestDTO result = refundService.cancelBookingWithRefund(1001L, "customer@test.com", req);

        assertNotNull(result);
        assertEquals(RefundStatus.COMPLETED.name(), result.getStatus());
        assertEquals(new BigDecimal("500.00"), result.getRefundAmount());
        assertEquals("MOCK-REF-XYZ789", result.getGatewayRefundId());

        // Verify seats released
        verify(showSeatRepository, times(1)).saveAll(any());
        // Verify tickets voided
        verify(ticketService, times(1)).voidTicketsForBooking(any());
        // Verify loyalty points reversed
        verify(loyaltyService, times(1)).reversePointsForBooking(any());
        // Verify coupon released
        verify(couponService, times(1)).releaseCouponUsage(1001L);
    }

    @Test
    @DisplayName("Cancel booking within 6h window should complete with 0 refund and 100% fee retained")
    void testCancelBookingLateWindow() {
        // Show in 2 hours
        Show soonShow = new Show(movie, screen, LocalDateTime.now().plusHours(2), LocalDateTime.now().plusHours(5), BigDecimal.valueOf(250.00));
        soonShow.setId(505L);
        Booking soonBooking = new Booking("CS-SOON-01", customer, soonShow, BigDecimal.valueOf(400.00));
        soonBooking.setId(1003L);
        soonBooking.confirm();

        when(bookingRepository.findById(1003L)).thenReturn(Optional.of(soonBooking));
        when(ticketRepository.findByBookingId(1003L)).thenReturn(Collections.emptyList());
        when(refundRequestRepository.existsByBookingIdAndStatusNotIn(eq(1003L), any())).thenReturn(false);
        when(refundRequestRepository.save(any(RefundRequest.class))).thenAnswer(invocation -> invocation.getArgument(0));

        BookingCancellationRequest req = new BookingCancellationRequest("Late cancellation");
        RefundRequestDTO result = refundService.cancelBookingWithRefund(1003L, "customer@test.com", req);

        assertNotNull(result);
        assertEquals(RefundStatus.COMPLETED.name(), result.getStatus());
        assertEquals(0, result.getRefundAmount().compareTo(BigDecimal.ZERO));
        assertEquals(0, result.getCancellationFee().compareTo(BigDecimal.valueOf(400.00)));
    }

    @Test
    @DisplayName("Admin approve and process refund workflow")
    void testAdminApproveAndProcessRefund() {
        RefundRequest pendingReq = new RefundRequest(
                "REF-2026-TEST01", confirmedBooking, paymentTxn, customer, policyEarly,
                BigDecimal.valueOf(500.00), BigDecimal.ZERO, BigDecimal.valueOf(500.00),
                BigDecimal.valueOf(100.00), BigDecimal.valueOf(30.0), "Customer requested"
        );
        pendingReq.setId(5001L);

        when(refundRequestRepository.findById(5001L)).thenReturn(Optional.of(pendingReq));
        when(refundRequestRepository.save(any(RefundRequest.class))).thenAnswer(invocation -> invocation.getArgument(0));

        // 1. Approve
        RefundAdminActionRequest approveAction = new RefundAdminActionRequest("Approved by manager");
        RefundRequestDTO approvedDto = refundService.approveRefund(5001L, "admin@test.com", approveAction);
        assertEquals(RefundStatus.APPROVED.name(), approvedDto.getStatus());

        // 2. Process
        RefundGatewayResponse mockGwResp = RefundGatewayResponse.success("MOCK-REF-APPROVED-001", "Simulated refund successful");
        when(paymentGateway.processRefund(any(RefundGatewayRequest.class))).thenReturn(mockGwResp);

        RefundAdminActionRequest processAction = new RefundAdminActionRequest("Disbursed to bank");
        RefundRequestDTO processedDto = refundService.processRefund(5001L, "admin@test.com", processAction);
        assertEquals(RefundStatus.COMPLETED.name(), processedDto.getStatus());
        assertEquals("MOCK-REF-APPROVED-001", processedDto.getGatewayRefundId());
    }
}
