package com.cinesmart.refund.dto;

import java.math.BigDecimal;

public class RefundStatsDTO {

    private long totalRequests;
    private long requestedCount;
    private long approvedCount;
    private long processingCount;
    private long completedCount;
    private long rejectedCount;
    private long failedCount;
    private BigDecimal totalRefundAmountCompleted;
    private BigDecimal totalCancellationFeesRetained;

    public RefundStatsDTO() {
    }

    public RefundStatsDTO(long totalRequests, long requestedCount, long approvedCount,
                          long processingCount, long completedCount, long rejectedCount,
                          long failedCount, BigDecimal totalRefundAmountCompleted,
                          BigDecimal totalCancellationFeesRetained) {
        this.totalRequests = totalRequests;
        this.requestedCount = requestedCount;
        this.approvedCount = approvedCount;
        this.processingCount = processingCount;
        this.completedCount = completedCount;
        this.rejectedCount = rejectedCount;
        this.failedCount = failedCount;
        this.totalRefundAmountCompleted = totalRefundAmountCompleted != null ? totalRefundAmountCompleted : BigDecimal.ZERO;
        this.totalCancellationFeesRetained = totalCancellationFeesRetained != null ? totalCancellationFeesRetained : BigDecimal.ZERO;
    }

    // Getters and Setters
    public long getTotalRequests() {
        return totalRequests;
    }

    public void setTotalRequests(long totalRequests) {
        this.totalRequests = totalRequests;
    }

    public long getRequestedCount() {
        return requestedCount;
    }

    public void setRequestedCount(long requestedCount) {
        this.requestedCount = requestedCount;
    }

    public long getApprovedCount() {
        return approvedCount;
    }

    public void setApprovedCount(long approvedCount) {
        this.approvedCount = approvedCount;
    }

    public long getProcessingCount() {
        return processingCount;
    }

    public void setProcessingCount(long processingCount) {
        this.processingCount = processingCount;
    }

    public long getCompletedCount() {
        return completedCount;
    }

    public void setCompletedCount(long completedCount) {
        this.completedCount = completedCount;
    }

    public long getRejectedCount() {
        return rejectedCount;
    }

    public void setRejectedCount(long rejectedCount) {
        this.rejectedCount = rejectedCount;
    }

    public long getFailedCount() {
        return failedCount;
    }

    public void setFailedCount(long failedCount) {
        this.failedCount = failedCount;
    }

    public BigDecimal getTotalRefundAmountCompleted() {
        return totalRefundAmountCompleted;
    }

    public void setTotalRefundAmountCompleted(BigDecimal totalRefundAmountCompleted) {
        this.totalRefundAmountCompleted = totalRefundAmountCompleted;
    }

    public BigDecimal getTotalCancellationFeesRetained() {
        return totalCancellationFeesRetained;
    }

    public void setTotalCancellationFeesRetained(BigDecimal totalCancellationFeesRetained) {
        this.totalCancellationFeesRetained = totalCancellationFeesRetained;
    }
}
