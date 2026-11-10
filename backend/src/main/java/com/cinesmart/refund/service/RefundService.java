package com.cinesmart.refund.service;

import com.cinesmart.booking.entity.Booking;
import com.cinesmart.booking.entity.BookingSeat;
import com.cinesmart.booking.entity.BookingStatus;
import com.cinesmart.booking.repository.BookingRepository;
import com.cinesmart.common.exception.BadRequestException;
import com.cinesmart.common.exception.ResourceNotFoundException;
import com.cinesmart.coupon.service.CouponService;
import com.cinesmart.loyalty.entity.LoyaltyTransaction;
import com.cinesmart.loyalty.entity.LoyaltyTransactionType;
import com.cinesmart.loyalty.repository.LoyaltyTransactionRepository;
import com.cinesmart.loyalty.service.LoyaltyService;
import com.cinesmart.payment.entity.PaymentStatus;
import com.cinesmart.payment.entity.PaymentTransaction;
import com.cinesmart.payment.gateway.PaymentGateway;
import com.cinesmart.payment.gateway.RefundGatewayRequest;
import com.cinesmart.payment.gateway.RefundGatewayResponse;
import com.cinesmart.payment.repository.PaymentTransactionRepository;
import com.cinesmart.refund.dto.*;
import com.cinesmart.refund.entity.CancellationPolicy;
import com.cinesmart.refund.entity.RefundRequest;
import com.cinesmart.refund.entity.RefundStatus;
import com.cinesmart.refund.repository.RefundRequestRepository;
import com.cinesmart.show.entity.ShowSeat;
import com.cinesmart.show.repository.ShowSeatRepository;
import com.cinesmart.ticket.entity.Ticket;
import com.cinesmart.ticket.entity.TicketStatus;
import com.cinesmart.ticket.repository.TicketRepository;
import com.cinesmart.ticket.service.TicketService;
import com.cinesmart.user.entity.User;
import com.cinesmart.user.entity.UserRole;
import com.cinesmart.user.repository.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@Transactional(readOnly = true)
public class RefundService {

    private static final Logger log = LoggerFactory.getLogger(RefundService.class);

    private final RefundRequestRepository refundRequestRepository;
    private final CancellationPolicyService cancellationPolicyService;
    private final BookingRepository bookingRepository;
    private final UserRepository userRepository;
    private final PaymentTransactionRepository paymentTransactionRepository;
    private final PaymentGateway paymentGateway;
    private final TicketRepository ticketRepository;
    private final TicketService ticketService;
    private final ShowSeatRepository showSeatRepository;
    private final CouponService couponService;
    private final LoyaltyService loyaltyService;
    private final LoyaltyTransactionRepository loyaltyTransactionRepository;

    public RefundService(RefundRequestRepository refundRequestRepository,
                         CancellationPolicyService cancellationPolicyService,
                         BookingRepository bookingRepository,
                         UserRepository userRepository,
                         PaymentTransactionRepository paymentTransactionRepository,
                         PaymentGateway paymentGateway,
                         TicketRepository ticketRepository,
                         TicketService ticketService,
                         ShowSeatRepository showSeatRepository,
                         CouponService couponService,
                         LoyaltyService loyaltyService,
                         LoyaltyTransactionRepository loyaltyTransactionRepository) {
        this.refundRequestRepository = refundRequestRepository;
        this.cancellationPolicyService = cancellationPolicyService;
        this.bookingRepository = bookingRepository;
        this.userRepository = userRepository;
        this.paymentTransactionRepository = paymentTransactionRepository;
        this.paymentGateway = paymentGateway;
        this.ticketRepository = ticketRepository;
        this.ticketService = ticketService;
        this.showSeatRepository = showSeatRepository;
        this.couponService = couponService;
        this.loyaltyService = loyaltyService;
        this.loyaltyTransactionRepository = loyaltyTransactionRepository;
    }

