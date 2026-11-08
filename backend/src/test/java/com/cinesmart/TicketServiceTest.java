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
@DisplayName("TicketService Comprehensive Unit Tests (Scenarios 17-20)")
class TicketServiceTest {

    @Mock
    private TicketRepository ticketRepository;
    @Mock
    private UserRepository userRepository;

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
}
