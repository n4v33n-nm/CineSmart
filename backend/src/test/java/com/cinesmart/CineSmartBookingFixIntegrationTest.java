package com.cinesmart;

import com.cinesmart.booking.dto.BookingCreateRequest;
import com.cinesmart.booking.dto.BookingDTO;
import com.cinesmart.booking.entity.Booking;
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
import com.cinesmart.payment.dto.PaymentInitiateRequest;
import com.cinesmart.payment.dto.PaymentResponseDTO;
import com.cinesmart.payment.service.PaymentService;
import com.cinesmart.seat.dto.SeatLockResponseDTO;
import com.cinesmart.seat.dto.SeatMapResponseDTO;
import com.cinesmart.seat.entity.Seat;
import com.cinesmart.seat.entity.SeatTier;
import com.cinesmart.seat.repository.SeatRepository;
import com.cinesmart.seat.service.SeatLockCleanupService;
import com.cinesmart.seat.service.SeatService;
import com.cinesmart.show.entity.Show;
import com.cinesmart.show.entity.ShowSeat;
import com.cinesmart.show.entity.ShowSeatStatus;
import com.cinesmart.show.repository.ShowRepository;
import com.cinesmart.show.repository.ShowSeatRepository;
import com.cinesmart.ticket.repository.TicketRepository;
import com.cinesmart.user.entity.User;
import com.cinesmart.user.entity.UserRole;
import com.cinesmart.user.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@DisplayName("CineSmart Booking Fix Comprehensive Integration Test Suite")
public class CineSmartBookingFixIntegrationTest {

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

    @Autowired
    private SeatService seatService;

    @Autowired
    private PaymentService paymentService;

    @Autowired
    private SeatLockCleanupService seatLockCleanupService;

    @Autowired
    private TicketRepository ticketRepository;

    private Cinema testCinema;
    private Screen testScreen;
    private Movie testMovie;
    private User testUser1;
    private User testUser2;

    @BeforeEach
    void setupCommonEntities() {
        String uniqueSuffix = UUID.randomUUID().toString().substring(0, 6);

        testCinema = cinemaRepository.save(new Cinema("Plex " + uniqueSuffix, "City", "Addr", "9999"));
        testScreen = screenRepository.save(new Screen(testCinema, 1, ScreenType.STANDARD, 50));
        testMovie = movieRepository.save(new Movie("Film " + uniqueSuffix, "Synopsis", 130, "Hindi", "Sci-Fi", "UA", "url"));

        testUser1 = userRepository.save(new User("cust1_" + uniqueSuffix + "@cinesmart.io", "pass", "User One", "911", UserRole.ROLE_CUSTOMER));
        testUser2 = userRepository.save(new User("cust2_" + uniqueSuffix + "@cinesmart.io", "pass", "User Two", "912", UserRole.ROLE_CUSTOMER));
    }

    @Test
    @DisplayName("Scenario 1: Two users attempting to book same seat simultaneously (pessimistic lock verification)")
    void testSimultaneousBookingPrevention() throws Exception {
        Seat seat = seatRepository.save(new Seat(testScreen, "A", 1, SeatTier.STANDARD, 1, 1));
        LocalDateTime showTime = LocalDateTime.now().plusDays(1);
        Show show = showRepository.save(new Show(testMovie, testScreen, showTime, showTime.plusHours(2), BigDecimal.valueOf(150.00)));
        ShowSeat showSeat = showSeatRepository.save(new ShowSeat(show, seat, BigDecimal.valueOf(150.00)));

        int threadCount = 2;
        ExecutorService executor = Executors.newFixedThreadPool(threadCount);
        CountDownLatch readyLatch = new CountDownLatch(threadCount);
        CountDownLatch startLatch = new CountDownLatch(1);

        AtomicInteger successCount = new AtomicInteger(0);
        AtomicInteger failureCount = new AtomicInteger(0);

        BookingCreateRequest request = new BookingCreateRequest(show.getId(), Collections.singletonList(showSeat.getId()));

        Callable<Void> task1 = () -> {
            readyLatch.countDown();
            readyLatch.await();
            startLatch.await();
            try {
                bookingService.createBooking(testUser1.getEmail(), request);
                successCount.incrementAndGet();
            } catch (Exception e) {
                failureCount.incrementAndGet();
            }
            return null;
        };

        Callable<Void> task2 = () -> {
            readyLatch.countDown();
            readyLatch.await();
            startLatch.await();
            try {
                bookingService.createBooking(testUser2.getEmail(), request);
                successCount.incrementAndGet();
            } catch (Exception e) {
                failureCount.incrementAndGet();
            }
            return null;
        };

        Future<Void> f1 = executor.submit(task1);
        Future<Void> f2 = executor.submit(task2);

        readyLatch.await();
        startLatch.countDown();

        f1.get(10, TimeUnit.SECONDS);
        f2.get(10, TimeUnit.SECONDS);
        executor.shutdown();

        assertEquals(1, successCount.get(), "Exactly one user must successfully acquire the seat");
        assertEquals(1, failureCount.get(), "The competing concurrent user must be rejected");

        ShowSeat persisted = showSeatRepository.findById(showSeat.getId()).orElseThrow();
        assertEquals(ShowSeatStatus.LOCKED, persisted.getStatus(), "Seat status must be LOCKED by the successful user");
        assertNotNull(persisted.getLockedUntil(), "Seat must have an active lock expiration timestamp");
    }

