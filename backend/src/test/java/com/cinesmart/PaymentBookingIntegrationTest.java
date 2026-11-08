package com.cinesmart;

import com.cinesmart.booking.dto.BookingCreateRequest;
import com.cinesmart.booking.dto.BookingDTO;
import com.cinesmart.booking.entity.BookingStatus;
import com.cinesmart.booking.repository.BookingRepository;
import com.cinesmart.booking.service.BookingService;
import com.cinesmart.cinema.entity.Cinema;
import com.cinesmart.cinema.entity.ScreenType;
import com.cinesmart.cinema.repository.CinemaRepository;
import com.cinesmart.movie.entity.Movie;
import com.cinesmart.movie.repository.MovieRepository;
import com.cinesmart.payment.dto.PaymentInitiateRequest;
import com.cinesmart.payment.dto.PaymentResponseDTO;
import com.cinesmart.payment.dto.RefundRequestDTO;
import com.cinesmart.payment.dto.RefundResponseDTO;
import com.cinesmart.payment.entity.PaymentStatus;
import com.cinesmart.payment.repository.PaymentTransactionRepository;
import com.cinesmart.payment.service.PaymentService;
import com.cinesmart.screen.entity.Screen;
import com.cinesmart.screen.repository.ScreenRepository;
import com.cinesmart.seat.entity.Seat;
import com.cinesmart.seat.entity.SeatTier;
import com.cinesmart.seat.repository.SeatRepository;
import com.cinesmart.show.entity.Show;
import com.cinesmart.show.entity.ShowSeat;
import com.cinesmart.show.entity.ShowSeatStatus;
import com.cinesmart.show.repository.ShowRepository;
import com.cinesmart.show.repository.ShowSeatRepository;
import com.cinesmart.ticket.dto.TicketDTO;
import com.cinesmart.ticket.entity.TicketStatus;
import com.cinesmart.ticket.repository.TicketRepository;
import com.cinesmart.ticket.service.TicketService;
import com.cinesmart.user.entity.User;
import com.cinesmart.user.entity.UserRole;
import com.cinesmart.user.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@DisplayName("Phase 4 End-to-End Payment, Booking & Transaction Management Integration Tests")
class PaymentBookingIntegrationTest {

    @Autowired
    private BookingService bookingService;
    @Autowired
    private PaymentService paymentService;
    @Autowired
    private TicketService ticketService;

    @Autowired
    private BookingRepository bookingRepository;
    @Autowired
    private PaymentTransactionRepository paymentTransactionRepository;
    @Autowired
    private TicketRepository ticketRepository;
    @Autowired
    private ShowSeatRepository showSeatRepository;
    @Autowired
    private ShowRepository showRepository;
    @Autowired
    private SeatRepository seatRepository;
    @Autowired
    private ScreenRepository screenRepository;
    @Autowired
    private CinemaRepository cinemaRepository;
    @Autowired
    private MovieRepository movieRepository;
    @Autowired
    private UserRepository userRepository;

    private User customer;
    private Show show;
    private ShowSeat showSeat1;
    private ShowSeat showSeat2;

    @BeforeEach
    void setUp() {
        Cinema cinema = cinemaRepository.save(new Cinema("Integration Cinema", "Metropolis", "100 Ave", "123"));
        Screen screen = screenRepository.save(new Screen(cinema, 10, ScreenType.STANDARD, 10));

        Seat s1 = seatRepository.save(new Seat(screen, "A", 1, SeatTier.STANDARD, 1, 1));
        Seat s2 = seatRepository.save(new Seat(screen, "A", 2, SeatTier.STANDARD, 2, 1));

        Movie movie = movieRepository.save(new Movie("The Matrix", "Sci-fi classic", 136, "English", "Sci-Fi", "R", "url"));
        LocalDateTime start = LocalDateTime.now().plusDays(3);
        show = showRepository.save(new Show(movie, screen, start, start.plusHours(2), BigDecimal.valueOf(150.00)));

        showSeat1 = showSeatRepository.save(new ShowSeat(show, s1, BigDecimal.valueOf(150.00)));
        showSeat2 = showSeatRepository.save(new ShowSeat(show, s2, BigDecimal.valueOf(150.00)));

        customer = userRepository.save(new User(
                "integration_user_" + System.currentTimeMillis() + "@test.com",
                "hash",
                "Alice Customer",
                "999",
                UserRole.ROLE_CUSTOMER
        ));
    }

