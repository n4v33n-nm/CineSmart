package com.cinesmart;

import com.cinesmart.booking.entity.Booking;
import com.cinesmart.booking.entity.BookingStatus;
import com.cinesmart.booking.repository.BookingRepository;
import com.cinesmart.cinema.entity.Cinema;
import com.cinesmart.cinema.entity.ScreenType;
import com.cinesmart.movie.entity.Movie;
import com.cinesmart.movie.repository.MovieRepository;
import com.cinesmart.recommendation.dto.HomeRecommendationSectionsDTO;
import com.cinesmart.recommendation.dto.MovieRecommendationDTO;
import com.cinesmart.recommendation.dto.UserPreferencesDTO;
import com.cinesmart.recommendation.service.RecommendationService;
import com.cinesmart.screen.entity.Screen;
import com.cinesmart.show.entity.Show;
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
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class RecommendationServiceTest {

    @Mock
    private MovieRepository movieRepository;

    @Mock
    private BookingRepository bookingRepository;

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private RecommendationService recommendationService;

    private Movie movieSciFi1;
    private Movie movieSciFi2;
    private Movie movieAction;
    private Movie movieAnimation;
    private User testUser;
    private Cinema cinema;
    private Screen screen;

    @BeforeEach
    void setUp() {
        movieSciFi1 = new Movie("Interstellar Odyssey", "Space exploration", 165, "English", "Sci-Fi", "PG-13", "https://img.com/1.jpg");
        movieSciFi1.setId(1L);
        movieSciFi1.setRating(8.9);
        movieSciFi1.setReleaseDate(LocalDate.now().minusDays(10));
        movieSciFi1.setTotalReviews(1420);

        movieSciFi2 = new Movie("Dune: The Desert Prophecy", "Desert epic", 155, "English", "Sci-Fi", "PG-13", "https://img.com/2.jpg");
        movieSciFi2.setId(2L);
        movieSciFi2.setRating(8.7);
        movieSciFi2.setReleaseDate(LocalDate.now().minusDays(20));
        movieSciFi2.setTotalReviews(980);

        movieAction = new Movie("The Dark Knight Returns", "Gotham vigilant", 152, "English", "Action", "PG-13", "https://img.com/3.jpg");
        movieAction.setId(3L);
        movieAction.setRating(9.1);
        movieAction.setReleaseDate(LocalDate.now().minusDays(40));
        movieAction.setTotalReviews(2850);

        movieAnimation = new Movie("Spider-Man: Multiverse Rift", "Animated multiverse", 140, "English", "Animation", "PG", "https://img.com/4.jpg");
        movieAnimation.setId(4L);
        movieAnimation.setRating(8.6);
        movieAnimation.setReleaseDate(LocalDate.now().minusDays(5));
        movieAnimation.setTotalReviews(1120);

        testUser = new User("user@cinesmart.com", "hash", "Test User", "9876543210", UserRole.ROLE_CUSTOMER);
        testUser.setId(10L);
        testUser.setPreferredGenres("Sci-Fi");

        cinema = new Cinema("CineSmart Central", "Bangalore", "MG Road", "9876543210");
        cinema.setId(1L);
        screen = new Screen(cinema, 1, ScreenType.IMAX_3D, 100);
        screen.setId(1L);
    }

    @Test
    @DisplayName("Personalized recommendation prioritizes user preferred genre and booking affinity")
    void testPersonalizedRecommendations_WithPreferredGenreAndBookingHistory() {
        List<Movie> allMovies = List.of(movieSciFi1, movieSciFi2, movieAction, movieAnimation);
        when(movieRepository.findByIsActiveTrue()).thenReturn(allMovies);
        when(userRepository.findByEmail("user@cinesmart.com")).thenReturn(Optional.of(testUser));

        // User has a confirmed booking for Sci-Fi movie 1
        Show show = new Show(movieSciFi1, screen, LocalDateTime.now().plusDays(1), LocalDateTime.now().plusDays(1).plusHours(2), BigDecimal.valueOf(250));
        Booking booking = new Booking("CS-TEST-1", testUser, show, BigDecimal.valueOf(250));
        booking.setStatus(BookingStatus.CONFIRMED);

        when(bookingRepository.findConfirmedByUserIdOrderByCreatedAtDesc(10L)).thenReturn(List.of(booking));
        when(bookingRepository.countConfirmedBookingsGroupByMovieId()).thenReturn(Collections.emptyList());
        when(bookingRepository.countConfirmedBookingsSinceGroupByMovieId(any(LocalDateTime.class))).thenReturn(Collections.emptyList());

        List<MovieRecommendationDTO> recommendations = recommendationService.getPersonalizedRecommendations("user@cinesmart.com", 4);

        assertNotNull(recommendations);
        assertFalse(recommendations.isEmpty());

        // Sci-Fi movies should be at the top due to preferred genre "Sci-Fi" and past booking in Sci-Fi
        MovieRecommendationDTO topRec = recommendations.get(0);
        assertEquals("Sci-Fi", topRec.getMovie().getGenre());
        assertTrue(topRec.getMatchPercentage() >= 80);
        assertTrue(topRec.getRecommendationReason().contains("Sci-Fi"));
    }

    @Test
    @DisplayName("Cold-start user without booking history receives recommendations ranked by popularity and ratings")
    void testColdStartRecommendations_NewUserWithoutHistory() {
        List<Movie> allMovies = List.of(movieSciFi1, movieSciFi2, movieAction, movieAnimation);
        when(movieRepository.findByIsActiveTrue()).thenReturn(allMovies);
        when(bookingRepository.countConfirmedBookingsGroupByMovieId()).thenReturn(Collections.emptyList());
        when(bookingRepository.countConfirmedBookingsSinceGroupByMovieId(any(LocalDateTime.class))).thenReturn(Collections.emptyList());

        // Calling without email (guest)
        List<MovieRecommendationDTO> recommendations = recommendationService.getPersonalizedRecommendations(null, 4);

        assertNotNull(recommendations);
        assertEquals(4, recommendations.size());

        // High rated action movie should rank prominently
        boolean containsTopRated = recommendations.stream().anyMatch(r -> r.getMovie().getTitle().equals("The Dark Knight Returns"));
        assertTrue(containsTopRated);
        assertEquals("COLD_START", recommendations.get(0).getCategory());
    }

    @Test
    @DisplayName("Home sections DTO populates all 6 personalized and general sections correctly")
    void testHomeSections_PopulatesAllSections() {
        List<Movie> allMovies = List.of(movieSciFi1, movieSciFi2, movieAction, movieAnimation);
        when(movieRepository.findByIsActiveTrue()).thenReturn(allMovies);
        when(userRepository.findByEmail("user@cinesmart.com")).thenReturn(Optional.of(testUser));

        Show show = new Show(movieSciFi1, screen, LocalDateTime.now().plusDays(1), LocalDateTime.now().plusDays(1).plusHours(2), BigDecimal.valueOf(250));
        Booking booking = new Booking("CS-TEST-2", testUser, show, BigDecimal.valueOf(250));
        booking.setStatus(BookingStatus.CONFIRMED);

        when(bookingRepository.findConfirmedByUserIdOrderByCreatedAtDesc(10L)).thenReturn(List.of(booking));
        when(bookingRepository.countConfirmedBookingsGroupByMovieId()).thenReturn(Collections.emptyList());
        when(bookingRepository.countConfirmedBookingsSinceGroupByMovieId(any(LocalDateTime.class))).thenReturn(Collections.emptyList());

        HomeRecommendationSectionsDTO sections = recommendationService.getHomeSections("user@cinesmart.com");

        assertNotNull(sections);
        assertTrue(sections.isPersonalized());
        assertNotNull(sections.getRecommendedForYou());
        assertNotNull(sections.getBecauseYouWatched());
        assertEquals("Interstellar Odyssey", sections.getBecauseYouWatched().getSourceMovie().getTitle());
        // Because you watched Interstellar (Sci-Fi), recommendations should include Dune (Sci-Fi)
        assertEquals("Dune: The Desert Prophecy", sections.getBecauseYouWatched().getRecommendations().get(0).getMovie().getTitle());

        assertNotNull(sections.getTrendingMovies());
        assertNotNull(sections.getPopularMovies());
        assertNotNull(sections.getNewReleases());
        assertNotNull(sections.getTopRated());
    }

    @Test
    @DisplayName("Similar movies endpoint returns genre-matched movies excluding the target movie")
    void testSimilarMovies_ReturnsGenreMatchedCandidates() {
        List<Movie> allMovies = List.of(movieSciFi1, movieSciFi2, movieAction, movieAnimation);
        when(movieRepository.findById(1L)).thenReturn(Optional.of(movieSciFi1));
        when(movieRepository.findByIsActiveTrue()).thenReturn(allMovies);
        when(bookingRepository.countConfirmedBookingsGroupByMovieId()).thenReturn(Collections.emptyList());

        List<MovieRecommendationDTO> similar = recommendationService.getSimilarMovies(1L, 3);

        assertNotNull(similar);
        assertFalse(similar.isEmpty());
        // Must exclude movie 1 itself
        assertTrue(similar.stream().noneMatch(r -> r.getMovie().getId().equals(1L)));
        // Top similar should be Dune (Sci-Fi)
        assertEquals(2L, similar.get(0).getMovie().getId());
        assertEquals("Sci-Fi", similar.get(0).getMovie().getGenre());
    }

    @Test
    @DisplayName("Updating user preferences persists genres and affects recommendations")
    void testUpdateUserPreferences_PersistsAndReturns() {
        when(userRepository.findByEmail("user@cinesmart.com")).thenReturn(Optional.of(testUser));
        when(userRepository.save(any(User.class))).thenAnswer(invocation -> invocation.getArgument(0));

        UserPreferencesDTO updated = recommendationService.updateUserPreferences("user@cinesmart.com", List.of("Animation", "Action"));

        assertNotNull(updated);
        assertEquals(List.of("Animation", "Action"), updated.getPreferredGenres());
        assertEquals("Animation,Action", testUser.getPreferredGenres());
    }
}
