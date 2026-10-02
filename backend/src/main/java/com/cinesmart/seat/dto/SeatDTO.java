package com.cinesmart.seat.dto;

import com.cinesmart.seat.entity.Seat;
import com.cinesmart.seat.entity.SeatTier;

public class SeatDTO {

    private Long id;
    private Long screenId;
    private String rowIdentifier;
    private int columnNumber;
    private SeatTier seatTier;
    private int gridX;
    private int gridY;
    private boolean active;
    private boolean accessible;

    public SeatDTO() {
    }

    public SeatDTO(Long id, Long screenId, String rowIdentifier, int columnNumber, SeatTier seatTier, int gridX, int gridY, boolean active, boolean accessible) {
        this.id = id;
        this.screenId = screenId;
        this.rowIdentifier = rowIdentifier;
        this.columnNumber = columnNumber;
        this.seatTier = seatTier;
        this.gridX = gridX;
        this.gridY = gridY;
        this.active = active;
        this.accessible = accessible;
    }

    public static SeatDTO fromEntity(Seat seat) {
        if (seat == null) return null;
        return new SeatDTO(
                seat.getId(),
                seat.getScreen() != null ? seat.getScreen().getId() : null,
                seat.getRowIdentifier(),
                seat.getColumnNumber(),
                seat.getSeatTier(),
                seat.getGridX(),
                seat.getGridY(),
                seat.isActive(),
                seat.isAccessible()
        );
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getScreenId() {
        return screenId;
    }

    public void setScreenId(Long screenId) {
        this.screenId = screenId;
    }

    public String getRowIdentifier() {
        return rowIdentifier;
    }

    public void setRowIdentifier(String rowIdentifier) {
        this.rowIdentifier = rowIdentifier;
    }

    public int getColumnNumber() {
        return columnNumber;
    }

    public void setColumnNumber(int columnNumber) {
        this.columnNumber = columnNumber;
    }

    public SeatTier getSeatTier() {
        return seatTier;
    }

    public void setSeatTier(SeatTier seatTier) {
        this.seatTier = seatTier;
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

    public boolean isActive() {
        return active;
    }

    public void setActive(boolean active) {
        this.active = active;
    }

    public boolean isAccessible() {
        return accessible;
    }

    public void setAccessible(boolean accessible) {
        this.accessible = accessible;
    }
}