    @Test
    @DisplayName("Scenario 2: One user booking multiple seats for a single show")
    void testOneUserBookingMultipleSeatsSingleShow() {
        Seat seatA = seatRepository.save(new Seat(testScreen, "B", 1, SeatTier.STANDARD, 1, 2));
        Seat seatB = seatRepository.save(new Seat(testScreen, "B", 2, SeatTier.STANDARD, 2, 2));
        Seat seatC = seatRepository.save(new Seat(testScreen, "B", 3, SeatTier.STANDARD, 3, 2));

        LocalDateTime showTime = LocalDateTime.now().plusDays(2);
        Show show = showRepository.save(new Show(testMovie, testScreen, showTime, showTime.plusHours(2), BigDecimal.valueOf(180.00)));

        ShowSeat ssA = showSeatRepository.save(new ShowSeat(show, seatA, BigDecimal.valueOf(180.00)));
        ShowSeat ssB = showSeatRepository.save(new ShowSeat(show, seatB, BigDecimal.valueOf(180.00)));
        ShowSeat ssC = showSeatRepository.save(new ShowSeat(show, seatC, BigDecimal.valueOf(180.00)));

        BookingCreateRequest request = new BookingCreateRequest(show.getId(), Arrays.asList(ssA.getId(), ssB.getId(), ssC.getId()));
        BookingDTO booking = bookingService.createBooking(testUser1.getEmail(), request);

        assertNotNull(booking);
        assertEquals(BookingStatus.PENDING_PAYMENT, booking.getStatus());
        assertEquals(0, BigDecimal.valueOf(540.00).compareTo(booking.getTotalAmount()), "3 seats x ₹180 = ₹540 total");

        List<ShowSeat> refreshedSeats = showSeatRepository.findAllById(Arrays.asList(ssA.getId(), ssB.getId(), ssC.getId()));
        for (ShowSeat s : refreshedSeats) {
            assertEquals(ShowSeatStatus.LOCKED, s.getStatus());
            assertNotNull(s.getLockedUntil());
        }
    }

    @Test
    @DisplayName("Scenario 3: Same seat booked for two different shows of the same movie (independent availability)")
    void testSameSeatIndependentAvailabilityAcrossDifferentShows() {
        Seat seatA1 = seatRepository.save(new Seat(testScreen, "C", 1, SeatTier.STANDARD, 1, 3));

        LocalDateTime morningTime = LocalDateTime.now().plusDays(3).withHour(9).withMinute(0);
        LocalDateTime eveningTime = LocalDateTime.now().plusDays(3).withHour(18).withMinute(0);

        Show morningShow = showRepository.save(new Show(testMovie, testScreen, morningTime, morningTime.plusHours(2), BigDecimal.valueOf(150.00)));
        Show eveningShow = showRepository.save(new Show(testMovie, testScreen, eveningTime, eveningTime.plusHours(2), BigDecimal.valueOf(220.00)));

        ShowSeat morningSeat = showSeatRepository.save(new ShowSeat(morningShow, seatA1, BigDecimal.valueOf(150.00)));
        ShowSeat eveningSeat = showSeatRepository.save(new ShowSeat(eveningShow, seatA1, BigDecimal.valueOf(220.00)));

        // User 1 books and confirms seat A1 in the morning show
        BookingDTO morningBooking = bookingService.createBooking(testUser1.getEmail(),
                new BookingCreateRequest(morningShow.getId(), Collections.singletonList(morningSeat.getId())));
        bookingService.confirmBooking(morningBooking.getId());

        ShowSeat freshMorningSeat = showSeatRepository.findById(morningSeat.getId()).orElseThrow();
        assertEquals(ShowSeatStatus.BOOKED, freshMorningSeat.getStatus());

        // User 2 can still book the EXACT same physical seat in the evening show without interference
        BookingDTO eveningBooking = bookingService.createBooking(testUser2.getEmail(),
                new BookingCreateRequest(eveningShow.getId(), Collections.singletonList(eveningSeat.getId())));

        assertNotNull(eveningBooking);
        assertEquals(BookingStatus.PENDING_PAYMENT, eveningBooking.getStatus());
        ShowSeat freshEveningSeat = showSeatRepository.findById(eveningSeat.getId()).orElseThrow();
        assertEquals(ShowSeatStatus.LOCKED, freshEveningSeat.getStatus());
    }

