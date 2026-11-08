package com.cinesmart.ticket.service;

import com.cinesmart.booking.entity.Booking;
import com.cinesmart.booking.entity.BookingSeat;
import com.cinesmart.common.exception.ResourceNotFoundException;
import com.cinesmart.ticket.dto.TicketDTO;
import com.cinesmart.ticket.entity.Ticket;
import com.cinesmart.ticket.repository.TicketRepository;
import com.cinesmart.user.entity.User;
import com.cinesmart.user.entity.UserRole;
import com.cinesmart.user.repository.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HexFormat;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@Transactional(readOnly = true)
public class TicketService {

    private static final Logger log = LoggerFactory.getLogger(TicketService.class);

    private final TicketRepository ticketRepository;
    private final UserRepository userRepository;

    public TicketService(TicketRepository ticketRepository, UserRepository userRepository) {
        this.ticketRepository = ticketRepository;
        this.userRepository = userRepository;
    }

    @Transactional
    public List<TicketDTO> generateTicketsForBooking(Booking booking) {
        log.info("Generating digital tickets for booking reference: {}", booking.getBookingReference());

        // Check if tickets already exist for this booking (idempotency)
        List<Ticket> existingTickets = ticketRepository.findByBookingId(booking.getId());
        if (!existingTickets.isEmpty()) {
            log.info("Tickets already generated for booking id: {}", booking.getId());
            return existingTickets.stream().map(TicketDTO::fromEntity).collect(Collectors.toList());
        }

        List<Ticket> generated = new ArrayList<>();
        int year = LocalDateTime.now().getYear();

        for (BookingSeat seat : booking.getBookingSeats()) {
            String ticketCode = String.format("TKT-%d-%s", year, UUID.randomUUID().toString().substring(0, 8).toUpperCase());
            String hashInput = ticketCode + ":" + booking.getBookingReference() + ":" + seat.getSeatLabel();
            String qrHash = computeSha256(hashInput);

            Ticket ticket = new Ticket(booking, seat, ticketCode, qrHash);
            generated.add(ticket);
        }

        List<Ticket> savedTickets = ticketRepository.saveAll(generated);
        log.info("Successfully generated {} tickets for booking id: {}", savedTickets.size(), booking.getId());
        return savedTickets.stream().map(TicketDTO::fromEntity).collect(Collectors.toList());
    }

    public List<TicketDTO> getTicketsByBookingId(Long bookingId, String userEmail) {
        User user = userRepository.findByEmail(userEmail)
                .orElseThrow(() -> new ResourceNotFoundException("User", "email", userEmail));

        List<Ticket> tickets = ticketRepository.findByBookingId(bookingId);
        if (!tickets.isEmpty()) {
            Booking booking = tickets.get(0).getBooking();
            if (!booking.getUser().getId().equals(user.getId()) && user.getRole() != UserRole.ROLE_ADMIN) {
                throw new AccessDeniedException("You do not have permission to view tickets for this booking");
            }
        }

        return tickets.stream().map(TicketDTO::fromEntity).collect(Collectors.toList());
    }

    public TicketDTO getTicketByCode(String ticketCode) {
        Ticket ticket = ticketRepository.findByTicketCode(ticketCode)
                .orElseThrow(() -> new ResourceNotFoundException("Ticket", "ticketCode", ticketCode));
        return TicketDTO.fromEntity(ticket);
    }

    @Transactional
    public void voidTicketsForBooking(Booking booking) {
        List<Ticket> tickets = ticketRepository.findByBookingId(booking.getId());
        for (Ticket ticket : tickets) {
            ticket.cancelTicket();
        }
        ticketRepository.saveAll(tickets);
        log.info("Voided/cancelled {} tickets for booking id: {}", tickets.size(), booking.getId());
    }

    private String computeSha256(String input) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(input.getBytes(StandardCharsets.UTF_8));
            return HexFormat.of().formatHex(hash);
        } catch (NoSuchAlgorithmException e) {
            return UUID.randomUUID().toString();
        }
    }
}
