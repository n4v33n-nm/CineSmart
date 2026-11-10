package com.cinesmart.loyalty.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;

public class LoyaltyRedeemRequest {

    @NotNull(message = "Points to redeem is required")
    @Min(value = 1, message = "Points to redeem must be at least 1")
    private Integer pointsToRedeem;

    @NotNull(message = "Current order amount is required")
    @DecimalMin(value = "0.01", message = "Current order amount must be greater than zero")
    private BigDecimal currentAmount;

    public LoyaltyRedeemRequest() {
    }

    public LoyaltyRedeemRequest(Integer pointsToRedeem, BigDecimal currentAmount) {
        this.pointsToRedeem = pointsToRedeem;
        this.currentAmount = currentAmount;
    }

    public Integer getPointsToRedeem() {
        return pointsToRedeem;
    }

    public void setPointsToRedeem(Integer pointsToRedeem) {
        this.pointsToRedeem = pointsToRedeem;
    }

    public BigDecimal getCurrentAmount() {
        return currentAmount;
    }

    public void setCurrentAmount(BigDecimal currentAmount) {
        this.currentAmount = currentAmount;
    }
}
