package com.cinesmart;

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
import com.cinesmart.show.entity.ShowSeatStatus;
import com.cinesmart.show.repository.ShowRepository;
import com.cinesmart.show.repository.ShowSeatRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@DataJpaTest
@DisplayName("ShowSeatRepository JPA Integration Tests")
class ShowSeatRepositoryTest {

    @Autowired
    private CinemaRepository cinemaRepository;
    @Autowired
    private ScreenRepository screenRepository;
    @Autowired
    private SeatRepository seatRepository;
    @Autowired
    private MovieRepository movieRepository;
    @Autowired
    private ShowRepository showRepository;
    @Autowired
    private ShowSeatRepository showSeatRepository;

    @Test
    @DisplayName("Should query show seats by show and status accurately")
    void testShowSeatInventoryQueries() {
        Cinema cinema = cinemaRepository.save(new Cinema("Test Cinema", "City", "Address", "123"));
        Screen screen = screenRepository.save(new Screen(cinema, 1, ScreenType.STANDARD, 2));
        Seat seat1 = seatRepository.save(new Seat(screen, "A", 1, SeatTier.STANDARD, 1, 1));
        Seat seat2 = seatRepository.save(new Seat(screen, "A", 2, SeatTier.STANDARD, 2, 1));

        Movie movie = movieRepository.save(new Movie("Test Movie", "Syn", 120, "English", "Action", "PG", "url"));
        LocalDateTime start = LocalDateTime.now().plusDays(1);
        Show show = showRepository.save(new Show(movie, screen, start, start.plusHours(2), BigDecimal.valueOf(15.00)));

        ShowSeat ss1 = showSeatRepository.save(new ShowSeat(show, seat1, BigDecimal.valueOf(15.00)));
        ShowSeat ss2 = showSeatRepository.save(new ShowSeat(show, seat2, BigDecimal.valueOf(15.00)));

        List<ShowSeat> allSeats = showSeatRepository.findByShowId(show.getId());
        assertEquals(2, allSeats.size());

        assertEquals(2, showSeatRepository.countByShowIdAndStatus(show.getId(), ShowSeatStatus.AVAILABLE));

        // Mark one seat as BOOKED
        ss1.setStatus(ShowSeatStatus.BOOKED);
        showSeatRepository.save(ss1);

        assertEquals(1, showSeatRepository.countByShowIdAndStatus(show.getId(), ShowSeatStatus.AVAILABLE));
        assertEquals(1, showSeatRepository.countByShowIdAndStatus(show.getId(), ShowSeatStatus.BOOKED));
    }
}
