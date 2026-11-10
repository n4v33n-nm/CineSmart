package com.cinesmart.seat.dto;

import java.time.LocalDateTime;
import java.util.List;

public class SeatLockResponseDTO {

    private Long showId;
    private List<Long> lockedSeatIds;
    private LocalDateTime expiresAt;
    private String status;
    private String message;

    public SeatLockResponseDTO() {}

    public SeatLockResponseDTO(Long showId, List<Long> lockedSeatIds, LocalDateTime expiresAt, String status, String message) {
        this.showId = showId;
        this.lockedSeatIds = lockedSeatIds;
        this.expiresAt = expiresAt;
        this.status = status;
        this.message = message;
    }

    public Long getShowId() {
        return showId;
    }

    public void setShowId(Long showId) {
        this.showId = showId;
    }

    public List<Long> getLockedSeatIds() {
        return lockedSeatIds;
    }

    public void setLockedSeatIds(List<Long> lockedSeatIds) {
        this.lockedSeatIds = lockedSeatIds;
    }

    public LocalDateTime getExpiresAt() {
        return expiresAt;
    }

    public void setExpiresAt(LocalDateTime expiresAt) {
        this.expiresAt = expiresAt;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }
}

