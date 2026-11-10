package com.cinesmart.loyalty.dto;

import com.cinesmart.loyalty.entity.LoyaltyAccount;

import java.math.BigDecimal;

public class LoyaltyAccountDTO {

    private Long id;
    private Long userId;
    private Integer pointsBalance;
    private BigDecimal rupeeValue;
    private Integer lifetimePointsEarned;
    private Integer lifetimePointsRedeemed;
    private String earnRateDescription;
    private String redemptionRateDescription;

    public LoyaltyAccountDTO() {
    }

    public static LoyaltyAccountDTO fromEntity(LoyaltyAccount account, BigDecimal conversionRate) {
        if (account == null) return null;
        LoyaltyAccountDTO dto = new LoyaltyAccountDTO();
        dto.setId(account.getId());
        if (account.getUser() != null) {
            dto.setUserId(account.getUser().getId());
        }
        int balance = account.getPointsBalance();
        dto.setPointsBalance(balance);
        dto.setRupeeValue(BigDecimal.valueOf(balance).multiply(conversionRate != null ? conversionRate : BigDecimal.ONE));
        dto.setLifetimePointsEarned(account.getLifetimePointsEarned());
        dto.setLifetimePointsRedeemed(account.getLifetimePointsRedeemed());
        dto.setEarnRateDescription("Earn 1 reward point for every ₹10 spent on confirmed bookings");
        dto.setRedemptionRateDescription("1 Reward Point = ₹1.00 instant discount on tickets");
        return dto;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getUserId() {
        return userId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }

    public Integer getPointsBalance() {
        return pointsBalance;
    }

    public void setPointsBalance(Integer pointsBalance) {
        this.pointsBalance = pointsBalance;
    }

    public BigDecimal getRupeeValue() {
        return rupeeValue;
    }

    public void setRupeeValue(BigDecimal rupeeValue) {
        this.rupeeValue = rupeeValue;
    }

    public Integer getLifetimePointsEarned() {
        return lifetimePointsEarned;
    }

    public void setLifetimePointsEarned(Integer lifetimePointsEarned) {
        this.lifetimePointsEarned = lifetimePointsEarned;
    }

    public Integer getLifetimePointsRedeemed() {
        return lifetimePointsRedeemed;
    }

    public void setLifetimePointsRedeemed(Integer lifetimePointsRedeemed) {
        this.lifetimePointsRedeemed = lifetimePointsRedeemed;
    }

    public String getEarnRateDescription() {
        return earnRateDescription;
    }

    public void setEarnRateDescription(String earnRateDescription) {
        this.earnRateDescription = earnRateDescription;
    }

    public String getRedemptionRateDescription() {
        return redemptionRateDescription;
    }

    public void setRedemptionRateDescription(String redemptionRateDescription) {
        this.redemptionRateDescription = redemptionRateDescription;
    }
}

