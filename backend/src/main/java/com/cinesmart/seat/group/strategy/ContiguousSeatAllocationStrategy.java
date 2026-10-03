package com.cinesmart.seat.group.strategy;

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
public class ContiguousSeatAllocationStrategy implements SeatAllocationStrategy {

    private final SeatScoringService seatScoringService;

    public ContiguousSeatAllocationStrategy(SeatScoringService seatScoringService) {
        this.seatScoringService = seatScoringService;
    }

    @Override
    public boolean supports(GroupSeatingRequest request) {
        // Contiguous allocation is evaluated for all non-accessible or standard group requests
        return request != null && !Boolean.TRUE.equals(request.getRequireAccessibility());
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

        // Group available seats by row identifier
        Map<String, List<ShowSeat>> rowMap = availableSeats.stream()
                .filter(s -> s.getStatus() == ShowSeatStatus.AVAILABLE)
                .filter(s -> s.getSeat() != null && s.getSeat().isActive())
                .filter(s -> !s.getSeat().isAccessible()) // Reserve accessible for accessible requests
                .collect(Collectors.groupingBy(s -> s.getSeat().getRowIdentifier()));

        for (Map.Entry<String, List<ShowSeat>> entry : rowMap.entrySet()) {
            List<ShowSeat> rowSeats = entry.getValue();
            // Sort by column number ascending
            rowSeats.sort(Comparator.comparingInt(s -> s.getSeat().getColumnNumber()));

            if (rowSeats.size() < n) {
                continue;
            }

            // Sliding window scan of size N
            for (int i = 0; i <= rowSeats.size() - n; i++) {
                List<ShowSeat> window = new ArrayList<>(n);
                boolean isContiguous = true;

                for (int j = 0; j < n; j++) {
                    ShowSeat current = rowSeats.get(i + j);
                    if (j > 0) {
                        ShowSeat prev = rowSeats.get(i + j - 1);
                        // Check exact consecutive column number
                        if (current.getSeat().getColumnNumber() != prev.getSeat().getColumnNumber() + 1) {
                            isContiguous = false;
                            break;
                        }
                    }
                    window.add(current);
                }

                if (isContiguous) {
                    ScoredArrangement arrangement = seatScoringService.scoreArrangement(
                            window,
                            ArrangementType.CONTIGUOUS_ROW,
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

        return candidates;
    }
}
