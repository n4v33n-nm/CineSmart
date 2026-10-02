package com.cinesmart.seat.service;

import com.cinesmart.common.exception.ResourceNotFoundException;
import com.cinesmart.seat.dto.SeatDTO;
import com.cinesmart.seat.dto.SeatMapResponseDTO;
import com.cinesmart.seat.dto.ShowSeatDTO;
import com.cinesmart.seat.repository.SeatRepository;
import com.cinesmart.show.entity.Show;
import com.cinesmart.show.entity.ShowSeat;
import com.cinesmart.show.entity.ShowSeatStatus;
import com.cinesmart.show.repository.ShowRepository;
import com.cinesmart.show.repository.ShowSeatRepository;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class SeatService {

    private final SeatRepository seatRepository;
    private final ShowRepository showRepository;
    private final ShowSeatRepository showSeatRepository;

    public SeatService(SeatRepository seatRepository,
                       ShowRepository showRepository,
                       ShowSeatRepository showSeatRepository) {
        this.seatRepository = seatRepository;
        this.showRepository = showRepository;
        this.showSeatRepository = showSeatRepository;
    }

    public List<SeatDTO> getSeatsByScreen(Long screenId) {
        return seatRepository.findByScreenIdAndIsActiveTrueOrderByRowIdentifierAscColumnNumberAsc(screenId).stream()
                .map(SeatDTO::fromEntity)
                .collect(Collectors.toList());
    }

    public SeatMapResponseDTO getSeatMapForShow(Long showId) {
        Show show = showRepository.findById(showId)
                .orElseThrow(() -> new ResourceNotFoundException("Show", "id", showId));

        List<ShowSeat> showSeats = showSeatRepository.findByShowId(showId);
        List<ShowSeatDTO> seatDTOs = showSeats.stream()
                .map(ShowSeatDTO::fromEntity)
                .collect(Collectors.toList());

        int totalSeats = seatDTOs.size();
        int availableSeats = (int) seatDTOs.stream()
                .filter(s -> s.getStatus() == ShowSeatStatus.AVAILABLE)
                .count();

        String movieTitle = show.getMovie() != null ? show.getMovie().getTitle() : "";
        String cinemaName = "";
        String screenName = "";

        if (show.getScreen() != null) {
            screenName = "Screen " + show.getScreen().getScreenNumber();
            if (show.getScreen().getCinema() != null) {
                cinemaName = show.getScreen().getCinema().getName();
            }
        }

        return new SeatMapResponseDTO(
                showId,
                movieTitle,
                cinemaName,
                screenName,
                totalSeats,
                availableSeats,
                seatDTOs
        );
    }
}
