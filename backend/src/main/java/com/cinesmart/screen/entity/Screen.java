package com.cinesmart.screen.entity;

import com.cinesmart.cinema.entity.Cinema;
import com.cinesmart.cinema.entity.ScreenType;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

@Entity
@Table(name = "screens", uniqueConstraints = {
    @UniqueConstraint(name = "uq_cinema_screen", columnNames = {"cinema_id", "screen_number"})
})
public class Screen {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotNull
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "cinema_id", nullable = false)
    private Cinema cinema;

    @Column(name = "screen_number", nullable = false)
    private int screenNumber;

    @NotNull
    @Enumerated(EnumType.STRING)
    @Column(name = "screen_type", nullable = false, length = 30)
    private ScreenType screenType = ScreenType.STANDARD;

    @Positive
    @Column(name = "total_capacity", nullable = false)
    private int totalCapacity;

    public Screen() {
    }

    public Screen(Cinema cinema, int screenNumber, ScreenType screenType, int totalCapacity) {
        this.cinema = cinema;
        this.screenNumber = screenNumber;
        this.screenType = screenType != null ? screenType : ScreenType.STANDARD;
        this.totalCapacity = totalCapacity;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Cinema getCinema() {
        return cinema;
    }

    public void setCinema(Cinema cinema) {
        this.cinema = cinema;
    }

    public int getScreenNumber() {
        return screenNumber;
    }

    public void setScreenNumber(int screenNumber) {
        this.screenNumber = screenNumber;
    }

    public ScreenType getScreenType() {
        return screenType;
    }

    public void setScreenType(ScreenType screenType) {
        this.screenType = screenType;
    }

    public int getTotalCapacity() {
        return totalCapacity;
    }

    public void setTotalCapacity(int totalCapacity) {
        this.totalCapacity = totalCapacity;
    }
}
