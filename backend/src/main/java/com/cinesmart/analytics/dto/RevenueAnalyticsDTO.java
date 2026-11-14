package com.cinesmart.analytics.dto;

import java.math.BigDecimal;
import java.util.List;

public class RevenueAnalyticsDTO {

    private List<TimeSeriesDataPointDTO> dailyRevenue;
    private List<TimeSeriesDataPointDTO> weeklyRevenue;
    private List<TimeSeriesDataPointDTO> monthlyRevenue;
    private BigDecimal currentPeriodRevenue;
    private BigDecimal previousPeriodRevenue;
    private double growthPercentage;

    public RevenueAnalyticsDTO() {
    }

    public RevenueAnalyticsDTO(List<TimeSeriesDataPointDTO> dailyRevenue,
                               List<TimeSeriesDataPointDTO> weeklyRevenue,
                               List<TimeSeriesDataPointDTO> monthlyRevenue,
                               BigDecimal currentPeriodRevenue,
                               BigDecimal previousPeriodRevenue,
                               double growthPercentage) {
        this.dailyRevenue = dailyRevenue;
        this.weeklyRevenue = weeklyRevenue;
        this.monthlyRevenue = monthlyRevenue;
        this.currentPeriodRevenue = currentPeriodRevenue != null ? currentPeriodRevenue : BigDecimal.ZERO;
        this.previousPeriodRevenue = previousPeriodRevenue != null ? previousPeriodRevenue : BigDecimal.ZERO;
        this.growthPercentage = growthPercentage;
    }

    public List<TimeSeriesDataPointDTO> getDailyRevenue() {
        return dailyRevenue;
    }

    public void setDailyRevenue(List<TimeSeriesDataPointDTO> dailyRevenue) {
        this.dailyRevenue = dailyRevenue;
    }

    public List<TimeSeriesDataPointDTO> getWeeklyRevenue() {
        return weeklyRevenue;
    }

    public void setWeeklyRevenue(List<TimeSeriesDataPointDTO> weeklyRevenue) {
        this.weeklyRevenue = weeklyRevenue;
    }

    public List<TimeSeriesDataPointDTO> getMonthlyRevenue() {
        return monthlyRevenue;
    }

    public void setMonthlyRevenue(List<TimeSeriesDataPointDTO> monthlyRevenue) {
        this.monthlyRevenue = monthlyRevenue;
    }

    public BigDecimal getCurrentPeriodRevenue() {
        return currentPeriodRevenue != null ? currentPeriodRevenue : BigDecimal.ZERO;
    }

    public void setCurrentPeriodRevenue(BigDecimal currentPeriodRevenue) {
        this.currentPeriodRevenue = currentPeriodRevenue;
    }

    public BigDecimal getPreviousPeriodRevenue() {
        return previousPeriodRevenue != null ? previousPeriodRevenue : BigDecimal.ZERO;
    }

    public void setPreviousPeriodRevenue(BigDecimal previousPeriodRevenue) {
        this.previousPeriodRevenue = previousPeriodRevenue;
    }

    public double getGrowthPercentage() {
        return growthPercentage;
    }

    public void setGrowthPercentage(double growthPercentage) {
        this.growthPercentage = growthPercentage;
    }
}
