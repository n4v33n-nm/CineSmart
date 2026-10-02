package com.cinesmart;

import com.cinesmart.booking.dto.BookingCreateRequest;
import com.cinesmart.booking.dto.BookingDTO;
import com.cinesmart.booking.entity.Booking;
import com.cinesmart.booking.entity.BookingStatus;
import com.cinesmart.booking.repository.BookingRepository;
import com.cinesmart.booking.service.BookingService;
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
@DisplayName("BookingService Unit & Concurrency Tests")
class BookingServiceTest {

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

        Seat physicalSeat1 = new Seat(null, "E", 5, SeatTier.STANDARD, 5, 5);
        physicalSeat1.setId(501L);

        Seat physicalSeat2 = new Seat(null, "E", 6, SeatTier.STANDARD, 6, 5);
        physicalSeat2.setId(502L);

        seat1 = new ShowSeat(testShow, physicalSeat1, BigDecimal.valueOf(15.00));
        seat1.setId(1201L);

        seat2 = new ShowSeat(testShow, physicalSeat2, BigDecimal.valueOf(15.00));
        seat2.setId(1202L);
    }

    @Test
    @DisplayName("Should successfully create booking and transition seats to BOOKED")
    void testCreateBookingSuccess() {
        BookingCreateRequest request = new BookingCreateRequest(101L, Arrays.asList(1201L, 1202L));

        when(userRepository.findByEmail("customer@test.com")).thenReturn(Optional.of(testUser));
        when(showRepository.findById(101L)).thenReturn(Optional.of(testShow));
        when(showSeatRepository.findAllByIdWithLock(Arrays.asList(1201L, 1202L))).thenReturn(Arrays.asList(seat1, seat2));
        when(bookingRepository.save(any(Booking.class))).thenAnswer(invocation -> {
            Booking b = invocation.getArgument(0);
            b.setId(5001L);
            return b;
        });

        BookingDTO bookingDTO = bookingService.createBooking("customer@test.com", request);

        assertNotNull(bookingDTO);
        assertEquals(BookingStatus.CONFIRMED, bookingDTO.getStatus());
        assertEquals(BigDecimal.valueOf(30.00), bookingDTO.getTotalAmount());
        assertEquals(ShowSeatStatus.BOOKED, seat1.getStatus());
        assertEquals(ShowSeatStatus.BOOKED, seat2.getStatus());

        verify(showSeatRepository, times(1)).saveAll(Arrays.asList(seat1, seat2));
        verify(bookingRepository, times(1)).save(any(Booking.class));
    }

    @Test
    @DisplayName("Should throw SeatUnavailableException when a seat is already BOOKED or HELD")
    void testCreateBookingWhenSeatAlreadyBooked() {
        seat1.setStatus(ShowSeatStatus.BOOKED); // Seat already taken!
        BookingCreateRequest request = new BookingCreateRequest(101L, Collections.singletonList(1201L));

        when(userRepository.findByEmail("customer@test.com")).thenReturn(Optional.of(testUser));
        when(showRepository.findById(101L)).thenReturn(Optional.of(testShow));
        when(showSeatRepository.findAllByIdWithLock(Collections.singletonList(1201L))).thenReturn(Collections.singletonList(seat1));

        assertThrows(SeatUnavailableException.class, () -> bookingService.createBooking("customer@test.com", request));
        verify(bookingRepository, never()).save(any(Booking.class));
    }
}