    @Test
    @DisplayName("Scenario 4: Dynamic ticket pricing based on show time (9:00 AM, 1:00 PM, 6:00 PM, 10:00 PM)")
    void testDynamicPricingPerShowTime() {
        Seat seat = seatRepository.save(new Seat(testScreen, "D", 1, SeatTier.STANDARD, 1, 4));

        LocalDateTime baseDate = LocalDateTime.now().plusDays(4);
        Show morningShow = showRepository.save(new Show(testMovie, testScreen, baseDate.withHour(9).withMinute(0), baseDate.withHour(11).withMinute(0), BigDecimal.valueOf(150.00)));
        Show afternoonShow = showRepository.save(new Show(testMovie, testScreen, baseDate.withHour(13).withMinute(0), baseDate.withHour(15).withMinute(0), BigDecimal.valueOf(180.00)));
        Show eveningShow = showRepository.save(new Show(testMovie, testScreen, baseDate.withHour(18).withMinute(0), baseDate.withHour(20).withMinute(0), BigDecimal.valueOf(220.00)));
        Show nightShow = showRepository.save(new Show(testMovie, testScreen, baseDate.withHour(22).withMinute(0), baseDate.withHour(23).withMinute(59), BigDecimal.valueOf(250.00)));

        ShowSeat mSeat = showSeatRepository.save(new ShowSeat(morningShow, seat, morningShow.getBasePrice()));
        ShowSeat aSeat = showSeatRepository.save(new ShowSeat(afternoonShow, seat, afternoonShow.getBasePrice()));
        ShowSeat eSeat = showSeatRepository.save(new ShowSeat(eveningShow, seat, eveningShow.getBasePrice()));
        ShowSeat nSeat = showSeatRepository.save(new ShowSeat(nightShow, seat, nightShow.getBasePrice()));

        BookingDTO bMorning = bookingService.createBooking(testUser1.getEmail(), new BookingCreateRequest(morningShow.getId(), Collections.singletonList(mSeat.getId())));
        assertEquals(0, BigDecimal.valueOf(150.00).compareTo(bMorning.getTotalAmount()));

        BookingDTO bAfternoon = bookingService.createBooking(testUser1.getEmail(), new BookingCreateRequest(afternoonShow.getId(), Collections.singletonList(aSeat.getId())));
        assertEquals(0, BigDecimal.valueOf(180.00).compareTo(bAfternoon.getTotalAmount()));

        BookingDTO bEvening = bookingService.createBooking(testUser1.getEmail(), new BookingCreateRequest(eveningShow.getId(), Collections.singletonList(eSeat.getId())));
        assertEquals(0, BigDecimal.valueOf(220.00).compareTo(bEvening.getTotalAmount()));

        BookingDTO bNight = bookingService.createBooking(testUser1.getEmail(), new BookingCreateRequest(nightShow.getId(), Collections.singletonList(nSeat.getId())));
        assertEquals(0, BigDecimal.valueOf(250.00).compareTo(bNight.getTotalAmount()));
    }

