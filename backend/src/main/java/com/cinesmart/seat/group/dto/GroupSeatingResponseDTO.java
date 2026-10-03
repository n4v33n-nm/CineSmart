package com.cinesmart.seat.group.dto;

import java.util.Collections;
import java.util.List;

public class GroupSeatingResponseDTO {

    private Long showId;
    private String movieTitle;
    private String screenName;
    private String cinemaName;
    private int partySize;
    private int candidateCount;
    private String message;
    private List<GroupSeatingRecommendationDTO> recommendations;

    public GroupSeatingResponseDTO() {
        this.recommendations = Collections.emptyList();
    }

    public GroupSeatingResponseDTO(Long showId,
                                  String movieTitle,
                                  String screenName,
                                  String cinemaName,
                                  int partySize,
                                  String message,
                                  List<GroupSeatingRecommendationDTO> recommendations) {
        this.showId = showId;
        this.movieTitle = movieTitle;
        this.screenName = screenName;
        this.cinemaName = cinemaName;
        this.partySize = partySize;
        this.message = message;
        this.recommendations = recommendations != null ? recommendations : Collections.emptyList();
        this.candidateCount = this.recommendations.size();
    }

    public Long getShowId() {
        return showId;
    }

    public void setShowId(Long showId) {
        this.showId = showId;
    }

    public String getMovieTitle() {
        return movieTitle;
    }

    public void setMovieTitle(String movieTitle) {
        this.movieTitle = movieTitle;
    }

    public String getScreenName() {
        return screenName;
    }

    public void setScreenName(String screenName) {
        this.screenName = screenName;
    }

    public String getCinemaName() {
        return cinemaName;
    }

    public void setCinemaName(String cinemaName) {
        this.cinemaName = cinemaName;
    }

    public int getPartySize() {
        return partySize;
    }

    public void setPartySize(int partySize) {
        this.partySize = partySize;
    }

    public int getCandidateCount() {
        return candidateCount;
    }

    public void setCandidateCount(int candidateCount) {
        this.candidateCount = candidateCount;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public List<GroupSeatingRecommendationDTO> getRecommendations() {
        return recommendations;
    }

    public void setRecommendations(List<GroupSeatingRecommendationDTO> recommendations) {
        this.recommendations = recommendations;
        this.candidateCount = recommendations != null ? recommendations.size() : 0;
    }
}
