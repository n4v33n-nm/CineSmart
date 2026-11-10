package com.cinesmart.seat.service;

import com.cinesmart.common.exception.BadRequestException;
import com.cinesmart.common.exception.ResourceNotFoundException;
import com.cinesmart.common.exception.SeatUnavailableException;
import com.cinesmart.seat.dto.SeatDTO;
import com.cinesmart.seat.dto.SeatLockResponseDTO;
import com.cinesmart.seat.dto.SeatMapResponseDTO;
import com.cinesmart.seat.dto.ShowSeatDTO;
import com.cinesmart.seat.repository.SeatRepository;
import com.cinesmart.show.entity.Show;
import com.cinesmart.show.entity.ShowSeat;
import com.cinesmart.show.entity.ShowSeatStatus;
import com.cinesmart.show.repository.ShowRepository;
import com.cinesmart.show.repository.ShowSeatRepository;
import com.cinesmart.user.entity.User;
import com.cinesmart.user.entity.UserRole;
import com.cinesmart.user.repository.UserRepository;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(readOnly = true)
public class SeatService {

    private final SeatRepository seatRepository;
    private final ShowRepository showRepository;
    private final ShowSeatRepository showSeatRepository;
    private final SeatLockCleanupService seatLockCleanupService;
    private final UserRepository userRepository;

    public SeatService(SeatRepository seatRepository,
                       ShowRepository showRepository,
                       ShowSeatRepository showSeatRepository,
                       SeatLockCleanupService seatLockCleanupService,
                       UserRepository userRepository) {
        this.seatRepository = seatRepository;
        this.showRepository = showRepository;
        this.showSeatRepository = showSeatRepository;
        this.seatLockCleanupService = seatLockCleanupService;
        this.userRepository = userRepository;
    }

    public List<SeatDTO> getSeatsByScreen(Long screenId) {
        return seatRepository.findByScreenIdAndIsActiveTrueOrderByRowIdentifierAscColumnNumberAsc(screenId).stream()
                .map(SeatDTO::fromEntity)
                .collect(Collectors.toList());
    }

    @Transactional
    public SeatMapResponseDTO getSeatMapForShow(Long showId) {
        seatLockCleanupService.releaseExpiredLocksForShow(showId);

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

    @Transactional
    public SeatLockResponseDTO lockSeats(Long showId, List<Long> showSeatIds, String userEmail) {
        Show show = showRepository.findById(showId)
                .orElseThrow(() -> new ResourceNotFoundException("Show", "id", showId));

        User user = userRepository.findByEmail(userEmail)
                .orElseThrow(() -> new ResourceNotFoundException("User", "email", userEmail));

        if (showSeatIds == null || showSeatIds.isEmpty()) {
            throw new BadRequestException("At least one seat must be selected to lock", "NO_SEATS_SELECTED");
        }

        // Concurrency-safe: Acquire pessimistic write lock on target show seats
        List<ShowSeat> seats = showSeatRepository.findAllByIdWithLock(showSeatIds);
        if (seats.size() != showSeatIds.size()) {
            throw new BadRequestException("One or more selected seats do not exist", "INVALID_SEAT_IDS");
        }

        LocalDateTime now = LocalDateTime.now();
        LocalDateTime expiresAt = now.plusMinutes(5);

        for (ShowSeat seat : seats) {
            if (!seat.getShow().getId().equals(show.getId())) {
                throw new BadRequestException("Seat does not belong to specified show", "CROSS_SHOW_SEAT_MISMATCH");
            }
            if (seat.isLockExpired(now)) {
                seat.release();
            }
            if (seat.getStatus() != ShowSeatStatus.AVAILABLE) {
                String seatLabel = seat.getSeat() != null
                        ? String.format("Row %s Seat %d", seat.getSeat().getRowIdentifier(), seat.getSeat().getColumnNumber())
                        : "Seat ID " + seat.getId();
                String statusDetail = (seat.getStatus() == ShowSeatStatus.LOCKED || seat.getStatus() == ShowSeatStatus.HELD)
                        ? "is currently locked by another customer. Please choose another seat."
                        : "has already been booked for this show.";
                throw new SeatUnavailableException(seatLabel + " " + statusDetail);
            }
            seat.lock(user, expiresAt);
        }

        showSeatRepository.saveAll(seats);
        return new SeatLockResponseDTO(showId, showSeatIds, expiresAt, "LOCKED", "Seats locked successfully for 5 minutes");
    }

    @Transactional
    public SeatLockResponseDTO releaseSeats(Long showId, List<Long> showSeatIds, String userEmail) {
        Show show = showRepository.findById(showId)
                .orElseThrow(() -> new ResourceNotFoundException("Show", "id", showId));

        User user = userRepository.findByEmail(userEmail)
                .orElseThrow(() -> new ResourceNotFoundException("User", "email", userEmail));

        if (showSeatIds == null || showSeatIds.isEmpty()) {
            return new SeatLockResponseDTO(showId, showSeatIds, null, "AVAILABLE", "No seats provided to release");
        }

        List<ShowSeat> seats = showSeatRepository.findAllByIdWithLock(showSeatIds);
        for (ShowSeat seat : seats) {
            if (seat.getShow().getId().equals(show.getId())) {
                // Only release if locked by this user or user is admin
                if (seat.getLockedBy() == null || seat.getLockedBy().getId().equals(user.getId()) || user.getRole() == UserRole.ROLE_ADMIN) {
                    if (seat.getStatus() == ShowSeatStatus.LOCKED || seat.getStatus() == ShowSeatStatus.HELD) {
                        seat.release();
                    }
                }
            }
        }
        showSeatRepository.saveAll(seats);
        return new SeatLockResponseDTO(showId, showSeatIds, null, "AVAILABLE", "Seats released successfully");
    }
}
