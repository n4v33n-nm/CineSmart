package com.cinesmart;

import com.cinesmart.seat.entity.Seat;
import com.cinesmart.seat.entity.SeatTier;
import com.cinesmart.show.entity.Show;
import com.cinesmart.show.entity.ShowSeat;
import com.cinesmart.show.entity.ShowSeatStatus;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("ShowSeat Domain Entity Tests")
class ShowSeatTest {

    @Test
    @DisplayName("Should successfully transition from AVAILABLE to HELD, then to BOOKED")
    void testValidStateTransitions() {
        Seat seat = new Seat(null, "E", 5, SeatTier.PREMIUM, 5, 5);
        ShowSeat showSeat = new ShowSeat(null, seat, BigDecimal.valueOf(18.00));

        assertEquals(ShowSeatStatus.AVAILABLE, showSeat.getStatus());

        showSeat.hold();
        assertEquals(ShowSeatStatus.HELD, showSeat.getStatus());

        showSeat.book();
        assertEquals(ShowSeatStatus.BOOKED, showSeat.getStatus());
    }

    @Test
    @DisplayName("Should throw IllegalStateException when attempting to hold an already held seat")
    void testHoldAlreadyHeldSeatThrowsException() {
        Seat seat = new Seat(null, "E", 5, SeatTier.PREMIUM, 5, 5);
        ShowSeat showSeat = new ShowSeat(null, seat, BigDecimal.valueOf(18.00));
        showSeat.hold();

        assertThrows(IllegalStateException.class, showSeat::hold);
    }

    @Test
    @DisplayName("Should correctly release a HELD seat back to AVAILABLE")
    void testReleaseSeat() {
        Seat seat = new Seat(null, "E", 5, SeatTier.PREMIUM, 5, 5);
        ShowSeat showSeat = new ShowSeat(null, seat, BigDecimal.valueOf(18.00));
        showSeat.hold();
        assertEquals(ShowSeatStatus.HELD, showSeat.getStatus());

        showSeat.release();
        assertEquals(ShowSeatStatus.AVAILABLE, showSeat.getStatus());
    }
}
