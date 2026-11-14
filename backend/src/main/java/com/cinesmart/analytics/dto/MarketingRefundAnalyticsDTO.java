package com.cinesmart.analytics.dto;

import java.math.BigDecimal;
import java.util.List;

public class MarketingRefundAnalyticsDTO {

    private List<CategoryDataPointDTO> couponUsage;
    private long loyaltyPointsEarnedTotal;
    private long loyaltyPointsRedeemedTotal;
    private BigDecimal loyaltyDiscountTotal;
    private List<TimeSeriesDataPointDTO> cancellationTrends;
    private List<CategoryDataPointDTO> refundStatusDistribution;
    private BigDecimal totalRefundedAmount;
    private BigDecimal totalCancellationFeesRetained;

    public MarketingRefundAnalyticsDTO() {
    }

    public MarketingRefundAnalyticsDTO(List<CategoryDataPointDTO> couponUsage,
                                       long loyaltyPointsEarnedTotal,
                                       long loyaltyPointsRedeemedTotal,
                                       BigDecimal loyaltyDiscountTotal,
                                       List<TimeSeriesDataPointDTO> cancellationTrends,
                                       List<CategoryDataPointDTO> refundStatusDistribution,
                                       BigDecimal totalRefundedAmount,
                                       BigDecimal totalCancellationFeesRetained) {
        this.couponUsage = couponUsage;
        this.loyaltyPointsEarnedTotal = loyaltyPointsEarnedTotal;
        this.loyaltyPointsRedeemedTotal = loyaltyPointsRedeemedTotal;
        this.loyaltyDiscountTotal = loyaltyDiscountTotal != null ? loyaltyDiscountTotal : BigDecimal.ZERO;
        this.cancellationTrends = cancellationTrends;
        this.refundStatusDistribution = refundStatusDistribution;
        this.totalRefundedAmount = totalRefundedAmount != null ? totalRefundedAmount : BigDecimal.ZERO;
        this.totalCancellationFeesRetained = totalCancellationFeesRetained != null ? totalCancellationFeesRetained : BigDecimal.ZERO;
    }

    public List<CategoryDataPointDTO> getCouponUsage() {
        return couponUsage;
    }

    public void setCouponUsage(List<CategoryDataPointDTO> couponUsage) {
        this.couponUsage = couponUsage;
    }

    public long getLoyaltyPointsEarnedTotal() {
        return loyaltyPointsEarnedTotal;
    }

    public void setLoyaltyPointsEarnedTotal(long loyaltyPointsEarnedTotal) {
        this.loyaltyPointsEarnedTotal = loyaltyPointsEarnedTotal;
    }

    public long getLoyaltyPointsRedeemedTotal() {
        return loyaltyPointsRedeemedTotal;
    }

    public void setLoyaltyPointsRedeemedTotal(long loyaltyPointsRedeemedTotal) {
        this.loyaltyPointsRedeemedTotal = loyaltyPointsRedeemedTotal;
    }

    public BigDecimal getLoyaltyDiscountTotal() {
        return loyaltyDiscountTotal;
    }

    public void setLoyaltyDiscountTotal(BigDecimal loyaltyDiscountTotal) {
        this.loyaltyDiscountTotal = loyaltyDiscountTotal;
    }

    public List<TimeSeriesDataPointDTO> getCancellationTrends() {
        return cancellationTrends;
    }

    public void setCancellationTrends(List<TimeSeriesDataPointDTO> cancellationTrends) {
        this.cancellationTrends = cancellationTrends;
    }

    public List<CategoryDataPointDTO> getRefundStatusDistribution() {
        return refundStatusDistribution;
    }

    public void setRefundStatusDistribution(List<CategoryDataPointDTO> refundStatusDistribution) {
        this.refundStatusDistribution = refundStatusDistribution;
    }

    public BigDecimal getTotalRefundedAmount() {
        return totalRefundedAmount;
    }

    public void setTotalRefundedAmount(BigDecimal totalRefundedAmount) {
        this.totalRefundedAmount = totalRefundedAmount;
    }

    public BigDecimal getTotalCancellationFeesRetained() {
        return totalCancellationFeesRetained;
    }

    public void setTotalCancellationFeesRetained(BigDecimal totalCancellationFeesRetained) {
        this.totalCancellationFeesRetained = totalCancellationFeesRetained;
    }
}
