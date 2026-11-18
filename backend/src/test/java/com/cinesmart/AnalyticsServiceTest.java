package com.cinesmart;

import com.cinesmart.analytics.dto.*;
import com.cinesmart.analytics.service.AnalyticsService;
import com.cinesmart.booking.entity.Booking;
import com.cinesmart.booking.entity.BookingSeat;
import com.cinesmart.booking.entity.BookingStatus;
import com.cinesmart.booking.repository.BookingRepository;
import com.cinesmart.cinema.entity.Cinema;
import com.cinesmart.cinema.entity.ScreenType;
import com.cinesmart.cinema.repository.CinemaRepository;
import com.cinesmart.movie.entity.Movie;
import com.cinesmart.movie.repository.MovieRepository;
import com.cinesmart.refund.entity.CancellationPolicy;
import com.cinesmart.refund.entity.RefundRequest;
import com.cinesmart.refund.entity.RefundStatus;
import com.cinesmart.refund.repository.RefundRequestRepository;
import com.cinesmart.screen.entity.Screen;
import com.cinesmart.screen.repository.ScreenRepository;
import com.cinesmart.seat.entity.Seat;
import com.cinesmart.seat.entity.SeatTier;
import com.cinesmart.show.entity.Show;
import com.cinesmart.show.entity.ShowSeat;
import com.cinesmart.show.repository.ShowRepository;
import com.cinesmart.user.entity.User;
import com.cinesmart.user.entity.UserRole;
import com.cinesmart.user.repository.UserRepository;
import jakarta.persistence.EntityManager;
import jakarta.persistence.TypedQuery;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
public class AnalyticsServiceTest {

    @Mock
    private EntityManager entityManager;

    @Mock
    private BookingRepository bookingRepository;

    @Mock
    private RefundRequestRepository refundRequestRepository;

    @Mock
    private MovieRepository movieRepository;

    @Mock
    private CinemaRepository cinemaRepository;

    @Mock
    private ScreenRepository screenRepository;

    @Mock
    private ShowRepository showRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private TypedQuery<Booking> bookingTypedQuery;

    @Mock
    private TypedQuery<RefundRequest> refundTypedQuery;

    @InjectMocks
    private AnalyticsService analyticsService;

    private Movie testMovie;
    private Cinema testCinema;
    private Screen testScreen;
    private Show testShow;
    private User testUser;
    private Booking confirmedBooking1;
    private Booking confirmedBooking2;
    private Booking cancelledBooking;
    private Booking failedBooking;
    private RefundRequest completedRefund;

