package com.cinesmart.loyalty.service;

import com.cinesmart.booking.entity.Booking;
import com.cinesmart.common.exception.ResourceNotFoundException;
import com.cinesmart.loyalty.dto.LoyaltyAccountDTO;
import com.cinesmart.loyalty.dto.LoyaltyRedeemResponseDTO;
import com.cinesmart.loyalty.dto.LoyaltyTransactionDTO;
import com.cinesmart.loyalty.entity.LoyaltyAccount;
import com.cinesmart.loyalty.entity.LoyaltyTransaction;
import com.cinesmart.loyalty.entity.LoyaltyTransactionType;
import com.cinesmart.loyalty.repository.LoyaltyAccountRepository;
import com.cinesmart.loyalty.repository.LoyaltyTransactionRepository;
import com.cinesmart.user.entity.User;
import com.cinesmart.user.repository.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class LoyaltyService {

    private static final Logger log = LoggerFactory.getLogger(LoyaltyService.class);

    // Default conversion: 1 Point = ₹1.00 Discount
    @Value("${cinesmart.loyalty.point-value-in-rupees:1.00}")
    private BigDecimal pointValueInRupees = BigDecimal.valueOf(1.00);

    // Default earn rate: 1 Point for every ₹10.00 spent
    @Value("${cinesmart.loyalty.rupees-per-point:10.00}")
    private BigDecimal rupeesPerPoint = BigDecimal.valueOf(10.00);

    private final LoyaltyAccountRepository loyaltyAccountRepository;
    private final LoyaltyTransactionRepository loyaltyTransactionRepository;
    private final UserRepository userRepository;

    public LoyaltyService(LoyaltyAccountRepository loyaltyAccountRepository,
                          LoyaltyTransactionRepository loyaltyTransactionRepository,
                          UserRepository userRepository) {
        this.loyaltyAccountRepository = loyaltyAccountRepository;
        this.loyaltyTransactionRepository = loyaltyTransactionRepository;
        this.userRepository = userRepository;
    }

    @Transactional
    public LoyaltyAccount getOrCreateAccount(User user) {
        return loyaltyAccountRepository.findByUserId(user.getId())
                .orElseGet(() -> {
                    LoyaltyAccount newAcc = new LoyaltyAccount(user);
                    return loyaltyAccountRepository.save(newAcc);
                });
    }

    @Transactional
    public LoyaltyAccountDTO getAccountForUser(String userEmail) {
        User user = userRepository.findByEmail(userEmail)
                .orElseThrow(() -> new ResourceNotFoundException("User", "email", userEmail));

        LoyaltyAccount account = getOrCreateAccount(user);
        return LoyaltyAccountDTO.fromEntity(account, pointValueInRupees);
    }

    @Transactional(readOnly = true)
    public List<LoyaltyTransactionDTO> getTransactionsForUser(String userEmail) {
        User user = userRepository.findByEmail(userEmail)
                .orElseThrow(() -> new ResourceNotFoundException("User", "email", userEmail));

        return loyaltyTransactionRepository.findByUserIdOrderByCreatedAtDesc(user.getId()).stream()
                .map(LoyaltyTransactionDTO::fromEntity)
                .collect(Collectors.toList());
    }

    @Transactional
    public LoyaltyRedeemResponseDTO calculateRedemption(String userEmail, int pointsToRedeem, BigDecimal currentTotal) {
        User user = userRepository.findByEmail(userEmail)
                .orElseThrow(() -> new ResourceNotFoundException("User", "email", userEmail));

        LoyaltyAccount account = getOrCreateAccount(user);
        int availableBalance = account.getPointsBalance();

        if (pointsToRedeem <= 0) {
            return LoyaltyRedeemResponseDTO.invalid("Points to redeem must be greater than zero.", availableBalance);
        }

        if (pointsToRedeem > availableBalance) {
            return LoyaltyRedeemResponseDTO.invalid(
                    String.format("Insufficient points. You requested %d points but have only %d points available.",
                            pointsToRedeem, availableBalance),
                    availableBalance
            );
        }

        if (currentTotal == null || currentTotal.compareTo(BigDecimal.ZERO) <= 0) {
            return LoyaltyRedeemResponseDTO.invalid("Order amount must be greater than zero.", availableBalance);
        }

        BigDecimal discount = BigDecimal.valueOf(pointsToRedeem).multiply(pointValueInRupees);
        if (discount.compareTo(currentTotal) > 0) {
            // Cap redemption to not exceed current total
            int maxUsablePoints = currentTotal.divide(pointValueInRupees, 0, RoundingMode.FLOOR).intValue();
            pointsToRedeem = maxUsablePoints;
            discount = BigDecimal.valueOf(pointsToRedeem).multiply(pointValueInRupees);
        }

        BigDecimal finalTotal = currentTotal.subtract(discount);
        if (finalTotal.compareTo(BigDecimal.ZERO) < 0) {
            finalTotal = BigDecimal.ZERO;
        }

        int remaining = availableBalance - pointsToRedeem;
        String msg = String.format("Redeemed %d loyalty points for ₹%.2f instant discount!", pointsToRedeem, discount);
        return LoyaltyRedeemResponseDTO.valid(pointsToRedeem, discount, remaining, finalTotal, msg);
    }

    @Transactional
    public void awardPointsForBooking(Booking booking) {
        if (booking == null || booking.getUser() == null) return;

        // Idempotency check: Never award points more than once per booking
        if (loyaltyTransactionRepository.existsByBookingIdAndTransactionType(booking.getId(), LoyaltyTransactionType.EARNED)) {
            log.info("Loyalty points already awarded for booking id: {}", booking.getId());
            return;
        }

        BigDecimal paidAmount = booking.getTotalAmount() != null ? booking.getTotalAmount() : BigDecimal.ZERO;
        if (paidAmount.compareTo(BigDecimal.ZERO) <= 0) return;

        int earnedPoints = paidAmount.divide(rupeesPerPoint, 0, RoundingMode.FLOOR).intValue();
        if (earnedPoints <= 0) return;

        LoyaltyAccount account = getOrCreateAccount(booking.getUser());
        account.addPoints(earnedPoints);
        loyaltyAccountRepository.save(account);

        LoyaltyTransaction txn = new LoyaltyTransaction(
                booking.getUser(),
                booking,
                earnedPoints,
                LoyaltyTransactionType.EARNED,
                String.format("Earned %d points for booking %s", earnedPoints, booking.getBookingReference())
        );
        loyaltyTransactionRepository.save(txn);

        log.info("Awarded {} loyalty points to user {} for booking {} (New Balance: {})",
                earnedPoints, booking.getUser().getEmail(), booking.getBookingReference(), account.getPointsBalance());
    }

    @Transactional
    public void redeemPointsForBooking(Booking booking, int pointsToRedeem) {
        if (booking == null || booking.getUser() == null || pointsToRedeem <= 0) return;

        LoyaltyAccount account = getOrCreateAccount(booking.getUser());
        account.deductPoints(pointsToRedeem);
        loyaltyAccountRepository.save(account);

        LoyaltyTransaction txn = new LoyaltyTransaction(
                booking.getUser(),
                booking,
                -pointsToRedeem,
                LoyaltyTransactionType.REDEEMED,
                String.format("Redeemed %d points on booking %s", pointsToRedeem, booking.getBookingReference())
        );
        loyaltyTransactionRepository.save(txn);

        log.info("Deducted {} redeemed loyalty points from user {} for booking {} (New Balance: {})",
                pointsToRedeem, booking.getUser().getEmail(), booking.getBookingReference(), account.getPointsBalance());
    }

    @Transactional
    public void reversePointsForBooking(Booking booking) {
        if (booking == null || booking.getUser() == null) return;

        User user = booking.getUser();
        LoyaltyAccount account = getOrCreateAccount(user);

        // 1. If points were EARNED from this booking: claw them back
        Optional<LoyaltyTransaction> earnedTxn = loyaltyTransactionRepository
                .findByBookingIdAndTransactionType(booking.getId(), LoyaltyTransactionType.EARNED);
        if (earnedTxn.isPresent()) {
            int earnedPoints = earnedTxn.get().getPoints();
            if (earnedPoints > 0) {
                account.reverseClawback(earnedPoints);
                LoyaltyTransaction reverseEarned = new LoyaltyTransaction(
                        user,
                        booking,
                        -earnedPoints,
                        LoyaltyTransactionType.REVERSED,
                        String.format("Reversal: Clawback of %d earned points due to booking %s cancellation",
                                earnedPoints, booking.getBookingReference())
                );
                loyaltyTransactionRepository.save(reverseEarned);
                log.info("Clawed back {} earned points for cancelled booking {}", earnedPoints, booking.getBookingReference());
            }
        }

        // 2. If points were REDEEMED for this booking: restore them back to user!
        if (booking.getLoyaltyPointsRedeemed() != null && booking.getLoyaltyPointsRedeemed() > 0) {
            int redeemedPoints = booking.getLoyaltyPointsRedeemed();
            account.restoreRedeemed(redeemedPoints);
            LoyaltyTransaction restoreRedeemed = new LoyaltyTransaction(
                    user,
                    booking,
                    redeemedPoints,
                    LoyaltyTransactionType.REVERSED,
                    String.format("Reversal: Restored %d redeemed points due to booking %s cancellation",
                            redeemedPoints, booking.getBookingReference())
            );
            loyaltyTransactionRepository.save(restoreRedeemed);
            log.info("Restored {} redeemed points to user {} for cancelled booking {}",
                    redeemedPoints, user.getEmail(), booking.getBookingReference());
        }

        loyaltyAccountRepository.save(account);
    }
}
