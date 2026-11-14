package com.cinesmart.recommendation.service;

import com.cinesmart.booking.entity.Booking;
import com.cinesmart.booking.repository.BookingRepository;
import com.cinesmart.common.exception.ResourceNotFoundException;
import com.cinesmart.movie.dto.MovieDTO;
import com.cinesmart.movie.entity.Movie;
import com.cinesmart.movie.repository.MovieRepository;
import com.cinesmart.recommendation.dto.*;
import com.cinesmart.user.entity.User;
import com.cinesmart.user.repository.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class RecommendationService {

    private final MovieRepository movieRepository;
    private final BookingRepository bookingRepository;
    private final UserRepository userRepository;

    public static final List<String> ALL_AVAILABLE_GENRES = List.of(
            "Action", "Sci-Fi", "Drama", "Animation", "Adventure", "Crime", "Comedy", "Thriller", "Horror"
    );

    public RecommendationService(MovieRepository movieRepository,
                                 BookingRepository bookingRepository,
                                 UserRepository userRepository) {
        this.movieRepository = movieRepository;
        this.bookingRepository = bookingRepository;
        this.userRepository = userRepository;
    }

    /**
     * Retrieves personalized recommendations for a user (or cold-start if email is null or user has no data).
     */
    @Transactional(readOnly = true)
    public List<MovieRecommendationDTO> getPersonalizedRecommendations(String userEmail, int limit) {
        List<Movie> activeMovies = movieRepository.findByIsActiveTrue();
        if (activeMovies.isEmpty()) {
            return Collections.emptyList();
        }

        User user = null;
        if (userEmail != null && !userEmail.isBlank()) {
            user = userRepository.findByEmail(userEmail.trim()).orElse(null);
        }

        return computePersonalizedRecommendations(activeMovies, user, limit);
    }

    /**
     * Builds all 6 home recommendation sections.
     */
    @Transactional(readOnly = true)
    public HomeRecommendationSectionsDTO getHomeSections(String userEmail) {
        List<Movie> activeMovies = movieRepository.findByIsActiveTrue();
        if (activeMovies.isEmpty()) {
            return new HomeRecommendationSectionsDTO(
                    Collections.emptyList(), null, Collections.emptyList(),
                    Collections.emptyList(), Collections.emptyList(), Collections.emptyList(),
                    false, Collections.emptyList()
            );
        }

        User user = null;
        if (userEmail != null && !userEmail.isBlank()) {
            user = userRepository.findByEmail(userEmail.trim()).orElse(null);
        }

        // 1. Personalized recommendations
        List<MovieRecommendationDTO> recommendedForYou = computePersonalizedRecommendations(activeMovies, user, 6);

        // 2. "Because You Watched Similar Movies"
        BecauseYouWatchedDTO becauseYouWatched = null;
        if (user != null) {
            List<Booking> userBookings = bookingRepository.findConfirmedByUserIdOrderByCreatedAtDesc(user.getId());
            if (!userBookings.isEmpty()) {
                Booking latestBooking = userBookings.get(0);
                Movie sourceMovie = latestBooking.getShow().getMovie();
                List<MovieRecommendationDTO> similar = computeSimilarMovies(sourceMovie, activeMovies, 4);
                if (!similar.isEmpty()) {
                    becauseYouWatched = new BecauseYouWatchedDTO(MovieDTO.fromEntity(sourceMovie), similar);
                }
            }
        }

        // Pre-compute booking stats for section ranking
        Map<Long, Long> allTimeBookings = fetchAllTimeBookingCounts();
        Map<Long, Long> recentBookings = fetchRecentBookingCounts(7);
        long maxRecent = recentBookings.values().stream().max(Long::compare).orElse(1L);
        long maxAllTime = allTimeBookings.values().stream().max(Long::compare).orElse(1L);

        // 3. Trending Movies (ordered by recent booking velocity + review count)
        List<MovieRecommendationDTO> trendingMovies = activeMovies.stream()
                .sorted((m1, m2) -> {
                    double t1 = (double) recentBookings.getOrDefault(m1.getId(), 0L) / Math.max(1, maxRecent);
                    double t2 = (double) recentBookings.getOrDefault(m2.getId(), 0L) / Math.max(1, maxRecent);
                    int cmp = Double.compare(t2, t1);
                    if (cmp != 0) return cmp;
                    return Double.compare(m2.getRating(), m1.getRating());
                })
                .limit(6)
                .map(m -> {
                    long recentCount = recentBookings.getOrDefault(m.getId(), 0L);
                    String reason = recentCount > 0
                            ? "High booking velocity (" + recentCount + " tickets this week)"
                            : "Trending in theaters right now";
                    int matchPct = (int) Math.min(99, Math.round(82 + (m.getRating() * 1.5)));
                    return new MovieRecommendationDTO(
                            MovieDTO.fromEntity(m),
                            0.88,
                            matchPct,
                            reason,
                            "TRENDING",
                            "🔥 Trending Now"
                    );
                })
                .collect(Collectors.toList());

        // 4. Popular Movies (all-time box office)
        List<MovieRecommendationDTO> popularMovies = activeMovies.stream()
                .sorted((m1, m2) -> {
                    long b1 = allTimeBookings.getOrDefault(m1.getId(), 0L);
                    long b2 = allTimeBookings.getOrDefault(m2.getId(), 0L);
                    int cmp = Long.compare(b2, b1);
                    if (cmp != 0) return cmp;
                    return Integer.compare(m2.getTotalReviews(), m1.getTotalReviews());
                })
                .limit(6)
                .map(m -> {
                    long totalCount = allTimeBookings.getOrDefault(m.getId(), 0L);
                    String reason = totalCount > 0
                            ? "All-time box office hit (" + totalCount + "+ bookings)"
                            : "Popular among CineSmart patrons";
                    int matchPct = (int) Math.min(99, Math.round(80 + (m.getRating() * 1.8)));
                    return new MovieRecommendationDTO(
                            MovieDTO.fromEntity(m),
                            0.85,
                            matchPct,
                            reason,
                            "POPULAR",
                            "⭐ Box Office Hit"
                    );
                })
                .collect(Collectors.toList());

        // 5. New Releases (by release date DESC)
        List<MovieRecommendationDTO> newReleases = activeMovies.stream()
                .sorted((m1, m2) -> m2.getReleaseDate().compareTo(m1.getReleaseDate()))
                .limit(6)
                .map(m -> {
                    String reason = "Released " + m.getReleaseDate().toString();
                    return new MovieRecommendationDTO(
                            MovieDTO.fromEntity(m),
                            0.82,
                            88,
                            reason,
                            "NEW_RELEASE",
                            "🎬 New in Theaters"
                    );
                })
                .collect(Collectors.toList());

        // 6. Top Rated Movies (by rating DESC, total reviews DESC)
        List<MovieRecommendationDTO> topRated = activeMovies.stream()
                .sorted((m1, m2) -> {
                    int cmp = Double.compare(m2.getRating(), m1.getRating());
                    if (cmp != 0) return cmp;
                    return Integer.compare(m2.getTotalReviews(), m1.getTotalReviews());
                })
                .limit(6)
                .map(m -> {
                    String reason = "Critically acclaimed • " + m.getRating() + "/10 (" + m.getTotalReviews() + " reviews)";
                    int matchPct = (int) Math.min(99, Math.round(m.getRating() * 10));
                    return new MovieRecommendationDTO(
                            MovieDTO.fromEntity(m),
                            m.getRating() / 10.0,
                            matchPct,
                            reason,
                            "TOP_RATED",
                            "🏆 Top Rated"
                    );
                })
                .collect(Collectors.toList());

        boolean isPersonalized = (user != null && (
                (user.getPreferredGenres() != null && !user.getPreferredGenres().isBlank()) ||
                !bookingRepository.findConfirmedByUserIdOrderByCreatedAtDesc(user.getId()).isEmpty()
        ));

        List<String> userPrefGenres = parseGenres(user != null ? user.getPreferredGenres() : null);

        return new HomeRecommendationSectionsDTO(
                recommendedForYou,
                becauseYouWatched,
                trendingMovies,
                popularMovies,
                newReleases,
                topRated,
                isPersonalized,
                userPrefGenres
        );
    }

    /**
     * Returns similar movies to a given movie (for Movie Details "More Like This" section).
     */
    @Transactional(readOnly = true)
    public List<MovieRecommendationDTO> getSimilarMovies(Long movieId, int limit) {
        Movie movie = movieRepository.findById(movieId)
                .orElseThrow(() -> new ResourceNotFoundException("Movie", "id", movieId));

        List<Movie> activeMovies = movieRepository.findByIsActiveTrue();
        return computeSimilarMovies(movie, activeMovies, limit);
    }

    /**
     * Get user genre preferences.
     */
    @Transactional(readOnly = true)
    public UserPreferencesDTO getUserPreferences(String userEmail) {
        User user = userRepository.findByEmail(userEmail)
                .orElseThrow(() -> new ResourceNotFoundException("User", "email", userEmail));

        return new UserPreferencesDTO(
                user.getId(),
                user.getEmail(),
                parseGenres(user.getPreferredGenres()),
                ALL_AVAILABLE_GENRES
        );
    }

    /**
     * Update user genre preferences.
     */
    @Transactional
    public UserPreferencesDTO updateUserPreferences(String userEmail, List<String> preferredGenres) {
        User user = userRepository.findByEmail(userEmail)
                .orElseThrow(() -> new ResourceNotFoundException("User", "email", userEmail));

        String joined = (preferredGenres == null || preferredGenres.isEmpty())
                ? ""
                : preferredGenres.stream().map(String::trim).filter(s -> !s.isBlank()).collect(Collectors.joining(","));

        user.setPreferredGenres(joined);
        userRepository.save(user);

        return new UserPreferencesDTO(
                user.getId(),
                user.getEmail(),
                parseGenres(user.getPreferredGenres()),
                ALL_AVAILABLE_GENRES
        );
    }

    // =========================================================================
    // Transparent Weighted Recommendation Engine Implementation
    // =========================================================================

    private List<MovieRecommendationDTO> computePersonalizedRecommendations(List<Movie> activeMovies, User user, int limit) {
        Map<Long, Long> allTimeBookings = fetchAllTimeBookingCounts();
        Map<Long, Long> recentBookings = fetchRecentBookingCounts(7);

        long maxAllTime = Math.max(1, allTimeBookings.values().stream().max(Long::compare).orElse(1L));
        long maxRecent = Math.max(1, recentBookings.values().stream().max(Long::compare).orElse(1L));

        // User profile & historical genre affinity
        Set<String> preferredGenreSet = new HashSet<>();
        Map<String, Long> userGenreHistoryCounts = new HashMap<>();
        long totalUserBookings = 0;

        if (user != null) {
            List<String> prefs = parseGenres(user.getPreferredGenres());
            prefs.forEach(g -> preferredGenreSet.add(g.toLowerCase()));

            List<Booking> userBookings = bookingRepository.findConfirmedByUserIdOrderByCreatedAtDesc(user.getId());
            totalUserBookings = userBookings.size();
            for (Booking b : userBookings) {
                if (b.getShow() != null && b.getShow().getMovie() != null) {
                    String g = b.getShow().getMovie().getGenre().toLowerCase();
                    userGenreHistoryCounts.put(g, userGenreHistoryCounts.getOrDefault(g, 0L) + 1);
                }
            }
        }

        final boolean hasHistory = totalUserBookings > 0;
        final boolean hasPreferredGenres = !preferredGenreSet.isEmpty();
        final boolean isPersonalized = hasHistory || hasPreferredGenres;
        final long finalTotalUserBookings = totalUserBookings;

        // Dynamic weights distribution based on profile maturity
        final double wPref;
        final double wHist;
        final double wRating;
        final double wPop;
        final double wTrend;
        final double wRecency;

        if (hasPreferredGenres && hasHistory) {
            // Full personalized configuration
            wPref = 0.25;
            wHist = 0.25;
            wRating = 0.20;
            wPop = 0.15;
            wTrend = 0.10;
            wRecency = 0.05;
        } else if (hasPreferredGenres) {
            // User declared preferences but hasn't booked yet
            wPref = 0.40;
            wHist = 0.00;
            wRating = 0.25;
            wPop = 0.15;
            wTrend = 0.12;
            wRecency = 0.08;
        } else if (hasHistory) {
            // User has booking history but hasn't picked explicit preferences
            wPref = 0.00;
            wHist = 0.40;
            wRating = 0.25;
            wPop = 0.15;
            wTrend = 0.12;
            wRecency = 0.08;
        } else {
            // Cold Start: unauthenticated or new user without history
            wPref = 0.00;
            wHist = 0.00;
            wRating = 0.25;
            wPop = 0.35;
            wTrend = 0.30;
            wRecency = 0.10;
        }

        List<ScoredCandidate> scoredList = new ArrayList<>();

        for (Movie movie : activeMovies) {
            String genreLower = movie.getGenre() != null ? movie.getGenre().toLowerCase() : "";

            // 1. Preferred genre score (0.0 or 1.0)
            double prefScore = preferredGenreSet.contains(genreLower) ? 1.0 : 0.0;

            // 2. Booking history genre affinity (0.0 to 1.0)
            double histScore = 0.0;
            if (hasHistory) {
                long genreCount = userGenreHistoryCounts.getOrDefault(genreLower, 0L);
                histScore = (double) genreCount / finalTotalUserBookings;
            }

            // 3. Movie rating normalized (0.0 to 1.0)
            double ratingScore = Math.min(1.0, Math.max(0.0, movie.getRating() / 10.0));

            // 4. Popularity (ticket sales volume normalized)
            long movieAllTime = allTimeBookings.getOrDefault(movie.getId(), 0L);
            double popScore = (double) movieAllTime / maxAllTime;

            // 5. Trending velocity (last 7 days bookings normalized)
            long movieRecent = recentBookings.getOrDefault(movie.getId(), 0L);
            double trendScore = (double) movieRecent / maxRecent;

            // 6. Release recency (within 60 days)
            long daysOld = movie.getReleaseDate() != null
                    ? Math.max(0, ChronoUnit.DAYS.between(movie.getReleaseDate(), LocalDate.now()))
                    : 30;
            double recencyScore = Math.max(0.0, 1.0 - (daysOld / 60.0));

            // Total composite score
            double totalScore = (prefScore * wPref)
                    + (histScore * wHist)
                    + (ratingScore * wRating)
                    + (popScore * wPop)
                    + (trendScore * wTrend)
                    + (recencyScore * wRecency);

            // Compute human-readable recommendation explanation and highlight
            String reason;
            String highlight;

            if (prefScore > 0 && histScore > 0) {
                reason = "Matches your favorite genre (" + movie.getGenre() + ") & past bookings";
                highlight = "🎯 Top Preference Match";
            } else if (prefScore > 0) {
                reason = "Matches your preferred genre: " + movie.getGenre();
                highlight = "✨ Preferred Genre";
            } else if (histScore >= 0.4) {
                reason = "Because you frequently book " + movie.getGenre() + " movies";
                highlight = "🍿 Booking Habit";
            } else if (trendScore >= 0.7) {
                reason = "Trending rapidly in CineSmart theaters this week";
                highlight = "🔥 Trending Choice";
            } else if (popScore >= 0.7) {
                reason = "Box office bestseller with " + movieAllTime + "+ tickets sold";
                highlight = "⭐ Crowd Favorite";
            } else if (ratingScore >= 0.85) {
                reason = "Critically acclaimed masterpiece with " + movie.getRating() + "/10 score";
                highlight = "🏆 High Rating";
            } else if (recencyScore >= 0.7) {
                reason = "Fresh new release now playing";
                highlight = "🎬 New in Theaters";
            } else {
                reason = "Popular choice in " + movie.getGenre();
                highlight = "Recommended";
            }

            // Match percentage calibrated for transparent cinema recommendation (70% - 99%)
            int matchPct;
            if (prefScore > 0 && histScore > 0) {
                matchPct = (int) Math.min(99, Math.round(86 + (ratingScore * 8) + (recencyScore * 5)));
            } else if (prefScore > 0 || histScore >= 0.5) {
                matchPct = (int) Math.min(99, Math.round(80 + (ratingScore * 8) + (prefScore * 6)));
            } else {
                matchPct = (int) Math.min(99, Math.max(68, Math.round(totalScore * 100 + (ratingScore * 15))));
            }

            scoredList.add(new ScoredCandidate(movie, totalScore, matchPct, reason, highlight));
        }

        // Sort descending by total score, then rating
        scoredList.sort((a, b) -> {
            int cmp = Double.compare(b.score, a.score);
            if (cmp != 0) return cmp;
            return Double.compare(b.movie.getRating(), a.movie.getRating());
        });

        return scoredList.stream()
                .limit(limit)
                .map(sc -> new MovieRecommendationDTO(
                        MovieDTO.fromEntity(sc.movie),
                        sc.score,
                        sc.matchPercentage,
                        sc.reason,
                        isPersonalized ? "PERSONALIZED" : "COLD_START",
                        sc.matchPercentage + "% Match • " + sc.highlight
                ))
                .collect(Collectors.toList());
    }

    private List<MovieRecommendationDTO> computeSimilarMovies(Movie sourceMovie, List<Movie> activeMovies, int limit) {
        Map<Long, Long> allTimeBookings = fetchAllTimeBookingCounts();
        long maxAllTime = Math.max(1, allTimeBookings.values().stream().max(Long::compare).orElse(1L));

        String targetGenre = sourceMovie.getGenre() != null ? sourceMovie.getGenre().trim().toLowerCase() : "";

        return activeMovies.stream()
                .filter(m -> !m.getId().equals(sourceMovie.getId()))
                .map(m -> {
                    String g = m.getGenre() != null ? m.getGenre().trim().toLowerCase() : "";
                    boolean sameGenre = g.equalsIgnoreCase(targetGenre);

                    double genreScore = sameGenre ? 0.60 : 0.10;
                    double ratingScore = (m.getRating() / 10.0) * 0.25;
                    double popScore = ((double) allTimeBookings.getOrDefault(m.getId(), 0L) / maxAllTime) * 0.15;
                    double score = genreScore + ratingScore + popScore;

                    int matchPct = (int) Math.min(99, Math.max(70, Math.round(score * 100)));
                    String reason = sameGenre
                            ? "Similar " + m.getGenre() + " blockbuster like " + sourceMovie.getTitle()
                            : "Recommended for fans of " + sourceMovie.getTitle();
                    String highlight = sameGenre ? "🎬 Similar Genre" : "More Like This";

                    return new MovieRecommendationDTO(
                            MovieDTO.fromEntity(m),
                            score,
                            matchPct,
                            reason,
                            "BECAUSE_YOU_WATCHED",
                            highlight
                    );
                })
                .sorted((a, b) -> Double.compare(b.getScore(), a.getScore()))
                .limit(limit)
                .collect(Collectors.toList());
    }

    private Map<Long, Long> fetchAllTimeBookingCounts() {
        Map<Long, Long> map = new HashMap<>();
        try {
            List<Object[]> rows = bookingRepository.countConfirmedBookingsGroupByMovieId();
            for (Object[] r : rows) {
                if (r != null && r.length >= 2 && r[0] != null && r[1] != null) {
                    Long movieId = ((Number) r[0]).longValue();
                    Long count = ((Number) r[1]).longValue();
                    map.put(movieId, count);
                }
            }
        } catch (Exception ignored) {
        }
        return map;
    }

    private Map<Long, Long> fetchRecentBookingCounts(int days) {
        Map<Long, Long> map = new HashMap<>();
        try {
            LocalDateTime since = LocalDateTime.now().minusDays(days);
            List<Object[]> rows = bookingRepository.countConfirmedBookingsSinceGroupByMovieId(since);
            for (Object[] r : rows) {
                if (r != null && r.length >= 2 && r[0] != null && r[1] != null) {
                    Long movieId = ((Number) r[0]).longValue();
                    Long count = ((Number) r[1]).longValue();
                    map.put(movieId, count);
                }
            }
        } catch (Exception ignored) {
        }
        return map;
    }

    private List<String> parseGenres(String csv) {
        if (csv == null || csv.isBlank()) {
            return Collections.emptyList();
        }
        return Arrays.stream(csv.split(","))
                .map(String::trim)
                .filter(s -> !s.isBlank())
                .collect(Collectors.toList());
    }

    private static class ScoredCandidate {
        final Movie movie;
        final double score;
        final int matchPercentage;
        final String reason;
        final String highlight;

        ScoredCandidate(Movie movie, double score, int matchPercentage, String reason, String highlight) {
            this.movie = movie;
            this.score = score;
            this.matchPercentage = matchPercentage;
            this.reason = reason;
            this.highlight = highlight;
        }
    }
}
