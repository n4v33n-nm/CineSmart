package com.cinesmart.analytics.dto;

import java.util.List;

public class BookingAnalyticsDTO {

    private List<TimeSeriesDataPointDTO> dailyBookings;
    private List<TimeSeriesDataPointDTO> weeklyBookings;
    private List<TimeSeriesDataPointDTO> monthlyBookings;
    private List<CategoryDataPointDTO> statusDistribution;

    public BookingAnalyticsDTO() {
    }

    public BookingAnalyticsDTO(List<TimeSeriesDataPointDTO> dailyBookings,
                               List<TimeSeriesDataPointDTO> weeklyBookings,
                               List<TimeSeriesDataPointDTO> monthlyBookings,
                               List<CategoryDataPointDTO> statusDistribution) {
        this.dailyBookings = dailyBookings;
        this.weeklyBookings = weeklyBookings;
        this.monthlyBookings = monthlyBookings;
        this.statusDistribution = statusDistribution;
    }

    public List<TimeSeriesDataPointDTO> getDailyBookings() {
        return dailyBookings;
    }

    public void setDailyBookings(List<TimeSeriesDataPointDTO> dailyBookings) {
        this.dailyBookings = dailyBookings;
    }

    public List<TimeSeriesDataPointDTO> getWeeklyBookings() {
        return weeklyBookings;
    }

    public void setWeeklyBookings(List<TimeSeriesDataPointDTO> weeklyBookings) {
        this.weeklyBookings = weeklyBookings;
    }

    public List<TimeSeriesDataPointDTO> getMonthlyBookings() {
        return monthlyBookings;
    }

    public void setMonthlyBookings(List<TimeSeriesDataPointDTO> monthlyBookings) {
        this.monthlyBookings = monthlyBookings;
    }

    public List<CategoryDataPointDTO> getStatusDistribution() {
        return statusDistribution;
    }

    public void setStatusDistribution(List<CategoryDataPointDTO> statusDistribution) {
        this.statusDistribution = statusDistribution;
    }
}
