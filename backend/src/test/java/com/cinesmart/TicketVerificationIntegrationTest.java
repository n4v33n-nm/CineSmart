package com.cinesmart;

import com.cinesmart.booking.dto.BookingCreateRequest;
import com.cinesmart.booking.dto.BookingDTO;
import com.cinesmart.booking.entity.BookingStatus;
import com.cinesmart.booking.repository.BookingRepository;
import com.cinesmart.booking.service.BookingService;
import com.cinesmart.cinema.entity.Cinema;
import com.cinesmart.cinema.entity.ScreenType;
import com.cinesmart.cinema.repository.CinemaRepository;
import com.cinesmart.common.exception.BadRequestException;
import com.cinesmart.movie.entity.Movie;
import com.cinesmart.movie.repository.MovieRepository;
import com.cinesmart.payment.dto.PaymentInitiateRequest;
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
import com.cinesmart.ticket.dto.TicketStatsDTO;
import com.cinesmart.ticket.dto.TicketVerificationResponseDTO;
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
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@DisplayName("Phase 2 QR Code E-Ticket & Verification Integration Tests")
class TicketVerificationIntegrationTest {

    @Autowired
    private BookingService bookingService;
    @Autowired
    private PaymentService paymentService;
    @Autowired
    private TicketService ticketService;
    @Autowired
    private TicketRepository ticketRepository;
    @Autowired
    private UserRepository userRepository;
    @Autowired
    private MovieRepository movieRepository;
    @Autowired
    private CinemaRepository cinemaRepository;
    @Autowired
    private ScreenRepository screenRepository;
    @Autowired
    private SeatRepository seatRepository;
    @Autowired
    private ShowRepository showRepository;
    @Autowired
    private ShowSeatRepository showSeatRepository;

    private User testCustomer;
    private User testStaff;
    private Show testShow;
    private ShowSeat showSeat1;
    private ShowSeat showSeat2;

    @BeforeEach
    void setUp() {
        String suffix = String.valueOf(System.currentTimeMillis()).substring(7);

        testCustomer = userRepository.save(new User(
                "customer_" + suffix + "@cinesmart.test",
                "encodedPass",
                "QR Test Customer " + suffix,
                "+919876543" + suffix.substring(0, 3),
                UserRole.ROLE_CUSTOMER
        ));

        testStaff = userRepository.save(new User(
                "staff_" + suffix + "@cinesmart.test",
                "encodedPass",
                "Gate Staff " + suffix,
                "+919876544" + suffix.substring(0, 3),
                UserRole.ROLE_STAFF
        ));

        Movie movie = movieRepository.save(new Movie(
                "QR Odyssey " + suffix,
                "Cyber thriller",
                120,
                "English",
                "Sci-Fi",
                "PG-13",
                "https://image.tmdb.org/t/p/w500/test.jpg"
        ));

        Cinema cinema = cinemaRepository.save(new Cinema(
                "CineSmart Metroplex " + suffix,
                "Cyber City",
                "Boulevard",
                "+9180234567"
        ));

        Screen screen = screenRepository.save(new Screen(cinema, 1, ScreenType.STANDARD, 50));
        Seat physical1 = seatRepository.save(new Seat(screen, "V", 1, SeatTier.STANDARD, 1, 1));
        Seat physical2 = seatRepository.save(new Seat(screen, "V", 2, SeatTier.PREMIUM, 2, 1));

        testShow = showRepository.save(new Show(
                movie,
                screen,
                LocalDateTime.now().plusDays(2),
                LocalDateTime.now().plusDays(2).plusHours(2),
                BigDecimal.valueOf(250.00)
        ));

        showSeat1 = showSeatRepository.save(new ShowSeat(testShow, physical1, BigDecimal.valueOf(250.00)));
        showSeat2 = showSeatRepository.save(new ShowSeat(testShow, physical2, BigDecimal.valueOf(350.00)));
    }

    private BookingDTO createAndConfirmBooking() {
        BookingCreateRequest bReq = new BookingCreateRequest(testShow.getId(), Collections.singletonList(showSeat1.getId()));
        BookingDTO booking = bookingService.createBooking(testCustomer.getEmail(), bReq);

        PaymentInitiateRequest pReq = new PaymentInitiateRequest(
                booking.getId(),
                "UPI",
                "IDEM-QR-" + System.currentTimeMillis() + "-" + booking.getId(),
                true,
                null
        );
        paymentService.initiatePayment(testCustomer.getEmail(), pReq, true);

        return bookingService.getBookingById(booking.getId(), testCustomer.getEmail());
    }

    @Test
    @DisplayName("Scenario 1: Confirmed booking generates valid E-Tickets with QR cryptographic hash")
    void testConfirmedBookingGeneratesTicketsWithQrHash() {
        BookingDTO booking = createAndConfirmBooking();
        assertEquals(BookingStatus.CONFIRMED, booking.getStatus());

        List<TicketDTO> tickets = ticketService.getTicketsByBookingId(booking.getId(), testCustomer.getEmail());
        assertFalse(tickets.isEmpty(), "Tickets must be generated upon confirmed booking");

        TicketDTO ticket = tickets.get(0);
        assertNotNull(ticket.getTicketCode());
        assertTrue(ticket.getTicketCode().startsWith("TKT-"));
        assertNotNull(ticket.getQrCodeHash());
        assertEquals(64, ticket.getQrCodeHash().length(), "QR hash must be a 64-character SHA-256 hex string");
        assertEquals(TicketStatus.ISSUED, ticket.getStatus());
        assertNotNull(ticket.getPosterUrl());
    }

