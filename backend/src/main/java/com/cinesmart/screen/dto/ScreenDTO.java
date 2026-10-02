package com.cinesmart.screen.dto;

import com.cinesmart.cinema.entity.ScreenType;
import com.cinesmart.screen.entity.Screen;

public class ScreenDTO {

    private Long id;
    private Long cinemaId;
    private String cinemaName;
    private int screenNumber;
    private ScreenType screenType;
    private int totalCapacity;

    public ScreenDTO() {
    }

    public ScreenDTO(Long id, Long cinemaId, String cinemaName, int screenNumber, ScreenType screenType, int totalCapacity) {
        this.id = id;
        this.cinemaId = cinemaId;
        this.cinemaName = cinemaName;
        this.screenNumber = screenNumber;
        this.screenType = screenType;
        this.totalCapacity = totalCapacity;
    }

    public static ScreenDTO fromEntity(Screen screen) {
        if (screen == null) return null;
        return new ScreenDTO(
                screen.getId(),
                screen.getCinema() != null ? screen.getCinema().getId() : null,
                screen.getCinema() != null ? screen.getCinema().getName() : null,
                screen.getScreenNumber(),
                screen.getScreenType(),
                screen.getTotalCapacity()
        );
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getCinemaId() {
        return cinemaId;
    }

    public void setCinemaId(Long cinemaId) {
        this.cinemaId = cinemaId;
    }

    public String getCinemaName() {
        return cinemaName;
    }

    public void setCinemaName(String cinemaName) {
        this.cinemaName = cinemaName;
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
