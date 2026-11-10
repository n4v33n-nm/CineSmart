package com.cinesmart;

import com.cinesmart.common.exception.BadRequestException;
import com.cinesmart.coupon.dto.CouponCreateRequest;
import com.cinesmart.coupon.dto.CouponDTO;
import com.cinesmart.coupon.dto.CouponValidateResponseDTO;
import com.cinesmart.coupon.entity.Coupon;
import com.cinesmart.coupon.entity.DiscountType;
import com.cinesmart.coupon.repository.CouponRepository;
import com.cinesmart.coupon.repository.CouponUsageRepository;
import com.cinesmart.coupon.service.CouponService;
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
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("CouponService Comprehensive Unit Tests")
class CouponServiceTest {

    @Mock
    private CouponRepository couponRepository;

    @Mock
    private CouponUsageRepository couponUsageRepository;

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private CouponService couponService;

    private User customer;
    private Coupon percentCoupon;
    private Coupon fixedCoupon;

    @BeforeEach
    void setUp() {
        customer = new User("customer@test.com", "hash", "Test Customer", "+919876543210", UserRole.ROLE_CUSTOMER);
        customer.setId(10L);

        percentCoupon = new Coupon(
                "CINEMA20",
                "20% off up to ₹100",
                DiscountType.PERCENTAGE,
                BigDecimal.valueOf(20.00),
                BigDecimal.valueOf(300.00),
                BigDecimal.valueOf(100.00),
                LocalDateTime.now().minusDays(1),
                LocalDateTime.now().plusDays(30),
                100,
                2
        );
        percentCoupon.setId(1L);

        fixedCoupon = new Coupon(
                "WELCOME50",
                "Flat ₹50 off",
                DiscountType.FIXED_AMOUNT,
                BigDecimal.valueOf(50.00),
                BigDecimal.valueOf(200.00),
                BigDecimal.valueOf(50.00),
                LocalDateTime.now().minusDays(1),
                LocalDateTime.now().plusDays(30),
                500,
                1
        );
        fixedCoupon.setId(2L);
    }

    @Test
    @DisplayName("Validate percentage coupon successfully applies discount and respects max cap")
    void testValidatePercentageCouponSuccess() {
        when(couponRepository.findByCodeIgnoreCase("CINEMA20")).thenReturn(Optional.of(percentCoupon));
        when(userRepository.findByEmail("customer@test.com")).thenReturn(Optional.of(customer));
        when(couponUsageRepository.countByCouponIdAndUserId(1L, 10L)).thenReturn(0);

        // 20% of 400 is 80 (less than max discount 100)
        CouponValidateResponseDTO response = couponService.validateCoupon("CINEMA20", BigDecimal.valueOf(400.00), "customer@test.com");

        assertTrue(response.isValid());
        assertEquals("CINEMA20", response.getCode());
        assertEquals(new BigDecimal("80.00"), response.getDiscountAmount());
        assertEquals(new BigDecimal("320.00"), response.getFinalAmount());
    }

    @Test
    @DisplayName("Validate percentage coupon respects maximum discount cap")
    void testValidatePercentageCouponCapsAtMax() {
        when(couponRepository.findByCodeIgnoreCase("CINEMA20")).thenReturn(Optional.of(percentCoupon));
        when(userRepository.findByEmail("customer@test.com")).thenReturn(Optional.of(customer));
        when(couponUsageRepository.countByCouponIdAndUserId(1L, 10L)).thenReturn(0);

        // 20% of 1000 is 200, but cap is 100
        CouponValidateResponseDTO response = couponService.validateCoupon("CINEMA20", BigDecimal.valueOf(1000.00), "customer@test.com");

        assertTrue(response.isValid());
        assertEquals(new BigDecimal("100.00"), response.getDiscountAmount());
        assertEquals(new BigDecimal("900.00"), response.getFinalAmount());
    }

    @Test
    @DisplayName("Validate fixed amount coupon successfully applies discount")
    void testValidateFixedCouponSuccess() {
        when(couponRepository.findByCodeIgnoreCase("WELCOME50")).thenReturn(Optional.of(fixedCoupon));
        when(userRepository.findByEmail("customer@test.com")).thenReturn(Optional.of(customer));
        when(couponUsageRepository.countByCouponIdAndUserId(2L, 10L)).thenReturn(0);

        CouponValidateResponseDTO response = couponService.validateCoupon("WELCOME50", BigDecimal.valueOf(250.00), "customer@test.com");

        assertTrue(response.isValid());
        assertEquals(new BigDecimal("50.00"), response.getDiscountAmount());
        assertEquals(new BigDecimal("200.00"), response.getFinalAmount());
    }

