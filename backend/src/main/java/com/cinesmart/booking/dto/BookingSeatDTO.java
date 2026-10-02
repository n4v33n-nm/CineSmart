package com.cinesmart.booking.dto;

import com.cinesmart.booking.entity.BookingSeat;

import java.math.BigDecimal;

public class BookingSeatDTO {

    private Long id;
    private Long showSeatId;
    private BigDecimal snapshotPrice;
    private String seatLabel;
    private String tierSnapshot;

    public BookingSeatDTO() {
    }

    public BookingSeatDTO(Long id, Long showSeatId, BigDecimal snapshotPrice, String seatLabel, String tierSnapshot) {
        this.id = id;
        this.showSeatId = showSeatId;
        this.snapshotPrice = snapshotPrice;
        this.seatLabel = seatLabel;
        this.tierSnapshot = tierSnapshot;
    }

    public static BookingSeatDTO fromEntity(BookingSeat seat) {
        if (seat == null) return null;
        return new BookingSeatDTO(
                seat.getId(),
                seat.getShowSeat() != null ? seat.getShowSeat().getId() : null,
                seat.getSnapshotPrice(),
                seat.getSeatLabel(),
                seat.getTierSnapshot()
        );
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getShowSeatId() {
        return showSeatId;
    }

    public void setShowSeatId(Long showSeatId) {
        this.showSeatId = showSeatId;
    }

    public BigDecimal getSnapshotPrice() {
        return snapshotPrice;
    }

    public void setSnapshotPrice(BigDecimal snapshotPrice) {
        this.snapshotPrice = snapshotPrice;
    }

    public String getSeatLabel() {
        return seatLabel;
    }

    public void setSeatLabel(String seatLabel) {
        this.seatLabel = seatLabel;
    }

    public String getTierSnapshot() {
        return tierSnapshot;
    }

    public void setTierSnapshot(String tierSnapshot) {
        this.tierSnapshot = tierSnapshot;
    }
}