    @BeforeEach
    void setUp() {
        testMovie = new Movie("Interstellar Odyssey", "Space exploration", 165, "English", "Sci-Fi", "PG-13", "https://img.com/1.jpg");
        testMovie.setId(1L);

        testCinema = new Cinema("CineSmart Central", "Bangalore", "MG Road", "9876543210");
        testCinema.setId(1L);

        testScreen = new Screen(testCinema, 1, ScreenType.IMAX_3D, 100);
        testScreen.setId(1L);

        testShow = new Show(testMovie, testScreen, LocalDateTime.now().plusDays(1), LocalDateTime.now().plusDays(1).plusHours(2), BigDecimal.valueOf(250));
        testShow.setId(10L);

        testUser = new User("customer@test.com", "hash", "Customer One", "9876543210", UserRole.ROLE_CUSTOMER);
        testUser.setId(5L);

        // Confirmed Booking 1: Rs. 500
        confirmedBooking1 = new Booking("CS-BKG-01", testUser, testShow, BigDecimal.valueOf(500));
        confirmedBooking1.setId(101L);
        confirmedBooking1.setStatus(BookingStatus.CONFIRMED);
        confirmedBooking1.setCouponCode("WELCOME50");
        confirmedBooking1.setDiscountAmount(BigDecimal.valueOf(50));
        confirmedBooking1.setLoyaltyPointsRedeemed(20);
        confirmedBooking1.setLoyaltyDiscountAmount(BigDecimal.valueOf(20));
        confirmedBooking1.setCreatedAt(LocalDateTime.now().minusDays(2));

        Seat seat1 = new Seat(testScreen, "A", 1, SeatTier.STANDARD, 0, 0);
        ShowSeat showSeat1 = new ShowSeat(testShow, seat1, BigDecimal.valueOf(250));
        BookingSeat bs1 = new BookingSeat(confirmedBooking1, showSeat1, BigDecimal.valueOf(250), "A-1", "STANDARD");
        confirmedBooking1.getBookingSeats().add(bs1);

        // Confirmed Booking 2: Rs. 250
        confirmedBooking2 = new Booking("CS-BKG-02", testUser, testShow, BigDecimal.valueOf(250));
        confirmedBooking2.setId(102L);
        confirmedBooking2.setStatus(BookingStatus.CONFIRMED);
        confirmedBooking2.setCreatedAt(LocalDateTime.now().minusDays(1));

        Seat seat2 = new Seat(testScreen, "A", 2, SeatTier.STANDARD, 0, 1);
        ShowSeat showSeat2 = new ShowSeat(testShow, seat2, BigDecimal.valueOf(250));
        BookingSeat bs2 = new BookingSeat(confirmedBooking2, showSeat2, BigDecimal.valueOf(250), "A-2", "STANDARD");
        confirmedBooking2.getBookingSeats().add(bs2);

        // Cancelled Booking: Rs. 250
        cancelledBooking = new Booking("CS-BKG-03", testUser, testShow, BigDecimal.valueOf(250));
        cancelledBooking.setId(103L);
        cancelledBooking.setStatus(BookingStatus.CANCELLED);
        cancelledBooking.setCreatedAt(LocalDateTime.now().minusDays(3));

        // Payment Failed Booking: Rs. 500 (Must NOT count toward revenue)
        failedBooking = new Booking("CS-BKG-04", testUser, testShow, BigDecimal.valueOf(500));
        failedBooking.setId(104L);
        failedBooking.setStatus(BookingStatus.PAYMENT_FAILED);
        failedBooking.setCreatedAt(LocalDateTime.now().minusDays(4));

        // Completed Refund for cancelled booking: Rs. 200
        CancellationPolicy policy = new CancellationPolicy("Standard", "Full", 24, null, BigDecimal.valueOf(80), BigDecimal.valueOf(20), 1);
        completedRefund = new RefundRequest(
                "REF-001", cancelledBooking, null, testUser, policy,
                BigDecimal.valueOf(250), BigDecimal.valueOf(50), BigDecimal.valueOf(200),
                BigDecimal.valueOf(80), BigDecimal.valueOf(30.0), "User cancelled"
        );
        completedRefund.setStatus(RefundStatus.COMPLETED);
    }

    private void mockEntityManagerQueries(List<Booking> bookings, List<RefundRequest> refunds) {
        when(entityManager.createQuery(anyString(), eq(Booking.class))).thenReturn(bookingTypedQuery);
        when(bookingTypedQuery.getResultList()).thenReturn(bookings);

        when(entityManager.createQuery(anyString(), eq(RefundRequest.class))).thenReturn(refundTypedQuery);
        when(refundTypedQuery.getResultList()).thenReturn(refunds);
    }

    @Test
    @DisplayName("KPI calculation sums confirmed revenue strictly excluding failed/cancelled bookings")
    void testKPICalculation_StrictRevenueAndCounts() {
        List<Booking> bookings = List.of(confirmedBooking1, confirmedBooking2, cancelledBooking, failedBooking);
        List<RefundRequest> refunds = List.of(completedRefund);

        mockEntityManagerQueries(bookings, refunds);
        when(userRepository.count()).thenReturn(15L);
        when(movieRepository.count()).thenReturn(5L);
        when(cinemaRepository.count()).thenReturn(2L);
        when(showRepository.findById(10L)).thenReturn(Optional.of(testShow));

        DashboardKPIDTO kpi = analyticsService.getKPI(new AnalyticsFilterRequest());

        assertNotNull(kpi);
        assertEquals(15L, kpi.getTotalUsers());
        assertEquals(4L, kpi.getTotalBookings());
        assertEquals(2L, kpi.getConfirmedBookings());
        assertEquals(1L, kpi.getCancelledBookings());

        // Total confirmed revenue = 500 + 250 = 750.00 (failed 500 and cancelled 250 excluded!)
        assertEquals(new BigDecimal("750.00"), kpi.getTotalRevenue());

        // Refund amount = 200.00
        assertEquals(new BigDecimal("200.00"), kpi.getRefundAmount());

        // Net revenue = 750 - 200 = 550.00
        assertEquals(new BigDecimal("550.00"), kpi.getNetRevenue());

        assertEquals(5L, kpi.getTotalMovies());
        assertEquals(2L, kpi.getTotalTheatres());

        // Occupancy: 2 booked seats in 100 capacity = 2.0%
        assertEquals(2.0, kpi.getAverageOccupancyPercentage());
    }

