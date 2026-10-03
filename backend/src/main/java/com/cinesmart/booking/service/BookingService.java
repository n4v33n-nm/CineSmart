package com.cinesmart.booking.service;

import com.cinesmart.booking.dto.BookingCreateRequest;
import com.cinesmart.booking.dto.BookingDTO;
import com.cinesmart.booking.entity.Booking;
import com.cinesmart.booking.entity.BookingSeat;
import com.cinesmart.booking.entity.BookingStatus;
import com.cinesmart.booking.repository.BookingRepository;
import com.cinesmart.common.exception.BadRequestException;
import com.cinesmart.common.exception.ResourceNotFoundException;
import com.cinesmart.common.exception.SeatUnavailableException;
import com.cinesmart.show.entity.Show;
import com.cinesmart.show.entity.ShowSeat;
import com.cinesmart.show.entity.ShowSeatStatus;
import com.cinesmart.show.repository.ShowRepository;
import com.cinesmart.show.repository.ShowSeatRepository;
import com.cinesmart.user.entity.User;
import com.cinesmart.user.entity.UserRole;
import com.cinesmart.user.repository.UserRepository;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class BookingService {

    private final BookingRepository bookingRepository;
    private final ShowRepository showRepository;
    private final ShowSeatRepository showSeatRepository;
    private final UserRepository userRepository;

    public BookingService(BookingRepository bookingRepository,
                          ShowRepository showRepository,
                          ShowSeatRepository showSeatRepository,
                          UserRepository userRepository) {
        this.bookingRepository = bookingRepository;
        this.showRepository = showRepository;
        this.showSeatRepository = showSeatRepository;
        this.userRepository = userRepository;
    }

    public List<BookingDTO> getUserBookings(String userEmail) {
        User user = userRepository.findByEmail(userEmail)
                .orElseThrow(() -> new ResourceNotFoundException("User", "email", userEmail));

        return bookingRepository.findByUserIdOrderByCreatedAtDesc(user.getId()).stream()
                .map(BookingDTO::fromEntity)
                .collect(Collectors.toList());
    }

    public BookingDTO getBookingById(Long id, String userEmail) {
        Booking booking = bookingRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Booking", "id", id));

        User user = userRepository.findByEmail(userEmail)
                .orElseThrow(() -> new ResourceNotFoundException("User", "email", userEmail));

        // Enforce ownership: user must own booking or be an ADMIN
        if (!booking.getUser().getId().equals(user.getId()) && user.getRole() != UserRole.ROLE_ADMIN) {
            throw new AccessDeniedException("You do not have permission to view this booking");
        }

        return BookingDTO.fromEntity(booking);
    }

    @Transactional
    public BookingDTO createBooking(String userEmail, BookingCreateRequest request) {
        User user = userRepository.findByEmail(userEmail)
                .orElseThrow(() -> new ResourceNotFoundException("User", "email", userEmail));

        Show show = showRepository.findById(request.getShowId())
                .orElseThrow(() -> new ResourceNotFoundException("Show", "id", request.getShowId()));

        if (request.getShowSeatIds() == null || request.getShowSeatIds().isEmpty()) {
            throw new BadRequestException("At least one seat must be selected", "NO_SEATS_SELECTED");
        }

        // Validate individual seat IDs for validity
        if (request.getShowSeatIds().stream().anyMatch(id -> id == null || id <= 0)) {
            throw new BadRequestException("Invalid seat identifier provided", "INVALID_SEAT_IDS");
        }

        // Check for duplicate seat IDs in the request
        java.util.Set<Long> uniqueIds = new java.util.HashSet<>(request.getShowSeatIds());
        if (uniqueIds.size() != request.getShowSeatIds().size()) {
            throw new BadRequestException("Duplicate seat selections are not allowed", "DUPLICATE_SEAT_IDS");
        }

        if (request.getShowSeatIds().size() > com.cinesmart.seat.group.dto.GroupSeatingRequest.MAX_GROUP_SIZE) {
            throw new BadRequestException("Cannot book more than " + com.cinesmart.seat.group.dto.GroupSeatingRequest.MAX_GROUP_SIZE + " seats per booking", "GROUP_SIZE_EXCEEDS_LIMIT");
        }

        // Concurrency-safe: Acquire pessimistic write lock on target seats
        List<ShowSeat> selectedSeats = showSeatRepository.findAllByIdWithLock(request.getShowSeatIds());

        if (selectedSeats.size() != request.getShowSeatIds().size()) {
            throw new BadRequestException("One or more selected seats do not exist", "INVALID_SEAT_IDS");
        }

        // Verify all selected seats belong to this show and are AVAILABLE
        BigDecimal totalAmount = BigDecimal.ZERO;
        for (ShowSeat seat : selectedSeats) {
            if (!seat.getShow().getId().equals(show.getId())) {
                throw new BadRequestException("Seat does not belong to specified show", "CROSS_SHOW_SEAT_MISMATCH");
            }
            if (seat.getStatus() != ShowSeatStatus.AVAILABLE) {
                String seatLabel = seat.getSeat() != null
                        ? String.format("Row %s Seat %d", seat.getSeat().getRowIdentifier(), seat.getSeat().getColumnNumber())
                        : "Seat ID " + seat.getId();
                throw new SeatUnavailableException(seatLabel + " is no longer available. Please select another seat.");
            }
            totalAmount = totalAmount.add(seat.getPrice());
        }

        // Generate unique booking reference
        String ref = "CS-" + LocalDateTime.now().getYear() + "-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase();

        Booking booking = new Booking(ref, user, show, totalAmount);
        booking.setStatus(BookingStatus.CONFIRMED); // Foundation status for Phase 2 demo
        booking.setConfirmedAt(LocalDateTime.now());

        // Snapshot seat line items and transition seat status
        for (ShowSeat seat : selectedSeats) {
            String label = seat.getSeat() != null
                    ? String.format("%s-%d", seat.getSeat().getRowIdentifier(), seat.getSeat().getColumnNumber())
                    : "Seat-" + seat.getId();
            String tier = seat.getSeat() != null ? seat.getSeat().getSeatTier().name() : "STANDARD";

            BookingSeat bookingSeat = new BookingSeat(booking, seat, seat.getPrice(), label, tier);
            booking.addBookingSeat(bookingSeat);

            seat.setStatus(ShowSeatStatus.BOOKED);
        }

        showSeatRepository.saveAll(selectedSeats);
        Booking savedBooking = bookingRepository.save(booking);

        return BookingDTO.fromEntity(savedBooking);
    }
}
