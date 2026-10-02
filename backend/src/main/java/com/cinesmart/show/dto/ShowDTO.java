package com.cinesmart.show.dto;

import com.cinesmart.show.entity.Show;
import com.cinesmart.show.entity.ShowStatus;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public class ShowDTO {

    private Long id;
    private Long movieId;
    private String movieTitle;
    private String moviePosterUrl;
    private Long cinemaId;
    private String cinemaName;
    private Long screenId;
    private int screenNumber;
    private String screenType;
    private LocalDateTime startTime;
    private LocalDateTime endTime;
    private BigDecimal basePrice;
    private ShowStatus status;
    private int availableSeatCount;

    public ShowDTO() {
    }

    public static ShowDTO fromEntity(Show show, int availableSeatCount) {
        if (show == null) return null;
        ShowDTO dto = new ShowDTO();
        dto.setId(show.getId());
        if (show.getMovie() != null) {
            dto.setMovieId(show.getMovie().getId());
            dto.setMovieTitle(show.getMovie().getTitle());
            dto.setMoviePosterUrl(show.getMovie().getPosterUrl());
        }
        if (show.getScreen() != null) {
            dto.setScreenId(show.getScreen().getId());
            dto.setScreenNumber(show.getScreen().getScreenNumber());
            dto.setScreenType(show.getScreen().getScreenType().name());
            if (show.getScreen().getCinema() != null) {
                dto.setCinemaId(show.getScreen().getCinema().getId());
                dto.setCinemaName(show.getScreen().getCinema().getName());
            }
        }
        dto.setStartTime(show.getStartTime());
        dto.setEndTime(show.getEndTime());
        dto.setBasePrice(show.getBasePrice());
        dto.setStatus(show.getStatus());
        dto.setAvailableSeatCount(availableSeatCount);
        return dto;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getMovieId() {
        return movieId;
    }

    public void setMovieId(Long movieId) {
        this.movieId = movieId;
    }

    public String getMovieTitle() {
        return movieTitle;
    }

    public void setMovieTitle(String movieTitle) {
        this.movieTitle = movieTitle;
    }

    public String getMoviePosterUrl() {
        return moviePosterUrl;
    }

    public void setMoviePosterUrl(String moviePosterUrl) {
        this.moviePosterUrl = moviePosterUrl;
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

    public Long getScreenId() {
        return screenId;
    }

    public void setScreenId(Long screenId) {
        this.screenId = screenId;
    }

    public int getScreenNumber() {
        return screenNumber;
    }

    public void setScreenNumber(int screenNumber) {
        this.screenNumber = screenNumber;
    }

    public String getScreenType() {
        return screenType;
    }

    public void setScreenType(String screenType) {
        this.screenType = screenType;
    }

    public LocalDateTime getStartTime() {
        return startTime;
    }

    public void setStartTime(LocalDateTime startTime) {
        this.startTime = startTime;
    }

    public LocalDateTime getEndTime() {
        return endTime;
    }

    public void setEndTime(LocalDateTime endTime) {
        this.endTime = endTime;
    }

    public BigDecimal getBasePrice() {
        return basePrice;
    }

    public void setBasePrice(BigDecimal basePrice) {
        this.basePrice = basePrice;
    }

    public ShowStatus getStatus() {
        return status;
    }

    public void setStatus(ShowStatus status) {
        this.status = status;
    }

    public int getAvailableSeatCount() {
        return availableSeatCount;
    }

    public void setAvailableSeatCount(int availableSeatCount) {
        this.availableSeatCount = availableSeatCount;
    }
}
