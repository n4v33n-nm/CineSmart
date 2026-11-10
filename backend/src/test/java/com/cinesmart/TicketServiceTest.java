package com.cinesmart;

import com.cinesmart.booking.entity.Booking;
import com.cinesmart.booking.entity.BookingSeat;
import com.cinesmart.booking.entity.BookingStatus;
import com.cinesmart.cinema.entity.Cinema;
import com.cinesmart.cinema.entity.ScreenType;
import com.cinesmart.movie.entity.Movie;
import com.cinesmart.screen.entity.Screen;
import com.cinesmart.seat.entity.Seat;
import com.cinesmart.seat.entity.SeatTier;
import com.cinesmart.show.entity.Show;
import com.cinesmart.show.entity.ShowSeat;
import com.cinesmart.ticket.dto.TicketDTO;
import com.cinesmart.ticket.dto.TicketStatsDTO;
import com.cinesmart.ticket.dto.TicketVerificationResponseDTO;
import com.cinesmart.ticket.entity.Ticket;
import com.cinesmart.ticket.entity.TicketStatus;
import com.cinesmart.ticket.repository.TicketRepository;
import com.cinesmart.ticket.repository.TicketVerificationLogRepository;
import com.cinesmart.ticket.service.TicketService;
import com.cinesmart.common.exception.BadRequestException;
import com.cinesmart.common.exception.ResourceNotFoundException;
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

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("TicketService Comprehensive Unit Tests (Scenarios 17-20 + Verification)")
class TicketServiceTest {

    @Mock
    private TicketRepository ticketRepository;
    @Mock
    private UserRepository userRepository;
    @Mock
    private TicketVerificationLogRepository ticketVerificationLogRepository;

    @InjectMocks
    private TicketService ticketService;

    private Booking testBooking;
    private BookingSeat seatLine1;
    private BookingSeat seatLine2;
    private User customer;

    @BeforeEach
    void setUp() {
        customer = new User("jane@customer.com", "hash", "Jane Customer", "+123", UserRole.ROLE_CUSTOMER);
        customer.setId(5L);

        Movie movie = new Movie("Inception", "Mind-bending thriller", 148, "English", "Sci-Fi", "PG-13", "url");
        Cinema cinema = new Cinema("CineSmart IMAX", "Metropolis", "Main St", "+100");
        Screen screen = new Screen(cinema, 1, ScreenType.IMAX_3D, 100);
        Show show = new Show(movie, screen, LocalDateTime.now().plusDays(1), LocalDateTime.now().plusDays(1).plusHours(2), BigDecimal.valueOf(200.00));
        show.setId(55L);

        testBooking = new Booking("CS-2026-TICKETTEST", customer, show, BigDecimal.valueOf(400.00));
        testBooking.setId(88L);
        testBooking.setStatus(BookingStatus.CONFIRMED);

        Seat physical1 = new Seat(screen, "D", 4, SeatTier.PREMIUM, 4, 4);
        ShowSeat showSeat1 = new ShowSeat(show, physical1, BigDecimal.valueOf(200.00));
        seatLine1 = new BookingSeat(testBooking, showSeat1, BigDecimal.valueOf(200.00), "D-4", "PREMIUM");
        testBooking.addBookingSeat(seatLine1);

        Seat physical2 = new Seat(screen, "D", 5, SeatTier.PREMIUM, 5, 4);
        ShowSeat showSeat2 = new ShowSeat(show, physical2, BigDecimal.valueOf(200.00));
        seatLine2 = new BookingSeat(testBooking, showSeat2, BigDecimal.valueOf(200.00), "D-5", "PREMIUM");
        testBooking.addBookingSeat(seatLine2);
    }

    @Test
    @DisplayName("Requirement 17: Successful booking generates a ticket for each reserved seat")
    void testSuccessfulBookingGeneratesTickets() {
        when(ticketRepository.findByBookingId(88L)).thenReturn(Collections.emptyList());
        when(ticketRepository.saveAll(any())).thenAnswer(invocation -> invocation.getArgument(0));

        List<TicketDTO> tickets = ticketService.generateTicketsForBooking(testBooking);

        assertNotNull(tickets);
        assertEquals(2, tickets.size());
        verify(ticketRepository, times(1)).saveAll(any());
    }

