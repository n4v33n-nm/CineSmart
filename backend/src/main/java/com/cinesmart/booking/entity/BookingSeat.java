package com.cinesmart.booking.entity;

import com.cinesmart.show.entity.ShowSeat;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;
import java.math.BigDecimal;

@Entity
@Table(name = "booking_seats")
public class BookingSeat {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotNull
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "booking_id", nullable = false)
    private Booking booking;

    @NotNull
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "show_seat_id", nullable = false)
    private ShowSeat showSeat;

    @NotNull
    @PositiveOrZero
    @Column(name = "snapshot_price", nullable = false, precision = 10, scale = 2)
    private BigDecimal snapshotPrice;

    @NotBlank
    @Column(name = "seat_label", nullable = false, length = 10)
    private String seatLabel;

    @NotBlank
    @Column(name = "tier_snapshot", nullable = false, length = 30)
    private String tierSnapshot;

    public BookingSeat() {
    }

    public BookingSeat(Booking booking, ShowSeat showSeat, BigDecimal snapshotPrice, String seatLabel, String tierSnapshot) {
        this.booking = booking;
        this.showSeat = showSeat;
        this.snapshotPrice = snapshotPrice;
        this.seatLabel = seatLabel;
        this.tierSnapshot = tierSnapshot;
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

    public ShowSeat getShowSeat() {
        return showSeat;
    }

    public void setShowSeat(ShowSeat showSeat) {
        this.showSeat = showSeat;
    }

    public BigDecimal getSnapshotPrice() {
        return snapshotPrice;
    }

    public void setSnapshotPrice(BigDecimal snapshotPrice) {
        this.snapshotPrice = snapshotPrice;
    }

    public String getSeatLabel() {
        return seatLabel;
    }

    public void setSeatLabel(String seatLabel) {
        this.seatLabel = seatLabel;
    }

    public String getTierSnapshot() {
        return tierSnapshot;
    }

    public void setTierSnapshot(String tierSnapshot) {
        this.tierSnapshot = tierSnapshot;
    }
}
