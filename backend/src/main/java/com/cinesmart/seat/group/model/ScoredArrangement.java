package com.cinesmart.seat.group.model;

import com.cinesmart.show.entity.ShowSeat;

import java.math.BigDecimal;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

public class ScoredArrangement {

    private final List<ShowSeat> seats;
    private final double score;
    private final ArrangementType arrangementType;
    private final String description;
    private final String explanation;
    private final int clusterCount;
    private final double centerDeviation;
    private final double rowDeviation;
    private final double tierPenalty;
    private final double splitPenalty;
    private final double misalignmentPenalty;

    public ScoredArrangement(List<ShowSeat> seats,
                             double score,
                             ArrangementType arrangementType,
                             String description,
                             String explanation,
                             int clusterCount,
                             double centerDeviation,
                             double rowDeviation,
                             double tierPenalty,
                             double splitPenalty,
                             double misalignmentPenalty) {
        this.seats = seats;
        this.score = score;
        this.arrangementType = arrangementType;
        this.description = description;
        this.explanation = explanation;
        this.clusterCount = clusterCount;
        this.centerDeviation = centerDeviation;
        this.rowDeviation = rowDeviation;
        this.tierPenalty = tierPenalty;
        this.splitPenalty = splitPenalty;
        this.misalignmentPenalty = misalignmentPenalty;
    }

    public List<ShowSeat> getSeats() {
        return Collections.unmodifiableList(seats);
    }

    public double getScore() {
        return score;
    }

    public ArrangementType getArrangementType() {
        return arrangementType;
    }

    public String getDescription() {
        return description;
    }

    public String getExplanation() {
        return explanation;
    }

    public int getClusterCount() {
        return clusterCount;
    }

    public double getCenterDeviation() {
        return centerDeviation;
    }

    public double getRowDeviation() {
        return rowDeviation;
    }

    public double getTierPenalty() {
        return tierPenalty;
    }

    public double getSplitPenalty() {
        return splitPenalty;
    }

    public double getMisalignmentPenalty() {
        return misalignmentPenalty;
    }

    public BigDecimal getTotalPrice() {
        return seats.stream()
                .map(ShowSeat::getPrice)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    public String getMinRow() {
        return seats.stream()
                .map(s -> s.getSeat() != null ? s.getSeat().getRowIdentifier() : "")
                .min(Comparator.naturalOrder())
                .orElse("");
    }

    public int getMinCol() {
        return seats.stream()
                .mapToInt(s -> s.getSeat() != null ? s.getSeat().getColumnNumber() : 0)
                .min()
                .orElse(0);
    }

    public double getRowDistance() {
        return rowDeviation;
    }
}
