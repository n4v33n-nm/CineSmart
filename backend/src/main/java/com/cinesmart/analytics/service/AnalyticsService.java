package com.cinesmart.analytics.service;

import com.cinesmart.analytics.dto.*;
import com.cinesmart.booking.entity.Booking;
import com.cinesmart.booking.entity.BookingStatus;
import com.cinesmart.booking.repository.BookingRepository;
import com.cinesmart.cinema.entity.Cinema;
import com.cinesmart.cinema.repository.CinemaRepository;
import com.cinesmart.movie.entity.Movie;
import com.cinesmart.movie.repository.MovieRepository;
import com.cinesmart.refund.entity.RefundRequest;
import com.cinesmart.refund.entity.RefundStatus;
import com.cinesmart.refund.repository.RefundRequestRepository;
import com.cinesmart.screen.entity.Screen;
import com.cinesmart.screen.repository.ScreenRepository;
import com.cinesmart.show.entity.Show;
import com.cinesmart.show.repository.ShowRepository;
import com.cinesmart.user.repository.UserRepository;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.TypedQuery;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.IsoFields;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class AnalyticsService {

    private final EntityManager entityManager;
    private final BookingRepository bookingRepository;
    private final RefundRequestRepository refundRequestRepository;
    private final MovieRepository movieRepository;
    private final CinemaRepository cinemaRepository;
    private final ScreenRepository screenRepository;
    private final ShowRepository showRepository;
    private final UserRepository userRepository;

    public AnalyticsService(EntityManager entityManager,
                            BookingRepository bookingRepository,
                            RefundRequestRepository refundRequestRepository,
                            MovieRepository movieRepository,
                            CinemaRepository cinemaRepository,
                            ScreenRepository screenRepository,
                            ShowRepository showRepository,
                            UserRepository userRepository) {
        this.entityManager = entityManager;
        this.bookingRepository = bookingRepository;
        this.refundRequestRepository = refundRequestRepository;
        this.movieRepository = movieRepository;
        this.cinemaRepository = cinemaRepository;
        this.screenRepository = screenRepository;
        this.showRepository = showRepository;
        this.userRepository = userRepository;
    }

    /**
     * Builds comprehensive admin analytics overview with all 6 analytics sections and filter options.
     */
    @Transactional(readOnly = true)
    public AdminAnalyticsOverviewDTO getOverview(AnalyticsFilterRequest filter) {
        List<Booking> filteredBookings = fetchFilteredBookings(filter);
        List<RefundRequest> filteredRefunds = fetchFilteredRefunds(filter);

        DashboardKPIDTO kpi = buildKPI(filteredBookings, filteredRefunds);
        RevenueAnalyticsDTO revenue = buildRevenueAnalytics(filteredBookings, filteredRefunds);
        BookingAnalyticsDTO bookings = buildBookingAnalytics(filteredBookings);
        MovieAnalyticsDTO movies = buildMovieAnalytics(filteredBookings);
        TheatreAnalyticsDTO theatres = buildTheatreAnalytics(filteredBookings);
        MarketingRefundAnalyticsDTO marketingRefunds = buildMarketingRefundAnalytics(filteredBookings, filteredRefunds);
        AnalyticsFilterOptionsDTO filterOptions = buildFilterOptions();

        return new AdminAnalyticsOverviewDTO(kpi, revenue, bookings, movies, theatres, marketingRefunds, filterOptions);
    }

    @Transactional(readOnly = true)
    public DashboardKPIDTO getKPI(AnalyticsFilterRequest filter) {
        return buildKPI(fetchFilteredBookings(filter), fetchFilteredRefunds(filter));
    }

    @Transactional(readOnly = true)
    public RevenueAnalyticsDTO getRevenueAnalytics(AnalyticsFilterRequest filter) {
        return buildRevenueAnalytics(fetchFilteredBookings(filter), fetchFilteredRefunds(filter));
    }

    @Transactional(readOnly = true)
    public BookingAnalyticsDTO getBookingAnalytics(AnalyticsFilterRequest filter) {
        return buildBookingAnalytics(fetchFilteredBookings(filter));
    }

    @Transactional(readOnly = true)
    public MovieAnalyticsDTO getMovieAnalytics(AnalyticsFilterRequest filter) {
        return buildMovieAnalytics(fetchFilteredBookings(filter));
    }

    @Transactional(readOnly = true)
    public TheatreAnalyticsDTO getTheatreAnalytics(AnalyticsFilterRequest filter) {
        return buildTheatreAnalytics(fetchFilteredBookings(filter));
    }

    @Transactional(readOnly = true)
    public MarketingRefundAnalyticsDTO getMarketingRefundAnalytics(AnalyticsFilterRequest filter) {
        return buildMarketingRefundAnalytics(fetchFilteredBookings(filter), fetchFilteredRefunds(filter));
    }

    @Transactional(readOnly = true)
    public AnalyticsFilterOptionsDTO getFilterOptions() {
        return buildFilterOptions();
    }

    // =========================================================================
    // Core Analytics Builders
    // =========================================================================

    private DashboardKPIDTO buildKPI(List<Booking> bookings, List<RefundRequest> refunds) {
        long totalUsers = userRepository.count();
        long totalBookings = bookings.size();

        long confirmedBookings = bookings.stream()
                .filter(b -> b.getStatus() == BookingStatus.CONFIRMED)
                .count();

        long cancelledBookings = bookings.stream()
                .filter(b -> b.getStatus() == BookingStatus.CANCELLED)
                .count();

        // Revenue defined strictly as confirmed bookings amount paid
        BigDecimal totalRevenue = bookings.stream()
                .filter(b -> b.getStatus() == BookingStatus.CONFIRMED)
                .map(Booking::getTotalAmount)
                .filter(Objects::nonNull)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        // Refund amount defined as approved/completed refunds
        BigDecimal totalRefundAmount = refunds.stream()
                .filter(r -> r.getStatus() == RefundStatus.COMPLETED || r.getStatus() == RefundStatus.APPROVED)
                .map(RefundRequest::getRefundAmount)
                .filter(Objects::nonNull)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        BigDecimal netRevenue = totalRevenue.subtract(totalRefundAmount);
        if (netRevenue.compareTo(BigDecimal.ZERO) < 0) {
            netRevenue = BigDecimal.ZERO;
        }

        long totalMovies = movieRepository.count();
        long totalTheatres = cinemaRepository.count();

        // Calculate Average Occupancy Percentage
        double avgOccupancy = calculateAverageOccupancy(bookings);

        double confirmedRate = totalBookings > 0
                ? Math.round(((double) confirmedBookings / totalBookings) * 1000.0) / 10.0
                : 0.0;

        double cancellationRate = totalBookings > 0
                ? Math.round(((double) cancelledBookings / totalBookings) * 1000.0) / 10.0
                : 0.0;

        return new DashboardKPIDTO(
                totalUsers,
                totalBookings,
                confirmedBookings,
                cancelledBookings,
                totalRevenue.setScale(2, RoundingMode.HALF_UP),
                totalRefundAmount.setScale(2, RoundingMode.HALF_UP),
                netRevenue.setScale(2, RoundingMode.HALF_UP),
                totalMovies,
                totalTheatres,
                avgOccupancy,
                confirmedRate,
                cancellationRate
        );
    }

    private RevenueAnalyticsDTO buildRevenueAnalytics(List<Booking> bookings, List<RefundRequest> refunds) {
        // Daily Revenue Map
        Map<String, BigDecimal> dailyRev = new TreeMap<>();
        Map<String, Long> dailyCount = new HashMap<>();
        Map<String, Long> dailyCancelled = new HashMap<>();

        // Weekly Revenue Map
        Map<String, BigDecimal> weeklyRev = new TreeMap<>();
        Map<String, Long> weeklyCount = new HashMap<>();

        // Monthly Revenue Map
        Map<String, BigDecimal> monthlyRev = new TreeMap<>();
        Map<String, Long> monthlyCount = new HashMap<>();

        DateTimeFormatter dayFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
        DateTimeFormatter monthFormatter = DateTimeFormatter.ofPattern("MMM yyyy");

        for (Booking b : bookings) {
            LocalDateTime created = b.getCreatedAt() != null ? b.getCreatedAt() : LocalDateTime.now();
            String dayKey = created.format(dayFormatter);
            String weekKey = "W" + created.get(IsoFields.WEEK_OF_WEEK_BASED_YEAR) + " " + created.getYear();
            String monthKey = created.format(monthFormatter);

            if (b.getStatus() == BookingStatus.CONFIRMED) {
                BigDecimal amt = b.getTotalAmount() != null ? b.getTotalAmount() : BigDecimal.ZERO;
                dailyRev.put(dayKey, dailyRev.getOrDefault(dayKey, BigDecimal.ZERO).add(amt));
                dailyCount.put(dayKey, dailyCount.getOrDefault(dayKey, 0L) + 1);

                weeklyRev.put(weekKey, weeklyRev.getOrDefault(weekKey, BigDecimal.ZERO).add(amt));
                weeklyCount.put(weekKey, weeklyCount.getOrDefault(weekKey, 0L) + 1);

                monthlyRev.put(monthKey, monthlyRev.getOrDefault(monthKey, BigDecimal.ZERO).add(amt));
                monthlyCount.put(monthKey, monthlyCount.getOrDefault(monthKey, 0L) + 1);
            } else if (b.getStatus() == BookingStatus.CANCELLED) {
                dailyCancelled.put(dayKey, dailyCancelled.getOrDefault(dayKey, 0L) + 1);
            }
        }

        List<TimeSeriesDataPointDTO> dailyPoints = dailyRev.entrySet().stream()
                .map(e -> new TimeSeriesDataPointDTO(
                        e.getKey(),
                        e.getValue().setScale(2, RoundingMode.HALF_UP),
                        dailyCount.getOrDefault(e.getKey(), 0L),
                        dailyCancelled.getOrDefault(e.getKey(), 0L),
                        BigDecimal.ZERO
                ))
                .collect(Collectors.toList());

        List<TimeSeriesDataPointDTO> weeklyPoints = weeklyRev.entrySet().stream()
                .map(e -> new TimeSeriesDataPointDTO(
                        e.getKey(),
                        e.getValue().setScale(2, RoundingMode.HALF_UP),
                        weeklyCount.getOrDefault(e.getKey(), 0L),
                        0L,
                        BigDecimal.ZERO
                ))
                .collect(Collectors.toList());

        List<TimeSeriesDataPointDTO> monthlyPoints = monthlyRev.entrySet().stream()
                .map(e -> new TimeSeriesDataPointDTO(
                        e.getKey(),
                        e.getValue().setScale(2, RoundingMode.HALF_UP),
                        monthlyCount.getOrDefault(e.getKey(), 0L),
                        0L,
                        BigDecimal.ZERO
                ))
                .collect(Collectors.toList());

        // Calculate Revenue Comparison (Current vs Previous period)
        BigDecimal currentPeriod = BigDecimal.ZERO;
        BigDecimal previousPeriod = BigDecimal.ZERO;

        if (dailyPoints.size() >= 2) {
            int mid = dailyPoints.size() / 2;
            for (int i = 0; i < mid; i++) {
                previousPeriod = previousPeriod.add(dailyPoints.get(i).getRevenue());
            }
            for (int i = mid; i < dailyPoints.size(); i++) {
                currentPeriod = currentPeriod.add(dailyPoints.get(i).getRevenue());
            }
        } else if (!dailyPoints.isEmpty()) {
            currentPeriod = dailyPoints.get(0).getRevenue();
        }

        double growth = 0.0;
        if (previousPeriod.compareTo(BigDecimal.ZERO) > 0) {
            growth = currentPeriod.subtract(previousPeriod)
                    .divide(previousPeriod, 4, RoundingMode.HALF_UP)
                    .multiply(BigDecimal.valueOf(100))
                    .doubleValue();
        }

        return new RevenueAnalyticsDTO(
                dailyPoints,
                weeklyPoints,
                monthlyPoints,
                currentPeriod.setScale(2, RoundingMode.HALF_UP),
                previousPeriod.setScale(2, RoundingMode.HALF_UP),
                Math.round(growth * 10.0) / 10.0
        );
    }

    private BookingAnalyticsDTO buildBookingAnalytics(List<Booking> bookings) {
        DateTimeFormatter dayFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
        DateTimeFormatter monthFormatter = DateTimeFormatter.ofPattern("MMM yyyy");

        Map<String, Long> dailyTotal = new TreeMap<>();
        Map<String, Long> dailyConfirmed = new HashMap<>();
        Map<String, Long> dailyCancelled = new HashMap<>();
        Map<String, BigDecimal> dailyRev = new HashMap<>();

        Map<String, Long> weeklyTotal = new TreeMap<>();
        Map<String, Long> monthlyTotal = new TreeMap<>();

        Map<BookingStatus, Long> statusCounts = new EnumMap<>(BookingStatus.class);

        for (Booking b : bookings) {
            LocalDateTime created = b.getCreatedAt() != null ? b.getCreatedAt() : LocalDateTime.now();
            String dayKey = created.format(dayFormatter);
            String weekKey = "W" + created.get(IsoFields.WEEK_OF_WEEK_BASED_YEAR) + " " + created.getYear();
            String monthKey = created.format(monthFormatter);

            dailyTotal.put(dayKey, dailyTotal.getOrDefault(dayKey, 0L) + 1);
            weeklyTotal.put(weekKey, weeklyTotal.getOrDefault(weekKey, 0L) + 1);
            monthlyTotal.put(monthKey, monthlyTotal.getOrDefault(monthKey, 0L) + 1);

            statusCounts.put(b.getStatus(), statusCounts.getOrDefault(b.getStatus(), 0L) + 1);

            if (b.getStatus() == BookingStatus.CONFIRMED) {
                dailyConfirmed.put(dayKey, dailyConfirmed.getOrDefault(dayKey, 0L) + 1);
                BigDecimal amt = b.getTotalAmount() != null ? b.getTotalAmount() : BigDecimal.ZERO;
                dailyRev.put(dayKey, dailyRev.getOrDefault(dayKey, BigDecimal.ZERO).add(amt));
            } else if (b.getStatus() == BookingStatus.CANCELLED) {
                dailyCancelled.put(dayKey, dailyCancelled.getOrDefault(dayKey, 0L) + 1);
            }
        }

        List<TimeSeriesDataPointDTO> dailyBookings = dailyTotal.entrySet().stream()
                .map(e -> new TimeSeriesDataPointDTO(
                        e.getKey(),
                        dailyRev.getOrDefault(e.getKey(), BigDecimal.ZERO).setScale(2, RoundingMode.HALF_UP),
                        e.getValue(),
                        dailyCancelled.getOrDefault(e.getKey(), 0L),
                        BigDecimal.ZERO
                ))
                .collect(Collectors.toList());

        List<TimeSeriesDataPointDTO> weeklyBookings = weeklyTotal.entrySet().stream()
                .map(e -> new TimeSeriesDataPointDTO(
                        e.getKey(),
                        BigDecimal.ZERO,
                        e.getValue(),
                        0L,
                        BigDecimal.ZERO
                ))
                .collect(Collectors.toList());

        List<TimeSeriesDataPointDTO> monthlyBookings = monthlyTotal.entrySet().stream()
                .map(e -> new TimeSeriesDataPointDTO(
                        e.getKey(),
                        BigDecimal.ZERO,
                        e.getValue(),
                        0L,
                        BigDecimal.ZERO
                ))
                .collect(Collectors.toList());

        long totalBookings = Math.max(1, bookings.size());
        List<CategoryDataPointDTO> statusDistribution = statusCounts.entrySet().stream()
                .map(e -> {
                    double pct = Math.round(((double) e.getValue() / totalBookings) * 1000.0) / 10.0;
                    return new CategoryDataPointDTO(e.getKey().name(), e.getValue(), BigDecimal.ZERO, pct, null);
                })
                .sorted((a, b) -> Long.compare(b.getCount(), a.getCount()))
                .collect(Collectors.toList());

        return new BookingAnalyticsDTO(dailyBookings, weeklyBookings, monthlyBookings, statusDistribution);
    }

    private MovieAnalyticsDTO buildMovieAnalytics(List<Booking> bookings) {
        Map<String, Long> movieBookingCounts = new HashMap<>();
        Map<String, BigDecimal> movieRevenues = new HashMap<>();
        Map<String, String> movieGenres = new HashMap<>();
        Map<String, Long> genreBookingCounts = new HashMap<>();
        Map<String, BigDecimal> genreRevenues = new HashMap<>();

        long totalConfirmedBookings = 0;
        BigDecimal totalConfirmedRevenue = BigDecimal.ZERO;

        for (Booking b : bookings) {
            if (b.getStatus() == BookingStatus.CONFIRMED && b.getShow() != null && b.getShow().getMovie() != null) {
                Movie m = b.getShow().getMovie();
                String title = m.getTitle();
                String genre = m.getGenre() != null ? m.getGenre() : "Other";
                BigDecimal amt = b.getTotalAmount() != null ? b.getTotalAmount() : BigDecimal.ZERO;

                totalConfirmedBookings++;
                totalConfirmedRevenue = totalConfirmedRevenue.add(amt);

                movieBookingCounts.put(title, movieBookingCounts.getOrDefault(title, 0L) + 1);
                movieRevenues.put(title, movieRevenues.getOrDefault(title, BigDecimal.ZERO).add(amt));
                movieGenres.put(title, genre);

                genreBookingCounts.put(genre, genreBookingCounts.getOrDefault(genre, 0L) + 1);
                genreRevenues.put(genre, genreRevenues.getOrDefault(genre, BigDecimal.ZERO).add(amt));
            }
        }

        final long finalTotalBookings = Math.max(1, totalConfirmedBookings);
        final BigDecimal finalTotalRevenue = totalConfirmedRevenue.compareTo(BigDecimal.ZERO) > 0 ? totalConfirmedRevenue : BigDecimal.ONE;

        List<CategoryDataPointDTO> mostBookedMovies = movieBookingCounts.entrySet().stream()
                .map(e -> {
                    double pct = Math.round(((double) e.getValue() / finalTotalBookings) * 1000.0) / 10.0;
                    return new CategoryDataPointDTO(
                            e.getKey(),
                            e.getValue(),
                            movieRevenues.getOrDefault(e.getKey(), BigDecimal.ZERO).setScale(2, RoundingMode.HALF_UP),
                            pct,
                            movieGenres.getOrDefault(e.getKey(), "Cinema")
                    );
                })
                .sorted((a, b) -> Long.compare(b.getCount(), a.getCount()))
                .collect(Collectors.toList());

        List<CategoryDataPointDTO> popularGenres = genreBookingCounts.entrySet().stream()
                .map(e -> {
                    double pct = Math.round(((double) e.getValue() / finalTotalBookings) * 1000.0) / 10.0;
                    return new CategoryDataPointDTO(
                            e.getKey(),
                            e.getValue(),
                            genreRevenues.getOrDefault(e.getKey(), BigDecimal.ZERO).setScale(2, RoundingMode.HALF_UP),
                            pct,
                            null
                    );
                })
                .sorted((a, b) -> Long.compare(b.getCount(), a.getCount()))
                .collect(Collectors.toList());

        List<CategoryDataPointDTO> movieWiseRevenue = movieRevenues.entrySet().stream()
                .map(e -> {
                    double pct = e.getValue()
                            .divide(finalTotalRevenue, 4, RoundingMode.HALF_UP)
                            .multiply(BigDecimal.valueOf(100))
                            .doubleValue();
                    return new CategoryDataPointDTO(
                            e.getKey(),
                            movieBookingCounts.getOrDefault(e.getKey(), 0L),
                            e.getValue().setScale(2, RoundingMode.HALF_UP),
                            Math.round(pct * 10.0) / 10.0,
                            movieGenres.getOrDefault(e.getKey(), "Cinema")
                    );
                })
                .sorted((a, b) -> b.getValue().compareTo(a.getValue()))
                .collect(Collectors.toList());

        return new MovieAnalyticsDTO(mostBookedMovies, popularGenres, movieWiseRevenue);
    }

    private TheatreAnalyticsDTO buildTheatreAnalytics(List<Booking> bookings) {
        Map<String, Long> theatreBookings = new HashMap<>();
        Map<String, BigDecimal> theatreRevenues = new HashMap<>();
        Map<String, String> theatreCities = new HashMap<>();

        Map<String, Long> screenBookedSeats = new HashMap<>();
        Map<String, Integer> screenCapacities = new HashMap<>();
        Map<String, String> screenTheatres = new HashMap<>();

        Map<Long, Long> showBookedSeats = new HashMap<>();
        Map<Long, BigDecimal> showRevenues = new HashMap<>();
        Map<Long, Show> showEntities = new HashMap<>();

        long totalBookings = 0;

        for (Booking b : bookings) {
            if (b.getShow() != null && b.getShow().getScreen() != null) {
                Show show = b.getShow();
                Screen screen = show.getScreen();
                Cinema cinema = screen.getCinema();
                String cinemaName = cinema != null ? cinema.getName() : "Main CineSmart";
                String city = cinema != null ? cinema.getCity() : "Bangalore";
                String screenKey = cinemaName + " - Screen " + screen.getScreenNumber();

                showEntities.put(show.getId(), show);

                if (b.getStatus() == BookingStatus.CONFIRMED) {
                    totalBookings++;
                    BigDecimal amt = b.getTotalAmount() != null ? b.getTotalAmount() : BigDecimal.ZERO;
                    int seatsCount = b.getBookingSeats() != null && !b.getBookingSeats().isEmpty() ? b.getBookingSeats().size() : 1;

                    theatreBookings.put(cinemaName, theatreBookings.getOrDefault(cinemaName, 0L) + 1);
                    theatreRevenues.put(cinemaName, theatreRevenues.getOrDefault(cinemaName, BigDecimal.ZERO).add(amt));
                    theatreCities.put(cinemaName, city);

                    screenBookedSeats.put(screenKey, screenBookedSeats.getOrDefault(screenKey, 0L) + seatsCount);
                    screenCapacities.put(screenKey, screen.getTotalCapacity());
                    screenTheatres.put(screenKey, cinemaName);

                    showBookedSeats.put(show.getId(), showBookedSeats.getOrDefault(show.getId(), 0L) + seatsCount);
                    showRevenues.put(show.getId(), showRevenues.getOrDefault(show.getId(), BigDecimal.ZERO).add(amt));
                }
            }
        }

        final long finalTotal = Math.max(1, totalBookings);

        List<CategoryDataPointDTO> theatreWiseBookings = theatreBookings.entrySet().stream()
                .map(e -> {
                    double pct = Math.round(((double) e.getValue() / finalTotal) * 1000.0) / 10.0;
                    return new CategoryDataPointDTO(
                            e.getKey(),
                            e.getValue(),
                            theatreRevenues.getOrDefault(e.getKey(), BigDecimal.ZERO).setScale(2, RoundingMode.HALF_UP),
                            pct,
                            theatreCities.getOrDefault(e.getKey(), "City")
                    );
                })
                .sorted((a, b) -> Long.compare(b.getCount(), a.getCount()))
                .collect(Collectors.toList());

        List<CategoryDataPointDTO> theatreWiseRevenue = theatreRevenues.entrySet().stream()
                .map(e -> {
                    return new CategoryDataPointDTO(
                            e.getKey(),
                            theatreBookings.getOrDefault(e.getKey(), 0L),
                            e.getValue().setScale(2, RoundingMode.HALF_UP),
                            0.0,
                            theatreCities.getOrDefault(e.getKey(), "City")
                    );
                })
                .sorted((a, b) -> b.getValue().compareTo(a.getValue()))
                .collect(Collectors.toList());

        List<CategoryDataPointDTO> screenOccupancy = screenCapacities.entrySet().stream()
                .map(e -> {
                    long booked = screenBookedSeats.getOrDefault(e.getKey(), 0L);
                    int cap = Math.max(1, e.getValue());
                    double occ = Math.min(100.0, Math.round(((double) booked / cap) * 1000.0) / 10.0);
                    return new CategoryDataPointDTO(
                            e.getKey(),
                            booked,
                            BigDecimal.valueOf(cap),
                            occ,
                            screenTheatres.getOrDefault(e.getKey(), "")
                    );
                })
                .sorted((a, b) -> Double.compare(b.getPercentage(), a.getPercentage()))
                .collect(Collectors.toList());

        DateTimeFormatter timeFmt = DateTimeFormatter.ofPattern("MMM dd HH:mm");
        List<CategoryDataPointDTO> showPerformance = showEntities.entrySet().stream()
                .map(e -> {
                    Show s = e.getValue();
                    Long showId = e.getKey();
                    long booked = showBookedSeats.getOrDefault(showId, 0L);
                    BigDecimal rev = showRevenues.getOrDefault(showId, BigDecimal.ZERO).setScale(2, RoundingMode.HALF_UP);
                    int cap = s.getScreen() != null ? s.getScreen().getTotalCapacity() : 100;
                    double occ = Math.min(100.0, Math.round(((double) booked / Math.max(1, cap)) * 1000.0) / 10.0);
                    String label = (s.getMovie() != null ? s.getMovie().getTitle() : "Movie") +
                            " (" + s.getStartTime().format(timeFmt) + ")";
                    return new CategoryDataPointDTO(label, booked, rev, occ, "Show #" + showId);
                })
                .sorted((a, b) -> b.getValue().compareTo(a.getValue()))
                .limit(10)
                .collect(Collectors.toList());

        return new TheatreAnalyticsDTO(theatreWiseBookings, theatreWiseRevenue, screenOccupancy, showPerformance);
    }

    private MarketingRefundAnalyticsDTO buildMarketingRefundAnalytics(List<Booking> bookings, List<RefundRequest> refunds) {
        // 1. Coupon Usage
        Map<String, Long> couponCounts = new HashMap<>();
        Map<String, BigDecimal> couponDiscounts = new HashMap<>();

        long loyaltyEarned = 0;
        long loyaltyRedeemed = 0;
        BigDecimal loyaltyDiscount = BigDecimal.ZERO;

        for (Booking b : bookings) {
            if (b.getCouponCode() != null && !b.getCouponCode().isBlank()) {
                String code = b.getCouponCode().trim().toUpperCase();
                couponCounts.put(code, couponCounts.getOrDefault(code, 0L) + 1);
                BigDecimal disc = b.getDiscountAmount() != null ? b.getDiscountAmount() : BigDecimal.ZERO;
                couponDiscounts.put(code, couponDiscounts.getOrDefault(code, BigDecimal.ZERO).add(disc));
            }

            if (b.getStatus() == BookingStatus.CONFIRMED && b.getTotalAmount() != null) {
                // In CineSmart, patrons earn 1 point per 10 INR spent (10% back in points)
                loyaltyEarned += b.getTotalAmount().divide(BigDecimal.TEN, 0, RoundingMode.FLOOR).longValue();
            }

            if (b.getLoyaltyPointsRedeemed() != null) {
                loyaltyRedeemed += b.getLoyaltyPointsRedeemed();
            }
            if (b.getLoyaltyDiscountAmount() != null) {
                loyaltyDiscount = loyaltyDiscount.add(b.getLoyaltyDiscountAmount());
            }
        }

        List<CategoryDataPointDTO> couponUsage = couponCounts.entrySet().stream()
                .map(e -> new CategoryDataPointDTO(
                        e.getKey(),
                        e.getValue(),
                        couponDiscounts.getOrDefault(e.getKey(), BigDecimal.ZERO).setScale(2, RoundingMode.HALF_UP),
                        0.0,
                        "Times Applied"
                ))
                .sorted((a, b) -> Long.compare(b.getCount(), a.getCount()))
                .collect(Collectors.toList());

        // 2. Cancellation Trends (by Day)
        DateTimeFormatter dayFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
        Map<String, Long> dailyCancels = new TreeMap<>();
        for (Booking b : bookings) {
            if (b.getStatus() == BookingStatus.CANCELLED) {
                LocalDateTime dt = b.getCreatedAt() != null ? b.getCreatedAt() : LocalDateTime.now();
                String dayKey = dt.format(dayFormatter);
                dailyCancels.put(dayKey, dailyCancels.getOrDefault(dayKey, 0L) + 1);
            }
        }

        List<TimeSeriesDataPointDTO> cancellationTrends = dailyCancels.entrySet().stream()
                .map(e -> new TimeSeriesDataPointDTO(e.getKey(), BigDecimal.ZERO, 0L, e.getValue(), BigDecimal.ZERO))
                .collect(Collectors.toList());

        // 3. Refund Status Distribution & Amounts
        Map<RefundStatus, Long> refundStatusCounts = new EnumMap<>(RefundStatus.class);
        Map<RefundStatus, BigDecimal> refundStatusAmounts = new EnumMap<>(RefundStatus.class);
        BigDecimal totalRefunded = BigDecimal.ZERO;
        BigDecimal totalFees = BigDecimal.ZERO;

        for (RefundRequest r : refunds) {
            refundStatusCounts.put(r.getStatus(), refundStatusCounts.getOrDefault(r.getStatus(), 0L) + 1);
            BigDecimal amt = r.getRefundAmount() != null ? r.getRefundAmount() : BigDecimal.ZERO;
            refundStatusAmounts.put(r.getStatus(), refundStatusAmounts.getOrDefault(r.getStatus(), BigDecimal.ZERO).add(amt));

            if (r.getStatus() == RefundStatus.COMPLETED || r.getStatus() == RefundStatus.APPROVED) {
                totalRefunded = totalRefunded.add(amt);
            }
            if (r.getCancellationFee() != null) {
                totalFees = totalFees.add(r.getCancellationFee());
            }
        }

        long totalRefunds = Math.max(1, refunds.size());
        List<CategoryDataPointDTO> refundDistribution = refundStatusCounts.entrySet().stream()
                .map(e -> {
                    double pct = Math.round(((double) e.getValue() / totalRefunds) * 1000.0) / 10.0;
                    return new CategoryDataPointDTO(
                            e.getKey().name(),
                            e.getValue(),
                            refundStatusAmounts.getOrDefault(e.getKey(), BigDecimal.ZERO).setScale(2, RoundingMode.HALF_UP),
                            pct,
                            null
                    );
                })
                .sorted((a, b) -> Long.compare(b.getCount(), a.getCount()))
                .collect(Collectors.toList());

        return new MarketingRefundAnalyticsDTO(
                couponUsage,
                loyaltyEarned,
                loyaltyRedeemed,
                loyaltyDiscount.setScale(2, RoundingMode.HALF_UP),
                cancellationTrends,
                refundDistribution,
                totalRefunded.setScale(2, RoundingMode.HALF_UP),
                totalFees.setScale(2, RoundingMode.HALF_UP)
        );
    }

    private AnalyticsFilterOptionsDTO buildFilterOptions() {
        List<Map<String, Object>> moviesList = movieRepository.findAll().stream()
                .map(m -> {
                    Map<String, Object> map = new HashMap<>();
                    map.put("id", m.getId());
                    map.put("title", m.getTitle());
                    map.put("genre", m.getGenre());
                    return map;
                })
                .collect(Collectors.toList());

        List<Map<String, Object>> cinemasList = cinemaRepository.findAll().stream()
                .map(c -> {
                    Map<String, Object> map = new HashMap<>();
                    map.put("id", c.getId());
                    map.put("name", c.getName());
                    map.put("city", c.getCity());
                    return map;
                })
                .collect(Collectors.toList());

        DateTimeFormatter showTimeFmt = DateTimeFormatter.ofPattern("MMM dd HH:mm");
        List<Map<String, Object>> showsList = showRepository.findAll().stream()
                .map(s -> {
                    Map<String, Object> map = new HashMap<>();
                    map.put("id", s.getId());
                    String movieTitle = s.getMovie() != null ? s.getMovie().getTitle() : "Movie";
                    String cinemaName = s.getScreen() != null && s.getScreen().getCinema() != null
                            ? s.getScreen().getCinema().getName() : "Cinema";
                    map.put("label", movieTitle + " • " + cinemaName + " (" + s.getStartTime().format(showTimeFmt) + ")");
                    return map;
                })
                .collect(Collectors.toList());

        List<String> statuses = Arrays.stream(BookingStatus.values())
                .map(Enum::name)
                .collect(Collectors.toList());

        return new AnalyticsFilterOptionsDTO(moviesList, cinemasList, showsList, statuses);
    }

    private double calculateAverageOccupancy(List<Booking> bookings) {
        Set<Long> showIds = new HashSet<>();
        long totalBookedSeats = 0;

        for (Booking b : bookings) {
            if (b.getStatus() == BookingStatus.CONFIRMED && b.getShow() != null) {
                showIds.add(b.getShow().getId());
                int seats = (b.getBookingSeats() != null && !b.getBookingSeats().isEmpty())
                        ? b.getBookingSeats().size() : 1;
                totalBookedSeats += seats;
            }
        }

        if (showIds.isEmpty()) {
            return 0.0;
        }

        long totalCapacity = 0;
        for (Long sId : showIds) {
            Show show = showRepository.findById(sId).orElse(null);
            if (show != null && show.getScreen() != null) {
                totalCapacity += show.getScreen().getTotalCapacity();
            }
        }

        if (totalCapacity == 0) {
            return 0.0;
        }

        double ratio = (double) totalBookedSeats / totalCapacity;
        return Math.min(100.0, Math.round(ratio * 1000.0) / 10.0);
    }

    // =========================================================================
    // Filtered Query Execution
    // =========================================================================

    private List<Booking> fetchFilteredBookings(AnalyticsFilterRequest filter) {
        StringBuilder jpql = new StringBuilder("SELECT DISTINCT b FROM Booking b ");
        jpql.append("LEFT JOIN FETCH b.show s ");
        jpql.append("LEFT JOIN FETCH s.movie m ");
        jpql.append("LEFT JOIN FETCH s.screen sc ");
        jpql.append("LEFT JOIN FETCH sc.cinema c ");
        jpql.append("WHERE 1=1 ");

        Map<String, Object> params = new HashMap<>();

        if (filter != null) {
            if (filter.getStartDate() != null) {
                jpql.append("AND b.createdAt >= :startDate ");
                params.put("startDate", filter.getStartDate().atStartOfDay());
            }
            if (filter.getEndDate() != null) {
                jpql.append("AND b.createdAt <= :endDate ");
                params.put("endDate", filter.getEndDate().atTime(23, 59, 59));
            }
            if (filter.getMovieId() != null) {
                jpql.append("AND s.movie.id = :movieId ");
                params.put("movieId", filter.getMovieId());
            }
            if (filter.getCinemaId() != null) {
                jpql.append("AND sc.cinema.id = :cinemaId ");
                params.put("cinemaId", filter.getCinemaId());
            }
            if (filter.getShowId() != null) {
                jpql.append("AND s.id = :showId ");
                params.put("showId", filter.getShowId());
            }
            if (filter.getStatus() != null) {
                jpql.append("AND b.status = :status ");
                params.put("status", filter.getStatus());
            }
        }

        jpql.append("ORDER BY b.createdAt ASC");

        TypedQuery<Booking> query = entityManager.createQuery(jpql.toString(), Booking.class);
        params.forEach(query::setParameter);

        return query.getResultList();
    }

    private List<RefundRequest> fetchFilteredRefunds(AnalyticsFilterRequest filter) {
        StringBuilder jpql = new StringBuilder("SELECT r FROM RefundRequest r WHERE 1=1 ");
        Map<String, Object> params = new HashMap<>();

        if (filter != null) {
            if (filter.getStartDate() != null) {
                jpql.append("AND r.requestedAt >= :startDate ");
                params.put("startDate", filter.getStartDate().atStartOfDay());
            }
            if (filter.getEndDate() != null) {
                jpql.append("AND r.requestedAt <= :endDate ");
                params.put("endDate", filter.getEndDate().atTime(23, 59, 59));
            }
            if (filter.getMovieId() != null) {
                jpql.append("AND r.booking.show.movie.id = :movieId ");
                params.put("movieId", filter.getMovieId());
            }
            if (filter.getCinemaId() != null) {
                jpql.append("AND r.booking.show.screen.cinema.id = :cinemaId ");
                params.put("cinemaId", filter.getCinemaId());
            }
        }

        jpql.append("ORDER BY r.requestedAt ASC");

        TypedQuery<RefundRequest> query = entityManager.createQuery(jpql.toString(), RefundRequest.class);
        params.forEach(query::setParameter);

        return query.getResultList();
    }
}