    @Test
    @DisplayName("Scenario 5: Expired seat locks being released automatically")
    void testExpiredSeatLocksReleasedAutomatically() {
        Seat seat = seatRepository.save(new Seat(testScreen, "E", 1, SeatTier.STANDARD, 1, 5));
        LocalDateTime showTime = LocalDateTime.now().plusDays(5);
        Show show = showRepository.save(new Show(testMovie, testScreen, showTime, showTime.plusHours(2), BigDecimal.valueOf(150.00)));

        ShowSeat showSeat = new ShowSeat(show, seat, BigDecimal.valueOf(150.00));
        // Manually lock it in the past (expired 10 minutes ago)
        showSeat.lock(testUser1, LocalDateTime.now().minusMinutes(10));
        showSeat = showSeatRepository.save(showSeat);

        assertEquals(ShowSeatStatus.LOCKED, showSeat.getStatus());
        assertTrue(showSeat.isLockExpired(LocalDateTime.now()));

        // Trigger cleanup
        seatLockCleanupService.cleanupExpiredLocks();

        ShowSeat cleanedSeat = showSeatRepository.findById(showSeat.getId()).orElseThrow();
        assertEquals(ShowSeatStatus.AVAILABLE, cleanedSeat.getStatus(), "Expired lock must be released to AVAILABLE");
        assertNull(cleanedSeat.getLockedUntil());
        assertNull(cleanedSeat.getLockedBy());

        // Now User 2 can book it immediately
        BookingDTO newBooking = bookingService.createBooking(testUser2.getEmail(),
                new BookingCreateRequest(show.getId(), Collections.singletonList(showSeat.getId())));
        assertNotNull(newBooking);
        assertEquals(BookingStatus.PENDING_PAYMENT, newBooking.getStatus());
    }

    @Test
    @DisplayName("Scenario 6: Payment failure releasing temporarily locked seats back to AVAILABLE")
    void testPaymentFailureReleasesSeats() {
        Seat seat = seatRepository.save(new Seat(testScreen, "F", 1, SeatTier.STANDARD, 1, 6));
        LocalDateTime showTime = LocalDateTime.now().plusDays(6);
        Show show = showRepository.save(new Show(testMovie, testScreen, showTime, showTime.plusHours(2), BigDecimal.valueOf(200.00)));
        ShowSeat showSeat = showSeatRepository.save(new ShowSeat(show, seat, BigDecimal.valueOf(200.00)));

        BookingDTO booking = bookingService.createBooking(testUser1.getEmail(),
                new BookingCreateRequest(show.getId(), Collections.singletonList(showSeat.getId())));

        ShowSeat lockedSeat = showSeatRepository.findById(showSeat.getId()).orElseThrow();
        assertEquals(ShowSeatStatus.LOCKED, lockedSeat.getStatus());

        // Simulate payment failure callback
        bookingService.handlePaymentFailure(booking.getId(), "Payment gateway timeout");

        Booking failedBooking = bookingRepository.findById(booking.getId()).orElseThrow();
        assertEquals(BookingStatus.PAYMENT_FAILED, failedBooking.getStatus());

        ShowSeat releasedSeat = showSeatRepository.findById(showSeat.getId()).orElseThrow();
        assertEquals(ShowSeatStatus.AVAILABLE, releasedSeat.getStatus(), "Seat must return to AVAILABLE after payment failure");
        assertNull(releasedSeat.getLockedUntil());
    }

    @Test
    @DisplayName("Scenario 7: Successful payment confirming booking and transitioning seats to BOOKED")
    void testSuccessfulPaymentConfirmsBookingAndBooksSeats() {
        Seat seat = seatRepository.save(new Seat(testScreen, "G", 1, SeatTier.STANDARD, 1, 7));
        LocalDateTime showTime = LocalDateTime.now().plusDays(7);
        Show show = showRepository.save(new Show(testMovie, testScreen, showTime, showTime.plusHours(2), BigDecimal.valueOf(200.00)));
        ShowSeat showSeat = showSeatRepository.save(new ShowSeat(show, seat, BigDecimal.valueOf(200.00)));

        BookingDTO booking = bookingService.createBooking(testUser1.getEmail(),
                new BookingCreateRequest(show.getId(), Collections.singletonList(showSeat.getId())));

        // Simulate successful payment confirmation
        BookingDTO confirmedBooking = bookingService.confirmBooking(booking.getId());

        assertEquals(BookingStatus.CONFIRMED, confirmedBooking.getStatus());
        ShowSeat bookedSeat = showSeatRepository.findById(showSeat.getId()).orElseThrow();
        assertEquals(ShowSeatStatus.BOOKED, bookedSeat.getStatus(), "Seat must transition to permanently BOOKED");
        assertNull(bookedSeat.getLockedUntil());

        // Digital tickets must be generated
        assertFalse(ticketRepository.findByBookingId(booking.getId()).isEmpty(), "Tickets must be generated for confirmed booking");
    }

