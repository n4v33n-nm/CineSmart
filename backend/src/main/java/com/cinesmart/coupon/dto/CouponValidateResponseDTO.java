package com.cinesmart.coupon.dto;

import com.cinesmart.coupon.entity.DiscountType;

import java.math.BigDecimal;

public class CouponValidateResponseDTO {

    private boolean valid;
    private String code;
    private String description;
    private DiscountType discountType;
    private BigDecimal discountValue;
    private BigDecimal originalAmount;
    private BigDecimal discountAmount;
    private BigDecimal finalAmount;
    private String message;

    public CouponValidateResponseDTO() {
    }

    public static CouponValidateResponseDTO valid(String code, String description, DiscountType discountType,
                                                  BigDecimal discountValue, BigDecimal originalAmount,
                                                  BigDecimal discountAmount, BigDecimal finalAmount, String message) {
        CouponValidateResponseDTO dto = new CouponValidateResponseDTO();
        dto.setValid(true);
        dto.setCode(code);
        dto.setDescription(description);
        dto.setDiscountType(discountType);
        dto.setDiscountValue(discountValue);
        dto.setOriginalAmount(originalAmount);
        dto.setDiscountAmount(discountAmount);
        dto.setFinalAmount(finalAmount);
        dto.setMessage(message != null ? message : "Coupon applied successfully");
        return dto;
    }

    public static CouponValidateResponseDTO invalid(String code, String message) {
        CouponValidateResponseDTO dto = new CouponValidateResponseDTO();
        dto.setValid(false);
        dto.setCode(code);
        dto.setMessage(message);
        dto.setDiscountAmount(BigDecimal.ZERO);
        return dto;
    }

    public boolean isValid() {
        return valid;
    }

    public void setValid(boolean valid) {
        this.valid = valid;
    }

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
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

    public BigDecimal getOriginalAmount() {
        return originalAmount;
    }

    public void setOriginalAmount(BigDecimal originalAmount) {
        this.originalAmount = originalAmount;
    }

    public BigDecimal getDiscountAmount() {
        return discountAmount;
    }

    public void setDiscountAmount(BigDecimal discountAmount) {
        this.discountAmount = discountAmount;
    }

    public BigDecimal getFinalAmount() {
        return finalAmount;
    }

    public void setFinalAmount(BigDecimal finalAmount) {
        this.finalAmount = finalAmount;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }
}

