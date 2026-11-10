package com.cinesmart.coupon.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;

public class CouponValidateRequest {

    @NotBlank(message = "Coupon code is required")
    private String code;

    @NotNull(message = "Booking amount is required")
    @DecimalMin(value = "0.01", message = "Booking amount must be greater than zero")
    private BigDecimal bookingAmount;

    public CouponValidateRequest() {
    }

    public CouponValidateRequest(String code, BigDecimal bookingAmount) {
        this.code = code;
        this.bookingAmount = bookingAmount;
    }

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public BigDecimal getBookingAmount() {
        return bookingAmount;
    }

    public void setBookingAmount(BigDecimal bookingAmount) {
        this.bookingAmount = bookingAmount;
    }
}