    /**
     * Previews cancellation calculations, deductions, and refund amount based on active policy.
     */
    public CancellationPreviewResponseDTO previewCancellation(Long bookingId, String userEmail) {
        Booking booking = bookingRepository.findById(bookingId)
                .orElseThrow(() -> new ResourceNotFoundException("Booking", "id", bookingId));

        validateOwnershipOrAdmin(booking, userEmail);

        LocalDateTime now = LocalDateTime.now();

        // 1. Check if already cancelled or refunded
        if (booking.getStatus() == BookingStatus.CANCELLED || booking.getStatus() == BookingStatus.CANCELLED_BY_CINEMA) {
            return CancellationPreviewResponseDTO.ineligible(bookingId, booking.getBookingReference(), "This booking has already been cancelled.");
        }
        if (booking.getStatus() == BookingStatus.REFUNDED) {
            return CancellationPreviewResponseDTO.ineligible(bookingId, booking.getBookingReference(), "This booking has already been refunded.");
        }
        if (booking.getStatus() != BookingStatus.CONFIRMED && booking.getStatus() != BookingStatus.PENDING_PAYMENT) {
            return CancellationPreviewResponseDTO.ineligible(bookingId, booking.getBookingReference(), "Only confirmed bookings can be cancelled with refund.");
        }

        // 2. Check if show has already started or ended
        if (booking.getShow() == null || booking.getShow().getStartTime() == null) {
            return CancellationPreviewResponseDTO.ineligible(bookingId, booking.getBookingReference(), "Show information is missing.");
        }
        LocalDateTime showStart = booking.getShow().getStartTime();
        if (showStart.isBefore(now)) {
            return CancellationPreviewResponseDTO.ineligible(bookingId, booking.getBookingReference(), "Show has already started or concluded. Cancellation is not allowed.");
        }

        // 3. Check if any ticket has already been used/scanned
        List<Ticket> tickets = ticketRepository.findByBookingId(bookingId);
        boolean anyUsed = tickets.stream().anyMatch(t -> t.getStatus() == TicketStatus.USED);
        if (anyUsed) {
            return CancellationPreviewResponseDTO.ineligible(bookingId, booking.getBookingReference(), "Cannot cancel booking because one or more tickets have already been scanned for entry.");
        }

        // 4. Calculate hours remaining before show
        Duration duration = Duration.between(now, showStart);
        double hoursRemaining = Math.max(0.0, duration.toMinutes() / 60.0);

        // 5. Match active CancellationPolicy
        CancellationPolicy policy = cancellationPolicyService.findMatchingPolicy(hoursRemaining);

        BigDecimal paidAmount = booking.getTotalAmount() != null ? booking.getTotalAmount() : BigDecimal.ZERO;
        BigDecimal refundPercentage = policy.getRefundPercentage();
        BigDecimal refundAmount = paidAmount.multiply(refundPercentage)
                .divide(new BigDecimal("100"), 2, RoundingMode.HALF_UP);
        BigDecimal fee = paidAmount.subtract(refundAmount);
        if (fee.compareTo(BigDecimal.ZERO) < 0) {
            fee = BigDecimal.ZERO;
        }

        // 6. Check loyalty points impact
        int pointsToClawback = 0;
        Optional<LoyaltyTransaction> earnedTxn = loyaltyTransactionRepository
                .findByBookingIdAndTransactionType(booking.getId(), LoyaltyTransactionType.EARNED);
        if (earnedTxn.isPresent() && earnedTxn.get().getPoints() > 0) {
            pointsToClawback = earnedTxn.get().getPoints();
        }

        int pointsToRestore = booking.getLoyaltyPointsRedeemed() != null ? booking.getLoyaltyPointsRedeemed() : 0;
        boolean couponRestored = booking.getCouponCode() != null && !booking.getCouponCode().trim().isEmpty();

        CancellationPreviewResponseDTO preview = new CancellationPreviewResponseDTO();
        preview.setBookingId(booking.getId());
        preview.setBookingReference(booking.getBookingReference());
        preview.setMovieTitle(booking.getShow().getMovie() != null ? booking.getShow().getMovie().getTitle() : "Movie");
        preview.setShowStartTime(showStart);
        preview.setCurrentTime(now);
        preview.setHoursRemaining(Math.round(hoursRemaining * 100.0) / 100.0);
        preview.setEligible(true);
        preview.setPaidAmount(paidAmount);
        preview.setRefundPercentage(refundPercentage);
        preview.setCancellationFee(fee);
        preview.setEstimatedRefundAmount(refundAmount);
        preview.setPolicyName(policy.getPolicyName());
        preview.setPolicyDescription(policy.getDescription());
        preview.setTicketCount(tickets.size());
        preview.setLoyaltyPointsToClawback(pointsToClawback);
        preview.setLoyaltyPointsToRestore(pointsToRestore);
        preview.setCouponRestored(couponRestored);

        return preview;
    }

