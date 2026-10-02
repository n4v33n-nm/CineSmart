package com.cinesmart.show.entity;

import com.cinesmart.seat.entity.Seat;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;
import java.math.BigDecimal;

@Entity
@Table(name = "show_seats", uniqueConstraints = {
    @UniqueConstraint(name = "uq_show_seat", columnNames = {"show_id", "seat_id"})
}, indexes = {
    @Index(name = "idx_show_seats_lookup", columnList = "show_id, status")
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

    public void hold() {
        if (this.status != ShowSeatStatus.AVAILABLE) {
            throw new IllegalStateException("Cannot hold seat: current status is " + this.status);
        }
        this.status = ShowSeatStatus.HELD;
    }

    public void book() {
        if (this.status != ShowSeatStatus.HELD && this.status != ShowSeatStatus.AVAILABLE) {
            throw new IllegalStateException("Cannot book seat: current status is " + this.status);
        }
        this.status = ShowSeatStatus.BOOKED;
    }

    public void release() {
        this.status = ShowSeatStatus.AVAILABLE;
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

    public Long getVersion() {
        return version;
    }

    public void setVersion(Long version) {
        this.version = version;
    }
}
