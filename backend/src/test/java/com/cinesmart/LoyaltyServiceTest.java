package com.cinesmart;

import com.cinesmart.booking.entity.Booking;
import com.cinesmart.loyalty.dto.LoyaltyAccountDTO;
import com.cinesmart.loyalty.dto.LoyaltyRedeemResponseDTO;
import com.cinesmart.loyalty.entity.LoyaltyAccount;
import com.cinesmart.loyalty.entity.LoyaltyTransaction;
import com.cinesmart.loyalty.entity.LoyaltyTransactionType;
import com.cinesmart.loyalty.repository.LoyaltyAccountRepository;
import com.cinesmart.loyalty.repository.LoyaltyTransactionRepository;
import com.cinesmart.loyalty.service.LoyaltyService;
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
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("LoyaltyService Comprehensive Unit Tests")
class LoyaltyServiceTest {

    @Mock
    private LoyaltyAccountRepository loyaltyAccountRepository;

    @Mock
    private LoyaltyTransactionRepository loyaltyTransactionRepository;

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private LoyaltyService loyaltyService;

    private User testUser;
    private LoyaltyAccount testAccount;
    private Booking testBooking;

    @BeforeEach
    void setUp() {
        testUser = new User("customer@test.com", "hash", "Jane Loyalty", "+919876543210", UserRole.ROLE_CUSTOMER);
        testUser.setId(5L);

        testAccount = new LoyaltyAccount(testUser);
        testAccount.setId(1L);
        testAccount.setPointsBalance(150);
        testAccount.setLifetimePointsEarned(200);
        testAccount.setLifetimePointsRedeemed(50);

        testBooking = new Booking("CS-2026-LOYALTY", testUser, null, BigDecimal.valueOf(500.00));
        testBooking.setId(20L);
    }

    @Test
    @DisplayName("Get balance retrieves user account points accurately")
    void testGetBalanceSuccess() {
        when(userRepository.findByEmail("customer@test.com")).thenReturn(Optional.of(testUser));
        when(loyaltyAccountRepository.findByUserId(5L)).thenReturn(Optional.of(testAccount));

        LoyaltyAccountDTO balance = loyaltyService.getAccountForUser("customer@test.com");

        assertNotNull(balance);
        assertEquals(150, balance.getPointsBalance());
        assertEquals(200, balance.getLifetimePointsEarned());
        assertEquals(50, balance.getLifetimePointsRedeemed());
        assertEquals(0, balance.getRupeeValue().compareTo(BigDecimal.valueOf(150)));
    }

    @Test
    @DisplayName("Calculate redemption successfully calculates discount and remaining points")
    void testCalculateRedemptionSuccess() {
        when(userRepository.findByEmail("customer@test.com")).thenReturn(Optional.of(testUser));
        when(loyaltyAccountRepository.findByUserId(5L)).thenReturn(Optional.of(testAccount));

        // User has 150 points, redeems 100 on ₹500 booking -> ₹100 discount, 50 remaining, ₹400 payable
        LoyaltyRedeemResponseDTO response = loyaltyService.calculateRedemption("customer@test.com", 100, BigDecimal.valueOf(500.00));

        assertTrue(response.isValid());
        assertEquals(100, response.getPointsRedeemed());
        assertEquals(0, response.getDiscountAmount().compareTo(BigDecimal.valueOf(100)));
        assertEquals(50, response.getRemainingPointsBalance());
        assertEquals(0, response.getFinalAmount().compareTo(BigDecimal.valueOf(400)));
    }

    @Test
    @DisplayName("Calculate redemption rejects when requesting more points than balance")
    void testCalculateRedemptionInsufficientPoints() {
        when(userRepository.findByEmail("customer@test.com")).thenReturn(Optional.of(testUser));
        when(loyaltyAccountRepository.findByUserId(5L)).thenReturn(Optional.of(testAccount));

        // Requesting 200 points when only 150 are available
        LoyaltyRedeemResponseDTO response = loyaltyService.calculateRedemption("customer@test.com", 200, BigDecimal.valueOf(500.00));

        assertFalse(response.isValid());
        assertTrue(response.getMessage().contains("Insufficient points"));
    }

