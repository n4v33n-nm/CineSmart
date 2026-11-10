package com.cinesmart.payment.service;

import com.cinesmart.booking.entity.Booking;
import com.cinesmart.booking.entity.BookingSeat;
import com.cinesmart.booking.entity.BookingStatus;
import com.cinesmart.booking.repository.BookingRepository;
import com.cinesmart.booking.service.BookingService;
import com.cinesmart.common.exception.BadRequestException;
import com.cinesmart.common.exception.ResourceNotFoundException;
import com.cinesmart.common.exception.SeatUnavailableException;
import com.cinesmart.coupon.dto.CouponValidateResponseDTO;
import com.cinesmart.coupon.entity.Coupon;
import com.cinesmart.coupon.service.CouponService;
import com.cinesmart.loyalty.dto.LoyaltyRedeemResponseDTO;
import com.cinesmart.loyalty.service.LoyaltyService;
import com.cinesmart.payment.dto.*;
import com.cinesmart.payment.entity.PaymentMethod;
import com.cinesmart.payment.entity.PaymentStatus;
import com.cinesmart.payment.entity.PaymentTransaction;
import com.cinesmart.payment.gateway.*;
import com.cinesmart.payment.repository.PaymentTransactionRepository;
import com.cinesmart.show.entity.ShowSeat;
import com.cinesmart.show.entity.ShowSeatStatus;
import com.cinesmart.ticket.dto.TicketDTO;
import com.cinesmart.ticket.service.TicketService;
import com.cinesmart.user.entity.User;
import com.cinesmart.user.entity.UserRole;
import com.cinesmart.user.repository.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Lazy;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
@Transactional(readOnly = true)
public class PaymentService {

    private static final Logger log = LoggerFactory.getLogger(PaymentService.class);

    private final PaymentTransactionRepository paymentTransactionRepository;
    private final BookingRepository bookingRepository;
    private final UserRepository userRepository;
    private final PaymentGateway paymentGateway;
    private final BookingService bookingService;
    private final TicketService ticketService;
    private final CouponService couponService;
    private final LoyaltyService loyaltyService;

    public PaymentService(PaymentTransactionRepository paymentTransactionRepository,
                          BookingRepository bookingRepository,
                          UserRepository userRepository,
                          PaymentGateway paymentGateway,
                          @Lazy BookingService bookingService,
                          TicketService ticketService,
                          CouponService couponService,
                          LoyaltyService loyaltyService) {
        this.paymentTransactionRepository = paymentTransactionRepository;
        this.bookingRepository = bookingRepository;
        this.userRepository = userRepository;
        this.paymentGateway = paymentGateway;
        this.bookingService = bookingService;
        this.ticketService = ticketService;
        this.couponService = couponService;
        this.loyaltyService = loyaltyService;
    }