    /**
     * Executes atomic cancellation of booking and registers/processes refund according to policy.
     */
    @Transactional
    public RefundRequestDTO cancelBookingWithRefund(Long bookingId, String userEmail, BookingCancellationRequest request) {
        Booking booking = bookingRepository.findById(bookingId)
                .orElseThrow(() -> new ResourceNotFoundException("Booking", "id", bookingId));

        User user = validateOwnershipOrAdmin(booking, userEmail);

        LocalDateTime now = LocalDateTime.now();

        // Strict Validations
        if (booking.getStatus() == BookingStatus.CANCELLED || booking.getStatus() == BookingStatus.CANCELLED_BY_CINEMA) {
            throw new BadRequestException("Booking has already been cancelled", "ALREADY_CANCELLED");
        }
        if (booking.getStatus() == BookingStatus.REFUNDED) {
            throw new BadRequestException("Booking has already been refunded and cancelled", "ALREADY_REFUNDED");
        }

        if (booking.getShow() == null || booking.getShow().getStartTime() == null || booking.getShow().getStartTime().isBefore(now)) {
            throw new BadRequestException("Cannot cancel booking for a show that has already started or ended", "SHOW_ALREADY_STARTED");
        }

        List<Ticket> tickets = ticketRepository.findByBookingId(bookingId);
        boolean anyUsed = tickets.stream().anyMatch(t -> t.getStatus() == TicketStatus.USED);
        if (anyUsed) {
            throw new BadRequestException("Cannot cancel booking because one or more tickets have already been scanned for entry", "TICKET_ALREADY_USED");
        }

        // Prevent duplicate active refund requests for the same booking
        boolean activeRefundExists = refundRequestRepository.existsByBookingIdAndStatusNotIn(
                bookingId, List.of(RefundStatus.REJECTED, RefundStatus.FAILED));
        if (activeRefundExists) {
            throw new BadRequestException("A refund request is already active or completed for this booking", "DUPLICATE_REFUND_REQUEST");
        }

        // Calculate hours before show & match active policy
        Duration duration = Duration.between(now, booking.getShow().getStartTime());
        double hoursBeforeShow = Math.max(0.0, duration.toMinutes() / 60.0);
        CancellationPolicy policy = cancellationPolicyService.findMatchingPolicy(hoursBeforeShow);

        BigDecimal paidAmount = booking.getTotalAmount() != null ? booking.getTotalAmount() : BigDecimal.ZERO;
        BigDecimal refundPercentage = policy.getRefundPercentage();
        BigDecimal refundAmount = paidAmount.multiply(refundPercentage)
                .divide(new BigDecimal("100"), 2, RoundingMode.HALF_UP);
        BigDecimal cancellationFee = paidAmount.subtract(refundAmount);
        if (cancellationFee.compareTo(BigDecimal.ZERO) < 0) {
            cancellationFee = BigDecimal.ZERO;
        }

        // Find associated successful payment transaction
        Optional<PaymentTransaction> paymentOpt = paymentTransactionRepository.findByBookingIdAndStatus(bookingId, PaymentStatus.SUCCESS);

        // 1. Release held/booked seats back to AVAILABLE
        List<ShowSeat> seatsToRelease = new ArrayList<>();
        for (BookingSeat bs : booking.getBookingSeats()) {
            ShowSeat seat = bs.getShowSeat();
            if (seat != null) {
                seat.release();
                seatsToRelease.add(seat);
            }
        }
        showSeatRepository.saveAll(seatsToRelease);

        // 2. Void all issued digital tickets
        ticketService.voidTicketsForBooking(booking);

        // 3. Release/invalidate coupon usage so customer can use it again if allowed
        couponService.releaseCouponUsage(booking.getId());

        // 4. Reverse loyalty points (claw back earned points & restore redeemed points)
        loyaltyService.reversePointsForBooking(booking);

        // 5. Update booking status
        booking.cancel();
        if (request != null && request.getReason() != null && !request.getReason().trim().isEmpty()) {
            booking.setCancellationReason(request.getReason().trim());
        }
        bookingRepository.save(booking);

        // 6. Generate unique refund reference
        String refundRef = "REF-" + LocalDateTime.now().getYear() + "-" + UUID.randomUUID().toString().substring(0, 10).toUpperCase();

        RefundRequest refundReq = new RefundRequest(
                refundRef,
                booking,
                paymentOpt.orElse(null),
                booking.getUser(),
                policy,
                paidAmount,
                cancellationFee,
                refundAmount,
                refundPercentage,
                BigDecimal.valueOf(Math.round(hoursBeforeShow * 100.0) / 100.0),
                request != null ? request.getReason() : null
        );

        // 7. Process refund: Handle zero-refund vs simulated auto-refund vs manual admin approval
        boolean autoProcess = (request == null || request.getSimulateAutoProcess() == null || Boolean.TRUE.equals(request.getSimulateAutoProcess()));

        if (refundAmount.compareTo(BigDecimal.ZERO) <= 0) {
            // No refund payable due to cancellation policy threshold (<6h)
            refundReq.markCompleted("ZERO-REFUND-FEE-100", null, "Cancelled under policy with 0% refund window (100% cancellation fee)");
            booking.markRefunded();
            bookingRepository.save(booking);
            log.info("Booking {} cancelled with zero refund under policy: {}", booking.getId(), policy.getPolicyName());
        } else if (autoProcess && paymentOpt.isPresent()) {
            // Simulated Instant Refund Execution
            PaymentTransaction txn = paymentOpt.get();
            RefundGatewayRequest gatewayReq = new RefundGatewayRequest(
                    txn.getTransactionReference(),
                    txn.getGatewayTxnId(),
                    refundAmount,
                    "CineSmart Booking Cancellation: " + (request != null ? request.getReason() : "Customer requested")
            );

            refundReq.markProcessing();
            RefundGatewayResponse gatewayResp = paymentGateway.processRefund(gatewayReq);

            if (gatewayResp.isSuccess()) {
                refundReq.markCompleted(gatewayResp.getRefundReference(), null, "Simulated instant refund issued to original payment method");
                booking.markRefunded();
                bookingRepository.save(booking);

                txn.markRefunded(gatewayResp.getRefundReference());
                paymentTransactionRepository.save(txn);

                log.info("Simulated refund {} for booking {} processed successfully. Gateway Ref: {}",
                        refundRef, booking.getId(), gatewayResp.getRefundReference());
            } else {
                refundReq.markFailed("Gateway refund failed: " + gatewayResp.getStatusMessage(), null);
                log.error("Simulated refund failed for booking {}: {}", booking.getId(), gatewayResp.getStatusMessage());
            }
        } else {
            // Held in REQUESTED state for administrator review
            log.info("Refund request {} created in REQUESTED state for admin review", refundRef);
        }

        RefundRequest savedRefund = refundRequestRepository.save(refundReq);
        return RefundRequestDTO.fromEntity(savedRefund);
    }

