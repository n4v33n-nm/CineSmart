package com.cinesmart.booking.dto;

import com.cinesmart.booking.entity.Booking;
import com.cinesmart.booking.entity.BookingStatus;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

public class BookingDTO {

    private Long id;
    private String bookingReference;
    private Long userId;
    private String userEmail;
    private String userName;
    private Long showId;
    private String movieTitle;
    private String cinemaName;
    private String screenName;
    private LocalDateTime showStartTime;
    private BigDecimal totalAmount;
    private BookingStatus status;
    private LocalDateTime createdAt;
    private LocalDateTime confirmedAt;
    private List<BookingSeatDTO> seats;

    public BookingDTO() {
    }

    public static BookingDTO fromEntity(Booking booking) {
        if (booking == null) return null;
        BookingDTO dto = new BookingDTO();
        dto.setId(booking.getId());
        dto.setBookingReference(booking.getBookingReference());
        if (booking.getUser() != null) {
            dto.setUserId(booking.getUser().getId());
            dto.setUserEmail(booking.getUser().getEmail());
            dto.setUserName(booking.getUser().getFullName());
        }
        if (booking.getShow() != null) {
            dto.setShowId(booking.getShow().getId());
            dto.setShowStartTime(booking.getShow().getStartTime());
            if (booking.getShow().getMovie() != null) {
                dto.setMovieTitle(booking.getShow().getMovie().getTitle());
            }
            if (booking.getShow().getScreen() != null) {
                dto.setScreenName("Screen " + booking.getShow().getScreen().getScreenNumber());
                if (booking.getShow().getScreen().getCinema() != null) {
                    dto.setCinemaName(booking.getShow().getScreen().getCinema().getName());
                }
            }
        }
        dto.setTotalAmount(booking.getTotalAmount());
        dto.setStatus(booking.getStatus());
        dto.setCreatedAt(booking.getCreatedAt());
        dto.setConfirmedAt(booking.getConfirmedAt());

        if (booking.getBookingSeats() != null) {
            dto.setSeats(booking.getBookingSeats().stream()
                    .map(BookingSeatDTO::fromEntity)
                    .collect(Collectors.toList()));
        } else {
            dto.setSeats(Collections.emptyList());
        }

        return dto;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getBookingReference() {
        return bookingReference;
    }

    public void setBookingReference(String bookingReference) {
        this.bookingReference = bookingReference;
    }

    public Long getUserId() {
        return userId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }

    public String getUserEmail() {
        return userEmail;
    }

    public void setUserEmail(String userEmail) {
        this.userEmail = userEmail;
    }

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
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

    public LocalDateTime getShowStartTime() {
        return showStartTime;
    }

    public void setShowStartTime(LocalDateTime showStartTime) {
        this.showStartTime = showStartTime;
    }

    public BigDecimal getTotalAmount() {
        return totalAmount;
    }

    public void setTotalAmount(BigDecimal totalAmount) {
        this.totalAmount = totalAmount;
    }

    public BookingStatus getStatus() {
        return status;
    }

    public void setStatus(BookingStatus status) {
        this.status = status;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public LocalDateTime getConfirmedAt() {
        return confirmedAt;
    }

    public void setConfirmedAt(LocalDateTime confirmedAt) {
        this.confirmedAt = confirmedAt;
    }

    public List<BookingSeatDTO> getSeats() {
        return seats;
    }

    public void setSeats(List<BookingSeatDTO> seats) {
        this.seats = seats;
    }
}