    @Test
    @DisplayName("Calculate redemption caps redemption at order total so discount doesn't exceed total")
    void testCalculateRedemptionCappedAtOrderTotal() {
        when(userRepository.findByEmail("customer@test.com")).thenReturn(Optional.of(testUser));
        when(loyaltyAccountRepository.findByUserId(5L)).thenReturn(Optional.of(testAccount));

        // Order is only ₹80, but user asks to redeem 120 points
        LoyaltyRedeemResponseDTO response = loyaltyService.calculateRedemption("customer@test.com", 120, BigDecimal.valueOf(80.00));

        assertTrue(response.isValid());
        assertEquals(80, response.getPointsRedeemed()); // capped at 80
        assertEquals(0, response.getDiscountAmount().compareTo(BigDecimal.valueOf(80)));
        assertEquals(70, response.getRemainingPointsBalance()); // 150 - 80 = 70
        assertEquals(0, response.getFinalAmount().compareTo(BigDecimal.ZERO));
    }

    @Test
    @DisplayName("Award points for booking awards 1 point per ₹10 spent idempotently")
    void testAwardPointsForBooking() {
        when(loyaltyTransactionRepository.existsByBookingIdAndTransactionType(20L, LoyaltyTransactionType.EARNED)).thenReturn(false);
        when(loyaltyAccountRepository.findByUserId(5L)).thenReturn(Optional.of(testAccount));

        // Booking is ₹500 -> 50 points
        loyaltyService.awardPointsForBooking(testBooking);

        assertEquals(200, testAccount.getPointsBalance()); // 150 + 50
        assertEquals(250, testAccount.getLifetimePointsEarned()); // 200 + 50
        verify(loyaltyTransactionRepository, times(1)).save(any(LoyaltyTransaction.class));
    }

    @Test
    @DisplayName("Award points skips when points were already awarded (Idempotency)")
    void testAwardPointsIdempotency() {
        when(loyaltyTransactionRepository.existsByBookingIdAndTransactionType(20L, LoyaltyTransactionType.EARNED)).thenReturn(true);

        loyaltyService.awardPointsForBooking(testBooking);

        verify(loyaltyAccountRepository, never()).save(any());
        verify(loyaltyTransactionRepository, never()).save(any());
    }

    @Test
    @DisplayName("Redeem points for booking deducts points from account")
    void testRedeemPointsForBooking() {
        when(loyaltyAccountRepository.findByUserId(5L)).thenReturn(Optional.of(testAccount));

        loyaltyService.redeemPointsForBooking(testBooking, 50);

        assertEquals(100, testAccount.getPointsBalance()); // 150 - 50
        assertEquals(100, testAccount.getLifetimePointsRedeemed()); // 50 + 50
        verify(loyaltyAccountRepository, times(1)).save(testAccount);
        verify(loyaltyTransactionRepository, times(1)).save(any(LoyaltyTransaction.class));
    }

    @Test
    @DisplayName("Reverse points on booking cancellation claws back earned points and restores redeemed points")
    void testReversePointsOnCancellation() {
        when(loyaltyAccountRepository.findByUserId(5L)).thenReturn(Optional.of(testAccount));

        // Booking had earned 50 points
        LoyaltyTransaction earnedTxn = new LoyaltyTransaction(testUser, testBooking, 50, LoyaltyTransactionType.EARNED, "Earned 50 points");
        when(loyaltyTransactionRepository.findByBookingIdAndTransactionType(20L, LoyaltyTransactionType.EARNED))
                .thenReturn(Optional.of(earnedTxn));

        // And booking had redeemed 30 points
        testBooking.setLoyaltyPointsRedeemed(30);

        loyaltyService.reversePointsForBooking(testBooking);

        // Initial balance was 150:
        // Claws back 50 earned -> 100
        // Restores 30 redeemed -> 130
        assertEquals(130, testAccount.getPointsBalance());
        verify(loyaltyTransactionRepository, times(2)).save(any(LoyaltyTransaction.class));
        verify(loyaltyAccountRepository, times(1)).save(testAccount);
    }
}
