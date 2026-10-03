package com.cinesmart.seat.group.dto;

import com.cinesmart.seat.dto.ShowSeatDTO;
import com.cinesmart.seat.group.model.ScoredArrangement;

import java.math.BigDecimal;
import java.util.List;
import java.util.stream.Collectors;

public class GroupSeatingRecommendationDTO {

    private int rank;
    private String arrangementType;
    private double score;
    private String description;
    private String explanation;
    private BigDecimal totalPrice;
    private List<ShowSeatDTO> seats;

    public GroupSeatingRecommendationDTO() {
    }

    public GroupSeatingRecommendationDTO(int rank,
                                        String arrangementType,
                                        double score,
                                        String description,
                                        String explanation,
                                        BigDecimal totalPrice,
                                        List<ShowSeatDTO> seats) {
        this.rank = rank;
        this.arrangementType = arrangementType;
        this.score = score;
        this.description = description;
        this.explanation = explanation;
        this.totalPrice = totalPrice;
        this.seats = seats;
    }

    public static GroupSeatingRecommendationDTO fromScoredArrangement(int rank, ScoredArrangement arrangement) {
        List<ShowSeatDTO> seatDTOs = arrangement.getSeats().stream()
                .map(ShowSeatDTO::fromEntity)
                .collect(Collectors.toList());

        return new GroupSeatingRecommendationDTO(
                rank,
                arrangement.getArrangementType().name(),
                Math.round(arrangement.getScore() * 10.0) / 10.0,
                arrangement.getDescription(),
                arrangement.getExplanation(),
                arrangement.getTotalPrice(),
                seatDTOs
        );
    }

    public int getRank() {
        return rank;
    }

    public void setRank(int rank) {
        this.rank = rank;
    }

    public String getArrangementType() {
        return arrangementType;
    }

    public void setArrangementType(String arrangementType) {
        this.arrangementType = arrangementType;
    }

    public double getScore() {
        return score;
    }

    public void setScore(double score) {
        this.score = score;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getExplanation() {
        return explanation;
    }

    public void setExplanation(String explanation) {
        this.explanation = explanation;
    }

    public BigDecimal getTotalPrice() {
        return totalPrice;
    }

    public void setTotalPrice(BigDecimal totalPrice) {
        this.totalPrice = totalPrice;
    }

    public List<ShowSeatDTO> getSeats() {
        return seats;
    }

    public void setSeats(List<ShowSeatDTO> seats) {
        this.seats = seats;
    }
}
