package com.cinesmart;

import com.cinesmart.booking.entity.Booking;
import com.cinesmart.booking.entity.BookingSeat;
import com.cinesmart.booking.entity.BookingStatus;
import com.cinesmart.show.entity.Show;
import com.cinesmart.show.entity.ShowSeat;
import com.cinesmart.user.entity.User;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("Booking Domain Entity Tests")
class BookingTest {

    @Test
    @DisplayName("Should create booking and add itemized line-item snapshots")
    void testBookingSnapshotCreation() {
        User user = new User("user@example.com", "hash", "Jane Doe", null, null);
        Show show = new Show();
        Booking booking = new Booking("CS-2026-TEST", user, show, BigDecimal.valueOf(36.00));

        assertEquals(BookingStatus.PENDING_PAYMENT, booking.getStatus());
        assertEquals("CS-2026-TEST", booking.getBookingReference());

        ShowSeat ss1 = new ShowSeat();
        ShowSeat ss2 = new ShowSeat();

        BookingSeat bs1 = new BookingSeat(booking, ss1, BigDecimal.valueOf(18.00), "E-5", "PREMIUM");
        BookingSeat bs2 = new BookingSeat(booking, ss2, BigDecimal.valueOf(18.00), "E-6", "PREMIUM");

        booking.addBookingSeat(bs1);
        booking.addBookingSeat(bs2);

        assertEquals(2, booking.getBookingSeats().size());
        assertEquals(booking, bs1.getBooking());
    }

    @Test
    @DisplayName("Should confirm and cancel booking properly")
    void testBookingLifecycleStateTransitions() {
        Booking booking = new Booking("CS-2026-LIFECYCLE", null, null, BigDecimal.valueOf(25.00));
        assertEquals(BookingStatus.PENDING_PAYMENT, booking.getStatus());

        booking.confirm();
        assertEquals(BookingStatus.CONFIRMED, booking.getStatus());
        assertNotNull(booking.getConfirmedAt());

        booking.cancel();
        assertEquals(BookingStatus.CANCELLED, booking.getStatus());
    }
}
