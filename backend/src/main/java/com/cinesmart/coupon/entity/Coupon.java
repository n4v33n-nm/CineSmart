package com.cinesmart.coupon.entity;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;

@Entity
@Table(name = "coupons")
public class Coupon {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true, length = 50)
    private String code;

    @Column(length = 255)
    private String description;

    @Enumerated(EnumType.STRING)
    @Column(name = "discount_type", nullable = false, length = 20)
    private DiscountType discountType;

    @Column(name = "discount_value", nullable = false, precision = 10, scale = 2)
    private BigDecimal discountValue;

    @Column(name = "min_booking_amount", nullable = false, precision = 10, scale = 2)
    private BigDecimal minBookingAmount = BigDecimal.ZERO;

    @Column(name = "max_discount_amount", precision = 10, scale = 2)
    private BigDecimal maxDiscountAmount;

    @Column(name = "valid_from", nullable = false)
    private LocalDateTime validFrom;

    @Column(name = "valid_until", nullable = false)
    private LocalDateTime validUntil;

    @Column(name = "usage_limit", nullable = false)
    private Integer usageLimit = 100;

    @Column(name = "per_user_limit", nullable = false)
    private Integer perUserLimit = 1;

    @Column(name = "times_used", nullable = false)
    private Integer timesUsed = 0;

    @Column(name = "is_active", nullable = false)
    private Boolean isActive = true;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt = LocalDateTime.now();

    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt = LocalDateTime.now();

    public Coupon() {
    }

    public Coupon(String code, String description, DiscountType discountType, BigDecimal discountValue,
                  BigDecimal minBookingAmount, BigDecimal maxDiscountAmount,
                  LocalDateTime validFrom, LocalDateTime validUntil,
                  Integer usageLimit, Integer perUserLimit) {
        this.code = code != null ? code.trim().toUpperCase() : null;
        this.description = description;
        this.discountType = discountType;
        this.discountValue = discountValue;
        this.minBookingAmount = minBookingAmount != null ? minBookingAmount : BigDecimal.ZERO;
        this.maxDiscountAmount = maxDiscountAmount;
        this.validFrom = validFrom != null ? validFrom : LocalDateTime.now();
        this.validUntil = validUntil;
        this.usageLimit = usageLimit != null ? usageLimit : 100;
        this.perUserLimit = perUserLimit != null ? perUserLimit : 1;
        this.timesUsed = 0;
        this.isActive = true;
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
    }

    public boolean isExpired(LocalDateTime now) {
        if (now == null) now = LocalDateTime.now();
        return now.isBefore(validFrom) || now.isAfter(validUntil);
    }

    public boolean isEligible(BigDecimal amount, int userTimesUsed) {
        if (!Boolean.TRUE.equals(isActive)) return false;
        if (isExpired(LocalDateTime.now())) return false;
        if (usageLimit != null && timesUsed >= usageLimit) return false;
        if (perUserLimit != null && userTimesUsed >= perUserLimit) return false;
        if (minBookingAmount != null && amount.compareTo(minBookingAmount) < 0) return false;
        return true;
    }

    public BigDecimal calculateDiscount(BigDecimal amount) {
        if (amount == null || amount.compareTo(BigDecimal.ZERO) <= 0) {
            return BigDecimal.ZERO;
        }

        BigDecimal calculated;
        if (discountType == DiscountType.PERCENTAGE) {
            calculated = amount.multiply(discountValue)
                    .divide(BigDecimal.valueOf(100), 2, RoundingMode.HALF_UP);
            if (maxDiscountAmount != null && calculated.compareTo(maxDiscountAmount) > 0) {
                calculated = maxDiscountAmount;
            }
        } else {
            calculated = discountValue;
        }

        // Discount cannot exceed booking total
        if (calculated.compareTo(amount) > 0) {
            calculated = amount;
        }

        return calculated.setScale(2, RoundingMode.HALF_UP);
    }

    public void incrementUsage() {
        this.timesUsed = (this.timesUsed != null ? this.timesUsed : 0) + 1;
        this.updatedAt = LocalDateTime.now();
    }

    public void decrementUsage() {
        if (this.timesUsed != null && this.timesUsed > 0) {
            this.timesUsed = this.timesUsed - 1;
            this.updatedAt = LocalDateTime.now();
        }
    }

    // Getters and Setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code != null ? code.trim().toUpperCase() : null;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public DiscountType getDiscountType() {
        return discountType;
    }

    public void setDiscountType(DiscountType discountType) {
        this.discountType = discountType;
    }

    public BigDecimal getDiscountValue() {
        return discountValue;
    }

    public void setDiscountValue(BigDecimal discountValue) {
        this.discountValue = discountValue;
    }

    public BigDecimal getMinBookingAmount() {
        return minBookingAmount;
    }

    public void setMinBookingAmount(BigDecimal minBookingAmount) {
        this.minBookingAmount = minBookingAmount;
    }

    public BigDecimal getMaxDiscountAmount() {
        return maxDiscountAmount;
    }

    public void setMaxDiscountAmount(BigDecimal maxDiscountAmount) {
        this.maxDiscountAmount = maxDiscountAmount;
    }

    public LocalDateTime getValidFrom() {
        return validFrom;
    }

    public void setValidFrom(LocalDateTime validFrom) {
        this.validFrom = validFrom;
    }

    public LocalDateTime getValidUntil() {
        return validUntil;
    }

    public void setValidUntil(LocalDateTime validUntil) {
        this.validUntil = validUntil;
    }

    public Integer getUsageLimit() {
        return usageLimit;
    }

    public void setUsageLimit(Integer usageLimit) {
        this.usageLimit = usageLimit;
    }

    public Integer getPerUserLimit() {
        return perUserLimit;
    }

    public void setPerUserLimit(Integer perUserLimit) {
        this.perUserLimit = perUserLimit;
    }

    public Integer getTimesUsed() {
        return timesUsed;
    }

    public void setTimesUsed(Integer timesUsed) {
        this.timesUsed = timesUsed;
    }

    public Boolean getIsActive() {
        return isActive;
    }

    public void setIsActive(Boolean active) {
        isActive = active;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }
}