    /**
     * Retrieve all refund records for authenticated customer.
     */
    public List<RefundRequestDTO> getRefundsForUser(String userEmail) {
        User user = userRepository.findByEmail(userEmail)
                .orElseThrow(() -> new ResourceNotFoundException("User", "email", userEmail));
        return refundRequestRepository.findByUserIdOrderByRequestedAtDesc(user.getId())
                .stream()
                .map(RefundRequestDTO::fromEntity)
                .collect(Collectors.toList());
    }

    /**
     * Retrieve specific refund record by ID.
     */
    public RefundRequestDTO getRefundById(Long id, String userEmail) {
        RefundRequest req = refundRequestRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("RefundRequest", "id", id));

        User user = userRepository.findByEmail(userEmail)
                .orElseThrow(() -> new ResourceNotFoundException("User", "email", userEmail));

        if (!req.getUser().getId().equals(user.getId()) && user.getRole() != UserRole.ROLE_ADMIN) {
            throw new AccessDeniedException("You do not have permission to view this refund request");
        }

        return RefundRequestDTO.fromEntity(req);
    }

    /**
     * Admin: Retrieve all refund records with optional status filter.
     */
    public List<RefundRequestDTO> getAllRefunds(RefundStatus status) {
        List<RefundRequest> list;
        if (status != null) {
            list = refundRequestRepository.findByStatusOrderByRequestedAtDesc(status);
        } else {
            list = refundRequestRepository.findAllByOrderByRequestedAtDesc();
        }
        return list.stream().map(RefundRequestDTO::fromEntity).collect(Collectors.toList());
    }

    /**
     * Admin: Retrieve refund dashboard statistics.
     */
    public RefundStatsDTO getRefundStats() {
        long total = refundRequestRepository.count();
        long requested = refundRequestRepository.countByStatus(RefundStatus.REQUESTED);
        long approved = refundRequestRepository.countByStatus(RefundStatus.APPROVED);
        long processing = refundRequestRepository.countByStatus(RefundStatus.PROCESSING);
        long completed = refundRequestRepository.countByStatus(RefundStatus.COMPLETED);
        long rejected = refundRequestRepository.countByStatus(RefundStatus.REJECTED);
        long failed = refundRequestRepository.countByStatus(RefundStatus.FAILED);

        BigDecimal totalRefundAmount = refundRequestRepository.sumRefundAmountByStatus(RefundStatus.COMPLETED);
        BigDecimal totalFees = refundRequestRepository.sumCancellationFeeByStatus(RefundStatus.COMPLETED);

        return new RefundStatsDTO(total, requested, approved, processing, completed, rejected, failed, totalRefundAmount, totalFees);
    }

    /**
     * Admin: Approve a pending refund request.
     */
    @Transactional
    public RefundRequestDTO approveRefund(Long id, String adminEmail, RefundAdminActionRequest action) {
        RefundRequest req = refundRequestRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("RefundRequest", "id", id));
        User admin = userRepository.findByEmail(adminEmail)
                .orElseThrow(() -> new ResourceNotFoundException("User", "email", adminEmail));

        req.approve(admin, action != null ? action.getNotes() : null);
        RefundRequest saved = refundRequestRepository.save(req);
        log.info("Refund request {} APPROVED by admin {}", req.getRefundReference(), adminEmail);
        return RefundRequestDTO.fromEntity(saved);
    }

    /**
     * Admin: Process an approved refund request through simulated/live gateway.
     */
    @Transactional
    public RefundRequestDTO processRefund(Long id, String adminEmail, RefundAdminActionRequest action) {
        RefundRequest req = refundRequestRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("RefundRequest", "id", id));
        User admin = userRepository.findByEmail(adminEmail)
                .orElseThrow(() -> new ResourceNotFoundException("User", "email", adminEmail));

        if (req.getStatus() == RefundStatus.COMPLETED) {
            return RefundRequestDTO.fromEntity(req);
        }

        req.markProcessing();
        refundRequestRepository.save(req);

        // Execute refund through payment gateway
        PaymentTransaction txn = req.getPayment();
        String txnRef = txn != null ? txn.getTransactionReference() : req.getRefundReference();
        String gwTxnId = txn != null ? txn.getGatewayTxnId() : "MOCK-TXN";

        RefundGatewayRequest gatewayReq = new RefundGatewayRequest(
                txnRef,
                gwTxnId,
                req.getRefundAmount(),
                "Admin Approved Refund: " + (action != null && action.getNotes() != null ? action.getNotes() : "Approved")
        );

        RefundGatewayResponse response = paymentGateway.processRefund(gatewayReq);

        if (response.isSuccess()) {
            req.markCompleted(response.getRefundReference(), admin, action != null ? action.getNotes() : "Approved and processed");
            if (req.getBooking() != null) {
                req.getBooking().markRefunded();
                bookingRepository.save(req.getBooking());
            }
            if (txn != null) {
                txn.markRefunded(response.getRefundReference());
                paymentTransactionRepository.save(txn);
            }
            log.info("Refund request {} successfully processed with gateway ref {}", req.getRefundReference(), response.getRefundReference());
        } else {
            req.markFailed("Gateway error: " + response.getStatusMessage(), admin);
            log.error("Refund request {} processing failed: {}", req.getRefundReference(), response.getStatusMessage());
        }

        RefundRequest saved = refundRequestRepository.save(req);
        return RefundRequestDTO.fromEntity(saved);
    }

    /**
     * Admin: Reject a refund request with justification note.
     */
    @Transactional
    public RefundRequestDTO rejectRefund(Long id, String adminEmail, RefundAdminActionRequest action) {
        RefundRequest req = refundRequestRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("RefundRequest", "id", id));
        User admin = userRepository.findByEmail(adminEmail)
                .orElseThrow(() -> new ResourceNotFoundException("User", "email", adminEmail));

        String reason = (action != null && action.getRejectionReason() != null)
                ? action.getRejectionReason()
                : "Rejected by cinema management based on terms of service";

        req.reject(reason, admin);
        RefundRequest saved = refundRequestRepository.save(req);
        log.info("Refund request {} REJECTED by admin {}: {}", req.getRefundReference(), adminEmail, reason);
        return RefundRequestDTO.fromEntity(saved);
    }

    private User validateOwnershipOrAdmin(Booking booking, String userEmail) {
        User user = userRepository.findByEmail(userEmail)
                .orElseThrow(() -> new ResourceNotFoundException("User", "email", userEmail));

        if (!booking.getUser().getId().equals(user.getId()) && user.getRole() != UserRole.ROLE_ADMIN) {
            throw new AccessDeniedException("You do not have permission to perform operations on this booking");
        }
        return user;
    }
}