    @Test
    @DisplayName("Validate coupon rejects when booking amount is below minBookingAmount")
    void testValidateRejectsBelowMinAmount() {
        when(couponRepository.findByCodeIgnoreCase("WELCOME50")).thenReturn(Optional.of(fixedCoupon));

        CouponValidateResponseDTO response = couponService.validateCoupon("WELCOME50", BigDecimal.valueOf(150.00), "customer@test.com");

        assertFalse(response.isValid());
        assertTrue(response.getMessage().contains("Minimum booking amount"));
    }

    @Test
    @DisplayName("Validate coupon rejects when expired")
    void testValidateRejectsExpiredCoupon() {
        percentCoupon.setValidUntil(LocalDateTime.now().minusDays(1));
        when(couponRepository.findByCodeIgnoreCase("CINEMA20")).thenReturn(Optional.of(percentCoupon));

        CouponValidateResponseDTO response = couponService.validateCoupon("CINEMA20", BigDecimal.valueOf(500.00), "customer@test.com");

        assertFalse(response.isValid());
        assertTrue(response.getMessage().contains("expired"));
    }

    @Test
    @DisplayName("Validate coupon rejects when inactive")
    void testValidateRejectsInactiveCoupon() {
        percentCoupon.setIsActive(false);
        when(couponRepository.findByCodeIgnoreCase("CINEMA20")).thenReturn(Optional.of(percentCoupon));

        CouponValidateResponseDTO response = couponService.validateCoupon("CINEMA20", BigDecimal.valueOf(500.00), "customer@test.com");

        assertFalse(response.isValid());
        assertTrue(response.getMessage().contains("inactive"));
    }

    @Test
    @DisplayName("Validate coupon rejects when per-user usage limit reached")
    void testValidateRejectsPerUserLimitReached() {
        when(couponRepository.findByCodeIgnoreCase("WELCOME50")).thenReturn(Optional.of(fixedCoupon));
        when(userRepository.findByEmail("customer@test.com")).thenReturn(Optional.of(customer));
        when(couponUsageRepository.countByCouponIdAndUserId(2L, 10L)).thenReturn(1); // Per-user limit is 1

        CouponValidateResponseDTO response = couponService.validateCoupon("WELCOME50", BigDecimal.valueOf(300.00), "customer@test.com");

        assertFalse(response.isValid());
        assertTrue(response.getMessage().contains("maximum usage limit"));
    }

    @Test
    @DisplayName("Admin creates a coupon successfully")
    void testAdminCreateCouponSuccess() {
        CouponCreateRequest request = new CouponCreateRequest();
        request.setCode("FESTIVE30");
        request.setDescription("Festive 30% discount");
        request.setDiscountType(DiscountType.PERCENTAGE);
        request.setDiscountValue(BigDecimal.valueOf(30.0));
        request.setMinBookingAmount(BigDecimal.valueOf(250.0));
        request.setMaxDiscountAmount(BigDecimal.valueOf(150.0));
        request.setValidFrom(LocalDateTime.now());
        request.setValidUntil(LocalDateTime.now().plusMonths(1));

        when(couponRepository.existsByCodeIgnoreCase("FESTIVE30")).thenReturn(false);
        when(couponRepository.save(any(Coupon.class))).thenAnswer(invocation -> {
            Coupon c = invocation.getArgument(0);
            c.setId(99L);
            return c;
        });

        CouponDTO result = couponService.createCoupon(request);

        assertNotNull(result);
        assertEquals("FESTIVE30", result.getCode());
        assertEquals(DiscountType.PERCENTAGE, result.getDiscountType());
    }

    @Test
    @DisplayName("Admin create coupon throws when duplicate code exists")
    void testAdminCreateDuplicateCouponThrows() {
        CouponCreateRequest request = new CouponCreateRequest();
        request.setCode("WELCOME50");
        request.setDiscountType(DiscountType.FIXED_AMOUNT);
        request.setDiscountValue(BigDecimal.valueOf(50.0));
        request.setValidUntil(LocalDateTime.now().plusDays(30));

        when(couponRepository.existsByCodeIgnoreCase("WELCOME50")).thenReturn(true);

        assertThrows(BadRequestException.class, () -> couponService.createCoupon(request));
    }

    @Test
    @DisplayName("Toggle coupon status switches active state")
    void testToggleCouponStatus() {
        when(couponRepository.findById(1L)).thenReturn(Optional.of(percentCoupon));
        when(couponRepository.save(any(Coupon.class))).thenAnswer(invocation -> invocation.getArgument(0));

        CouponDTO result = couponService.toggleCouponStatus(1L);

        assertFalse(result.getIsActive());
    }
}
