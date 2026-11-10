package com.cinesmart;

import com.cinesmart.booking.dto.BookingCreateRequest;
import com.cinesmart.booking.dto.BookingDTO;
import com.cinesmart.booking.entity.Booking;
import com.cinesmart.booking.entity.BookingStatus;
import com.cinesmart.booking.repository.BookingRepository;
import com.cinesmart.booking.service.BookingService;
import com.cinesmart.cinema.entity.Cinema;
import com.cinesmart.cinema.entity.ScreenType;
import com.cinesmart.cinema.repository.CinemaRepository;
import com.cinesmart.coupon.dto.CouponValidateResponseDTO;
import com.cinesmart.coupon.entity.Coupon;
import com.cinesmart.coupon.entity.DiscountType;
import com.cinesmart.coupon.repository.CouponRepository;
import com.cinesmart.coupon.repository.CouponUsageRepository;
import com.cinesmart.coupon.service.CouponService;
import com.cinesmart.loyalty.dto.LoyaltyAccountDTO;
import com.cinesmart.loyalty.entity.LoyaltyAccount;
import com.cinesmart.loyalty.repository.LoyaltyAccountRepository;
import com.cinesmart.loyalty.repository.LoyaltyTransactionRepository;
import com.cinesmart.loyalty.service.LoyaltyService;
import com.cinesmart.movie.entity.Movie;
import com.cinesmart.movie.repository.MovieRepository;
import com.cinesmart.payment.dto.PaymentInitiateRequest;
import com.cinesmart.payment.dto.PaymentResponseDTO;
import com.cinesmart.payment.entity.PaymentStatus;
import com.cinesmart.payment.service.PaymentService;
import com.cinesmart.screen.entity.Screen;
import com.cinesmart.screen.repository.ScreenRepository;
import com.cinesmart.seat.entity.Seat;
import com.cinesmart.seat.entity.SeatTier;
import com.cinesmart.seat.repository.SeatRepository;
import com.cinesmart.show.entity.Show;
import com.cinesmart.show.entity.ShowSeat;
import com.cinesmart.show.repository.ShowRepository;
import com.cinesmart.show.repository.ShowSeatRepository;
import com.cinesmart.user.entity.User;
import com.cinesmart.user.entity.UserRole;
import com.cinesmart.user.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@DisplayName("Coupon and Loyalty Rewards End-to-End Integration Tests")
class CouponLoyaltyIntegrationTest {

    @Autowired
    private BookingService bookingService;

    @Autowired
    private PaymentService paymentService;

    @Autowired
    private CouponService couponService;

    @Autowired
    private LoyaltyService loyaltyService;

    @Autowired
    private CouponRepository couponRepository;

    @Autowired
    private CouponUsageRepository couponUsageRepository;

    @Autowired
    private LoyaltyAccountRepository loyaltyAccountRepository;

    @Autowired
    private LoyaltyTransactionRepository loyaltyTransactionRepository;

    @Autowired
    private BookingRepository bookingRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private MovieRepository movieRepository;

    @Autowired
    private CinemaRepository cinemaRepository;

    @Autowired
    private ScreenRepository screenRepository;

    @Autowired
    private SeatRepository seatRepository;

    @Autowired
    private ShowRepository showRepository;

    @Autowired
    private ShowSeatRepository showSeatRepository;

    private User testUser;
    private Show testShow;
    private ShowSeat seat1;
    private ShowSeat seat2;
    private Coupon testCoupon;

    @BeforeEach
    void setUp() {
        String uniqueSuffix = UUID.randomUUID().toString().substring(0, 8);
        testUser = userRepository.save(new User(
                "rewarduser_" + uniqueSuffix + "@cinesmart.test",
                "password123",
                "Rewards Tester",
                "+91987654" + uniqueSuffix.substring(0, 4),
                UserRole.ROLE_CUSTOMER
        ));

        // Create user loyalty account with 100 initial points
        LoyaltyAccount loyaltyAccount = new LoyaltyAccount(testUser);
        loyaltyAccount.addPoints(100);
        loyaltyAccountRepository.save(loyaltyAccount);

        Cinema cinema = cinemaRepository.save(new Cinema("Rewards Multiplex " + uniqueSuffix, "Downtown", "100 Cine Blvd", "123456"));
        Screen screen = screenRepository.save(new Screen(cinema, 1, ScreenType.STANDARD, 10));

        Seat s1 = seatRepository.save(new Seat(screen, "A", 1, SeatTier.STANDARD, 1, 1));
        Seat s2 = seatRepository.save(new Seat(screen, "A", 2, SeatTier.STANDARD, 2, 1));

        Movie movie = movieRepository.save(new Movie(
                "Rewards Movie " + uniqueSuffix,
                "Description",
                120,
                "English",
                "Sci-Fi",
                "UA",
                "https://example.com/poster.jpg"
        ));

        testShow = showRepository.save(new Show(
                movie,
                screen,
                LocalDateTime.now().plusDays(2),
                LocalDateTime.now().plusDays(2).plusHours(2),
                BigDecimal.valueOf(200.00)
        ));

        seat1 = showSeatRepository.save(new ShowSeat(testShow, s1, BigDecimal.valueOf(200.00)));
        seat2 = showSeatRepository.save(new ShowSeat(testShow, s2, BigDecimal.valueOf(200.00)));

        // Create promotional coupon
        testCoupon = new Coupon(
                "DISCOUNT" + uniqueSuffix.toUpperCase(),
                "Flat ₹50 promotional discount",
                DiscountType.FIXED_AMOUNT,
                BigDecimal.valueOf(50.00),
                BigDecimal.valueOf(100.00),
                BigDecimal.valueOf(50.00),
                LocalDateTime.now().minusDays(1),
                LocalDateTime.now().plusDays(10),
                100,
                1
        );
        couponRepository.save(testCoupon);
    }

