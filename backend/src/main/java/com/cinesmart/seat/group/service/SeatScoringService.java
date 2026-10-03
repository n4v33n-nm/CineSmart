package com.cinesmart.seat.group.service;

import com.cinesmart.seat.entity.Seat;
import com.cinesmart.seat.entity.SeatTier;
import com.cinesmart.seat.group.dto.GroupSeatingRequest;
import com.cinesmart.seat.group.model.ArrangementType;
import com.cinesmart.seat.group.model.ScoredArrangement;
import com.cinesmart.show.entity.ShowSeat;
import org.springframework.stereotype.Service;

import java.util.Comparator;
import java.util.List;

@Service
public class SeatScoringService {

    // Configurable, documented mathematical weights from CineSmart Algorithm Design
    public static final double WEIGHT_CENTER = 0.35;
    public static final double WEIGHT_ROW = 0.25;
    public static final double WEIGHT_MISALIGNMENT = 3.0;

    public static final double PENALTY_TIER_ADJACENT = 15.0;
    public static final double PENALTY_TIER_DISCORDANT = 40.0;
    public static final double PENALTY_SPLIT_ADJACENT = 25.0;
    public static final double PENALTY_SPLIT_DISJOINT = 50.0;
    public static final double OPTIMAL_ROW_FACTOR = 0.65;

    public ScoredArrangement scoreArrangement(List<ShowSeat> seats,
                                             ArrangementType arrangementType,
                                             GroupSeatingRequest request,
                                             int totalRows,
                                             int totalCols,
                                             int clusterCount,
                                             double misalignmentOffset) {
        if (seats == null || seats.isEmpty()) {
            throw new IllegalArgumentException("Cannot score empty seat arrangement");
        }

        double xMid = (totalCols + 1) / 2.0;
        int yOpt = (int) Math.round(OPTIMAL_ROW_FACTOR * totalRows);
        if (yOpt < 1) yOpt = 1;

        // If user explicitly specified preferred row (e.g., 'E' -> row 5), factor it into optimal row
        if (request.getPreferredRow() != null && !request.getPreferredRow().trim().isEmpty()) {
            String pRow = request.getPreferredRow().trim().toUpperCase();
            char firstChar = pRow.charAt(0);
            if (firstChar >= 'A' && firstChar <= 'Z') {
                yOpt = (firstChar - 'A') + 1;
            }
        }

        // Calculate mean grid coordinates
        double meanX = seats.stream()
                .mapToDouble(s -> s.getSeat() != null ? s.getSeat().getGridX() : 1)
                .average()
                .orElse(xMid);

        double meanY = seats.stream()
                .mapToDouble(s -> s.getSeat() != null ? s.getSeat().getGridY() : 1)
                .average()
                .orElse(yOpt);

        // 1. Center Screen Viewing Angle Penalty
        double centerDiff = Math.abs(meanX - xMid);
        double pCenter = WEIGHT_CENTER * (centerDiff / (xMid > 0 ? xMid : 1.0)) * 100.0;

        // 2. Row Depth Proximity Penalty
        double rowDiff = Math.abs(meanY - yOpt);
        double pRow = WEIGHT_ROW * (rowDiff / (totalRows > 0 ? totalRows : 1.0)) * 100.0;

        // 3. Tier Concordance Penalty
        double pTier = computeTierPenalty(seats, request.getPreferredTier());

        // 4. Cluster Split Penalty
        double pSplit = 0.0;
        if (clusterCount == 2) {
            pSplit = PENALTY_SPLIT_ADJACENT;
        } else if (clusterCount > 2) {
            pSplit = PENALTY_SPLIT_DISJOINT;
        }

        // 5. Misalignment Penalty
        double pMisalign = WEIGHT_MISALIGNMENT * misalignmentOffset;

        // Cumulative Score Normalization: Score = max(0, 100 - sum(Penalties))
        double totalPenalty = pCenter + pRow + pTier + pSplit + pMisalign;
        double rawScore = 100.0 - totalPenalty;
        double finalScore = Math.max(0.0, Math.min(100.0, rawScore));

        String description = generateDescription(seats, arrangementType);
        String explanation = generateExplanation(finalScore, pCenter, pRow, pTier, pSplit, pMisalign, seats, request);

        return new ScoredArrangement(
                seats,
                finalScore,
                arrangementType,
                description,
                explanation,
                clusterCount,
                pCenter,
                rowDiff,
                pTier,
                pSplit,
                pMisalign
        );
    }

