package com.cinesmart.seat.dto;

import com.cinesmart.seat.entity.SeatTier;
import com.cinesmart.show.entity.ShowSeat;
import com.cinesmart.show.entity.ShowSeatStatus;

import java.math.BigDecimal;

public class ShowSeatDTO {

    private Long showSeatId;
    private Long seatId;
    private String row;
    private int number;
    private SeatTier tier;
    private BigDecimal price;
    private int gridX;
    private int gridY;
    private ShowSeatStatus status;
    private boolean accessible;
    private String label;

    public ShowSeatDTO() {
    }

    public ShowSeatDTO(Long showSeatId, Long seatId, String row, int number, SeatTier tier, BigDecimal price, int gridX, int gridY, ShowSeatStatus status, boolean accessible) {
        this.showSeatId = showSeatId;
        this.seatId = seatId;
        this.row = row;
        this.number = number;
        this.tier = tier;
        this.price = price;
        this.gridX = gridX;
        this.gridY = gridY;
        this.status = status;
        this.accessible = accessible;
        this.label = String.format("Row %s Seat %d", row, number);
    }

    public static ShowSeatDTO fromEntity(ShowSeat showSeat) {
        if (showSeat == null) return null;
        return new ShowSeatDTO(
                showSeat.getId(),
                showSeat.getSeat() != null ? showSeat.getSeat().getId() : null,
                showSeat.getSeat() != null ? showSeat.getSeat().getRowIdentifier() : "",
                showSeat.getSeat() != null ? showSeat.getSeat().getColumnNumber() : 0,
                showSeat.getSeat() != null ? showSeat.getSeat().getSeatTier() : SeatTier.STANDARD,
                showSeat.getPrice(),
                showSeat.getSeat() != null ? showSeat.getSeat().getGridX() : 0,
                showSeat.getSeat() != null ? showSeat.getSeat().getGridY() : 0,
                showSeat.getStatus(),
                showSeat.getSeat() != null && showSeat.getSeat().isAccessible()
        );
    }

    public Long getShowSeatId() {
        return showSeatId;
    }

    public void setShowSeatId(Long showSeatId) {
        this.showSeatId = showSeatId;
    }

    public Long getSeatId() {
        return seatId;
    }

    public void setSeatId(Long seatId) {
        this.seatId = seatId;
    }

    public String getRow() {
        return row;
    }

    public void setRow(String row) {
        this.row = row;
    }

    public int getNumber() {
        return number;
    }

    public void setNumber(int number) {
        this.number = number;
    }

    public SeatTier getTier() {
        return tier;
    }

    public void setTier(SeatTier tier) {
        this.tier = tier;
    }

    public BigDecimal getPrice() {
        return price;
    }

    public void setPrice(BigDecimal price) {
        this.price = price;
    }

    public int getGridX() {
        return gridX;
    }

    public void setGridX(int gridX) {
        this.gridX = gridX;
    }

    public int getGridY() {
        return gridY;
    }

    public void setGridY(int gridY) {
        this.gridY = gridY;
    }

    public ShowSeatStatus getStatus() {
        return status;
    }

    public void setStatus(ShowSeatStatus status) {
        this.status = status;
    }

    public boolean isAccessible() {
        return accessible;
    }

    public void setAccessible(boolean accessible) {
        this.accessible = accessible;
    }

    public String getLabel() {
        return label;
    }

    public void setLabel(String label) {
        this.label = label;
    }
}
