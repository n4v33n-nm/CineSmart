package com.cinesmart.analytics.dto;

import java.math.BigDecimal;

public class TimeSeriesDataPointDTO {

    private String period;
    private BigDecimal revenue;
    private long bookingsCount;
    private long cancelledCount;
    private BigDecimal refundAmount;

    public TimeSeriesDataPointDTO() {
    }

    public TimeSeriesDataPointDTO(String period, BigDecimal revenue, long bookingsCount, long cancelledCount, BigDecimal refundAmount) {
        this.period = period;
        this.revenue = revenue != null ? revenue : BigDecimal.ZERO;
        this.bookingsCount = bookingsCount;
        this.cancelledCount = cancelledCount;
        this.refundAmount = refundAmount != null ? refundAmount : BigDecimal.ZERO;
    }

    public String getPeriod() {
        return period;
    }

    public void setPeriod(String period) {
        this.period = period;
    }

    public BigDecimal getRevenue() {
        return revenue != null ? revenue : BigDecimal.ZERO;
    }

    public void setRevenue(BigDecimal revenue) {
        this.revenue = revenue;
    }

    public long getBookingsCount() {
        return bookingsCount;
    }

    public void setBookingsCount(long bookingsCount) {
        this.bookingsCount = bookingsCount;
    }

    public long getCancelledCount() {
        return cancelledCount;
    }

    public void setCancelledCount(long cancelledCount) {
        this.cancelledCount = cancelledCount;
    }

    public BigDecimal getRefundAmount() {
        return refundAmount != null ? refundAmount : BigDecimal.ZERO;
    }

    public void setRefundAmount(BigDecimal refundAmount) {
        this.refundAmount = refundAmount;
    }
}