    @Test
    @DisplayName("Scenario 2: Theatre staff successfully verifies and admits a valid ticket")
    void testSuccessfulTicketVerification() {
        BookingDTO booking = createAndConfirmBooking();
        List<TicketDTO> tickets = ticketService.getTicketsByBookingId(booking.getId(), testCustomer.getEmail());
        TicketDTO issuedTicket = tickets.get(0);

        TicketVerificationResponseDTO response = ticketService.verifyTicket(
                issuedTicket.getTicketCode(),
                issuedTicket.getQrCodeHash(),
                testStaff.getEmail()
        );

        assertTrue(response.isValid());
        assertEquals("VERIFIED_SUCCESSFULLY", response.getStatus());
        assertNotNull(response.getVerifiedAt());
        assertEquals(testStaff.getFullName(), response.getVerifiedBy());

        Ticket verifiedInDb = ticketRepository.findByTicketCode(issuedTicket.getTicketCode()).orElseThrow();
        assertEquals(TicketStatus.USED, verifiedInDb.getStatus());
        assertNotNull(verifiedInDb.getValidatedAt());
        assertEquals(testStaff.getId(), verifiedInDb.getValidatedBy().getId());
    }

    @Test
    @DisplayName("Scenario 3: Duplicate entry prevention - scanning an already used ticket is rejected")
    void testDuplicateScanningRejection() {
        BookingDTO booking = createAndConfirmBooking();
        List<TicketDTO> tickets = ticketService.getTicketsByBookingId(booking.getId(), testCustomer.getEmail());
        TicketDTO issuedTicket = tickets.get(0);

        // First scan succeeds
        ticketService.verifyTicket(issuedTicket.getTicketCode(), issuedTicket.getQrCodeHash(), testStaff.getEmail());

        // Second scan must be rejected
        BadRequestException ex = assertThrows(BadRequestException.class, () ->
                ticketService.verifyTicket(issuedTicket.getTicketCode(), issuedTicket.getQrCodeHash(), testStaff.getEmail())
        );

        assertEquals("TICKET_ALREADY_USED", ex.getErrorCode());
        assertTrue(ex.getMessage().contains("already used"));
    }

    @Test
    @DisplayName("Scenario 4: Tampered QR code with invalid hash is rejected")
    void testTamperedQrCodeRejection() {
        BookingDTO booking = createAndConfirmBooking();
        List<TicketDTO> tickets = ticketService.getTicketsByBookingId(booking.getId(), testCustomer.getEmail());
        TicketDTO issuedTicket = tickets.get(0);

        BadRequestException ex = assertThrows(BadRequestException.class, () ->
                ticketService.verifyTicket(issuedTicket.getTicketCode(), "FORGED-INVALID-HASH-VALUE", testStaff.getEmail())
        );

        assertEquals("TAMPERED_QR_CODE", ex.getErrorCode());
    }

    @Test
    @DisplayName("Scenario 5: Cancelled ticket cannot be used for entry")
    void testCancelledTicketRejection() {
        BookingDTO booking = createAndConfirmBooking();
        List<TicketDTO> tickets = ticketService.getTicketsByBookingId(booking.getId(), testCustomer.getEmail());
        TicketDTO issuedTicket = tickets.get(0);

        // Cancel booking (which voids/cancels tickets)
        bookingService.cancelBooking(booking.getId(), testCustomer.getEmail());

        BadRequestException ex = assertThrows(BadRequestException.class, () ->
                ticketService.verifyTicket(issuedTicket.getTicketCode(), issuedTicket.getQrCodeHash(), testStaff.getEmail())
        );

        assertEquals("TICKET_CANCELLED", ex.getErrorCode());
    }

    @Test
    @DisplayName("Scenario 6: Concurrency test - two staff scanning the same ticket simultaneously; exactly one succeeds")
    void testConcurrentVerificationRaceCondition() throws Exception {
        BookingDTO booking = createAndConfirmBooking();
        List<TicketDTO> tickets = ticketService.getTicketsByBookingId(booking.getId(), testCustomer.getEmail());
        String ticketCode = tickets.get(0).getTicketCode();
        String qrHash = tickets.get(0).getQrCodeHash();

        int numThreads = 4;
        ExecutorService executor = Executors.newFixedThreadPool(numThreads);
        CyclicBarrier barrier = new CyclicBarrier(numThreads);
        AtomicInteger successCount = new AtomicInteger(0);
        AtomicInteger alreadyUsedCount = new AtomicInteger(0);

        List<Callable<Void>> tasks = Collections.nCopies(numThreads, () -> {
            barrier.await();
            try {
                ticketService.verifyTicket(ticketCode, qrHash, testStaff.getEmail());
                successCount.incrementAndGet();
            } catch (BadRequestException ex) {
                if ("TICKET_ALREADY_USED".equals(ex.getErrorCode())) {
                    alreadyUsedCount.incrementAndGet();
                }
            }
            return null;
        });

        List<Future<Void>> futures = executor.invokeAll(tasks);
        for (Future<Void> f : futures) {
            f.get();
        }
        executor.shutdown();

        assertEquals(1, successCount.get(), "Exactly one staff verification must succeed");
        assertEquals(numThreads - 1, alreadyUsedCount.get(), "All other concurrent attempts must be rejected as ALREADY_USED");
    }

    @Test
    @DisplayName("Scenario 7: Telemetry & Statistics aggregate correctly")
    void testTicketStatistics() {
        createAndConfirmBooking();
        TicketStatsDTO stats = ticketService.getTicketStats();

        assertTrue(stats.getTotalGenerated() >= 1);
        assertTrue(stats.getUnusedTickets() >= 0);
        assertNotNull(stats.getRecentVerifications());
    }
}