    @Test
    @DisplayName("Scenario 8: Backend rejecting already-booked seats with clear error message")
    void testBackendRejectsAlreadyBookedSeats() {
        Seat seat = seatRepository.save(new Seat(testScreen, "H", 1, SeatTier.STANDARD, 1, 8));
        LocalDateTime showTime = LocalDateTime.now().plusDays(8);
        Show show = showRepository.save(new Show(testMovie, testScreen, showTime, showTime.plusHours(2), BigDecimal.valueOf(150.00)));
        ShowSeat showSeat = showSeatRepository.save(new ShowSeat(show, seat, BigDecimal.valueOf(150.00)));

        // User 1 books and confirms seat
        BookingDTO b1 = bookingService.createBooking(testUser1.getEmail(),
                new BookingCreateRequest(show.getId(), Collections.singletonList(showSeat.getId())));
        bookingService.confirmBooking(b1.getId());

        // User 2 attempts to book the same seat
        SeatUnavailableException ex = assertThrows(SeatUnavailableException.class, () ->
                bookingService.createBooking(testUser2.getEmail(),
                        new BookingCreateRequest(show.getId(), Collections.singletonList(showSeat.getId()))));

        assertTrue(ex.getMessage().contains("Row H Seat 1") || ex.getMessage().contains("already been booked"),
                "Error message must clearly identify the seat and explain it is booked");
    }

    @Test
    @DisplayName("Scenario 9: Correct total price calculation computed strictly by backend")
    void testBackendCalculatesTotalPriceAccurately() {
        Seat seatStandard = seatRepository.save(new Seat(testScreen, "I", 1, SeatTier.STANDARD, 1, 9));
        Seat seatPremium = seatRepository.save(new Seat(testScreen, "I", 2, SeatTier.PREMIUM, 2, 9));

        LocalDateTime showTime = LocalDateTime.now().plusDays(9);
        Show show = showRepository.save(new Show(testMovie, testScreen, showTime, showTime.plusHours(2), BigDecimal.valueOf(150.00)));

        ShowSeat ssStd = showSeatRepository.save(new ShowSeat(show, seatStandard, BigDecimal.valueOf(150.00)));
        ShowSeat ssPrem = showSeatRepository.save(new ShowSeat(show, seatPremium, BigDecimal.valueOf(200.00)));

        // Request only contains seat IDs, no price is submitted by the client
        BookingCreateRequest request = new BookingCreateRequest(show.getId(), Arrays.asList(ssStd.getId(), ssPrem.getId()));
        BookingDTO booking = bookingService.createBooking(testUser1.getEmail(), request);

        // Standard (150) + Premium (200) = 350
        assertEquals(0, BigDecimal.valueOf(350.00).compareTo(booking.getTotalAmount()), "Backend must compute total price from persisted prices");
    }

    @Test
    @DisplayName("Scenario 10: Existing functionality remaining unaffected (seat map, cancellation, user bookings)")
    void testExistingFunctionalityUnaffected() {
        Seat seat1 = seatRepository.save(new Seat(testScreen, "J", 1, SeatTier.STANDARD, 1, 10));
        Seat seat2 = seatRepository.save(new Seat(testScreen, "J", 2, SeatTier.STANDARD, 2, 10));

        LocalDateTime showTime = LocalDateTime.now().plusDays(10);
        Show show = showRepository.save(new Show(testMovie, testScreen, showTime, showTime.plusHours(2), BigDecimal.valueOf(160.00)));

        ShowSeat ss1 = showSeatRepository.save(new ShowSeat(show, seat1, BigDecimal.valueOf(160.00)));
        ShowSeat ss2 = showSeatRepository.save(new ShowSeat(show, seat2, BigDecimal.valueOf(160.00)));

        // 1. Check Seat Map
        SeatMapResponseDTO seatMap = seatService.getSeatMapForShow(show.getId());
        assertEquals(2, seatMap.getTotalSeats());
        assertEquals(2, seatMap.getAvailableSeats());

        // 2. Book seat 1
        BookingDTO booking = bookingService.createBooking(testUser1.getEmail(),
                new BookingCreateRequest(show.getId(), Collections.singletonList(ss1.getId())));

        // 3. User bookings list
        List<BookingDTO> userBookings = bookingService.getUserBookings(testUser1.getEmail());
        assertFalse(userBookings.isEmpty());
        assertEquals(booking.getId(), userBookings.get(0).getId());

        // 4. Cancel booking
        BookingDTO cancelled = bookingService.cancelBooking(booking.getId(), testUser1.getEmail());
        assertEquals(BookingStatus.CANCELLED, cancelled.getStatus());

        // 5. Verify seat 1 returned to AVAILABLE in seat map
        SeatMapResponseDTO seatMapAfterCancel = seatService.getSeatMapForShow(show.getId());
        assertEquals(2, seatMapAfterCancel.getAvailableSeats());
    }

