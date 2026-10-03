package com.cinesmart;

import com.cinesmart.booking.dto.BookingCreateRequest;
import com.cinesmart.booking.dto.BookingDTO;
import com.cinesmart.booking.entity.BookingStatus;
import com.cinesmart.booking.repository.BookingRepository;
import com.cinesmart.booking.service.BookingService;
import com.cinesmart.cinema.entity.Cinema;
import com.cinesmart.cinema.entity.ScreenType;
import com.cinesmart.cinema.repository.CinemaRepository;
import com.cinesmart.common.exception.SeatUnavailableException;
import com.cinesmart.movie.entity.Movie;
import com.cinesmart.movie.repository.MovieRepository;
import com.cinesmart.screen.entity.Screen;
import com.cinesmart.screen.repository.ScreenRepository;
import com.cinesmart.seat.entity.Seat;
import com.cinesmart.seat.entity.SeatTier;
import com.cinesmart.seat.repository.SeatRepository;
import com.cinesmart.show.entity.Show;
import com.cinesmart.show.entity.ShowSeat;
import com.cinesmart.show.entity.ShowSeatStatus;
import com.cinesmart.show.repository.ShowRepository;
import com.cinesmart.show.repository.ShowSeatRepository;
import com.cinesmart.user.entity.User;
import com.cinesmart.user.entity.UserRole;
import com.cinesmart.user.repository.UserRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@DisplayName("ConcurrencyBooking Integration Tests: Double-Booking Prevention")
class ConcurrencyBookingIntegrationTest {

    @Autowired
    private BookingService bookingService;

    @Autowired
    private BookingRepository bookingRepository;

    @Autowired
    private ShowSeatRepository showSeatRepository;

    @Autowired
    private ShowRepository showRepository;

    @Autowired
    private SeatRepository seatRepository;

    @Autowired
    private ScreenRepository screenRepository;

    @Autowired
    private CinemaRepository cinemaRepository;

    @Autowired
    private MovieRepository movieRepository;

    @Autowired
    private UserRepository userRepository;

    @Test
    @DisplayName("Concurrent booking attempts on identical seats: exactly one succeeds and one is rejected")
    void testConcurrentBookingPrevention() throws Exception {
        // 1. Seed unique entities for this test
        Cinema cinema = cinemaRepository.save(new Cinema("Concurrent Cinema", "Metro", "Address", "111"));
        Screen screen = screenRepository.save(new Screen(cinema, 99, ScreenType.STANDARD, 2));

        Seat seat1 = seatRepository.save(new Seat(screen, "E", 1, SeatTier.STANDARD, 1, 5));
        Seat seat2 = seatRepository.save(new Seat(screen, "E", 2, SeatTier.STANDARD, 2, 5));

        Movie movie = movieRepository.save(new Movie("Concurrent Movie", "Syn", 120, "English", "Action", "PG", "url"));
        LocalDateTime start = LocalDateTime.now().plusDays(2);
        Show show = showRepository.save(new Show(movie, screen, start, start.plusHours(2), BigDecimal.valueOf(15.00)));

        ShowSeat ss1 = showSeatRepository.save(new ShowSeat(show, seat1, BigDecimal.valueOf(15.00)));
        ShowSeat ss2 = showSeatRepository.save(new ShowSeat(show, seat2, BigDecimal.valueOf(15.00)));

        User user1 = userRepository.save(new User("concurrent_user1@test.com", "hash", "User One", "111", UserRole.ROLE_CUSTOMER));
        User user2 = userRepository.save(new User("concurrent_user2@test.com", "hash", "User Two", "222", UserRole.ROLE_CUSTOMER));

        // 2. Prepare concurrent threads to simultaneously book seats [ss1, ss2]
        int threadCount = 2;
        ExecutorService executorService = Executors.newFixedThreadPool(threadCount);
        CountDownLatch readyLatch = new CountDownLatch(threadCount);
        CountDownLatch startLatch = new CountDownLatch(1);

        AtomicInteger successCount = new AtomicInteger(0);
        AtomicInteger failureCount = new AtomicInteger(0);

        BookingCreateRequest req = new BookingCreateRequest(show.getId(), Arrays.asList(ss1.getId(), ss2.getId()));

        Callable<Void> task1 = () -> {
            readyLatch.countDown();
            startLatch.await();
            try {
                BookingDTO b = bookingService.createBooking(user1.getEmail(), req);
                if (b != null && b.getStatus() == BookingStatus.CONFIRMED) {
                    successCount.incrementAndGet();
                }
            } catch (Exception e) {
                failureCount.incrementAndGet();
            }
            return null;
        };

        Callable<Void> task2 = () -> {
            readyLatch.countDown();
            startLatch.await();
            try {
                BookingDTO b = bookingService.createBooking(user2.getEmail(), req);
                if (b != null && b.getStatus() == BookingStatus.CONFIRMED) {
                    successCount.incrementAndGet();
                }
            } catch (Exception e) {
                failureCount.incrementAndGet();
            }
            return null;
        };

        Future<Void> f1 = executorService.submit(task1);
        Future<Void> f2 = executorService.submit(task2);

        // Await all threads ready, then release them simultaneously
        readyLatch.await(5, TimeUnit.SECONDS);
        startLatch.countDown();

        f1.get(10, TimeUnit.SECONDS);
        f2.get(10, TimeUnit.SECONDS);

        executorService.shutdown();

        // 3. Assertions: Exactly 1 succeeded, exactly 1 failed
        assertEquals(1, successCount.get(), "Exactly one user must successfully claim the seats");
        assertEquals(1, failureCount.get(), "The other concurrent user must be rejected with seat conflict");

        // 4. Verify database state
        ShowSeat fresh1 = showSeatRepository.findById(ss1.getId()).orElseThrow();
        ShowSeat fresh2 = showSeatRepository.findById(ss2.getId()).orElseThrow();
        assertEquals(ShowSeatStatus.BOOKED, fresh1.getStatus());
        assertEquals(ShowSeatStatus.BOOKED, fresh2.getStatus());
    }
}
