package com.cinesmart.loyalty.dto;

import java.math.BigDecimal;

public class LoyaltyRedeemResponseDTO {

    private boolean valid;
    private int pointsRedeemed;
    private BigDecimal discountAmount;
    private int remainingPointsBalance;
    private BigDecimal finalAmount;
    private String message;

    public LoyaltyRedeemResponseDTO() {
    }

    public static LoyaltyRedeemResponseDTO valid(int pointsRedeemed, BigDecimal discountAmount,
                                                 int remainingPointsBalance, BigDecimal finalAmount, String message) {
        LoyaltyRedeemResponseDTO dto = new LoyaltyRedeemResponseDTO();
        dto.setValid(true);
        dto.setPointsRedeemed(pointsRedeemed);
        dto.setDiscountAmount(discountAmount);
        dto.setRemainingPointsBalance(remainingPointsBalance);
        dto.setFinalAmount(finalAmount);
        dto.setMessage(message != null ? message : "Loyalty rewards discount applied successfully");
        return dto;
    }

    public static LoyaltyRedeemResponseDTO invalid(String message, int currentBalance) {
        LoyaltyRedeemResponseDTO dto = new LoyaltyRedeemResponseDTO();
        dto.setValid(false);
        dto.setPointsRedeemed(0);
        dto.setDiscountAmount(BigDecimal.ZERO);
        dto.setRemainingPointsBalance(currentBalance);
        dto.setMessage(message);
        return dto;
    }

    public boolean isValid() {
        return valid;
    }

    public void setValid(boolean valid) {
        this.valid = valid;
    }

    public int getPointsRedeemed() {
        return pointsRedeemed;
    }

    public void setPointsRedeemed(int pointsRedeemed) {
        this.pointsRedeemed = pointsRedeemed;
    }

    public BigDecimal getDiscountAmount() {
        return discountAmount;
    }

    public void setDiscountAmount(BigDecimal discountAmount) {
        this.discountAmount = discountAmount;
    }

    public int getRemainingPointsBalance() {
        return remainingPointsBalance;
    }

    public void setRemainingPointsBalance(int remainingPointsBalance) {
        this.remainingPointsBalance = remainingPointsBalance;
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
