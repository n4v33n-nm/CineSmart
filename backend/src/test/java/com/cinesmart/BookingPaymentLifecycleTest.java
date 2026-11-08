package com.cinesmart;

import com.cinesmart.booking.dto.BookingDTO;
import com.cinesmart.booking.entity.Booking;
import com.cinesmart.booking.entity.BookingSeat;
import com.cinesmart.booking.entity.BookingStatus;
import com.cinesmart.booking.repository.BookingRepository;
import com.cinesmart.booking.service.BookingService;
import com.cinesmart.common.exception.BadRequestException;
import com.cinesmart.common.exception.ResourceNotFoundException;
import com.cinesmart.payment.dto.PaymentInitiateRequest;
import com.cinesmart.payment.gateway.PaymentGateway;
import com.cinesmart.payment.repository.PaymentTransactionRepository;
import com.cinesmart.payment.service.PaymentService;
import com.cinesmart.seat.entity.Seat;
import com.cinesmart.seat.entity.SeatTier;
import com.cinesmart.show.entity.Show;
import com.cinesmart.show.entity.ShowSeat;
import com.cinesmart.show.entity.ShowSeatStatus;
import com.cinesmart.show.repository.ShowRepository;
import com.cinesmart.show.repository.ShowSeatRepository;
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
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("BookingPaymentLifecycle Unit Tests (Scenarios 11-16)")
class BookingPaymentLifecycleTest {

    @Mock
    private BookingRepository bookingRepository;
    @Mock
    private ShowRepository showRepository;
    @Mock
    private ShowSeatRepository showSeatRepository;
    @Mock
    private UserRepository userRepository;
    @Mock
    private TicketService ticketService;
    @Mock
    private PaymentService paymentService;
    @Mock
    private PaymentTransactionRepository paymentTransactionRepository;
    @Mock
    private PaymentGateway paymentGateway;

    @InjectMocks
    private BookingService bookingService;

    private User testUser;
    private User otherUser;
    private Show testShow;
    private ShowSeat showSeat1;
    private Booking booking;

    @BeforeEach
    void setUp() {
        testUser = new User("customer@test.com", "hash", "Jane Customer", "+123", UserRole.ROLE_CUSTOMER);
        testUser.setId(1L);

        otherUser = new User("other@test.com", "hash", "Other User", "+456", UserRole.ROLE_CUSTOMER);
        otherUser.setId(2L);

        testShow = new Show();
        testShow.setId(50L);
        testShow.setStartTime(LocalDateTime.now().plusDays(2)); // Future show

        Seat physicalSeat = new Seat(null, "E", 1, SeatTier.STANDARD, 1, 5);
        showSeat1 = new ShowSeat(testShow, physicalSeat, BigDecimal.valueOf(180.00));
        showSeat1.setId(501L);
        showSeat1.setStatus(ShowSeatStatus.HELD);

        booking = new Booking("CS-2026-LIFECYCLE", testUser, testShow, BigDecimal.valueOf(180.00));
        booking.setId(100L);
        booking.setStatus(BookingStatus.PENDING_PAYMENT);

        BookingSeat bs = new BookingSeat(booking, showSeat1, BigDecimal.valueOf(180.00), "E-1", "STANDARD");
        booking.addBookingSeat(bs);
    }

    @Test
    @DisplayName("Requirement 11: Successful payment confirmation confirms booking and permanently marks seats BOOKED")
    void testSuccessfulPaymentConfirmsBooking() {
        when(bookingRepository.findById(100L)).thenReturn(Optional.of(booking));
        when(bookingRepository.save(any(Booking.class))).thenAnswer(invocation -> invocation.getArgument(0));

        BookingDTO confirmed = bookingService.confirmBooking(100L);

        assertNotNull(confirmed);
        assertEquals(BookingStatus.CONFIRMED, confirmed.getStatus());
        assertEquals(ShowSeatStatus.BOOKED, showSeat1.getStatus());
        assertNotNull(booking.getConfirmedAt());

        verify(showSeatRepository, times(1)).saveAll(any());
        verify(ticketService, times(1)).generateTicketsForBooking(any());
    }

    @Test
    @DisplayName("Requirement 12: Failed payment does not confirm booking and releases held seats back to AVAILABLE")
    void testFailedPaymentReleasesSeats() {
        when(bookingRepository.findById(100L)).thenReturn(Optional.of(booking));
        when(bookingRepository.save(any(Booking.class))).thenAnswer(invocation -> invocation.getArgument(0));

        bookingService.handlePaymentFailure(100L, "Simulated decline");

        assertEquals(BookingStatus.PAYMENT_FAILED, booking.getStatus());
        assertEquals(ShowSeatStatus.AVAILABLE, showSeat1.getStatus());

        verify(showSeatRepository, times(1)).saveAll(any());
        verify(ticketService, never()).generateTicketsForBooking(any());
    }

    @Test
    @DisplayName("Requirement 13: Cancelled booking releases seats and voids tickets")
    void testCancelledBookingReleasesSeats() {
        booking.setStatus(BookingStatus.CONFIRMED);
        showSeat1.setStatus(ShowSeatStatus.BOOKED);

        when(bookingRepository.findById(100L)).thenReturn(Optional.of(booking));
        when(userRepository.findByEmail("customer@test.com")).thenReturn(Optional.of(testUser));
        when(bookingRepository.save(any(Booking.class))).thenAnswer(invocation -> invocation.getArgument(0));

        BookingDTO cancelled = bookingService.cancelBooking(100L, "customer@test.com");

        assertNotNull(cancelled);
        assertEquals(BookingStatus.CANCELLED, cancelled.getStatus());
        assertEquals(ShowSeatStatus.AVAILABLE, showSeat1.getStatus());

        verify(paymentService, times(1)).refundBookingPayment(any(), eq("customer@test.com"));
        verify(ticketService, times(1)).voidTicketsForBooking(any());
        verify(showSeatRepository, times(1)).saveAll(any());
    }

    @Test
    @DisplayName("Requirement 14: Confirmed booking cannot be double-confirmed (idempotent)")
    void testDoubleConfirmationIsIdempotent() {
        booking.setStatus(BookingStatus.CONFIRMED);

        when(bookingRepository.findById(100L)).thenReturn(Optional.of(booking));

        BookingDTO result = bookingService.confirmBooking(100L);

        assertEquals(BookingStatus.CONFIRMED, result.getStatus());
        // Should not re-save or re-generate tickets
        verify(ticketService, never()).generateTicketsForBooking(any());
    }

    @Test
    @DisplayName("Requirement 15: Invalid booking cannot be confirmed if not pending")
    void testInvalidBookingCannotBeConfirmed() {
        booking.setStatus(BookingStatus.CANCELLED);

        when(bookingRepository.findById(100L)).thenReturn(Optional.of(booking));

        assertThrows(IllegalStateException.class, () -> bookingService.confirmBooking(100L));
    }

    @Test
    @DisplayName("Requirement 16: Unauthorized user cannot view or cancel another user's booking")
    void testUnauthorizedUserAccessBookingDenied() {
        when(bookingRepository.findById(100L)).thenReturn(Optional.of(booking));
        when(userRepository.findByEmail("other@test.com")).thenReturn(Optional.of(otherUser));

        assertThrows(AccessDeniedException.class,
                () -> bookingService.getBookingById(100L, "other@test.com"));

        assertThrows(AccessDeniedException.class,
                () -> bookingService.cancelBooking(100L, "other@test.com"));
    }
}
