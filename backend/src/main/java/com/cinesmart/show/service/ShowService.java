package com.cinesmart.show.service;

import com.cinesmart.common.exception.BadRequestException;
import com.cinesmart.common.exception.ResourceNotFoundException;
import com.cinesmart.movie.entity.Movie;
import com.cinesmart.movie.repository.MovieRepository;
import com.cinesmart.screen.entity.Screen;
import com.cinesmart.screen.repository.ScreenRepository;
import com.cinesmart.seat.entity.Seat;
import com.cinesmart.seat.entity.SeatTier;
import com.cinesmart.seat.repository.SeatRepository;
import com.cinesmart.show.dto.ShowCreateRequest;
import com.cinesmart.show.dto.ShowDTO;
import com.cinesmart.show.entity.Show;
import com.cinesmart.show.entity.ShowSeat;
import com.cinesmart.show.entity.ShowSeatStatus;
import com.cinesmart.show.entity.ShowStatus;
import com.cinesmart.show.repository.ShowRepository;
import com.cinesmart.show.repository.ShowSeatRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class ShowService {

    private final ShowRepository showRepository;
    private final ShowSeatRepository showSeatRepository;
    private final MovieRepository movieRepository;
    private final ScreenRepository screenRepository;
    private final SeatRepository seatRepository;

    public ShowService(ShowRepository showRepository,
                       ShowSeatRepository showSeatRepository,
                       MovieRepository movieRepository,
                       ScreenRepository screenRepository,
                       SeatRepository seatRepository) {
        this.showRepository = showRepository;
        this.showSeatRepository = showSeatRepository;
        this.movieRepository = movieRepository;
        this.screenRepository = screenRepository;
        this.seatRepository = seatRepository;
    }

    public List<ShowDTO> getShows(Long movieId, LocalDate date) {
        List<Show> shows;
        if (movieId != null) {
            shows = showRepository.findByMovieId(movieId);
        } else if (date != null) {
            LocalDateTime start = date.atStartOfDay();
            LocalDateTime end = date.atTime(LocalTime.MAX);
            shows = showRepository.findByStartTimeBetweenOrderByStartTimeAsc(start, end);
        } else {
            shows = showRepository.findAll();
        }

        return shows.stream().map(this::mapToShowDTO).collect(Collectors.toList());
    }

    public List<ShowDTO> getShowsByMovie(Long movieId) {
        if (!movieRepository.existsById(movieId)) {
            throw new ResourceNotFoundException("Movie", "id", movieId);
        }
        return showRepository.findByMovieId(movieId).stream()
                .map(this::mapToShowDTO)
                .collect(Collectors.toList());
    }

    public ShowDTO getShowById(Long id) {
        Show show = showRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Show", "id", id));
        return mapToShowDTO(show);
    }

    @Transactional
    public ShowDTO createShow(ShowCreateRequest request) {
        if (request.getEndTime().isBefore(request.getStartTime())) {
            throw new BadRequestException("Show end time must be after start time", "INVALID_SHOW_TIMES");
        }

        Movie movie = movieRepository.findById(request.getMovieId())
                .orElseThrow(() -> new ResourceNotFoundException("Movie", "id", request.getMovieId()));

        Screen screen = screenRepository.findById(request.getScreenId())
                .orElseThrow(() -> new ResourceNotFoundException("Screen", "id", request.getScreenId()));

        Show show = new Show(
                movie,
                screen,
                request.getStartTime(),
                request.getEndTime(),
                request.getBasePrice()
        );

        Show savedShow = showRepository.save(show);

        // Instantiate ShowSeat inventory for all physical seats in this screen
        List<Seat> physicalSeats = seatRepository.findByScreenIdAndIsActiveTrueOrderByRowIdentifierAscColumnNumberAsc(screen.getId());
        List<ShowSeat> showSeats = physicalSeats.stream().map(seat -> {
            BigDecimal seatPrice = calculateSeatPrice(show.getBasePrice(), seat.getSeatTier());
            return new ShowSeat(savedShow, seat, seatPrice);
        }).collect(Collectors.toList());

        showSeatRepository.saveAll(showSeats);

        return mapToShowDTO(savedShow);
    }

    @Transactional
    public ShowDTO cancelShow(Long id) {
        Show show = showRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Show", "id", id));
        show.setStatus(ShowStatus.CANCELLED);
        Show updated = showRepository.save(show);
        return mapToShowDTO(updated);
    }

    private ShowDTO mapToShowDTO(Show show) {
        long availableCount = showSeatRepository.countByShowIdAndStatus(show.getId(), ShowSeatStatus.AVAILABLE);
        return ShowDTO.fromEntity(show, (int) availableCount);
    }

    private BigDecimal calculateSeatPrice(BigDecimal basePrice, SeatTier tier) {
        if (tier == SeatTier.PREMIUM) {
            return basePrice.add(BigDecimal.valueOf(3.00));
        } else if (tier == SeatTier.RECLINER) {
            return basePrice.add(BigDecimal.valueOf(6.00));
        }
        return basePrice;
    }
}
