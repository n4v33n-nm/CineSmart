package com.cinesmart.recommendation.dto;

import com.cinesmart.movie.dto.MovieDTO;

public class MovieRecommendationDTO {

    private MovieDTO movie;
    private double score;
    private int matchPercentage;
    private String recommendationReason;
    private String category;
    private String matchHighlight;

    public MovieRecommendationDTO() {
    }

    public MovieRecommendationDTO(MovieDTO movie, double score, int matchPercentage, String recommendationReason, String category, String matchHighlight) {
        this.movie = movie;
        this.score = score;
        this.matchPercentage = matchPercentage;
        this.recommendationReason = recommendationReason;
        this.category = category;
        this.matchHighlight = matchHighlight;
    }

    public MovieDTO getMovie() {
        return movie;
    }

    public void setMovie(MovieDTO movie) {
        this.movie = movie;
    }

    public double getScore() {
        return score;
    }

    public void setScore(double score) {
        this.score = score;
    }

    public int getMatchPercentage() {
        return matchPercentage;
    }

    public void setMatchPercentage(int matchPercentage) {
        this.matchPercentage = matchPercentage;
    }

    public String getRecommendationReason() {
        return recommendationReason;
    }

    public void setRecommendationReason(String recommendationReason) {
        this.recommendationReason = recommendationReason;
    }

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public String getMatchHighlight() {
        return matchHighlight;
    }

    public void setMatchHighlight(String matchHighlight) {
        this.matchHighlight = matchHighlight;
    }
}
