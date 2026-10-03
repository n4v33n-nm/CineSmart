package com.cinesmart.seat.group.strategy;

import com.cinesmart.seat.entity.SeatTier;
import com.cinesmart.seat.group.dto.GroupSeatingRequest;
import com.cinesmart.seat.group.model.ArrangementType;
import com.cinesmart.seat.group.model.ScoredArrangement;
import com.cinesmart.seat.group.service.SeatScoringService;
import com.cinesmart.show.entity.ShowSeat;
import com.cinesmart.show.entity.ShowSeatStatus;
import org.springframework.stereotype.Component;

import java.util.*;
import java.util.stream.Collectors;

@Component
public class AccessibleSeatAllocationStrategy implements SeatAllocationStrategy {

    private final SeatScoringService seatScoringService;

    public AccessibleSeatAllocationStrategy(SeatScoringService seatScoringService) {
        this.seatScoringService = seatScoringService;
    }

    @Override
    public boolean supports(GroupSeatingRequest request) {
        return request != null && Boolean.TRUE.equals(request.getRequireAccessibility());
    }

    @Override
    public List<ScoredArrangement> allocate(List<ShowSeat> availableSeats,
                                             GroupSeatingRequest request,
                                             int totalRows,
                                             int totalCols) {
        List<ScoredArrangement> candidates = new ArrayList<>();
        int n = request.getPartySize();

        if (availableSeats == null || availableSeats.size() < n) {
            return candidates;
        }

        // Filter all available accessible and companion seats
        List<ShowSeat> accessibleSeats = availableSeats.stream()
                .filter(s -> s.getStatus() == ShowSeatStatus.AVAILABLE)
                .filter(s -> s.getSeat() != null && s.getSeat().isActive())
                .filter(s -> s.getSeat().isAccessible())
                .sorted(Comparator.comparingInt(s -> s.getSeat().getColumnNumber()))
                .collect(Collectors.toList());

        // Check if we have enough accessible/companion seats or need adjacent standard seats in the same row
        if (accessibleSeats.size() >= n) {
            // Sliding window across accessible cluster
            for (int i = 0; i <= accessibleSeats.size() - n; i++) {
                List<ShowSeat> window = new ArrayList<>(accessibleSeats.subList(i, i + n));
                boolean hasWheelchair = window.stream()
                        .anyMatch(s -> s.getSeat().getSeatTier() == SeatTier.ACCESSIBLE_WHEELCHAIR);

                if (hasWheelchair) {
                    ScoredArrangement arrangement = seatScoringService.scoreArrangement(
                            window,
                            ArrangementType.ACCESSIBLE_CLUSTER,
                            request,
                            totalRows,
                            totalCols,
                            1,
                            0.0
                    );
                    candidates.add(arrangement);
                }
            }
        } else if (!accessibleSeats.isEmpty()) {
            // Combine accessible seats with adjacent standard seats in row A
            List<ShowSeat> rowASeats = availableSeats.stream()
                    .filter(s -> s.getStatus() == ShowSeatStatus.AVAILABLE)
                    .filter(s -> s.getSeat() != null && s.getSeat().isActive())
                    .filter(s -> "A".equalsIgnoreCase(s.getSeat().getRowIdentifier()))
                    .sorted(Comparator.comparingInt(s -> s.getSeat().getColumnNumber()))
                    .collect(Collectors.toList());

            if (rowASeats.size() >= n) {
                for (int i = 0; i <= rowASeats.size() - n; i++) {
                    List<ShowSeat> window = new ArrayList<>(rowASeats.subList(i, i + n));
                    boolean hasWheelchair = window.stream()
                            .anyMatch(s -> s.getSeat().getSeatTier() == SeatTier.ACCESSIBLE_WHEELCHAIR);

                    if (hasWheelchair) {
                        ScoredArrangement arrangement = seatScoringService.scoreArrangement(
                                window,
                                ArrangementType.ACCESSIBLE_CLUSTER,
                                request,
                                totalRows,
                                totalCols,
                                1,
                                0.0
                        );
                        candidates.add(arrangement);
                    }
                }
            }
        }

        return candidates;
    }
}
