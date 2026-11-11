package com.cinesmart.refund.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "cancellation_policies", indexes = {
    @Index(name = "idx_cancellation_policies_active", columnList = "is_active, display_order")
})
public class CancellationPolicy {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank
    @Column(name = "policy_name", nullable = false, unique = true, length = 100)
    private String policyName;

    @Column(length = 255)
    private String description;

    @NotNull
    @Min(0)
    @Column(name = "min_hours_before_show", nullable = false)
    private Integer minHoursBeforeShow = 0;

    @Column(name = "max_hours_before_show")
    private Integer maxHoursBeforeShow; // null means unbounded

    @NotNull
    @DecimalMin("0.00")
    @DecimalMax("100.00")
    @Column(name = "refund_percentage", nullable = false, precision = 5, scale = 2)
    private BigDecimal refundPercentage = new BigDecimal("100.00");

    @NotNull
    @DecimalMin("0.00")
    @DecimalMax("100.00")
    @Column(name = "cancellation_fee_percent", nullable = false, precision = 5, scale = 2)
    private BigDecimal cancellationFeePercent = BigDecimal.ZERO;

    @Column(name = "is_active", nullable = false)
    private Boolean isActive = true;

    @Column(name = "display_order", nullable = false)
    private Integer displayOrder = 0;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt = LocalDateTime.now();

    @Column(name = "updated_at")
    private LocalDateTime updatedAt = LocalDateTime.now();

    public CancellationPolicy() {
    }

    public CancellationPolicy(String policyName, String description, Integer minHoursBeforeShow,
                              Integer maxHoursBeforeShow, BigDecimal refundPercentage,
                              BigDecimal cancellationFeePercent, Integer displayOrder) {
        this.policyName = policyName;
        this.description = description;
        this.minHoursBeforeShow = minHoursBeforeShow != null ? minHoursBeforeShow : 0;
        this.maxHoursBeforeShow = maxHoursBeforeShow;
        this.refundPercentage = refundPercentage != null ? refundPercentage : BigDecimal.ZERO;
        this.cancellationFeePercent = cancellationFeePercent != null ? cancellationFeePercent : BigDecimal.ZERO;
        this.displayOrder = displayOrder != null ? displayOrder : 0;
        this.isActive = true;
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
    }

    @PreUpdate
    public void onUpdate() {
        this.updatedAt = LocalDateTime.now();
    }

    public boolean matches(double hoursBeforeShow) {
        if (!Boolean.TRUE.equals(isActive)) {
            return false;
        }
        if (hoursBeforeShow < minHoursBeforeShow) {
            return false;
        }
        if (maxHoursBeforeShow != null && hoursBeforeShow >= maxHoursBeforeShow) {
            return false;
        }
        return true;
    }

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

