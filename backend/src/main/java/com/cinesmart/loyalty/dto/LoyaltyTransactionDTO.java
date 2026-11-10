package com.cinesmart.loyalty.dto;

import com.cinesmart.loyalty.entity.LoyaltyTransaction;
import com.cinesmart.loyalty.entity.LoyaltyTransactionType;

import java.time.LocalDateTime;

public class LoyaltyTransactionDTO {

    private Long id;
    private Integer points;
    private LoyaltyTransactionType transactionType;
    private String description;
    private String bookingReference;
    private LocalDateTime createdAt;

    public LoyaltyTransactionDTO() {
    }

    public static LoyaltyTransactionDTO fromEntity(LoyaltyTransaction txn) {
        if (txn == null) return null;
        LoyaltyTransactionDTO dto = new LoyaltyTransactionDTO();
        dto.setId(txn.getId());
        dto.setPoints(txn.getPoints());
        dto.setTransactionType(txn.getTransactionType());
        dto.setDescription(txn.getDescription());
        if (txn.getBooking() != null) {
            dto.setBookingReference(txn.getBooking().getBookingReference());
        }
        dto.setCreatedAt(txn.getCreatedAt());
        return dto;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Integer getPoints() {
        return points;
    }

    public void setPoints(Integer points) {
        this.points = points;
    }

    public LoyaltyTransactionType getTransactionType() {
        return transactionType;
    }

    public void setTransactionType(LoyaltyTransactionType transactionType) {
        this.transactionType = transactionType;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getBookingReference() {
        return bookingReference;
    }

    public void setBookingReference(String bookingReference) {
        this.bookingReference = bookingReference;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }
}

