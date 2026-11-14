package com.cinesmart.analytics.dto;

import java.math.BigDecimal;

public class DashboardKPIDTO {

    private long totalUsers;
    private long totalBookings;
    private long confirmedBookings;
    private long cancelledBookings;
    private BigDecimal totalRevenue;
    private BigDecimal refundAmount;
    private BigDecimal netRevenue;
    private long totalMovies;
    private long totalTheatres;
    private double averageOccupancyPercentage;
    private double confirmedRate;
    private double cancellationRate;

    public DashboardKPIDTO() {
    }

    public DashboardKPIDTO(long totalUsers, long totalBookings, long confirmedBookings, long cancelledBookings,
                           BigDecimal totalRevenue, BigDecimal refundAmount, BigDecimal netRevenue,
                           long totalMovies, long totalTheatres, double averageOccupancyPercentage,
                           double confirmedRate, double cancellationRate) {
        this.totalUsers = totalUsers;
        this.totalBookings = totalBookings;
        this.confirmedBookings = confirmedBookings;
        this.cancelledBookings = cancelledBookings;
        this.totalRevenue = totalRevenue;
        this.refundAmount = refundAmount;
        this.netRevenue = netRevenue;
        this.totalMovies = totalMovies;
        this.totalTheatres = totalTheatres;
        this.averageOccupancyPercentage = averageOccupancyPercentage;
        this.confirmedRate = confirmedRate;
        this.cancellationRate = cancellationRate;
    }

    public long getTotalUsers() {
        return totalUsers;
    }

    public void setTotalUsers(long totalUsers) {
        this.totalUsers = totalUsers;
    }

    public long getTotalBookings() {
        return totalBookings;
    }

    public void setTotalBookings(long totalBookings) {
        this.totalBookings = totalBookings;
    }

    public long getConfirmedBookings() {
        return confirmedBookings;
    }

    public void setConfirmedBookings(long confirmedBookings) {
        this.confirmedBookings = confirmedBookings;
    }

    public long getCancelledBookings() {
        return cancelledBookings;
    }

    public void setCancelledBookings(long cancelledBookings) {
        this.cancelledBookings = cancelledBookings;
    }

    public BigDecimal getTotalRevenue() {
        return totalRevenue != null ? totalRevenue : BigDecimal.ZERO;
    }

    public void setTotalRevenue(BigDecimal totalRevenue) {
        this.totalRevenue = totalRevenue;
    }

    public BigDecimal getRefundAmount() {
        return refundAmount != null ? refundAmount : BigDecimal.ZERO;
    }

    public void setRefundAmount(BigDecimal refundAmount) {
        this.refundAmount = refundAmount;
    }

    public BigDecimal getNetRevenue() {
        return netRevenue != null ? netRevenue : BigDecimal.ZERO;
    }

    public void setNetRevenue(BigDecimal netRevenue) {
        this.netRevenue = netRevenue;
    }

    public long getTotalMovies() {
        return totalMovies;
    }

    public void setTotalMovies(long totalMovies) {
        this.totalMovies = totalMovies;
    }

    public long getTotalTheatres() {
        return totalTheatres;
    }

    public void setTotalTheatres(long totalTheatres) {
        this.totalTheatres = totalTheatres;
    }

    public double getAverageOccupancyPercentage() {
        return averageOccupancyPercentage;
    }

    public void setAverageOccupancyPercentage(double averageOccupancyPercentage) {
        this.averageOccupancyPercentage = averageOccupancyPercentage;
    }

    public double getConfirmedRate() {
        return confirmedRate;
    }

    public void setConfirmedRate(double confirmedRate) {
        this.confirmedRate = confirmedRate;
    }

    public double getCancellationRate() {
        return cancellationRate;
    }

    public void setCancellationRate(double cancellationRate) {
        this.cancellationRate = cancellationRate;
    }
}
