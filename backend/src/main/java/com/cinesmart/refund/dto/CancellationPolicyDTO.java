package com.cinesmart.refund.dto;

import com.cinesmart.refund.entity.CancellationPolicy;
import java.math.BigDecimal;
import java.time.LocalDateTime;

public class CancellationPolicyDTO {

    private Long id;
    private String policyName;
    private String description;
    private Integer minHoursBeforeShow;
    private Integer maxHoursBeforeShow;
    private BigDecimal refundPercentage;
    private BigDecimal cancellationFeePercent;
    private Boolean isActive;
    private Integer displayOrder;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public CancellationPolicyDTO() {
    }

    public static CancellationPolicyDTO fromEntity(CancellationPolicy policy) {
        if (policy == null) return null;
        CancellationPolicyDTO dto = new CancellationPolicyDTO();
        dto.setId(policy.getId());
        dto.setPolicyName(policy.getPolicyName());
        dto.setDescription(policy.getDescription());
        dto.setMinHoursBeforeShow(policy.getMinHoursBeforeShow());
        dto.setMaxHoursBeforeShow(policy.getMaxHoursBeforeShow());
        dto.setRefundPercentage(policy.getRefundPercentage());
        dto.setCancellationFeePercent(policy.getCancellationFeePercent());
        dto.setIsActive(policy.getIsActive());
        dto.setDisplayOrder(policy.getDisplayOrder());
        dto.setCreatedAt(policy.getCreatedAt());
        dto.setUpdatedAt(policy.getUpdatedAt());
        return dto;
    }

    // Getters and Setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
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

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }
}