    @Test
    @DisplayName("Requirement 18: Generated ticket contains correct seat labels and tiers")
    void testTicketContainsCorrectSeats() {
        when(ticketRepository.findByBookingId(88L)).thenReturn(Collections.emptyList());
        when(ticketRepository.saveAll(any())).thenAnswer(invocation -> invocation.getArgument(0));

        List<TicketDTO> tickets = ticketService.generateTicketsForBooking(testBooking);

        TicketDTO t1 = tickets.get(0);
        TicketDTO t2 = tickets.get(1);

        assertEquals("D-4", t1.getSeatLabel());
        assertEquals("PREMIUM", t1.getSeatTier());
        assertEquals(BigDecimal.valueOf(200.00), t1.getSeatPrice());

        assertEquals("D-5", t2.getSeatLabel());
        assertEquals("PREMIUM", t2.getSeatTier());
    }

    @Test
    @DisplayName("Requirement 19: Ticket contains correct booking reference and movie details")
    void testTicketContainsCorrectBookingReference() {
        when(ticketRepository.findByBookingId(88L)).thenReturn(Collections.emptyList());
        when(ticketRepository.saveAll(any())).thenAnswer(invocation -> invocation.getArgument(0));

        List<TicketDTO> tickets = ticketService.generateTicketsForBooking(testBooking);

        for (TicketDTO t : tickets) {
            assertEquals("CS-2026-TICKETTEST", t.getBookingReference());
            assertEquals("Inception", t.getMovieTitle());
            assertEquals("CineSmart IMAX", t.getCinemaName());
            assertEquals("Screen 1", t.getScreenName());
            assertNotNull(t.getTicketCode());
            assertTrue(t.getTicketCode().startsWith("TKT-"));
            assertNotNull(t.getQrVerificationHash());
        }
    }

    @Test
    @DisplayName("Requirement 20: Ticket reflects correct booking status (CONFIRMED)")
    void testTicketContainsCorrectBookingStatus() {
        when(ticketRepository.findByBookingId(88L)).thenReturn(Collections.emptyList());
        when(ticketRepository.saveAll(any())).thenAnswer(invocation -> invocation.getArgument(0));

        List<TicketDTO> tickets = ticketService.generateTicketsForBooking(testBooking);

        for (TicketDTO t : tickets) {
            assertEquals("CONFIRMED", t.getBookingStatus());
            assertEquals(TicketStatus.ISSUED, t.getStatus());
            assertEquals(BigDecimal.valueOf(400.00), t.getTotalAmount());
        }
    }

    @Test
    @DisplayName("Ticket generation is idempotent: does not duplicate tickets if already generated")
    void testTicketGenerationIdempotent() {
        Ticket existingTicket = new Ticket(testBooking, seatLine1, "TKT-EXISTING", "HASH");
        when(ticketRepository.findByBookingId(88L)).thenReturn(Collections.singletonList(existingTicket));

        List<TicketDTO> tickets = ticketService.generateTicketsForBooking(testBooking);

        assertEquals(1, tickets.size());
        assertEquals("TKT-EXISTING", tickets.get(0).getTicketCode());
        verify(ticketRepository, never()).saveAll(any());
    }

    @Test
    @DisplayName("Verification: Valid ticket is atomically verified and marked as USED")
    void testVerifyTicketSuccessful() {
        User staff = new User("staff@cinema.com", "pass", "Staff Member", "+999", UserRole.ROLE_STAFF);
        Ticket ticket = new Ticket(testBooking, seatLine1, "TKT-2026-TEST01", "VALID-HASH-XYZ");

        when(userRepository.findByEmail("staff@cinema.com")).thenReturn(Optional.of(staff));
        when(ticketRepository.findByTicketCodeWithLock("TKT-2026-TEST01")).thenReturn(Optional.of(ticket));
        when(ticketRepository.save(any(Ticket.class))).thenAnswer(inv -> inv.getArgument(0));

        TicketVerificationResponseDTO response = ticketService.verifyTicket("TKT-2026-TEST01", "VALID-HASH-XYZ", "staff@cinema.com");

        assertTrue(response.isValid());
        assertEquals("VERIFIED_SUCCESSFULLY", response.getStatus());
        assertEquals(TicketStatus.USED, ticket.getStatus());
        assertEquals(staff, ticket.getValidatedBy());
        assertNotNull(ticket.getValidatedAt());
        verify(ticketVerificationLogRepository, times(1)).save(any());
    }

