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
public class FlexibleSeatAllocationStrategy implements SeatAllocationStrategy {

    private final SeatScoringService seatScoringService;
    private static final double MAX_COLUMN_OFFSET = 3.5;

    public FlexibleSeatAllocationStrategy(SeatScoringService seatScoringService) {
        this.seatScoringService = seatScoringService;
    }

    @Override
    public boolean supports(GroupSeatingRequest request) {
        return request != null
                && Boolean.TRUE.equals(request.getAllowSplitRows())
                && !Boolean.TRUE.equals(request.getRequireAccessibility())
                && request.getPartySize() >= 2;
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
                .filter(s -> !s.getSeat().isAccessible())
                .collect(Collectors.groupingBy(s -> s.getSeat().getRowIdentifier()));

        // Sort rows by rowIdentifier
        List<String> sortedRowKeys = new ArrayList<>(rowMap.keySet());
        Collections.sort(sortedRowKeys);

        // Partition N into 2 parts: n1 + n2 = N
        List<int[]> partitions = new ArrayList<>();
        int half1 = (int) Math.ceil(n / 2.0);
        int half2 = n - half1;
        partitions.add(new int[]{half1, half2});
        if (half1 != half2) {
            partitions.add(new int[]{half2, half1});
        }

        // Iterate through adjacent row pairs
        for (int r = 0; r < sortedRowKeys.size() - 1; r++) {
            String row1Key = sortedRowKeys.get(r);
            String row2Key = sortedRowKeys.get(r + 1);

            List<ShowSeat> row1Seats = rowMap.get(row1Key);
            List<ShowSeat> row2Seats = rowMap.get(row2Key);

            row1Seats.sort(Comparator.comparingInt(s -> s.getSeat().getColumnNumber()));
            row2Seats.sort(Comparator.comparingInt(s -> s.getSeat().getColumnNumber()));

            // Verify they are actually adjacent rows (e.g. row1 gridY + 1 == row2 gridY or consecutive letters)
            int y1 = row1Seats.get(0).getSeat().getGridY();
            int y2 = row2Seats.get(0).getSeat().getGridY();
            if (Math.abs(y1 - y2) != 1) {
                continue;
            }

            for (int[] partition : partitions) {
                int n1 = partition[0];
                int n2 = partition[1];

                List<List<ShowSeat>> blocksRow1 = findContiguousSubBlocks(row1Seats, n1);
                List<List<ShowSeat>> blocksRow2 = findContiguousSubBlocks(row2Seats, n2);

                for (List<ShowSeat> b1 : blocksRow1) {
                    double meanCol1 = b1.stream()
                            .mapToInt(s -> s.getSeat().getColumnNumber())
                            .average()
                            .orElse(0.0);

                    for (List<ShowSeat> b2 : blocksRow2) {
                        double meanCol2 = b2.stream()
                                .mapToInt(s -> s.getSeat().getColumnNumber())
                                .average()
                                .orElse(0.0);

                        double colOffsetDiff = Math.abs(meanCol1 - meanCol2);

                        // Only consider split arrangements with reasonable column proximity
                        if (colOffsetDiff <= MAX_COLUMN_OFFSET) {
                            List<ShowSeat> combinedSeats = new ArrayList<>(n);
                            combinedSeats.addAll(b1);
                            combinedSeats.addAll(b2);

                            ScoredArrangement arrangement = seatScoringService.scoreArrangement(
                                    combinedSeats,
                                    ArrangementType.ADJACENT_SPLIT_ROW,
                                    request,
                                    totalRows,
                                    totalCols,
                                    2,
                                    colOffsetDiff
                            );
                            candidates.add(arrangement);
                        }
                    }
                }
            }
        }

        return candidates;
    }

    private List<List<ShowSeat>> findContiguousSubBlocks(List<ShowSeat> rowSeats, int size) {
        List<List<ShowSeat>> blocks = new ArrayList<>();
        if (rowSeats.size() < size) {
            return blocks;
        }

        for (int i = 0; i <= rowSeats.size() - size; i++) {
            List<ShowSeat> window = new ArrayList<>(size);
            boolean isContiguous = true;

            for (int j = 0; j < size; j++) {
                ShowSeat current = rowSeats.get(i + j);
                if (j > 0) {
                    ShowSeat prev = rowSeats.get(i + j - 1);
                    if (current.getSeat().getColumnNumber() != prev.getSeat().getColumnNumber() + 1) {
                        isContiguous = false;
                        break;
                    }
                }
                window.add(current);
            }

            if (isContiguous) {
                blocks.add(window);
            }
        }
        return blocks;
    }
}
