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
import com.cinesmart.coupon.service.CouponService;
import com.cinesmart.loyalty.service.LoyaltyService;
import com.cinesmart.payment.service.PaymentService;
import com.cinesmart.show.entity.Show;
import com.cinesmart.show.entity.ShowSeat;
import com.cinesmart.show.entity.ShowSeatStatus;
import com.cinesmart.show.repository.ShowRepository;
import com.cinesmart.show.repository.ShowSeatRepository;
import com.cinesmart.ticket.service.TicketService;
import com.cinesmart.user.entity.User;
import com.cinesmart.user.entity.UserRole;
import com.cinesmart.user.repository.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Lazy;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@Transactional(readOnly = true)
public class BookingService {

    private static final Logger log = LoggerFactory.getLogger(BookingService.class);

    private final BookingRepository bookingRepository;
    private final ShowRepository showRepository;
    private final ShowSeatRepository showSeatRepository;
    private final UserRepository userRepository;
    private final TicketService ticketService;
    private final PaymentService paymentService;
    private final CouponService couponService;
    private final LoyaltyService loyaltyService;

    @Value("${cinesmart.booking.lock-timeout-minutes:5}")
    private int lockTimeoutMinutes = 5;

    public BookingService(BookingRepository bookingRepository,
                          ShowRepository showRepository,
                          ShowSeatRepository showSeatRepository,
                          UserRepository userRepository,
                          TicketService ticketService,
                          @Lazy PaymentService paymentService,
                          CouponService couponService,
                          LoyaltyService loyaltyService) {
        this.bookingRepository = bookingRepository;
        this.showRepository = showRepository;
        this.showSeatRepository = showSeatRepository;
        this.userRepository = userRepository;
        this.ticketService = ticketService;
        this.paymentService = paymentService;
        this.couponService = couponService;
        this.loyaltyService = loyaltyService;
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

        LocalDateTime now = LocalDateTime.now();

        // Verify all selected seats belong to this show and are AVAILABLE (or have an expired lock)
        BigDecimal totalAmount = BigDecimal.ZERO;
        for (ShowSeat seat : selectedSeats) {
            if (!seat.getShow().getId().equals(show.getId())) {
                throw new BadRequestException("Seat does not belong to specified show", "CROSS_SHOW_SEAT_MISMATCH");
            }
            if (seat.isLockExpired(now)) {
                seat.release();
            }
            if (seat.getStatus() != ShowSeatStatus.AVAILABLE) {
                String seatLabel = seat.getSeat() != null
                        ? String.format("Row %s Seat %d", seat.getSeat().getRowIdentifier(), seat.getSeat().getColumnNumber())
                        : "Seat ID " + seat.getId();
                String statusDetail = (seat.getStatus() == ShowSeatStatus.LOCKED || seat.getStatus() == ShowSeatStatus.HELD)
                        ? "is currently locked by another customer. Please choose another seat or wait for it to be released."
                        : "has already been booked for this show.";
                throw new SeatUnavailableException(seatLabel + " " + statusDetail);
            }
            totalAmount = totalAmount.add(seat.getPrice());
        }

        // Generate unique booking reference
        String ref = "CS-" + LocalDateTime.now().getYear() + "-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase();

        LocalDateTime lockExpiry = now.plusMinutes(lockTimeoutMinutes);
        Booking booking = new Booking(ref, user, show, totalAmount);
        // Phase 4: booking is created as PENDING_PAYMENT, awaiting payment processing
        booking.setStatus(BookingStatus.PENDING_PAYMENT);
        booking.setExpiresAt(lockExpiry);

        // Snapshot seat line items and lock seats temporarily
        for (ShowSeat seat : selectedSeats) {
            String label = seat.getSeat() != null
                    ? String.format("%s-%d", seat.getSeat().getRowIdentifier(), seat.getSeat().getColumnNumber())
                    : "Seat-" + seat.getId();
            String tier = seat.getSeat() != null ? seat.getSeat().getSeatTier().name() : "STANDARD";

            BookingSeat bookingSeat = new BookingSeat(booking, seat, seat.getPrice(), label, tier);
            booking.addBookingSeat(bookingSeat);

            seat.lock(user, lockExpiry);
        }

        showSeatRepository.saveAll(selectedSeats);
        Booking savedBooking = bookingRepository.save(booking);
        log.info("Created pending booking {} with {} seats locked until {} for user {}",
                savedBooking.getBookingReference(), selectedSeats.size(), lockExpiry, userEmail);

        return BookingDTO.fromEntity(savedBooking);
    }