    @Test
    @DisplayName("Complete Workflow 1: Happy Path (Hold -> Pending Booking -> Payment Success -> Confirmed -> Tickets)")
    void testCompleteHappyPathWorkflow() {
        // Step 1: Create pending booking (reserves seats with temporary hold)
        BookingCreateRequest bookingRequest = new BookingCreateRequest(show.getId(), Arrays.asList(showSeat1.getId(), showSeat2.getId()));
        BookingDTO pendingBooking = bookingService.createBooking(customer.getEmail(), bookingRequest);

        assertNotNull(pendingBooking);
        assertEquals(BookingStatus.PENDING_PAYMENT, pendingBooking.getStatus());
        assertEquals(0, BigDecimal.valueOf(300.00).compareTo(pendingBooking.getTotalAmount()));

        // Verify seats are in LOCKED status
        ShowSeat freshSeat1 = showSeatRepository.findById(showSeat1.getId()).orElseThrow();
        ShowSeat freshSeat2 = showSeatRepository.findById(showSeat2.getId()).orElseThrow();
        assertEquals(ShowSeatStatus.LOCKED, freshSeat1.getStatus());
        assertEquals(ShowSeatStatus.LOCKED, freshSeat2.getStatus());

        // Step 2: Customer executes payment
        PaymentInitiateRequest paymentRequest = new PaymentInitiateRequest(
                pendingBooking.getId(),
                "UPI",
                "IDEM-HAPPY-" + System.currentTimeMillis(),
                true,
                null
        );

        PaymentResponseDTO paymentResponse = paymentService.initiatePayment(customer.getEmail(), paymentRequest, true);

        // Step 3: Verify Payment is SUCCESS
        assertNotNull(paymentResponse);
        assertEquals(PaymentStatus.SUCCESS, paymentResponse.getStatus());
        assertNotNull(paymentResponse.getGatewayTxnId());
        assertEquals(0, BigDecimal.valueOf(300.00).compareTo(paymentResponse.getAmount()));

        // Step 4: Verify Booking is CONFIRMED and seats permanently BOOKED
        BookingDTO confirmedBooking = bookingService.getBookingById(pendingBooking.getId(), customer.getEmail());
        assertEquals(BookingStatus.CONFIRMED, confirmedBooking.getStatus());
        assertNotNull(confirmedBooking.getConfirmedAt());

        freshSeat1 = showSeatRepository.findById(showSeat1.getId()).orElseThrow();
        freshSeat2 = showSeatRepository.findById(showSeat2.getId()).orElseThrow();
        assertEquals(ShowSeatStatus.BOOKED, freshSeat1.getStatus());
        assertEquals(ShowSeatStatus.BOOKED, freshSeat2.getStatus());

        // Step 5: Verify Digital Tickets were generated
        List<TicketDTO> tickets = ticketService.getTicketsByBookingId(pendingBooking.getId(), customer.getEmail());
        assertEquals(2, tickets.size());
        for (TicketDTO ticket : tickets) {
            assertEquals(TicketStatus.ISSUED, ticket.getStatus());
            assertEquals("CONFIRMED", ticket.getBookingStatus());
            assertNotNull(ticket.getTicketCode());
            assertNotNull(ticket.getQrVerificationHash());
        }
    }

