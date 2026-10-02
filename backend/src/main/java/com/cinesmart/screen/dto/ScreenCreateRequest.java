package com.cinesmart.screen.dto;

import com.cinesmart.cinema.entity.ScreenType;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

public class ScreenCreateRequest {

    @NotNull(message = "Cinema ID is required")
    private Long cinemaId;

    @Positive(message = "Screen number must be positive")
    private int screenNumber;

    @NotNull(message = "Screen type is required")
    private ScreenType screenType = ScreenType.STANDARD;

    @Positive(message = "Total capacity must be positive")
    private int totalCapacity;

    public ScreenCreateRequest() {
    }

    public ScreenCreateRequest(Long cinemaId, int screenNumber, ScreenType screenType, int totalCapacity) {
        this.cinemaId = cinemaId;
        this.screenNumber = screenNumber;
        this.screenType = screenType;
        this.totalCapacity = totalCapacity;
    }

    public Long getCinemaId() {
        return cinemaId;
    }

    public void setCinemaId(Long cinemaId) {
        this.cinemaId = cinemaId;
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
