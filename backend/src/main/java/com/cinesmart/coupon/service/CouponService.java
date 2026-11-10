package com.cinesmart.coupon.service;

import com.cinesmart.booking.entity.Booking;
import com.cinesmart.common.exception.BadRequestException;
import com.cinesmart.common.exception.ResourceNotFoundException;
import com.cinesmart.coupon.dto.CouponCreateRequest;
import com.cinesmart.coupon.dto.CouponDTO;
import com.cinesmart.coupon.dto.CouponValidateResponseDTO;
import com.cinesmart.coupon.entity.Coupon;
import com.cinesmart.coupon.entity.CouponUsage;
import com.cinesmart.coupon.repository.CouponRepository;
import com.cinesmart.coupon.repository.CouponUsageRepository;
import com.cinesmart.user.entity.User;
import com.cinesmart.user.repository.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class CouponService {

    private static final Logger log = LoggerFactory.getLogger(CouponService.class);

    private final CouponRepository couponRepository;
    private final CouponUsageRepository couponUsageRepository;
    private final UserRepository userRepository;

    public CouponService(CouponRepository couponRepository,
                         CouponUsageRepository couponUsageRepository,
                         UserRepository userRepository) {
        this.couponRepository = couponRepository;
        this.couponUsageRepository = couponUsageRepository;
        this.userRepository = userRepository;
    }

    @Transactional(readOnly = true)
    public List<CouponDTO> getActiveCoupons() {
        return couponRepository.findActiveCoupons(LocalDateTime.now()).stream()
                .map(CouponDTO::fromEntity)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<CouponDTO> getAllCoupons() {
        return couponRepository.findAllByOrderByCreatedAtDesc().stream()
                .map(CouponDTO::fromEntity)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public CouponDTO getCouponById(Long id) {
        Coupon coupon = couponRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Coupon", "id", id));
        return CouponDTO.fromEntity(coupon);
    }

    @Transactional
    public CouponDTO createCoupon(CouponCreateRequest request) {
        String code = request.getCode().trim().toUpperCase();
        if (couponRepository.existsByCodeIgnoreCase(code)) {
            throw new BadRequestException("Coupon code '" + code + "' already exists", "COUPON_CODE_EXISTS");
        }

        if (request.getValidUntil().isBefore(LocalDateTime.now())) {
            throw new BadRequestException("Coupon expiration date must be in the future", "INVALID_EXPIRATION_DATE");
        }

        LocalDateTime validFrom = request.getValidFrom() != null ? request.getValidFrom() : LocalDateTime.now();
        if (request.getValidUntil().isBefore(validFrom)) {
            throw new BadRequestException("Expiration date cannot be before start date", "INVALID_DATE_RANGE");
        }

        Coupon coupon = new Coupon(
                code,
                request.getDescription(),
                request.getDiscountType(),
                request.getDiscountValue(),
                request.getMinBookingAmount(),
                request.getMaxDiscountAmount(),
                validFrom,
                request.getValidUntil(),
                request.getUsageLimit(),
                request.getPerUserLimit()
        );
        coupon.setIsActive(request.getIsActive() != null ? request.getIsActive() : true);

        Coupon saved = couponRepository.save(coupon);
        log.info("Admin created coupon: {} ({})", saved.getCode(), saved.getDiscountType());
        return CouponDTO.fromEntity(saved);
    }

    @Transactional
    public CouponDTO updateCoupon(Long id, CouponCreateRequest request) {
        Coupon coupon = couponRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Coupon", "id", id));

        String newCode = request.getCode().trim().toUpperCase();
        if (!coupon.getCode().equalsIgnoreCase(newCode) && couponRepository.existsByCodeIgnoreCase(newCode)) {
            throw new BadRequestException("Coupon code '" + newCode + "' already exists", "COUPON_CODE_EXISTS");
        }

        coupon.setCode(newCode);
        coupon.setDescription(request.getDescription());
        coupon.setDiscountType(request.getDiscountType());
        coupon.setDiscountValue(request.getDiscountValue());
        coupon.setMinBookingAmount(request.getMinBookingAmount() != null ? request.getMinBookingAmount() : BigDecimal.ZERO);
        coupon.setMaxDiscountAmount(request.getMaxDiscountAmount());
        if (request.getValidFrom() != null) coupon.setValidFrom(request.getValidFrom());
        coupon.setValidUntil(request.getValidUntil());
        if (request.getUsageLimit() != null) coupon.setUsageLimit(request.getUsageLimit());
        if (request.getPerUserLimit() != null) coupon.setPerUserLimit(request.getPerUserLimit());
        if (request.getIsActive() != null) coupon.setIsActive(request.getIsActive());
        coupon.setUpdatedAt(LocalDateTime.now());

        Coupon updated = couponRepository.save(coupon);
        log.info("Admin updated coupon: {}", updated.getCode());
        return CouponDTO.fromEntity(updated);
    }

    @Transactional
    public CouponDTO toggleCouponStatus(Long id) {
        Coupon coupon = couponRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Coupon", "id", id));

        coupon.setIsActive(!Boolean.TRUE.equals(coupon.getIsActive()));
        coupon.setUpdatedAt(LocalDateTime.now());
        Coupon saved = couponRepository.save(coupon);
        log.info("Toggled coupon status for {}: active={}", saved.getCode(), saved.getIsActive());
        return CouponDTO.fromEntity(saved);
    }

    @Transactional
    public void deleteCoupon(Long id) {
        Coupon coupon = couponRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Coupon", "id", id));
        // Soft-deactivate to preserve historical integrity if used
        coupon.setIsActive(false);
        coupon.setUpdatedAt(LocalDateTime.now());
        couponRepository.save(coupon);
        log.info("Deactivated coupon id: {}", id);
    }

    @Transactional(readOnly = true)
    public CouponValidateResponseDTO validateCoupon(String code, BigDecimal bookingAmount, String userEmail) {
        if (code == null || code.trim().isEmpty()) {
            return CouponValidateResponseDTO.invalid("", "Please enter a valid coupon code.");
        }

        String normalizedCode = code.trim().toUpperCase();
        Optional<Coupon> opt = couponRepository.findByCodeIgnoreCase(normalizedCode);
        if (opt.isEmpty()) {
            return CouponValidateResponseDTO.invalid(normalizedCode, "Coupon code '" + normalizedCode + "' does not exist.");
        }

        Coupon coupon = opt.get();
        if (!Boolean.TRUE.equals(coupon.getIsActive())) {
            return CouponValidateResponseDTO.invalid(normalizedCode, "Coupon '" + normalizedCode + "' is currently inactive.");
        }

        LocalDateTime now = LocalDateTime.now();
        if (coupon.isExpired(now)) {
            return CouponValidateResponseDTO.invalid(normalizedCode, "Coupon '" + normalizedCode + "' has expired.");
        }

        if (bookingAmount == null || bookingAmount.compareTo(BigDecimal.ZERO) <= 0) {
            return CouponValidateResponseDTO.invalid(normalizedCode, "Booking amount must be greater than zero.");
        }

        if (coupon.getMinBookingAmount() != null && bookingAmount.compareTo(coupon.getMinBookingAmount()) < 0) {
            return CouponValidateResponseDTO.invalid(normalizedCode,
                    String.format("Minimum booking amount of ₹%.2f required to apply this coupon.", coupon.getMinBookingAmount()));
        }

        if (coupon.getUsageLimit() != null && coupon.getTimesUsed() >= coupon.getUsageLimit()) {
            return CouponValidateResponseDTO.invalid(normalizedCode, "This coupon has reached its maximum global usage limit.");
        }

        // Check per-user limit
        if (userEmail != null && !userEmail.trim().isEmpty()) {
            Optional<User> userOpt = userRepository.findByEmail(userEmail);
            if (userOpt.isPresent()) {
                int timesUsedByUser = couponUsageRepository.countByCouponIdAndUserId(coupon.getId(), userOpt.get().getId());
                if (coupon.getPerUserLimit() != null && timesUsedByUser >= coupon.getPerUserLimit()) {
                    return CouponValidateResponseDTO.invalid(normalizedCode,
                            "You have already reached the maximum usage limit (" + coupon.getPerUserLimit() + ") for this coupon.");
                }
            }
        }

        BigDecimal discount = coupon.calculateDiscount(bookingAmount);
        BigDecimal finalAmount = bookingAmount.subtract(discount);
        if (finalAmount.compareTo(BigDecimal.ZERO) < 0) {
            finalAmount = BigDecimal.ZERO;
        }

        String msg = String.format("Coupon '%s' applied! You saved ₹%.2f", normalizedCode, discount);
        return CouponValidateResponseDTO.valid(
                coupon.getCode(),
                coupon.getDescription(),
                coupon.getDiscountType(),
                coupon.getDiscountValue(),
                bookingAmount,
                discount,
                finalAmount,
                msg
        );
    }

    @Transactional
    public void recordCouponUsage(Coupon coupon, User user, Booking booking, BigDecimal discount) {
        if (coupon == null || user == null || booking == null) return;

        coupon.incrementUsage();
        couponRepository.save(coupon);

        CouponUsage usage = new CouponUsage(coupon, user, booking, discount);
        couponUsageRepository.save(usage);

        log.info("Recorded usage of coupon {} by user {} on booking {} (Discount: ₹{})",
                coupon.getCode(), user.getEmail(), booking.getBookingReference(), discount);
    }

    @Transactional
    public void releaseCouponUsage(Long bookingId) {
        Optional<CouponUsage> usageOpt = couponUsageRepository.findByBookingId(bookingId);
        if (usageOpt.isPresent()) {
            CouponUsage usage = usageOpt.get();
            Coupon coupon = usage.getCoupon();
            if (coupon != null) {
                coupon.decrementUsage();
                couponRepository.save(coupon);
            }
            couponUsageRepository.deleteByBookingId(bookingId);
            log.info("Released coupon usage for cancelled booking id: {}", bookingId);
        }
    }

    @Transactional(readOnly = true)
    public Optional<Coupon> findByCode(String code) {
        if (code == null) return Optional.empty();
        return couponRepository.findByCodeIgnoreCase(code.trim());
    }
}
