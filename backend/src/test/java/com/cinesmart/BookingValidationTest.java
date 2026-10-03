package com.cinesmart;

import com.cinesmart.booking.dto.BookingCreateRequest;
import com.cinesmart.booking.dto.BookingDTO;
import com.cinesmart.booking.entity.Booking;
import com.cinesmart.booking.entity.BookingStatus;
import com.cinesmart.booking.repository.BookingRepository;
import com.cinesmart.booking.service.BookingService;
import com.cinesmart.common.exception.BadRequestException;
import com.cinesmart.common.exception.SeatUnavailableException;
import com.cinesmart.seat.entity.Seat;
import com.cinesmart.seat.entity.SeatTier;
import com.cinesmart.show.entity.Show;
import com.cinesmart.show.entity.ShowSeat;
import com.cinesmart.show.entity.ShowSeatStatus;
import com.cinesmart.show.repository.ShowRepository;
import com.cinesmart.show.repository.ShowSeatRepository;
import com.cinesmart.user.entity.User;
import com.cinesmart.user.entity.UserRole;
import com.cinesmart.user.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.Collections;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("BookingValidation & Integrity Tests")
class BookingValidationTest {

    @Mock
    private BookingRepository bookingRepository;
    @Mock
    private ShowRepository showRepository;
    @Mock
    private ShowSeatRepository showSeatRepository;
    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private BookingService bookingService;

    private User testUser;
    private Show testShow;
    private ShowSeat seat1;
    private ShowSeat seat2;

    @BeforeEach
    void setUp() {
        testUser = new User("customer@test.com", "hash", "Jane Customer", null, UserRole.ROLE_CUSTOMER);
        testUser.setId(1L);

        testShow = new Show();
        testShow.setId(101L);

        Seat physical1 = new Seat(null, "E", 1, SeatTier.STANDARD, 1, 5);
        physical1.setId(11L);
        Seat physical2 = new Seat(null, "E", 2, SeatTier.STANDARD, 2, 5);
        physical2.setId(12L);

        seat1 = new ShowSeat(testShow, physical1, BigDecimal.valueOf(15.00));
        seat1.setId(101L);

        seat2 = new ShowSeat(testShow, physical2, BigDecimal.valueOf(15.00));
        seat2.setId(102L);
    }

    @Test
    @DisplayName("Scenario 8: Duplicate seat IDs in request are rejected with DUPLICATE_SEAT_IDS")
    void testDuplicateSeatIdsRejected() {
        when(userRepository.findByEmail("customer@test.com")).thenReturn(Optional.of(testUser));
        when(showRepository.findById(101L)).thenReturn(Optional.of(testShow));

        // Client passes duplicate ID: [101L, 101L]
        BookingCreateRequest request = new BookingCreateRequest(101L, Arrays.asList(101L, 101L));

        BadRequestException ex = assertThrows(BadRequestException.class,
                () -> bookingService.createBooking("customer@test.com", request));

        assertEquals("DUPLICATE_SEAT_IDS", ex.getErrorCode());
        verify(showSeatRepository, never()).saveAll(any());
        verify(bookingRepository, never()).save(any());
    }

    @Test
    @DisplayName("Scenario 8b: Invalid or non-positive seat IDs are rejected")
    void testInvalidSeatIdRejected() {
        when(userRepository.findByEmail("customer@test.com")).thenReturn(Optional.of(testUser));
        when(showRepository.findById(101L)).thenReturn(Optional.of(testShow));

        BookingCreateRequest request = new BookingCreateRequest(101L, Arrays.asList(101L, -5L));

        BadRequestException ex = assertThrows(BadRequestException.class,
                () -> bookingService.createBooking("customer@test.com", request));

        assertEquals("INVALID_SEAT_IDS", ex.getErrorCode());
    }

    @Test
    @DisplayName("Cross-show seat mismatch is rejected")
    void testCrossShowSeatMismatch() {
        Show otherShow = new Show();
        otherShow.setId(999L);
        ShowSeat seatFromOtherShow = new ShowSeat(otherShow, new Seat(null, "A", 1, SeatTier.STANDARD, 1, 1), BigDecimal.valueOf(15.00));
        seatFromOtherShow.setId(505L);

        when(userRepository.findByEmail("customer@test.com")).thenReturn(Optional.of(testUser));
        when(showRepository.findById(101L)).thenReturn(Optional.of(testShow));
        when(showSeatRepository.findAllByIdWithLock(Collections.singletonList(505L)))
                .thenReturn(Collections.singletonList(seatFromOtherShow));

        BookingCreateRequest request = new BookingCreateRequest(101L, Collections.singletonList(505L));

        BadRequestException ex = assertThrows(BadRequestException.class,
                () -> bookingService.createBooking("customer@test.com", request));

        assertEquals("CROSS_SHOW_SEAT_MISMATCH", ex.getErrorCode());
    }

    @Test
    @DisplayName("Scenario 4 & 5: When one seat is unavailable, transaction fails atomically with no seats booked")
    void testAtomicReservationFailure() {
        seat2.setStatus(ShowSeatStatus.BOOKED); // Seat 2 is already booked!

        when(userRepository.findByEmail("customer@test.com")).thenReturn(Optional.of(testUser));
        when(showRepository.findById(101L)).thenReturn(Optional.of(testShow));
        when(showSeatRepository.findAllByIdWithLock(Arrays.asList(101L, 102L)))
                .thenReturn(Arrays.asList(seat1, seat2));

        BookingCreateRequest request = new BookingCreateRequest(101L, Arrays.asList(101L, 102L));

        assertThrows(SeatUnavailableException.class,
                () -> bookingService.createBooking("customer@test.com", request));

        // Verify that seat 1 remains AVAILABLE and was NOT partially booked
        assertEquals(ShowSeatStatus.AVAILABLE, seat1.getStatus());
        verify(bookingRepository, never()).save(any());
        verify(showSeatRepository, never()).saveAll(any());
    }
}
