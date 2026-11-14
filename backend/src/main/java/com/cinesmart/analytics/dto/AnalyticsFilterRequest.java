package com.cinesmart.analytics.dto;

import com.cinesmart.booking.entity.BookingStatus;
import org.springframework.format.annotation.DateTimeFormat;

import java.time.LocalDate;

public class AnalyticsFilterRequest {

    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
    private LocalDate startDate;

    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
    private LocalDate endDate;

    private Long movieId;
    private Long cinemaId;
    private Long showId;
    private BookingStatus status;

    public AnalyticsFilterRequest() {
    }

    public AnalyticsFilterRequest(LocalDate startDate, LocalDate endDate, Long movieId, Long cinemaId, Long showId, BookingStatus status) {
        this.startDate = startDate;
        this.endDate = endDate;
        this.movieId = movieId;
        this.cinemaId = cinemaId;
        this.showId = showId;
        this.status = status;
    }

    public LocalDate getStartDate() {
        return startDate;
    }

    public void setStartDate(LocalDate startDate) {
        this.startDate = startDate;
    }

    public LocalDate getEndDate() {
        return endDate;
    }

    public void setEndDate(LocalDate endDate) {
        this.endDate = endDate;
    }

    public Long getMovieId() {
        return movieId;
    }

    public void setMovieId(Long movieId) {
        this.movieId = movieId;
    }

    public Long getCinemaId() {
        return cinemaId;
    }

    public void setCinemaId(Long cinemaId) {
        this.cinemaId = cinemaId;
    }

    public Long getShowId() {
        return showId;
    }

    public void setShowId(Long showId) {
        this.showId = showId;
    }

    public BookingStatus getStatus() {
        return status;
    }

    public void setStatus(BookingStatus status) {
        this.status = status;
    }
}
