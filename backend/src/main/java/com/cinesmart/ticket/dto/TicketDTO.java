package com.cinesmart.ticket.dto;

import com.cinesmart.ticket.entity.Ticket;
import com.cinesmart.ticket.entity.TicketStatus;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public class TicketDTO {

    private Long id;
    private Long bookingId;
    private String bookingReference;
    private String ticketCode;
    private String qrVerificationHash;
    private TicketStatus status;
    private String movieTitle;
    private String cinemaName;
    private String screenName;
    private LocalDateTime showStartTime;
    private String seatLabel;
    private String seatTier;
    private BigDecimal seatPrice;
    private BigDecimal totalAmount;
    private String bookingStatus;
    private String paymentStatus;
    private String customerName;
    private String customerEmail;
    private LocalDateTime issuedAt;

    public TicketDTO() {
    }

    public static TicketDTO fromEntity(Ticket ticket) {
        if (ticket == null) return null;
        TicketDTO dto = new TicketDTO();
        dto.setId(ticket.getId());
        dto.setTicketCode(ticket.getTicketCode());
        dto.setQrVerificationHash(ticket.getQrVerificationHash());
        dto.setStatus(ticket.getStatus());
        dto.setIssuedAt(ticket.getIssuedAt());

        if (ticket.getBooking() != null) {
            dto.setBookingId(ticket.getBooking().getId());
            dto.setBookingReference(ticket.getBooking().getBookingReference());
            dto.setTotalAmount(ticket.getBooking().getTotalAmount());
            dto.setBookingStatus(ticket.getBooking().getStatus() != null ? ticket.getBooking().getStatus().name() : null);

            if (ticket.getBooking().getUser() != null) {
                dto.setCustomerName(ticket.getBooking().getUser().getFullName());
                dto.setCustomerEmail(ticket.getBooking().getUser().getEmail());
            }

            if (ticket.getBooking().getShow() != null) {
                dto.setShowStartTime(ticket.getBooking().getShow().getStartTime());
                if (ticket.getBooking().getShow().getMovie() != null) {
                    dto.setMovieTitle(ticket.getBooking().getShow().getMovie().getTitle());
                }
                if (ticket.getBooking().getShow().getScreen() != null) {
                    dto.setScreenName("Screen " + ticket.getBooking().getShow().getScreen().getScreenNumber());
                    if (ticket.getBooking().getShow().getScreen().getCinema() != null) {
                        dto.setCinemaName(ticket.getBooking().getShow().getScreen().getCinema().getName());
                    }
                }
            }
        }

        if (ticket.getBookingSeat() != null) {
            dto.setSeatLabel(ticket.getBookingSeat().getSeatLabel());
            dto.setSeatTier(ticket.getBookingSeat().getTierSnapshot());
            dto.setSeatPrice(ticket.getBookingSeat().getSnapshotPrice());
        }

        return dto;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getBookingId() {
        return bookingId;
    }

    public void setBookingId(Long bookingId) {
        this.bookingId = bookingId;
    }

    public String getBookingReference() {
        return bookingReference;
    }

    public void setBookingReference(String bookingReference) {
        this.bookingReference = bookingReference;
    }

    public String getTicketCode() {
        return ticketCode;
    }

    public void setTicketCode(String ticketCode) {
        this.ticketCode = ticketCode;
    }

    public String getQrVerificationHash() {
        return qrVerificationHash;
    }

    public void setQrVerificationHash(String qrVerificationHash) {
        this.qrVerificationHash = qrVerificationHash;
    }

    public TicketStatus getStatus() {
        return status;
    }

    public void setStatus(TicketStatus status) {
        this.status = status;
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

    public String getSeatLabel() {
        return seatLabel;
    }

    public void setSeatLabel(String seatLabel) {
        this.seatLabel = seatLabel;
    }

    public String getSeatTier() {
        return seatTier;
    }

    public void setSeatTier(String seatTier) {
        this.seatTier = seatTier;
    }

    public BigDecimal getSeatPrice() {
        return seatPrice;
    }

    public void setSeatPrice(BigDecimal seatPrice) {
        this.seatPrice = seatPrice;
    }

    public BigDecimal getTotalAmount() {
        return totalAmount;
    }

    public void setTotalAmount(BigDecimal totalAmount) {
        this.totalAmount = totalAmount;
    }

    public String getBookingStatus() {
        return bookingStatus;
    }

    public void setBookingStatus(String bookingStatus) {
        this.bookingStatus = bookingStatus;
    }

    public String getPaymentStatus() {
        return paymentStatus;
    }

    public void setPaymentStatus(String paymentStatus) {
        this.paymentStatus = paymentStatus;
    }

    public String getCustomerName() {
        return customerName;
    }

    public void setCustomerName(String customerName) {
        this.customerName = customerName;
    }

    public String getCustomerEmail() {
        return customerEmail;
    }

    public void setCustomerEmail(String customerEmail) {
        this.customerEmail = customerEmail;
    }

    public LocalDateTime getIssuedAt() {
        return issuedAt;
    }

    public void setIssuedAt(LocalDateTime issuedAt) {
        this.issuedAt = issuedAt;
    }
}
