package com.cinesmart.config;

import com.cinesmart.cinema.entity.Cinema;
import com.cinesmart.cinema.entity.ScreenType;
import com.cinesmart.cinema.repository.CinemaRepository;
import com.cinesmart.movie.entity.Movie;
import com.cinesmart.movie.repository.MovieRepository;
import com.cinesmart.screen.entity.Screen;
import com.cinesmart.screen.repository.ScreenRepository;
import com.cinesmart.seat.entity.Seat;
import com.cinesmart.seat.entity.SeatTier;
import com.cinesmart.seat.repository.SeatRepository;
import com.cinesmart.show.entity.Show;
import com.cinesmart.show.entity.ShowSeat;
import com.cinesmart.show.entity.ShowStatus;
import com.cinesmart.show.repository.ShowRepository;
import com.cinesmart.show.repository.ShowSeatRepository;
import com.cinesmart.user.entity.User;
import com.cinesmart.user.entity.UserRole;
import com.cinesmart.user.repository.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;

@Component
public class DataInitializer implements CommandLineRunner {

    private static final Logger log = LoggerFactory.getLogger(DataInitializer.class);

    private final UserRepository userRepository;
    private final MovieRepository movieRepository;
    private final CinemaRepository cinemaRepository;
    private final ScreenRepository screenRepository;
    private final SeatRepository seatRepository;
    private final ShowRepository showRepository;
    private final ShowSeatRepository showSeatRepository;
    private final PasswordEncoder passwordEncoder;

