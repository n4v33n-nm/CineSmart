package com.cinesmart.show.entity;

import com.cinesmart.seat.entity.Seat;
import com.cinesmart.user.entity.User;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "show_seats", uniqueConstraints = {
    @UniqueConstraint(name = "uq_show_seat", columnNames = {"show_id", "seat_id"})
}, indexes = {
    @Index(name = "idx_show_seats_lookup", columnList = "show_id, status"),
    @Index(name = "idx_show_seats_lock_lookup", columnList = "status, locked_until")
})
public class ShowSeat {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotNull
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "show_id", nullable = false)
    private Show show;

    @NotNull
    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "seat_id", nullable = false)
    private Seat seat;

    @NotNull
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private ShowSeatStatus status = ShowSeatStatus.AVAILABLE;

    @NotNull
    @PositiveOrZero
    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal price;

    @Column(name = "locked_until")
    private LocalDateTime lockedUntil;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "locked_by_id")
    private User lockedBy;

    @Version
    @Column(nullable = false)
    private Long version = 0L;

    public ShowSeat() {
    }

    public ShowSeat(Show show, Seat seat, BigDecimal price) {
        this.show = show;
        this.seat = seat;
        this.price = price;
        this.status = ShowSeatStatus.AVAILABLE;
        this.version = 0L;
    }

    public void lock(User user, LocalDateTime until) {
        if (this.status != ShowSeatStatus.AVAILABLE) {
            throw new IllegalStateException("Cannot lock seat: current status is " + this.status);
        }
        this.status = ShowSeatStatus.LOCKED;
        this.lockedBy = user;
        this.lockedUntil = until;
    }

    public void hold() {
        if (this.status != ShowSeatStatus.AVAILABLE) {
            throw new IllegalStateException("Cannot hold seat: current status is " + this.status);
        }
        this.status = ShowSeatStatus.HELD;
        this.lockedUntil = LocalDateTime.now().plusMinutes(5);
    }

    public void book() {
        if (this.status != ShowSeatStatus.LOCKED && this.status != ShowSeatStatus.HELD && this.status != ShowSeatStatus.AVAILABLE) {
            throw new IllegalStateException("Cannot book seat: current status is " + this.status);
        }
        this.status = ShowSeatStatus.BOOKED;
        this.lockedUntil = null;
    }

    public void release() {
        this.status = ShowSeatStatus.AVAILABLE;
        this.lockedBy = null;
        this.lockedUntil = null;
    }

    public boolean isLockExpired(LocalDateTime now) {
        return (this.status == ShowSeatStatus.LOCKED || this.status == ShowSeatStatus.HELD)
                && this.lockedUntil != null && this.lockedUntil.isBefore(now);
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Show getShow() {
        return show;
    }

    public void setShow(Show show) {
        this.show = show;
    }

    public Seat getSeat() {
        return seat;
    }

    public void setSeat(Seat seat) {
        this.seat = seat;
    }

    public ShowSeatStatus getStatus() {
        return status;
    }

    public void setStatus(ShowSeatStatus status) {
        this.status = status;
    }

    public BigDecimal getPrice() {
        return price;
    }

    public void setPrice(BigDecimal price) {
        this.price = price;
    }

    public LocalDateTime getLockedUntil() {
        return lockedUntil;
    }

    public void setLockedUntil(LocalDateTime lockedUntil) {
        this.lockedUntil = lockedUntil;
    }

    public User getLockedBy() {
        return lockedBy;
    }

    public void setLockedBy(User lockedBy) {
        this.lockedBy = lockedBy;
    }

    public Long getVersion() {
        return version;
    }

    public void setVersion(Long version) {
        this.version = version;
    }
}