    public PaymentResponseDTO getPaymentById(Long id, String userEmail) {
        PaymentTransaction txn = paymentTransactionRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("PaymentTransaction", "id", id));
        validateOwnershipOrAdmin(txn, userEmail);
        return mapToDTOWithTickets(txn);
    }

    public PaymentResponseDTO getPaymentByReference(String reference, String userEmail) {
        PaymentTransaction txn = paymentTransactionRepository.findByTransactionReference(reference)
                .orElseThrow(() -> new ResourceNotFoundException("PaymentTransaction", "reference", reference));
        validateOwnershipOrAdmin(txn, userEmail);
        return mapToDTOWithTickets(txn);
    }

    public List<PaymentResponseDTO> getPaymentsForBooking(Long bookingId, String userEmail) {
        Booking booking = bookingRepository.findById(bookingId)
                .orElseThrow(() -> new ResourceNotFoundException("Booking", "id", bookingId));

        User user = userRepository.findByEmail(userEmail)
                .orElseThrow(() -> new ResourceNotFoundException("User", "email", userEmail));

        if (!booking.getUser().getId().equals(user.getId()) && user.getRole() != UserRole.ROLE_ADMIN) {
            throw new AccessDeniedException("You do not have permission to view payments for this booking");
        }

        return paymentTransactionRepository.findByBookingId(bookingId).stream()
                .map(this::mapToDTOWithTickets)
                .toList();
    }

    @Transactional
    public PaymentResponseDTO initiatePayment(String userEmail, PaymentInitiateRequest request, boolean autoProcess) {
        User user = userRepository.findByEmail(userEmail)
                .orElseThrow(() -> new ResourceNotFoundException("User", "email", userEmail));

        Booking booking = bookingRepository.findById(request.getBookingId())
                .orElseThrow(() -> new ResourceNotFoundException("Booking", "id", request.getBookingId()));

        // Ownership enforcement
        if (!booking.getUser().getId().equals(user.getId()) && user.getRole() != UserRole.ROLE_ADMIN) {
            throw new AccessDeniedException("You do not have permission to initiate payment for this booking");
        }

        // Validate booking status
        if (booking.getStatus() == BookingStatus.CONFIRMED) {
            Optional<PaymentTransaction> existingSuccess = paymentTransactionRepository.findByBookingIdAndStatus(booking.getId(), PaymentStatus.SUCCESS);
            if (existingSuccess.isPresent()) {
                log.info("Booking {} is already confirmed and paid. Idempotently returning existing successful transaction.", booking.getId());
                return mapToDTOWithTickets(existingSuccess.get());
            }
            throw new BadRequestException("Booking is already confirmed and paid", "ALREADY_PAID");
        }
        if (booking.getStatus() == BookingStatus.CANCELLED || booking.getStatus() == BookingStatus.CANCELLED_BY_CINEMA) {
            throw new BadRequestException("Cannot pay for a cancelled booking", "BOOKING_CANCELLED");
        }
        if (booking.getStatus() != BookingStatus.PENDING_PAYMENT) {
            throw new BadRequestException("Booking is not in a payable state (" + booking.getStatus() + ")", "INVALID_BOOKING_STATUS");
        }

        LocalDateTime now = LocalDateTime.now();
        if (booking.getExpiresAt() != null && booking.getExpiresAt().isBefore(now)) {
            bookingService.handlePaymentFailure(booking.getId(), "Seat lock expired");
            throw new SeatUnavailableException("Your temporary seat reservation has expired. Please reselect your seats.");
        }

        // Idempotency verification: Check if an idempotency key was supplied
        String idempotencyKey = (request.getIdempotencyKey() != null && !request.getIdempotencyKey().trim().isEmpty())
                ? request.getIdempotencyKey().trim()
                : "IDEM-" + UUID.randomUUID();

        Optional<PaymentTransaction> existingByKey = paymentTransactionRepository.findByIdempotencyKey(idempotencyKey);
        if (existingByKey.isPresent()) {
            PaymentTransaction existingTxn = existingByKey.get();
            log.info("Idempotent payment request detected for key: {}", idempotencyKey);
            if (existingTxn.getStatus() == PaymentStatus.SUCCESS) {
                return mapToDTOWithTickets(existingTxn);
            }
        }

        // Check if there is already a SUCCESS payment for this booking
        Optional<PaymentTransaction> existingSuccess = paymentTransactionRepository.findByBookingIdAndStatus(booking.getId(), PaymentStatus.SUCCESS);
        if (existingSuccess.isPresent()) {
            log.info("Payment already completed for booking {}. Idempotently returning existing record.", booking.getId());
            return mapToDTOWithTickets(existingSuccess.get());
        }

        // Validate Payment Method
        PaymentMethod paymentMethod = PaymentMethod.fromString(request.getPaymentMethod());

        // Initialize original amount if not set
        java.math.BigDecimal originalAmount = booking.getOriginalAmount() != null && booking.getOriginalAmount().compareTo(java.math.BigDecimal.ZERO) > 0
                ? booking.getOriginalAmount()
                : booking.getTotalAmount();
        booking.setOriginalAmount(originalAmount);

        // Apply coupon discount if provided
        java.math.BigDecimal couponDiscount = java.math.BigDecimal.ZERO;
        if (request.getCouponCode() != null && !request.getCouponCode().trim().isEmpty()) {
            String couponCode = request.getCouponCode().trim().toUpperCase();
            CouponValidateResponseDTO couponResp = couponService.validateCoupon(couponCode, originalAmount, userEmail);
            if (!couponResp.isValid()) {
                throw new BadRequestException(couponResp.getMessage(), "INVALID_COUPON");
            }
            couponDiscount = couponResp.getDiscountAmount();
            booking.setCouponCode(couponCode);
            booking.setDiscountAmount(couponDiscount);
        } else {
            booking.setCouponCode(null);
            booking.setDiscountAmount(java.math.BigDecimal.ZERO);
        }

        // Apply loyalty points discount if provided
        java.math.BigDecimal loyaltyDiscount = java.math.BigDecimal.ZERO;
        if (request.getLoyaltyPointsToRedeem() != null && request.getLoyaltyPointsToRedeem() > 0) {
            java.math.BigDecimal amountAfterCoupon = originalAmount.subtract(couponDiscount);
            if (amountAfterCoupon.compareTo(java.math.BigDecimal.ZERO) < 0) {
                amountAfterCoupon = java.math.BigDecimal.ZERO;
            }

            LoyaltyRedeemResponseDTO loyaltyResp = loyaltyService.calculateRedemption(userEmail, request.getLoyaltyPointsToRedeem(), amountAfterCoupon);
            if (!loyaltyResp.isValid()) {
                throw new BadRequestException(loyaltyResp.getMessage(), "INVALID_LOYALTY_REDEMPTION");
            }
            int pointsToRedeem = loyaltyResp.getPointsRedeemed();
            loyaltyDiscount = loyaltyResp.getDiscountAmount();
            booking.setLoyaltyPointsRedeemed(pointsToRedeem);
            booking.setLoyaltyDiscountAmount(loyaltyDiscount);
        } else {
            booking.setLoyaltyPointsRedeemed(0);
            booking.setLoyaltyDiscountAmount(java.math.BigDecimal.ZERO);
        }

        // Final payable amount
        java.math.BigDecimal finalPayable = originalAmount.subtract(couponDiscount).subtract(loyaltyDiscount);
        if (finalPayable.compareTo(java.math.BigDecimal.ZERO) < 0) {
            finalPayable = java.math.BigDecimal.ZERO;
        }
        booking.setTotalAmount(finalPayable);
        bookingRepository.save(booking);

        // Amount is strictly calculated server-side from booking entity (CLIENT AMOUNT IS NEVER TRUSTED)
        String ref = "TXN-" + LocalDateTime.now().getYear() + "-" + UUID.randomUUID().toString().substring(0, 10).toUpperCase();

        PaymentTransaction transaction = new PaymentTransaction(
                ref,
                booking,
                user,
                idempotencyKey,
                finalPayable,
                "INR",
                paymentMethod
        );

        PaymentTransaction savedTxn = paymentTransactionRepository.save(transaction);
        log.info("Created payment transaction {} for booking {} amount {}", ref, booking.getId(), booking.getTotalAmount());

        if (autoProcess) {
            return processTransactionInternal(savedTxn, request.getSimulateSuccess(), request.getFailureReason());
        }

        return mapToDTOWithTickets(savedTxn);
    }

    @Transactional
    public PaymentResponseDTO processPayment(Long transactionId, String userEmail, PaymentProcessRequest request) {
        PaymentTransaction txn = paymentTransactionRepository.findById(transactionId)
                .orElseThrow(() -> new ResourceNotFoundException("PaymentTransaction", "id", transactionId));

        validateOwnershipOrAdmin(txn, userEmail);

        if (txn.getStatus() == PaymentStatus.SUCCESS) {
            return mapToDTOWithTickets(txn);
        }

        if (txn.getStatus() != PaymentStatus.INITIATED) {
            throw new IllegalStateException("Cannot process transaction in status " + txn.getStatus() + ". Only INITIATED transactions can be processed.");
        }

        Boolean simulateSuccess = request != null ? request.getSimulateSuccess() : true;
        String failureReason = request != null ? request.getFailureReason() : null;

        return processTransactionInternal(txn, simulateSuccess, failureReason);
    }

    @Transactional
    public PaymentResponseDTO cancelPayment(Long transactionId, String userEmail) {
        PaymentTransaction txn = paymentTransactionRepository.findById(transactionId)
                .orElseThrow(() -> new ResourceNotFoundException("PaymentTransaction", "id", transactionId));

        validateOwnershipOrAdmin(txn, userEmail);

        if (txn.getStatus() != PaymentStatus.INITIATED) {
            throw new IllegalStateException("Cannot cancel payment in status " + txn.getStatus() + ". Only INITIATED payments can be cancelled.");
        }

        txn.markCancelled();
        paymentTransactionRepository.save(txn);

        // Cancel the pending booking and release held seats
        bookingService.handlePaymentFailure(txn.getBooking().getId(), "Payment explicitly cancelled by user");

        log.info("Payment transaction {} cancelled by user", txn.getTransactionReference());
        return mapToDTOWithTickets(txn);
    }

    @Transactional
    public RefundResponseDTO refundPayment(Long transactionId, String userEmail, RefundRequestDTO request) {
        PaymentTransaction txn = paymentTransactionRepository.findById(transactionId)
                .orElseThrow(() -> new ResourceNotFoundException("PaymentTransaction", "id", transactionId));

        validateOwnershipOrAdmin(txn, userEmail);

        if (txn.getStatus() == PaymentStatus.REFUNDED) {
            throw new BadRequestException("Transaction has already been refunded", "ALREADY_REFUNDED");
        }

        if (txn.getStatus() != PaymentStatus.SUCCESS) {
            throw new BadRequestException("Only successful payments can be refunded. Current status is " + txn.getStatus(), "INVALID_REFUND_STATE");
        }

        String reason = (request != null && request.getReason() != null && !request.getReason().trim().isEmpty())
                ? request.getReason()
                : "Customer requested booking cancellation";

        RefundGatewayRequest gatewayRequest = new RefundGatewayRequest(
                txn.getTransactionReference(),
                txn.getGatewayTxnId(),
                txn.getAmount(),
                reason
        );

        RefundGatewayResponse gatewayResponse = paymentGateway.processRefund(gatewayRequest);

        if (!gatewayResponse.isSuccess()) {
            throw new IllegalStateException("Payment gateway rejected refund: " + gatewayResponse.getStatusMessage());
        }

        txn.markRefunded(gatewayResponse.getRefundReference());
        paymentTransactionRepository.save(txn);

        // Cancel the booking and release seats
        bookingService.cancelBooking(txn.getBooking().getId(), userEmail);

        log.info("Successfully refunded payment {} with refund ref {}", txn.getTransactionReference(), gatewayResponse.getRefundReference());

        return new RefundResponseDTO(
                txn.getId(),
                txn.getTransactionReference(),
                txn.getBooking().getId(),
                txn.getBooking().getBookingReference(),
                txn.getAmount(),
                gatewayResponse.getRefundReference(),
                PaymentStatus.REFUNDED.name(),
                "Refund completed successfully",
                LocalDateTime.now()
        );
    }

    @Transactional
    public RefundResponseDTO refundBookingPayment(Booking booking, String userEmail) {
        Optional<PaymentTransaction> successfulTxn = paymentTransactionRepository.findByBookingIdAndStatus(booking.getId(), PaymentStatus.SUCCESS);
        if (successfulTxn.isEmpty()) {
            log.info("No successful payment to refund for booking {}", booking.getId());
            return null;
        }

        PaymentTransaction txn = successfulTxn.get();
        RefundGatewayRequest gatewayRequest = new RefundGatewayRequest(
                txn.getTransactionReference(),
                txn.getGatewayTxnId(),
                txn.getAmount(),
                "Booking cancelled"
        );

        RefundGatewayResponse gatewayResponse = paymentGateway.processRefund(gatewayRequest);
        if (gatewayResponse.isSuccess()) {
            txn.markRefunded(gatewayResponse.getRefundReference());
            paymentTransactionRepository.save(txn);
        }

        return new RefundResponseDTO(
                txn.getId(),
                txn.getTransactionReference(),
                booking.getId(),
                booking.getBookingReference(),
                txn.getAmount(),
                gatewayResponse.getRefundReference(),
                PaymentStatus.REFUNDED.name(),
                "Refund processed",
                LocalDateTime.now()
        );
    }

    private PaymentResponseDTO processTransactionInternal(PaymentTransaction txn, Boolean simulateSuccess, String failureReason) {
        LocalDateTime now = LocalDateTime.now();
        Booking booking = txn.getBooking();

        // Feature 1 Requirement: Validate seat availability again before confirming payment
        if (booking != null) {
            if (booking.getExpiresAt() != null && booking.getExpiresAt().isBefore(now)) {
                txn.markFailed("Seat lock expired before payment was processed");
                paymentTransactionRepository.save(txn);
                bookingService.handlePaymentFailure(booking.getId(), "Seat lock expired");
                throw new SeatUnavailableException("Your temporary seat reservation has expired. Please select your seats again.");
            }

            for (BookingSeat bs : booking.getBookingSeats()) {
                ShowSeat seat = bs.getShowSeat();
                if (seat != null) {
                    if (seat.getStatus() != ShowSeatStatus.LOCKED && seat.getStatus() != ShowSeatStatus.HELD) {
                        txn.markFailed("Seat " + bs.getSeatLabel() + " is no longer locked by you");
                        paymentTransactionRepository.save(txn);
                        throw new SeatUnavailableException("Seat " + bs.getSeatLabel() + " is no longer available. Please select another seat.");
                    }
                    if (seat.isLockExpired(now)) {
                        txn.markFailed("Seat lock expired for " + bs.getSeatLabel());
                        paymentTransactionRepository.save(txn);
                        bookingService.handlePaymentFailure(booking.getId(), "Seat lock expired");
                        throw new SeatUnavailableException("Seat lock expired for " + bs.getSeatLabel() + ". Please reselect your seats.");
                    }
                }
            }
        }

        txn.markProcessing();
        paymentTransactionRepository.save(txn);

        PaymentGatewayRequest gatewayRequest = new PaymentGatewayRequest(
                txn.getTransactionReference(),
                txn.getAmount(),
                txn.getCurrency(),
                txn.getPaymentMethod(),
                txn.getIdempotencyKey(),
                simulateSuccess,
                failureReason
        );

        PaymentGatewayResponse response = paymentGateway.processPayment(gatewayRequest);

        if (response.isSuccess()) {
            txn.markSuccess(response.getGatewayTxnId());
            paymentTransactionRepository.save(txn);

            // Confirm the booking, permanently transition seats to BOOKED, and generate tickets
            bookingService.confirmBooking(txn.getBooking().getId());

            // Record coupon usage if coupon was applied
            if (booking.getCouponCode() != null && booking.getDiscountAmount() != null
                    && booking.getDiscountAmount().compareTo(java.math.BigDecimal.ZERO) > 0) {
                Optional<Coupon> couponOpt = couponService.findByCode(booking.getCouponCode());
                couponOpt.ifPresent(c -> couponService.recordCouponUsage(c, txn.getUser(), booking, booking.getDiscountAmount()));
            }

            // Deduct redeemed loyalty points if any
            if (booking.getLoyaltyPointsRedeemed() != null && booking.getLoyaltyPointsRedeemed() > 0) {
                loyaltyService.redeemPointsForBooking(booking, booking.getLoyaltyPointsRedeemed());
            }

            // Award loyalty points for booking
            loyaltyService.awardPointsForBooking(booking);

            log.info("Payment {} SUCCESS. Booking {} confirmed and rewards updated.", txn.getTransactionReference(), txn.getBooking().getId());
        } else {
            txn.markFailed(response.getStatusMessage());
            paymentTransactionRepository.save(txn);

            // Release temporary seat hold on booking failure
            bookingService.handlePaymentFailure(txn.getBooking().getId(), response.getStatusMessage());
            log.warn("Payment {} FAILED ({}). Seats released for booking {}.",
                    txn.getTransactionReference(), response.getStatusMessage(), txn.getBooking().getId());
        }

        return mapToDTOWithTickets(txn);
    }

    private void validateOwnershipOrAdmin(PaymentTransaction txn, String userEmail) {
        User user = userRepository.findByEmail(userEmail)
                .orElseThrow(() -> new ResourceNotFoundException("User", "email", userEmail));

        if (!txn.getUser().getId().equals(user.getId()) && user.getRole() != UserRole.ROLE_ADMIN) {
            throw new AccessDeniedException("You do not have permission to access this payment transaction");
        }
    }

    private PaymentResponseDTO mapToDTOWithTickets(PaymentTransaction txn) {
        PaymentResponseDTO dto = PaymentResponseDTO.fromEntity(txn);
        if (txn.getStatus() == PaymentStatus.SUCCESS && txn.getBooking() != null) {
            List<TicketDTO> tickets = ticketService.getTicketsByBookingId(txn.getBooking().getId(), txn.getUser().getEmail());
            dto.setTickets(tickets);
        }
        return dto;
    }
}
