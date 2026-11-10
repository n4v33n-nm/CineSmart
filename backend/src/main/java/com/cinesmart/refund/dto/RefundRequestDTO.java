package com.cinesmart.refund.dto;

import com.cinesmart.refund.entity.RefundRequest;
import java.math.BigDecimal;
import java.time.LocalDateTime;

public class RefundRequestDTO {

    private Long id;
    private String refundReference;
    private Long bookingId;
    private String bookingReference;
    private String movieTitle;
    private String cinemaName;
    private String screenName;
    private LocalDateTime showStartTime;
    private Long userId;
    private String userEmail;
    private String userName;
    private String policyName;
    private BigDecimal originalAmount;
    private BigDecimal cancellationFee;
    private BigDecimal refundAmount;
    private BigDecimal refundPercentage;
    private String status;
    private String reason;
    private String rejectionReason;
    private String adminNotes;
    private String gatewayRefundId;
    private BigDecimal hoursBeforeShow;
    private LocalDateTime requestedAt;
    private LocalDateTime processedAt;
    private String processedByAdminName;

    public RefundRequestDTO() {
    }

    public static RefundRequestDTO fromEntity(RefundRequest req) {
        if (req == null) return null;
        RefundRequestDTO dto = new RefundRequestDTO();
        dto.setId(req.getId());
        dto.setRefundReference(req.getRefundReference());
        dto.setBookingId(req.getBooking() != null ? req.getBooking().getId() : null);
        dto.setBookingReference(req.getBooking() != null ? req.getBooking().getBookingReference() : null);

        if (req.getBooking() != null && req.getBooking().getShow() != null) {
            if (req.getBooking().getShow().getMovie() != null) {
                dto.setMovieTitle(req.getBooking().getShow().getMovie().getTitle());
            }
            if (req.getBooking().getShow().getScreen() != null) {
                dto.setScreenName(req.getBooking().getShow().getScreen().getScreenType().name() + " Screen " + req.getBooking().getShow().getScreen().getScreenNumber());
                if (req.getBooking().getShow().getScreen().getCinema() != null) {
                    dto.setCinemaName(req.getBooking().getShow().getScreen().getCinema().getName());
                }
            }
            dto.setShowStartTime(req.getBooking().getShow().getStartTime());
        }

        if (req.getUser() != null) {
            dto.setUserId(req.getUser().getId());
            dto.setUserEmail(req.getUser().getEmail());
            dto.setUserName(req.getUser().getFullName());
        }

        if (req.getPolicy() != null) {
            dto.setPolicyName(req.getPolicy().getPolicyName());
        }

        dto.setOriginalAmount(req.getOriginalAmount());
        dto.setCancellationFee(req.getCancellationFee());
        dto.setRefundAmount(req.getRefundAmount());
        dto.setRefundPercentage(req.getRefundPercentage());
        dto.setStatus(req.getStatus() != null ? req.getStatus().name() : null);
        dto.setReason(req.getReason());
        dto.setRejectionReason(req.getRejectionReason());
        dto.setAdminNotes(req.getAdminNotes());
        dto.setGatewayRefundId(req.getGatewayRefundId());
        dto.setHoursBeforeShow(req.getHoursBeforeShow());
        dto.setRequestedAt(req.getRequestedAt());
        dto.setProcessedAt(req.getProcessedAt());

        if (req.getProcessedByUser() != null) {
            dto.setProcessedByAdminName(req.getProcessedByUser().getFullName());
        }

        return dto;
    }

    // Getters and Setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getRefundReference() {
        return refundReference;
    }

    public void setRefundReference(String refundReference) {
        this.refundReference = refundReference;
    }

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

    public String getCinemaName() {
        return cinemaName;
    }

    public void setCinemaName(String cinemaName) {
        this.cinemaName = cinemaName;
    }

    public String getScreenName() {
        return screenName;
    }

    public void setScreenName(String screenName) {
        this.screenName = screenName;
    }

    public LocalDateTime getShowStartTime() {
        return showStartTime;
    }

    public void setShowStartTime(LocalDateTime showStartTime) {
        this.showStartTime = showStartTime;
    }

    public Long getUserId() {
        return userId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }

    public String getUserEmail() {
        return userEmail;
    }

    public void setUserEmail(String userEmail) {
        this.userEmail = userEmail;
    }

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public String getPolicyName() {
        return policyName;
    }

    public void setPolicyName(String policyName) {
        this.policyName = policyName;
    }

    public BigDecimal getOriginalAmount() {
        return originalAmount;
    }

    public void setOriginalAmount(BigDecimal originalAmount) {
        this.originalAmount = originalAmount;
    }

    public BigDecimal getCancellationFee() {
        return cancellationFee;
    }

    public void setCancellationFee(BigDecimal cancellationFee) {
        this.cancellationFee = cancellationFee;
    }

    public BigDecimal getRefundAmount() {
        return refundAmount;
    }

    public void setRefundAmount(BigDecimal refundAmount) {
        this.refundAmount = refundAmount;
    }

    public BigDecimal getRefundPercentage() {
        return refundPercentage;
    }

    public void setRefundPercentage(BigDecimal refundPercentage) {
        this.refundPercentage = refundPercentage;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getReason() {
        return reason;
    }

    public void setReason(String reason) {
        this.reason = reason;
    }

    public String getRejectionReason() {
        return rejectionReason;
    }

    public void setRejectionReason(String rejectionReason) {
        this.rejectionReason = rejectionReason;
    }

    public String getAdminNotes() {
        return adminNotes;
    }

    public void setAdminNotes(String adminNotes) {
        this.adminNotes = adminNotes;
    }

    public String getGatewayRefundId() {
        return gatewayRefundId;
    }

    public void setGatewayRefundId(String gatewayRefundId) {
        this.gatewayRefundId = gatewayRefundId;
    }

    public BigDecimal getHoursBeforeShow() {
        return hoursBeforeShow;
    }

    public void setHoursBeforeShow(BigDecimal hoursBeforeShow) {
        this.hoursBeforeShow = hoursBeforeShow;
    }

    public LocalDateTime getRequestedAt() {
        return requestedAt;
    }

    public void setRequestedAt(LocalDateTime requestedAt) {
        this.requestedAt = requestedAt;
    }

    public LocalDateTime getProcessedAt() {
        return processedAt;
    }

    public void setProcessedAt(LocalDateTime processedAt) {
        this.processedAt = processedAt;
    }

    public String getProcessedByAdminName() {
        return processedByAdminName;
    }

    public void setProcessedByAdminName(String processedByAdminName) {
        this.processedByAdminName = processedByAdminName;
    }
}