    @Transactional
    public BookingDTO confirmBooking(Long bookingId) {
        Booking booking = bookingRepository.findById(bookingId)
                .orElseThrow(() -> new ResourceNotFoundException("Booking", "id", bookingId));

        if (booking.getStatus() == BookingStatus.CONFIRMED) {
            log.info("Booking {} is already confirmed", bookingId);
            return BookingDTO.fromEntity(booking);
        }

        if (booking.getStatus() != BookingStatus.PENDING_PAYMENT && booking.getStatus() != BookingStatus.PAYMENT_PENDING_VERIFICATION) {
            throw new IllegalStateException("Cannot confirm booking with status " + booking.getStatus());
        }

        booking.confirm();

        // Transition locked/held seats to permanently BOOKED
        List<ShowSeat> seatsToUpdate = new ArrayList<>();
        for (BookingSeat bs : booking.getBookingSeats()) {
            ShowSeat seat = bs.getShowSeat();
            if (seat != null) {
                seat.book();
                seatsToUpdate.add(seat);
            }
        }
        showSeatRepository.saveAll(seatsToUpdate);
        Booking saved = bookingRepository.save(booking);

        // Generate digital tickets for customer
        ticketService.generateTicketsForBooking(saved);
        log.info("Booking {} successfully CONFIRMED and seats permanently BOOKED", saved.getBookingReference());

        return BookingDTO.fromEntity(saved);
    }

    @Transactional
    public void handlePaymentFailure(Long bookingId, String failureReason) {
        Booking booking = bookingRepository.findById(bookingId)
                .orElseThrow(() -> new ResourceNotFoundException("Booking", "id", bookingId));

        if (booking.getStatus() == BookingStatus.CONFIRMED) {
            log.warn("Cannot mark confirmed booking {} as failed", bookingId);
            return;
        }

        booking.markPaymentFailed();

        // Release held seats back to AVAILABLE
        List<ShowSeat> seatsToRelease = new ArrayList<>();
        for (BookingSeat bs : booking.getBookingSeats()) {
            ShowSeat seat = bs.getShowSeat();
            if (seat != null) {
                seat.release();
                seatsToRelease.add(seat);
            }
        }
        showSeatRepository.saveAll(seatsToRelease);
        bookingRepository.save(booking);
        log.warn("Booking {} marked PAYMENT_FAILED ({}). Held seats released back to AVAILABLE.", bookingId, failureReason);
    }

    @Transactional
    public BookingDTO cancelBooking(Long bookingId, String userEmail) {
        Booking booking = bookingRepository.findById(bookingId)
                .orElseThrow(() -> new ResourceNotFoundException("Booking", "id", bookingId));

        User user = userRepository.findByEmail(userEmail)
                .orElseThrow(() -> new ResourceNotFoundException("User", "email", userEmail));

        if (!booking.getUser().getId().equals(user.getId()) && user.getRole() != UserRole.ROLE_ADMIN) {
            throw new AccessDeniedException("You do not have permission to cancel this booking");
        }

        if (booking.getStatus() == BookingStatus.CANCELLED || booking.getStatus() == BookingStatus.CANCELLED_BY_CINEMA) {
            throw new BadRequestException("Booking has already been cancelled", "ALREADY_CANCELLED");
        }

        if (booking.getStatus() == BookingStatus.REFUNDED) {
            throw new BadRequestException("Booking has already been refunded and cancelled", "ALREADY_REFUNDED");
        }

        if (booking.getShow() != null && booking.getShow().getStartTime() != null
                && booking.getShow().getStartTime().isBefore(LocalDateTime.now())) {
            throw new BadRequestException("Cannot cancel booking for a show that has already started or concluded", "SHOW_ALREADY_STARTED");
        }

        boolean wasConfirmed = (booking.getStatus() == BookingStatus.CONFIRMED);

        // If booking was confirmed, refund payment, void tickets, release coupon, and reverse loyalty points
        if (wasConfirmed) {
            paymentService.refundBookingPayment(booking, userEmail);
            ticketService.voidTicketsForBooking(booking);
            couponService.releaseCouponUsage(booking.getId());
            loyaltyService.reversePointsForBooking(booking);
        }

        // Release seats back to AVAILABLE
        List<ShowSeat> seatsToRelease = new ArrayList<>();
        for (BookingSeat bs : booking.getBookingSeats()) {
            ShowSeat seat = bs.getShowSeat();
            if (seat != null) {
                seat.release();
                seatsToRelease.add(seat);
            }
        }
        showSeatRepository.saveAll(seatsToRelease);

        booking.cancel();
        Booking saved = bookingRepository.save(booking);
        log.info("Booking {} successfully cancelled by {}, seats released", booking.getId(), userEmail);

        return BookingDTO.fromEntity(saved);
    }
}
