package com.cinesmart.refund.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public class CancellationPreviewResponseDTO {

    private Long bookingId;
    private String bookingReference;
    private String movieTitle;
    private LocalDateTime showStartTime;
    private LocalDateTime currentTime;
    private Double hoursRemaining;
    private boolean eligible;
    private String ineligibilityReason;
    private BigDecimal paidAmount;
    private BigDecimal refundPercentage;
    private BigDecimal cancellationFee;
    private BigDecimal estimatedRefundAmount;
    private String policyName;
    private String policyDescription;
    private int ticketCount;
    private int loyaltyPointsToClawback;
    private int loyaltyPointsToRestore;
    private boolean couponRestored;

    public CancellationPreviewResponseDTO() {
    }

    public static CancellationPreviewResponseDTO ineligible(Long bookingId, String bookingReference, String reason) {
        CancellationPreviewResponseDTO dto = new CancellationPreviewResponseDTO();
        dto.setBookingId(bookingId);
        dto.setBookingReference(bookingReference);
        dto.setEligible(false);
        dto.setIneligibilityReason(reason);
        dto.setCurrentTime(LocalDateTime.now());
        dto.setPaidAmount(BigDecimal.ZERO);
        dto.setRefundPercentage(BigDecimal.ZERO);
        dto.setCancellationFee(BigDecimal.ZERO);
        dto.setEstimatedRefundAmount(BigDecimal.ZERO);
        return dto;
    }

    // Getters and Setters
    public Long getBookingId() {
        return bookingId;
    }

    public void setBookingId(Long bookingId) {
        this.bookingId = bookingId;
    }

    public String getBookingReference() {
        return bookingReference;
    }

    public void setBookingReference(String bookingReference) {
        this.bookingReference = bookingReference;
    }

    public String getMovieTitle() {
        return movieTitle;
    }

    public void setMovieTitle(String movieTitle) {
        this.movieTitle = movieTitle;
    }

    public LocalDateTime getShowStartTime() {
        return showStartTime;
    }

    public void setShowStartTime(LocalDateTime showStartTime) {
        this.showStartTime = showStartTime;
    }

    public LocalDateTime getCurrentTime() {
        return currentTime;
    }

    public void setCurrentTime(LocalDateTime currentTime) {
        this.currentTime = currentTime;
    }

    public Double getHoursRemaining() {
        return hoursRemaining;
    }

    public void setHoursRemaining(Double hoursRemaining) {
        this.hoursRemaining = hoursRemaining;
    }

    public boolean isEligible() {
        return eligible;
    }

    public void setEligible(boolean eligible) {
        this.eligible = eligible;
    }

    public String getIneligibilityReason() {
        return ineligibilityReason;
    }

    public void setIneligibilityReason(String ineligibilityReason) {
        this.ineligibilityReason = ineligibilityReason;
    }

    public BigDecimal getPaidAmount() {
        return paidAmount;
    }

    public void setPaidAmount(BigDecimal paidAmount) {
        this.paidAmount = paidAmount;
    }

    public BigDecimal getRefundPercentage() {
        return refundPercentage;
    }

    public void setRefundPercentage(BigDecimal refundPercentage) {
        this.refundPercentage = refundPercentage;
    }

    public BigDecimal getCancellationFee() {
        return cancellationFee;
    }

    public void setCancellationFee(BigDecimal cancellationFee) {
        this.cancellationFee = cancellationFee;
    }

    public BigDecimal getEstimatedRefundAmount() {
        return estimatedRefundAmount;
    }

    public void setEstimatedRefundAmount(BigDecimal estimatedRefundAmount) {
        this.estimatedRefundAmount = estimatedRefundAmount;
    }

    public String getPolicyName() {
        return policyName;
    }

    public void setPolicyName(String policyName) {
        this.policyName = policyName;
    }

    public String getPolicyDescription() {
        return policyDescription;
    }

    public void setPolicyDescription(String policyDescription) {
        this.policyDescription = policyDescription;
    }

    public int getTicketCount() {
        return ticketCount;
    }

    public void setTicketCount(int ticketCount) {
        this.ticketCount = ticketCount;
    }

    public int getLoyaltyPointsToClawback() {
        return loyaltyPointsToClawback;
    }

    public void setLoyaltyPointsToClawback(int loyaltyPointsToClawback) {
        this.loyaltyPointsToClawback = loyaltyPointsToClawback;
    }

    public int getLoyaltyPointsToRestore() {
        return loyaltyPointsToRestore;
    }

    public void setLoyaltyPointsToRestore(int loyaltyPointsToRestore) {
        this.loyaltyPointsToRestore = loyaltyPointsToRestore;
    }

    public boolean isCouponRestored() {
        return couponRestored;
    }

    public void setCouponRestored(boolean couponRestored) {
        this.couponRestored = couponRestored;
    }
}