    private double computeTierPenalty(List<ShowSeat> seats, SeatTier preferredTier) {
        if (preferredTier == null) {
            return 0.0;
        }

        boolean allMatch = seats.stream().allMatch(s -> s.getSeat() != null && s.getSeat().getSeatTier() == preferredTier);
        if (allMatch) {
            return 0.0;
        }

        boolean acceptableAdjacent = seats.stream().allMatch(s -> {
            if (s.getSeat() == null) return false;
            SeatTier st = s.getSeat().getSeatTier();
            if (st == preferredTier) return true;
            if (preferredTier == SeatTier.PREMIUM && (st == SeatTier.STANDARD || st == SeatTier.RECLINER)) return true;
            if (preferredTier == SeatTier.RECLINER && st == SeatTier.PREMIUM) return true;
            if (preferredTier == SeatTier.STANDARD && st == SeatTier.PREMIUM) return true;
            return false;
        });

        return acceptableAdjacent ? PENALTY_TIER_ADJACENT : PENALTY_TIER_DISCORDANT;
    }

    private String generateDescription(List<ShowSeat> seats, ArrangementType arrangementType) {
        if (arrangementType == ArrangementType.CONTIGUOUS_ROW) {
            Seat firstSeat = seats.get(0).getSeat();
            Seat lastSeat = seats.get(seats.size() - 1).getSeat();
            if (firstSeat != null && lastSeat != null) {
                return String.format("Row %s, Seats %d to %d (Contiguous)",
                        firstSeat.getRowIdentifier(), firstSeat.getColumnNumber(), lastSeat.getColumnNumber());
            }
        } else if (arrangementType == ArrangementType.ADJACENT_SPLIT_ROW) {
            String rows = seats.stream()
                    .map(s -> s.getSeat() != null ? s.getSeat().getRowIdentifier() : "")
                    .distinct()
                    .sorted()
                    .reduce((a, b) -> a + " & " + b)
                    .orElse("");
            return String.format("Rows %s (Adjacent Split, %d Seats)", rows, seats.size());
        } else if (arrangementType == ArrangementType.ACCESSIBLE_CLUSTER) {
            return String.format("Row A Accessible Space & Companion (%d Seats)", seats.size());
        }

        return "Group Seating Block (" + seats.size() + " Seats)";
    }

    private String generateExplanation(double score,
                                       double pCenter,
                                       double pRow,
                                       double pTier,
                                       double pSplit,
                                       double pMisalign,
                                       List<ShowSeat> seats,
                                       GroupSeatingRequest request) {
        StringBuilder sb = new StringBuilder();
        if (pSplit == 0.0) {
            sb.append("Fully contiguous single-row block. ");
        } else {
            sb.append(String.format("Split across 2 adjacent rows (split penalty -%.1f). ", pSplit));
        }

        if (pCenter < 5.0) {
            sb.append("Prime center optical alignment. ");
        } else if (pCenter > 20.0) {
            sb.append("Off-center viewing angle. ");
        }

        if (pRow < 5.0) {
            sb.append("Optimal viewing distance. ");
        }

        if (request.getPreferredTier() != null) {
            if (pTier == 0.0) {
                sb.append(String.format("100%% match with preferred tier (%s). ", request.getPreferredTier()));
            } else {
                sb.append(String.format("Alternative tier allocated (tier penalty -%.1f). ", pTier));
            }
        }

        if (pMisalign > 0.0) {
            sb.append(String.format("Row column offset penalty (-%.1f). ", pMisalign));
        }

        return sb.toString().trim();
    }

    /**
     * Deterministic Comparator enforcing the tie-breaking rules:
     * 1. Match score DESC
     * 2. Cluster count ASC (1 contiguous strictly beats split)
     * 3. Row distance to optimal row ASC
     * 4. Alphabetical minimum row identifier ASC
     * 5. Lower starting column number ASC
     */
    public Comparator<ScoredArrangement> getDeterministicComparator() {
        return (a, b) -> {
            // Rule 1: Match Score DESC (higher is better)
            int scoreCmp = Double.compare(b.getScore(), a.getScore());
            if (Math.abs(b.getScore() - a.getScore()) > 0.001) {
                return scoreCmp;
            }

            // Rule 2: Minimal Cluster Count ASC (K=1 strictly wins over K=2)
            int clusterCmp = Integer.compare(a.getClusterCount(), b.getClusterCount());
            if (clusterCmp != 0) {
                return clusterCmp;
            }

            // Rule 3: Closer distance to optimal row ASC
            int rowDistCmp = Double.compare(a.getRowDistance(), b.getRowDistance());
            if (Math.abs(a.getRowDistance() - b.getRowDistance()) > 0.001) {
                return rowDistCmp;
            }

            // Rule 4: Alphabetical minimum row identifier ASC (Row A before Row B)
            int rowIdCmp = a.getMinRow().compareTo(b.getMinRow());
            if (rowIdCmp != 0) {
                return rowIdCmp;
            }

            // Rule 5: Left-to-right starting column index ASC
            return Integer.compare(a.getMinCol(), b.getMinCol());
        };
    }
}