    @Test
    @DisplayName("Verification: Already used ticket is rejected with TICKET_ALREADY_USED")
    void testVerifyTicketAlreadyUsed() {
        User staff = new User("staff@cinema.com", "pass", "Staff Member", "+999", UserRole.ROLE_STAFF);
        Ticket ticket = new Ticket(testBooking, seatLine1, "TKT-2026-TEST02", "HASH-XYZ");
        ticket.setStatus(TicketStatus.USED);
        ticket.setValidatedAt(LocalDateTime.now().minusHours(1));
        ticket.setValidatedBy(staff);

        when(userRepository.findByEmail("staff@cinema.com")).thenReturn(Optional.of(staff));
        when(ticketRepository.findByTicketCodeWithLock("TKT-2026-TEST02")).thenReturn(Optional.of(ticket));

        BadRequestException ex = assertThrows(BadRequestException.class, () ->
                ticketService.verifyTicket("TKT-2026-TEST02", "HASH-XYZ", "staff@cinema.com"));

        assertEquals("TICKET_ALREADY_USED", ex.getErrorCode());
        verify(ticketRepository, never()).save(any());
        verify(ticketVerificationLogRepository, times(1)).save(any());
    }

    @Test
    @DisplayName("Verification: Tampered or mismatching QR hash is rejected with INVALID_QR_TOKEN")
    void testVerifyTicketInvalidHash() {
        User staff = new User("staff@cinema.com", "pass", "Staff Member", "+999", UserRole.ROLE_STAFF);
        Ticket ticket = new Ticket(testBooking, seatLine1, "TKT-2026-TEST03", "LEGITIMATE-HASH");

        when(userRepository.findByEmail("staff@cinema.com")).thenReturn(Optional.of(staff));
        when(ticketRepository.findByTicketCodeWithLock("TKT-2026-TEST03")).thenReturn(Optional.of(ticket));

        BadRequestException ex = assertThrows(BadRequestException.class, () ->
                ticketService.verifyTicket("TKT-2026-TEST03", "TAMPERED-FORGED-HASH", "staff@cinema.com"));

        assertEquals("TAMPERED_QR_CODE", ex.getErrorCode());
        verify(ticketRepository, never()).save(any());
    }

    @Test
    @DisplayName("Verification: Cancelled ticket is rejected with TICKET_CANCELLED")
    void testVerifyTicketCancelled() {
        User staff = new User("staff@cinema.com", "pass", "Staff Member", "+999", UserRole.ROLE_STAFF);
        Ticket ticket = new Ticket(testBooking, seatLine1, "TKT-2026-TEST04", "HASH-XYZ");
        ticket.setStatus(TicketStatus.CANCELLED);

        when(userRepository.findByEmail("staff@cinema.com")).thenReturn(Optional.of(staff));
        when(ticketRepository.findByTicketCodeWithLock("TKT-2026-TEST04")).thenReturn(Optional.of(ticket));

        BadRequestException ex = assertThrows(BadRequestException.class, () ->
                ticketService.verifyTicket("TKT-2026-TEST04", "HASH-XYZ", "staff@cinema.com"));

        assertEquals("TICKET_CANCELLED", ex.getErrorCode());
        verify(ticketRepository, never()).save(any());
    }

    @Test
    @DisplayName("Verification: Ticket not found throws ResourceNotFoundException")
    void testVerifyTicketNotFound() {
        User staff = new User("staff@cinema.com", "pass", "Staff Member", "+999", UserRole.ROLE_STAFF);
        when(userRepository.findByEmail("staff@cinema.com")).thenReturn(Optional.of(staff));
        when(ticketRepository.findByTicketCodeWithLock("TKT-INVALID")).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () ->
                ticketService.verifyTicket("TKT-INVALID", null, "staff@cinema.com"));
    }

    @Test
    @DisplayName("Statistics: Correctly aggregates total, verified, unused, and invalid attempts")
    void testGetTicketStats() {
        when(ticketRepository.count()).thenReturn(50L);
        when(ticketRepository.countByStatus(TicketStatus.USED)).thenReturn(20L);
        when(ticketRepository.countByStatus(TicketStatus.ISSUED)).thenReturn(30L);
        when(ticketVerificationLogRepository.countByResultNot("SUCCESS")).thenReturn(4L);
        when(ticketVerificationLogRepository.findTop20ByOrderByAttemptedAtDesc()).thenReturn(Collections.emptyList());

        TicketStatsDTO stats = ticketService.getTicketStats();

        assertEquals(50L, stats.getTotalGenerated());
        assertEquals(20L, stats.getVerifiedTickets());
        assertEquals(30L, stats.getUnusedTickets());
        assertEquals(4L, stats.getInvalidAttempts());
        assertNotNull(stats.getRecentVerifications());
    }
}
