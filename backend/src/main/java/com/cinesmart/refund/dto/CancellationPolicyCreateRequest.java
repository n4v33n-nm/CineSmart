package com.cinesmart.refund.dto;

import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal;

public class CancellationPolicyCreateRequest {

    @NotBlank(message = "Policy name is required")
    private String policyName;

    private String description;

    @NotNull(message = "Minimum hours before show is required")
    @Min(value = 0, message = "Minimum hours must be zero or positive")
    private Integer minHoursBeforeShow;

    private Integer maxHoursBeforeShow;

    @NotNull(message = "Refund percentage is required")
    @DecimalMin(value = "0.00", message = "Refund percentage cannot be negative")
    @DecimalMax(value = "100.00", message = "Refund percentage cannot exceed 100%")
    private BigDecimal refundPercentage;

    @NotNull(message = "Cancellation fee percentage is required")
    @DecimalMin(value = "0.00", message = "Cancellation fee cannot be negative")
    @DecimalMax(value = "100.00", message = "Cancellation fee cannot exceed 100%")
    private BigDecimal cancellationFeePercent;

    private Boolean isActive = true;

    private Integer displayOrder = 0;

    public CancellationPolicyCreateRequest() {
    }

    public String getPolicyName() {
        return policyName;
    }

    public void setPolicyName(String policyName) {
        this.policyName = policyName;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public Integer getMinHoursBeforeShow() {
        return minHoursBeforeShow;
    }

    public void setMinHoursBeforeShow(Integer minHoursBeforeShow) {
        this.minHoursBeforeShow = minHoursBeforeShow;
    }

    public Integer getMaxHoursBeforeShow() {
        return maxHoursBeforeShow;
    }

    public void setMaxHoursBeforeShow(Integer maxHoursBeforeShow) {
        this.maxHoursBeforeShow = maxHoursBeforeShow;
    }

    public BigDecimal getRefundPercentage() {
        return refundPercentage;
    }

    public void setRefundPercentage(BigDecimal refundPercentage) {
        this.refundPercentage = refundPercentage;
    }

    public BigDecimal getCancellationFeePercent() {
        return cancellationFeePercent;
    }

    public void setCancellationFeePercent(BigDecimal cancellationFeePercent) {
        this.cancellationFeePercent = cancellationFeePercent;
    }

    public Boolean getIsActive() {
        return isActive;
    }

    public void setIsActive(Boolean active) {
        isActive = active;
    }

    public Integer getDisplayOrder() {
        return displayOrder;
    }

    public void setDisplayOrder(Integer displayOrder) {
        this.displayOrder = displayOrder;
    }
}