    @Test
    @DisplayName("Scenario 11: Dedicated Seat Lock and Release REST Service API")
    void testDirectSeatLockAndReleaseAPI() {
        Seat seat = seatRepository.save(new Seat(testScreen, "K", 1, SeatTier.STANDARD, 1, 11));
        LocalDateTime showTime = LocalDateTime.now().plusDays(11);
        Show show = showRepository.save(new Show(testMovie, testScreen, showTime, showTime.plusHours(2), BigDecimal.valueOf(175.00)));
        ShowSeat ss = showSeatRepository.save(new ShowSeat(show, seat, BigDecimal.valueOf(175.00)));

        // 1. Explicitly Lock via SeatService API
        SeatLockResponseDTO lockResp = seatService.lockSeats(show.getId(), Collections.singletonList(ss.getId()), testUser1.getEmail());
        assertEquals("LOCKED", lockResp.getStatus());
        assertNotNull(lockResp.getExpiresAt());

        ShowSeat lockedSeat = showSeatRepository.findById(ss.getId()).orElseThrow();
        assertEquals(ShowSeatStatus.LOCKED, lockedSeat.getStatus());

        // 2. Competing user cannot lock
        assertThrows(SeatUnavailableException.class, () ->
                seatService.lockSeats(show.getId(), Collections.singletonList(ss.getId()), testUser2.getEmail()));

        // 3. User releases the lock
        SeatLockResponseDTO releaseResp = seatService.releaseSeats(show.getId(), Collections.singletonList(ss.getId()), testUser1.getEmail());
        assertEquals("AVAILABLE", releaseResp.getStatus());

        ShowSeat releasedSeat = showSeatRepository.findById(ss.getId()).orElseThrow();
        assertEquals(ShowSeatStatus.AVAILABLE, releasedSeat.getStatus());
    }

    @Test
    @DisplayName("Scenario 12: Pre-payment validation rejects payment if seat lock expired during checkout")
    void testPrePaymentValidationRejectsExpiredLock() {
        Seat seat = seatRepository.save(new Seat(testScreen, "L", 1, SeatTier.STANDARD, 1, 12));
        LocalDateTime showTime = LocalDateTime.now().plusDays(12);
        Show show = showRepository.save(new Show(testMovie, testScreen, showTime, showTime.plusHours(2), BigDecimal.valueOf(190.00)));
        ShowSeat ss = showSeatRepository.save(new ShowSeat(show, seat, BigDecimal.valueOf(190.00)));

        BookingDTO booking = bookingService.createBooking(testUser1.getEmail(),
                new BookingCreateRequest(show.getId(), Collections.singletonList(ss.getId())));

        // Fast-forward expiration: set booking and seat expiration to 2 minutes in the past
        Booking bEntity = bookingRepository.findById(booking.getId()).orElseThrow();
        bEntity.setExpiresAt(LocalDateTime.now().minusMinutes(2));
        bookingRepository.save(bEntity);

        ShowSeat sEntity = showSeatRepository.findById(ss.getId()).orElseThrow();
        sEntity.setLockedUntil(LocalDateTime.now().minusMinutes(2));
        showSeatRepository.save(sEntity);

        // Payment attempt must be rejected by pre-payment lock validator
        PaymentInitiateRequest payReq = new PaymentInitiateRequest(
                booking.getId(),
                "UPI",
                "IDEM-EXPIRED-" + System.currentTimeMillis(),
                true,
                null
        );

        assertThrows(SeatUnavailableException.class, () ->
                paymentService.initiatePayment(testUser1.getEmail(), payReq, true));
    }
}

