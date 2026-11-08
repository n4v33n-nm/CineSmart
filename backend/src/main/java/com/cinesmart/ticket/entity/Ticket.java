package com.cinesmart.ticket.entity;

import com.cinesmart.booking.entity.Booking;
import com.cinesmart.booking.entity.BookingSeat;
import com.cinesmart.user.entity.User;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.time.LocalDateTime;

@Entity
@Table(name = "tickets", indexes = {
    @Index(name = "idx_tickets_booking", columnList = "booking_id"),
    @Index(name = "idx_tickets_code", columnList = "ticket_code")
})
public class Ticket {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotNull
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "booking_id", nullable = false)
    private Booking booking;

    @NotNull
    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "booking_seat_id", nullable = false, unique = true)
    private BookingSeat bookingSeat;

    @NotBlank
    @Column(name = "ticket_code", nullable = false, unique = true, length = 32)
    private String ticketCode;

    @NotBlank
    @Column(name = "qr_verification_hash", nullable = false, length = 255)
    private String qrVerificationHash;

    @NotNull
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private TicketStatus status = TicketStatus.ISSUED;

    @Column(name = "issued_at", nullable = false, updatable = false)
    private LocalDateTime issuedAt = LocalDateTime.now();

    @Column(name = "validated_at")
    private LocalDateTime validatedAt;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "validated_by")
    private User validatedBy;

    public Ticket() {
    }

    public Ticket(Booking booking, BookingSeat bookingSeat, String ticketCode, String qrVerificationHash) {
        this.booking = booking;
        this.bookingSeat = bookingSeat;
        this.ticketCode = ticketCode;
        this.qrVerificationHash = qrVerificationHash;
        this.status = TicketStatus.ISSUED;
        this.issuedAt = LocalDateTime.now();
    }

    public void voidTicket() {
        this.status = TicketStatus.VOIDED;
    }

    public void cancelTicket() {
        this.status = TicketStatus.CANCELLED;
    }

    public void validateTicket(User validator) {
        this.status = TicketStatus.USED;
        this.validatedAt = LocalDateTime.now();
        this.validatedBy = validator;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Booking getBooking() {
        return booking;
    }

    public void setBooking(Booking booking) {
        this.booking = booking;
    }

    public BookingSeat getBookingSeat() {
        return bookingSeat;
    }

    public void setBookingSeat(BookingSeat bookingSeat) {
        this.bookingSeat = bookingSeat;
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

    public LocalDateTime getIssuedAt() {
        return issuedAt;
    }

    public void setIssuedAt(LocalDateTime issuedAt) {
        this.issuedAt = issuedAt;
    }

    public LocalDateTime getValidatedAt() {
        return validatedAt;
    }

    public void setValidatedAt(LocalDateTime validatedAt) {
        this.validatedAt = validatedAt;
    }

    public User getValidatedBy() {
        return validatedBy;
    }

    public void setValidatedBy(User validatedBy) {
        this.validatedBy = validatedBy;
    }
}