    @Test
    @DisplayName("Empty database returns zero values safely without NullPointerException")
    void testEmptyDatabase_SafeZeroMetrics() {
        mockEntityManagerQueries(Collections.emptyList(), Collections.emptyList());
        when(userRepository.count()).thenReturn(0L);
        when(movieRepository.count()).thenReturn(0L);
        when(cinemaRepository.count()).thenReturn(0L);

        DashboardKPIDTO kpi = analyticsService.getKPI(new AnalyticsFilterRequest());

        assertNotNull(kpi);
        assertEquals(0L, kpi.getTotalUsers());
        assertEquals(0L, kpi.getTotalBookings());
        assertEquals(BigDecimal.ZERO.setScale(2), kpi.getTotalRevenue());
        assertEquals(BigDecimal.ZERO.setScale(2), kpi.getNetRevenue());
        assertEquals(0.0, kpi.getAverageOccupancyPercentage());
    }

    @Test
    @DisplayName("Revenue and booking time-series analytics aggregate correctly by day and status")
    void testRevenueAndBookingAnalytics_Aggregates() {
        List<Booking> bookings = List.of(confirmedBooking1, confirmedBooking2, cancelledBooking);
        List<RefundRequest> refunds = List.of(completedRefund);

        mockEntityManagerQueries(bookings, refunds);

        RevenueAnalyticsDTO rev = analyticsService.getRevenueAnalytics(new AnalyticsFilterRequest());
        assertNotNull(rev);
        assertNotNull(rev.getDailyRevenue());
        assertFalse(rev.getDailyRevenue().isEmpty());

        BookingAnalyticsDTO bkg = analyticsService.getBookingAnalytics(new AnalyticsFilterRequest());
        assertNotNull(bkg);
        assertNotNull(bkg.getStatusDistribution());
        // Status distribution should contain CONFIRMED and CANCELLED
        assertTrue(bkg.getStatusDistribution().stream().anyMatch(s -> s.getName().equals("CONFIRMED")));
        assertTrue(bkg.getStatusDistribution().stream().anyMatch(s -> s.getName().equals("CANCELLED")));
    }

    @Test
    @DisplayName("Movie and theatre analytics group correctly by title, genre, and cinema")
    void testMovieAndTheatreAnalytics_GroupsProperly() {
        List<Booking> bookings = List.of(confirmedBooking1, confirmedBooking2);
        mockEntityManagerQueries(bookings, Collections.emptyList());

        MovieAnalyticsDTO movieAnalytics = analyticsService.getMovieAnalytics(new AnalyticsFilterRequest());
        assertNotNull(movieAnalytics);
        assertFalse(movieAnalytics.getMostBookedMovies().isEmpty());
        assertEquals("Interstellar Odyssey", movieAnalytics.getMostBookedMovies().get(0).getName());
        assertEquals(2L, movieAnalytics.getMostBookedMovies().get(0).getCount());

        TheatreAnalyticsDTO theatreAnalytics = analyticsService.getTheatreAnalytics(new AnalyticsFilterRequest());
        assertNotNull(theatreAnalytics);
        assertFalse(theatreAnalytics.getTheatreWiseBookings().isEmpty());
        assertEquals("CineSmart Central", theatreAnalytics.getTheatreWiseBookings().get(0).getName());
    }

    @Test
    @DisplayName("Marketing and refund analytics capture coupon usage, loyalty points, and cancellation trends")
    void testMarketingRefundAnalytics_CapturesMetrics() {
        List<Booking> bookings = List.of(confirmedBooking1, confirmedBooking2, cancelledBooking);
        List<RefundRequest> refunds = List.of(completedRefund);

        mockEntityManagerQueries(bookings, refunds);

        MarketingRefundAnalyticsDTO mkt = analyticsService.getMarketingRefundAnalytics(new AnalyticsFilterRequest());
        assertNotNull(mkt);
        // Coupon WELCOME50 applied once with Rs 50 discount
        assertFalse(mkt.getCouponUsage().isEmpty());
        assertEquals("WELCOME50", mkt.getCouponUsage().get(0).getName());
        assertEquals(1L, mkt.getCouponUsage().get(0).getCount());
        assertEquals(new BigDecimal("50.00"), mkt.getCouponUsage().get(0).getValue());

        // Loyalty points earned = floor(500/100) + floor(250/100) = 5 + 2 = 7, redeemed = 20, discount = 20.00
        assertEquals(7L, mkt.getLoyaltyPointsEarnedTotal());
        assertEquals(20L, mkt.getLoyaltyPointsRedeemedTotal());
        assertEquals(new BigDecimal("20.00"), mkt.getLoyaltyDiscountTotal());

        // Completed refund = Rs 200
        assertEquals(new BigDecimal("200.00"), mkt.getTotalRefundedAmount());
    }
}
