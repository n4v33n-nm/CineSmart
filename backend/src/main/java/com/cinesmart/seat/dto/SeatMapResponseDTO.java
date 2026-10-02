package com.cinesmart.seat.dto;

import java.util.List;

public class SeatMapResponseDTO {

    private Long showId;
    private String movieTitle;
    private String cinemaName;
    private String screenName;
    private int totalSeats;
    private int availableSeats;
    private List<ShowSeatDTO> seats;

    public SeatMapResponseDTO() {
    }

    public SeatMapResponseDTO(Long showId, String movieTitle, String cinemaName, String screenName, int totalSeats, int availableSeats, List<ShowSeatDTO> seats) {
        this.showId = showId;
        this.movieTitle = movieTitle;
        this.cinemaName = cinemaName;
        this.screenName = screenName;
        this.totalSeats = totalSeats;
        this.availableSeats = availableSeats;
        this.seats = seats;
    }

    public Long getShowId() {
        return showId;
    }

    public void setShowId(Long showId) {
        this.showId = showId;
    }

    public String getMovieTitle() {
        return movieTitle;
    }

    public void setMovieTitle(String movieTitle) {
        this.movieTitle = movieTitle;
    }

    public String getCinemaName() {
        return cinemaName;
    }

    public void setCinemaName(String cinemaName) {
        this.cinemaName = cinemaName;
    }

    public String getScreenName() {
        return screenName;
    }

    public void setScreenName(String screenName) {
        this.screenName = screenName;
    }

    public int getTotalSeats() {
        return totalSeats;
    }

    public void setTotalSeats(int totalSeats) {
        this.totalSeats = totalSeats;
    }

    public int getAvailableSeats() {
        return availableSeats;
    }

    public void setAvailableSeats(int availableSeats) {
        this.availableSeats = availableSeats;
    }

    public List<ShowSeatDTO> getSeats() {
        return seats;
    }

    public void setSeats(List<ShowSeatDTO> seats) {
        this.seats = seats;
    }
}
