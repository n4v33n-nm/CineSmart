package com.cinesmart.seat.group.service;

import com.cinesmart.common.exception.BadRequestException;
import com.cinesmart.common.exception.ResourceNotFoundException;
import com.cinesmart.screen.entity.Screen;
import com.cinesmart.seat.group.dto.GroupSeatingRecommendationDTO;
import com.cinesmart.seat.group.dto.GroupSeatingRequest;
import com.cinesmart.seat.group.dto.GroupSeatingResponseDTO;
import com.cinesmart.seat.group.model.ScoredArrangement;
import com.cinesmart.seat.group.strategy.AccessibleSeatAllocationStrategy;
import com.cinesmart.seat.group.strategy.ContiguousSeatAllocationStrategy;
import com.cinesmart.seat.group.strategy.FlexibleSeatAllocationStrategy;
import com.cinesmart.show.entity.Show;
import com.cinesmart.show.entity.ShowSeat;
import com.cinesmart.show.entity.ShowSeatStatus;
import com.cinesmart.show.repository.ShowRepository;
import com.cinesmart.show.repository.ShowSeatRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.stream.Collectors;

@Service
public class GroupSeatingService {

    public static final int MAX_RECOMMENDATIONS = 3;

    private final ShowRepository showRepository;
    private final ShowSeatRepository showSeatRepository;
    private final SeatScoringService seatScoringService;
    private final ContiguousSeatAllocationStrategy contiguousStrategy;
    private final FlexibleSeatAllocationStrategy flexibleStrategy;
    private final AccessibleSeatAllocationStrategy accessibleStrategy;

    public GroupSeatingService(ShowRepository showRepository,
                               ShowSeatRepository showSeatRepository,
                               SeatScoringService seatScoringService,
                               ContiguousSeatAllocationStrategy contiguousStrategy,
                               FlexibleSeatAllocationStrategy flexibleStrategy,
                               AccessibleSeatAllocationStrategy accessibleStrategy) {
        this.showRepository = showRepository;
        this.showSeatRepository = showSeatRepository;
        this.seatScoringService = seatScoringService;
        this.contiguousStrategy = contiguousStrategy;
        this.flexibleStrategy = flexibleStrategy;
        this.accessibleStrategy = accessibleStrategy;
    }

    @Transactional(readOnly = true)
    public GroupSeatingResponseDTO getRecommendations(GroupSeatingRequest request) {
        if (request == null) {
            throw new BadRequestException("Group seating request cannot be null", "INVALID_REQUEST");
        }

        if (request.getShowId() == null) {
            throw new BadRequestException("Show ID is required", "MISSING_SHOW_ID");
        }

        if (request.getPartySize() == null || request.getPartySize() < GroupSeatingRequest.MIN_GROUP_SIZE) {
            throw new BadRequestException(
                    "Group size must be at least " + GroupSeatingRequest.MIN_GROUP_SIZE,
                    "INVALID_GROUP_SIZE"
            );
        }

        if (request.getPartySize() > GroupSeatingRequest.MAX_GROUP_SIZE) {
            throw new BadRequestException(
                    "Group size cannot exceed " + GroupSeatingRequest.MAX_GROUP_SIZE,
                    "GROUP_SIZE_EXCEEDS_LIMIT"
            );
        }

        Show show = showRepository.findById(request.getShowId())
                .orElseThrow(() -> new ResourceNotFoundException("Show", "id", request.getShowId()));

        List<ShowSeat> allShowSeats = showSeatRepository.findByShowId(request.getShowId());
        List<ShowSeat> availableSeats = allShowSeats.stream()
                .filter(s -> s.getStatus() == ShowSeatStatus.AVAILABLE)
                .collect(Collectors.toList());

        String movieTitle = show.getMovie() != null ? show.getMovie().getTitle() : "";
        String cinemaName = "";
        String screenName = "";
        Screen screen = show.getScreen();
        if (screen != null) {
            screenName = "Screen " + screen.getScreenNumber();
            if (screen.getCinema() != null) {
                cinemaName = screen.getCinema().getName();
            }
        }

        // Capacity check: if total available seats is less than party size, fail fast with informative message
        if (availableSeats.size() < request.getPartySize()) {
            return new GroupSeatingResponseDTO(
                    show.getId(),
                    movieTitle,
                    screenName,
                    cinemaName,
                    request.getPartySize(),
                    String.format("Requested party size of %d exceeds available capacity (%d available seats).",
                            request.getPartySize(), availableSeats.size()),
                    Collections.emptyList()
            );
        }

        // Determine grid bounds
        int totalRows = allShowSeats.stream()
                .mapToInt(s -> s.getSeat() != null ? s.getSeat().getGridY() : 1)
                .max()
                .orElse(8);

        int totalCols = allShowSeats.stream()
                .mapToInt(s -> s.getSeat() != null ? s.getSeat().getGridX() : 1)
                .max()
                .orElse(12);

        List<ScoredArrangement> allCandidates = new ArrayList<>();

        if (Boolean.TRUE.equals(request.getRequireAccessibility())) {
            // Accessible branch
            allCandidates.addAll(accessibleStrategy.allocate(availableSeats, request, totalRows, totalCols));
        } else {
            // Phase 1: Contiguous single-row search
            List<ScoredArrangement> contiguousCandidates = contiguousStrategy.allocate(availableSeats, request, totalRows, totalCols);
            allCandidates.addAll(contiguousCandidates);

            // Phase 2: Flexible adjacent-split search if allowed and needed
            if (contiguousCandidates.size() < MAX_RECOMMENDATIONS && Boolean.TRUE.equals(request.getAllowSplitRows())) {
                List<ScoredArrangement> splitCandidates = flexibleStrategy.allocate(availableSeats, request, totalRows, totalCols);
                allCandidates.addAll(splitCandidates);
            }
        }

        // Sort candidates with strict deterministic tie-breakers
        allCandidates.sort(seatScoringService.getDeterministicComparator());

        // Deduplicate identical seat sets and take top recommendations
        List<ScoredArrangement> distinctTopCandidates = new ArrayList<>();
        Set<String> seenSeatSets = new HashSet<>();

        for (ScoredArrangement cand : allCandidates) {
            String seatKey = cand.getSeats().stream()
                    .map(s -> String.valueOf(s.getId()))
                    .sorted()
                    .collect(Collectors.joining(","));

            if (seenSeatSets.add(seatKey)) {
                distinctTopCandidates.add(cand);
                if (distinctTopCandidates.size() >= MAX_RECOMMENDATIONS) {
                    break;
                }
            }
        }

        if (distinctTopCandidates.isEmpty()) {
            return new GroupSeatingResponseDTO(
                    show.getId(),
                    movieTitle,
                    screenName,
                    cinemaName,
                    request.getPartySize(),
                    String.format("No suitable group seating arrangement found for %d guests matching specified preferences.",
                            request.getPartySize()),
                    Collections.emptyList()
            );
        }

        List<GroupSeatingRecommendationDTO> recDTOs = new ArrayList<>();
        for (int i = 0; i < distinctTopCandidates.size(); i++) {
            recDTOs.add(GroupSeatingRecommendationDTO.fromScoredArrangement(i + 1, distinctTopCandidates.get(i)));
        }

        return new GroupSeatingResponseDTO(
                show.getId(),
                movieTitle,
                screenName,
                cinemaName,
                request.getPartySize(),
                String.format("Found %d intelligent group seating recommendations.", recDTOs.size()),
                recDTOs
        );
    }
}
