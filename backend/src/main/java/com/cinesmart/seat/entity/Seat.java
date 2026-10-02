package com.cinesmart.seat.entity;

import com.cinesmart.screen.entity.Screen;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

@Entity
@Table(name = "seats", uniqueConstraints = {
    @UniqueConstraint(name = "uq_screen_seat", columnNames = {"screen_id", "row_identifier", "column_number"})
})
public class Seat {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotNull
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "screen_id", nullable = false)
    private Screen screen;

    @NotBlank
    @Column(name = "row_identifier", nullable = false, length = 5)
    private String rowIdentifier;

    @Column(name = "column_number", nullable = false)
    private int columnNumber;

    @NotNull
    @Enumerated(EnumType.STRING)
    @Column(name = "seat_tier", nullable = false, length = 30)
    private SeatTier seatTier = SeatTier.STANDARD;

    @Column(name = "grid_x", nullable = false)
    private int gridX;

    @Column(name = "grid_y", nullable = false)
    private int gridY;

    @Column(name = "is_active", nullable = false)
    private boolean isActive = true;

    public Seat() {
    }

    public Seat(Screen screen, String rowIdentifier, int columnNumber, SeatTier seatTier, int gridX, int gridY) {
        this.screen = screen;
        this.rowIdentifier = rowIdentifier;
        this.columnNumber = columnNumber;
        this.seatTier = seatTier != null ? seatTier : SeatTier.STANDARD;
        this.gridX = gridX;
        this.gridY = gridY;
        this.isActive = true;
    }

    public boolean isAccessible() {
        return this.seatTier == SeatTier.ACCESSIBLE_WHEELCHAIR || this.seatTier == SeatTier.ACCESSIBLE_COMPANION;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Screen getScreen() {
        return screen;
    }

    public void setScreen(Screen screen) {
        this.screen = screen;
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
        return isActive;
    }

    public void setActive(boolean active) {
        isActive = active;
    }
}
