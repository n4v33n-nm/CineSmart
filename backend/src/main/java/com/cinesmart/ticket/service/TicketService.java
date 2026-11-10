package com.cinesmart.ticket.service;

import com.cinesmart.booking.entity.Booking;
import com.cinesmart.booking.entity.BookingSeat;
import com.cinesmart.booking.entity.BookingStatus;
import com.cinesmart.common.exception.BadRequestException;
import com.cinesmart.common.exception.ResourceNotFoundException;
import com.cinesmart.ticket.dto.TicketDTO;
import com.cinesmart.ticket.dto.TicketStatsDTO;
import com.cinesmart.ticket.dto.TicketVerificationLogDTO;
import com.cinesmart.ticket.dto.TicketVerificationResponseDTO;
import com.cinesmart.ticket.entity.Ticket;
import com.cinesmart.ticket.entity.TicketStatus;
import com.cinesmart.ticket.entity.TicketVerificationLog;
import com.cinesmart.ticket.repository.TicketRepository;
import com.cinesmart.ticket.repository.TicketVerificationLogRepository;
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
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@Transactional(readOnly = true)
public class TicketService {

    private static final Logger log = LoggerFactory.getLogger(TicketService.class);

    private final TicketRepository ticketRepository;
    private final UserRepository userRepository;
    private final TicketVerificationLogRepository ticketVerificationLogRepository;

    public TicketService(TicketRepository ticketRepository,
                         UserRepository userRepository,
                         TicketVerificationLogRepository ticketVerificationLogRepository) {
        this.ticketRepository = ticketRepository;
        this.userRepository = userRepository;
        this.ticketVerificationLogRepository = ticketVerificationLogRepository;
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

    @Transactional
    public TicketVerificationResponseDTO verifyTicket(String ticketCode, String verificationHash, String staffEmail) {
        User staff = userRepository.findByEmail(staffEmail)
                .orElseThrow(() -> new ResourceNotFoundException("User", "email", staffEmail));

        if (staff.getRole() != UserRole.ROLE_STAFF && staff.getRole() != UserRole.ROLE_ADMIN) {
            throw new AccessDeniedException("Only cinema staff and administrators are authorized to verify tickets");
        }

        if (ticketCode == null || ticketCode.trim().isEmpty()) {
            ticketVerificationLogRepository.save(new TicketVerificationLog("UNKNOWN", verificationHash, staff, "INVALID_CODE", "Empty or missing ticket code"));
            throw new BadRequestException("Ticket code must be provided for verification", "INVALID_TICKET_CODE");
        }

        String cleanCode = ticketCode.trim();

        // Concurrency-safe atomic check: acquire PESSIMISTIC_WRITE lock on ticket row
        Optional<Ticket> ticketOpt = ticketRepository.findByTicketCodeWithLock(cleanCode);
        if (ticketOpt.isEmpty()) {
            ticketVerificationLogRepository.save(new TicketVerificationLog(cleanCode, verificationHash, staff, "INVALID_CODE", "Ticket code not found"));
            throw new ResourceNotFoundException("Ticket", "ticketCode", cleanCode);
        }

        Ticket ticket = ticketOpt.get();

        // 1. Verify Hash (if provided from a digital QR scan)
        if (verificationHash != null && !verificationHash.trim().isEmpty()) {
            if (!ticket.getQrVerificationHash().equalsIgnoreCase(verificationHash.trim())) {
                ticketVerificationLogRepository.save(new TicketVerificationLog(cleanCode, verificationHash, staff, "INVALID_HASH", "Cryptographic QR verification hash mismatch"));
                throw new BadRequestException("Invalid QR code security token", "TAMPERED_QR_CODE");
            }
        }

        // 2. Reject Cancelled / Voided Ticket
        Booking booking = ticket.getBooking();
        if (ticket.getStatus() == TicketStatus.CANCELLED || ticket.getStatus() == TicketStatus.VOIDED
                || (booking != null && booking.getStatus() == BookingStatus.CANCELLED)) {
            ticketVerificationLogRepository.save(new TicketVerificationLog(cleanCode, verificationHash, staff, "CANCELLED", "Ticket status is CANCELLED"));
            throw new BadRequestException("Ticket is CANCELLED and cannot be accepted for entry", "TICKET_CANCELLED");
        }

        // 3. Reject Already-Used Ticket
        if (ticket.getStatus() == TicketStatus.USED) {
            String validatorName = ticket.getValidatedBy() != null ? ticket.getValidatedBy().getFullName() : "Staff";
            String reason = "Ticket was already used on " + ticket.getValidatedAt() + " by " + validatorName;
            ticketVerificationLogRepository.save(new TicketVerificationLog(cleanCode, verificationHash, staff, "ALREADY_USED", reason));
            throw new BadRequestException(reason, "TICKET_ALREADY_USED");
        }

        // 4. Verify Booking Status
        if (booking == null || booking.getStatus() != BookingStatus.CONFIRMED) {
            String bStatus = booking != null && booking.getStatus() != null ? booking.getStatus().name() : "NULL";
            ticketVerificationLogRepository.save(new TicketVerificationLog(cleanCode, verificationHash, staff, "BOOKING_NOT_CONFIRMED", "Booking status is " + bStatus));
            throw new BadRequestException("Ticket booking is not confirmed (Status: " + bStatus + ")", "BOOKING_NOT_CONFIRMED");
        }

        // 5. Successful Atomic Entry Validation
        ticket.validateTicket(staff);
        Ticket savedTicket = ticketRepository.save(ticket);
        ticketVerificationLogRepository.save(new TicketVerificationLog(cleanCode, verificationHash, staff, "SUCCESS", "Valid entry granted"));

        log.info("Ticket {} successfully verified and marked as USED by staff {}", cleanCode, staffEmail);

        return new TicketVerificationResponseDTO(
                true,
                cleanCode,
                "VERIFIED_SUCCESSFULLY",
                "Entry granted. Ticket successfully verified.",
                TicketDTO.fromEntity(savedTicket),
                savedTicket.getValidatedAt(),
                staff.getFullName()
        );
    }

    public TicketStatsDTO getTicketStats() {
        long totalGenerated = ticketRepository.count();
        long verified = ticketRepository.countByStatus(TicketStatus.USED);
        long unused = ticketRepository.countByStatus(TicketStatus.ISSUED);
        long invalidAttempts = ticketVerificationLogRepository.countByResultNot("SUCCESS");
        List<TicketVerificationLogDTO> recent = ticketVerificationLogRepository.findTop20ByOrderByAttemptedAtDesc()
                .stream()
                .map(TicketVerificationLogDTO::fromEntity)
                .collect(Collectors.toList());

        return new TicketStatsDTO(totalGenerated, verified, unused, invalidAttempts, recent);
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