    public DataInitializer(UserRepository userRepository,
                           MovieRepository movieRepository,
                           CinemaRepository cinemaRepository,
                           ScreenRepository screenRepository,
                           SeatRepository seatRepository,
                           ShowRepository showRepository,
                           ShowSeatRepository showSeatRepository,
                           PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.movieRepository = movieRepository;
        this.cinemaRepository = cinemaRepository;
        this.screenRepository = screenRepository;
        this.seatRepository = seatRepository;
        this.showRepository = showRepository;
        this.showSeatRepository = showSeatRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    @Transactional
    public void run(String... args) {
        if (userRepository.count() == 0) {
            log.info("Initializing CineSmart default users (admin, staff, customer)...");
            userRepository.save(new User("admin@cinesmart.com", passwordEncoder.encode("Admin123!"), "System Administrator", "+1-555-0100", UserRole.ROLE_ADMIN));
            userRepository.save(new User("staff@cinesmart.com", passwordEncoder.encode("Staff123!"), "Gate Operations Staff", "+1-555-0101", UserRole.ROLE_STAFF));
            userRepository.save(new User("customer@cinesmart.com", passwordEncoder.encode("Customer123!"), "Jane Customer", "+1-555-0102", UserRole.ROLE_CUSTOMER));
        }

        if (movieRepository.count() == 0) {
            log.info("Initializing CineSmart movies, cinema, screens, seats, and shows...");

            // 1. Create Cinema
            Cinema cinema = cinemaRepository.save(new Cinema(
                    "CineSmart Central",
                    "Metropolis",
                    "100 Broadway Boulevard, Suite 500",
                    "+1-555-9000"
            ));

            // 2. Create Screens
            Screen screen1 = screenRepository.save(new Screen(cinema, 1, ScreenType.IMAX_3D, 48));
            Screen screen2 = screenRepository.save(new Screen(cinema, 2, ScreenType.DOLBY_ATMOS, 48));

            // 3. Generate Physical Seats (Rows A-F, Cols 1-8 = 48 seats per screen)
            List<Seat> screen1Seats = generateSeatsForScreen(screen1);
            List<Seat> screen2Seats = generateSeatsForScreen(screen2);
            seatRepository.saveAll(screen1Seats);
            seatRepository.saveAll(screen2Seats);

            // 4. Create 5 Distinctive Sample Movies
            Movie movie1 = movieRepository.save(new Movie(
                    "Interstellar Odyssey",
                    "A heroic team of astronauts embarks on humanity's greatest voyage through a newly discovered wormhole in deep space.",
                    169,
                    "English",
                    "Sci-Fi",
                    "PG-13",
                    "https://images.unsplash.com/photo-1534447677768-be436bb09401?w=800&q=80"
            ));

            Movie movie2 = movieRepository.save(new Movie(
                    "Dune: The Desert Prophecy",
                    "Paul Atreides unites with the Fremen on a spiritual quest of vengeance against conspirators who destroyed his family.",
                    166,
                    "English",
                    "Sci-Fi",
                    "PG-13",
                    "https://images.unsplash.com/photo-1509198397868-475647b2a1e5?w=800&q=80"
            ));

            Movie movie3 = movieRepository.save(new Movie(
                    "The Dark Knight Returns",
                    "Eight years after the Joker's reign of chaos, Batman returns to defend Gotham City from the enigmatic masked terrorist Bane.",
                    164,
                    "English",
                    "Action",
                    "PG-13",
                    "https://images.unsplash.com/photo-1509281373149-e957c6296406?w=800&q=80"
            ));

            Movie movie4 = movieRepository.save(new Movie(
                    "Cyberpunk: Neon Horizon",
                    "In a neon-drenched metropolis, an outlaw mercenary takes on corporate syndicates to retrieve a stolen cybernetic bio-chip.",
                    142,
                    "English",
                    "Action",
                    "R",
                    "https://images.unsplash.com/photo-1518709268805-4e9042af9f23?w=800&q=80"
            ));

            Movie movie5 = movieRepository.save(new Movie(
                    "Spider-Man: Multiverse Rift",
                    "Teenager Miles Morales must unite with alternate universe web-slingers to defeat a reality-shattering extradimensional threat.",
                    140,
                    "English",
                    "Animation",
                    "PG",
                    "https://images.unsplash.com/photo-1635805737707-575885ab0820?w=800&q=80"
            ));

            // 5. Create Scheduled Shows
            LocalDate today = LocalDate.now();
            LocalDate tomorrow = today.plusDays(1);

            createShowWithSeats(movie1, screen1, today.atTime(LocalTime.of(10, 0)), today.atTime(LocalTime.of(12, 49)), BigDecimal.valueOf(14.50), screen1Seats);
            createShowWithSeats(movie1, screen1, today.atTime(LocalTime.of(13, 30)), today.atTime(LocalTime.of(16, 19)), BigDecimal.valueOf(16.50), screen1Seats);
            createShowWithSeats(movie1, screen1, today.atTime(LocalTime.of(19, 0)), today.atTime(LocalTime.of(21, 49)), BigDecimal.valueOf(18.00), screen1Seats);

            createShowWithSeats(movie2, screen2, today.atTime(LocalTime.of(13, 0)), today.atTime(LocalTime.of(15, 46)), BigDecimal.valueOf(15.00), screen2Seats);
            createShowWithSeats(movie2, screen2, today.atTime(LocalTime.of(16, 30)), today.atTime(LocalTime.of(19, 16)), BigDecimal.valueOf(17.00), screen2Seats);

            createShowWithSeats(movie3, screen2, today.atTime(LocalTime.of(20, 0)), today.atTime(LocalTime.of(22, 44)), BigDecimal.valueOf(18.00), screen2Seats);
            createShowWithSeats(movie4, screen1, tomorrow.atTime(LocalTime.of(21, 0)), tomorrow.atTime(LocalTime.of(23, 22)), BigDecimal.valueOf(16.00), screen1Seats);
            createShowWithSeats(movie5, screen2, tomorrow.atTime(LocalTime.of(11, 0)), tomorrow.atTime(LocalTime.of(13, 20)), BigDecimal.valueOf(13.00), screen2Seats);

            log.info("CineSmart initial development data loaded successfully.");
        }
    }

    private List<Seat> generateSeatsForScreen(Screen screen) {
        List<Seat> seats = new ArrayList<>();
        String[] rows = {"A", "B", "C", "D", "E", "F"};

        for (int r = 0; r < rows.length; r++) {
            String row = rows[r];
            for (int col = 1; col <= 8; col++) {
                SeatTier tier;
                if (r == 0 && (col == 1 || col == 2)) {
                    tier = SeatTier.ACCESSIBLE_WHEELCHAIR;
                } else if (r == 0 && (col == 3 || col == 4)) {
                    tier = SeatTier.ACCESSIBLE_COMPANION;
                } else if (r <= 1) {
                    tier = SeatTier.STANDARD;
                } else if (r <= 3) {
                    tier = SeatTier.PREMIUM;
                } else {
                    tier = SeatTier.RECLINER;
                }

                seats.add(new Seat(screen, row, col, tier, col, r + 1));
            }
        }
        return seats;
    }

    private void createShowWithSeats(Movie movie, Screen screen, LocalDateTime start, LocalDateTime end, BigDecimal basePrice, List<Seat> seats) {
        Show show = new Show(movie, screen, start, end, basePrice);
        show.setStatus(ShowStatus.SCHEDULED);
        Show savedShow = showRepository.save(show);

        List<ShowSeat> showSeats = new ArrayList<>();
        for (Seat seat : seats) {
            BigDecimal seatPrice = basePrice;
            if (seat.getSeatTier() == SeatTier.PREMIUM) {
                seatPrice = basePrice.add(BigDecimal.valueOf(3.00));
            } else if (seat.getSeatTier() == SeatTier.RECLINER) {
                seatPrice = basePrice.add(BigDecimal.valueOf(6.00));
            }
            showSeats.add(new ShowSeat(savedShow, seat, seatPrice));
        }
        showSeatRepository.saveAll(showSeats);
    }
}