    @Test
    @DisplayName("Complete checkout workflow with Coupon Discount & Loyalty Points Redemption")
    void testBookingPaymentWithCouponAndLoyaltyRedemption() {
        // Step 1: Create booking for 2 seats (₹200 + ₹200 = ₹400 subtotal)
        BookingCreateRequest createReq = new BookingCreateRequest();
        createReq.setShowId(testShow.getId());
        createReq.setShowSeatIds(List.of(seat1.getId(), seat2.getId()));

        BookingDTO bookingDTO = bookingService.createBooking(testUser.getEmail(), createReq);
        assertNotNull(bookingDTO);
        assertEquals(BookingStatus.PENDING_PAYMENT, bookingDTO.getStatus());
        BigDecimal originalAmount = bookingDTO.getTotalAmount();
        assertTrue(originalAmount.compareTo(BigDecimal.valueOf(400.00)) >= 0);

        // Step 2: Validate coupon
        CouponValidateResponseDTO couponResp = couponService.validateCoupon(testCoupon.getCode(), originalAmount, testUser.getEmail());
        assertTrue(couponResp.isValid());
        assertEquals(0, couponResp.getDiscountAmount().compareTo(BigDecimal.valueOf(50.00)));

        // Step 3: Initiate and auto-process payment with coupon + 40 loyalty points
        PaymentInitiateRequest payReq = new PaymentInitiateRequest();
        payReq.setBookingId(bookingDTO.getId());
        payReq.setPaymentMethod("UPI");
        payReq.setCouponCode(testCoupon.getCode());
        payReq.setLoyaltyPointsToRedeem(40); // 40 points = ₹40 discount
        payReq.setSimulateSuccess(true);

        PaymentResponseDTO paymentResp = paymentService.initiatePayment(testUser.getEmail(), payReq, true);

        // Step 4: Verify payment success & amount calculations
        assertNotNull(paymentResp);
        assertEquals(PaymentStatus.SUCCESS, paymentResp.getStatus());

        Booking confirmedBooking = bookingRepository.findById(bookingDTO.getId()).orElseThrow();
        assertEquals(BookingStatus.CONFIRMED, confirmedBooking.getStatus());
        assertEquals(testCoupon.getCode(), confirmedBooking.getCouponCode());
        assertEquals(0, confirmedBooking.getDiscountAmount().compareTo(BigDecimal.valueOf(50.00)));
        assertEquals(40, confirmedBooking.getLoyaltyPointsRedeemed());
        assertEquals(0, confirmedBooking.getLoyaltyDiscountAmount().compareTo(BigDecimal.valueOf(40.00)));

        // Final payable = originalAmount - ₹50 - ₹40
        BigDecimal expectedPayable = originalAmount.subtract(BigDecimal.valueOf(90.00));
        assertEquals(0, confirmedBooking.getTotalAmount().compareTo(expectedPayable));
        assertEquals(0, paymentResp.getAmount().compareTo(expectedPayable));

        // Step 5: Verify Coupon usage recorded
        assertTrue(couponUsageRepository.findByBookingId(confirmedBooking.getId()).isPresent());
        Coupon updatedCoupon = couponRepository.findById(testCoupon.getId()).orElseThrow();
        assertEquals(1, updatedCoupon.getTimesUsed());

        // Step 6: Verify Loyalty points balance:
        // Started with 100. Redeemed 40 -> 60.
        // Earned points for paying expectedPayable (at least ₹310 -> at least 31 points)
        LoyaltyAccountDTO userLoyalty = loyaltyService.getAccountForUser(testUser.getEmail());
        assertTrue(userLoyalty.getPointsBalance() >= 91); // 60 + ~31
        assertEquals(40, userLoyalty.getLifetimePointsRedeemed());

        // Step 7: Booking Cancellation Reversal:
        // Cancelling booking should restore 40 redeemed points, claw back earned points, and release coupon usage
        bookingService.cancelBooking(confirmedBooking.getId(), testUser.getEmail());

        // Verify coupon usage released
        assertFalse(couponUsageRepository.findByBookingId(confirmedBooking.getId()).isPresent());
        Coupon revertedCoupon = couponRepository.findById(testCoupon.getId()).orElseThrow();
        assertEquals(0, revertedCoupon.getTimesUsed());

        // Verify redeemed points restored to balance (100 initial - 40 redeemed + 40 restored = 100)
        LoyaltyAccountDTO postCancelLoyalty = loyaltyService.getAccountForUser(testUser.getEmail());
        assertEquals(100, postCancelLoyalty.getPointsBalance());
    }
}