    @Test
    @DisplayName("Complete Workflow 2: Payment Failure (Hold -> Pending Booking -> Payment Declined -> Seats Released)")
    void testCompleteFailureRecoveryWorkflow() {
        // Step 1: Create pending booking
        BookingCreateRequest bookingRequest = new BookingCreateRequest(show.getId(), Arrays.asList(showSeat1.getId(), showSeat2.getId()));
        BookingDTO pendingBooking = bookingService.createBooking(customer.getEmail(), bookingRequest);

        assertEquals(BookingStatus.PENDING_PAYMENT, pendingBooking.getStatus());

        // Step 2: Payment is simulated to fail
        PaymentInitiateRequest paymentRequest = new PaymentInitiateRequest(
                pendingBooking.getId(),
                "CARD",
                "IDEM-FAIL-" + System.currentTimeMillis(),
                false,
                "Insufficient credit limit"
        );

        PaymentResponseDTO paymentResponse = paymentService.initiatePayment(customer.getEmail(), paymentRequest, true);

        // Step 3: Verify payment status is FAILED
        assertEquals(PaymentStatus.FAILED, paymentResponse.getStatus());
        assertEquals("Insufficient credit limit", paymentResponse.getFailureReason());

        // Step 4: Verify booking status transitioned to PAYMENT_FAILED
        BookingDTO failedBooking = bookingService.getBookingById(pendingBooking.getId(), customer.getEmail());
        assertEquals(BookingStatus.PAYMENT_FAILED, failedBooking.getStatus());

        // Step 5: Verify held seats were automatically released back to AVAILABLE
        ShowSeat freshSeat1 = showSeatRepository.findById(showSeat1.getId()).orElseThrow();
        ShowSeat freshSeat2 = showSeatRepository.findById(showSeat2.getId()).orElseThrow();
        assertEquals(ShowSeatStatus.AVAILABLE, freshSeat1.getStatus());
        assertEquals(ShowSeatStatus.AVAILABLE, freshSeat2.getStatus());

        // Step 6: Verify no tickets were generated
        List<TicketDTO> tickets = ticketService.getTicketsByBookingId(pendingBooking.getId(), customer.getEmail());
        assertTrue(tickets.isEmpty());
    }

    @Test
    @DisplayName("Complete Workflow 3: Cancellation & Refund (Confirmed -> Cancel -> Refund -> Seats Released)")
    void testCompleteCancellationAndRefundWorkflow() {
        // Step 1: Book and pay
        BookingCreateRequest bookingRequest = new BookingCreateRequest(show.getId(), Arrays.asList(showSeat1.getId(), showSeat2.getId()));
        BookingDTO pendingBooking = bookingService.createBooking(customer.getEmail(), bookingRequest);

        PaymentInitiateRequest paymentRequest = new PaymentInitiateRequest(
                pendingBooking.getId(),
                "NET_BANKING",
                "IDEM-CANCEL-" + System.currentTimeMillis(),
                true,
                null
        );
        paymentService.initiatePayment(customer.getEmail(), paymentRequest, true);

        // Verify booking is confirmed and seats booked
        BookingDTO confirmed = bookingService.getBookingById(pendingBooking.getId(), customer.getEmail());
        assertEquals(BookingStatus.CONFIRMED, confirmed.getStatus());

        // Step 2: Customer cancels booking
        BookingDTO cancelled = bookingService.cancelBooking(pendingBooking.getId(), customer.getEmail());

        // Step 3: Verify booking is CANCELLED
        assertEquals(BookingStatus.CANCELLED, cancelled.getStatus());

        // Step 4: Verify seats are released back to AVAILABLE
        ShowSeat freshSeat1 = showSeatRepository.findById(showSeat1.getId()).orElseThrow();
        ShowSeat freshSeat2 = showSeatRepository.findById(showSeat2.getId()).orElseThrow();
        assertEquals(ShowSeatStatus.AVAILABLE, freshSeat1.getStatus());
        assertEquals(ShowSeatStatus.AVAILABLE, freshSeat2.getStatus());

        // Step 5: Verify payment was transitioned to REFUNDED
        List<PaymentResponseDTO> payments = paymentService.getPaymentsForBooking(pendingBooking.getId(), customer.getEmail());
        assertFalse(payments.isEmpty());
        PaymentResponseDTO payment = payments.get(0);
        assertEquals(PaymentStatus.REFUNDED, payment.getStatus());
    }
}
