package com.cinesmart.analytics.dto;

import java.util.List;
import java.util.Map;

public class AnalyticsFilterOptionsDTO {

    private List<Map<String, Object>> movies;
    private List<Map<String, Object>> cinemas;
    private List<Map<String, Object>> shows;
    private List<String> statuses;

    public AnalyticsFilterOptionsDTO() {
    }

    public AnalyticsFilterOptionsDTO(List<Map<String, Object>> movies,
                                     List<Map<String, Object>> cinemas,
                                     List<Map<String, Object>> shows,
                                     List<String> statuses) {
        this.movies = movies;
        this.cinemas = cinemas;
        this.shows = shows;
        this.statuses = statuses;
    }

    public List<Map<String, Object>> getMovies() {
        return movies;
    }

    public void setMovies(List<Map<String, Object>> movies) {
        this.movies = movies;
    }

    public List<Map<String, Object>> getCinemas() {
        return cinemas;
    }

    public void setCinemas(List<Map<String, Object>> cinemas) {
        this.cinemas = cinemas;
    }

    public List<Map<String, Object>> getShows() {
        return shows;
    }

    public void setShows(List<Map<String, Object>> shows) {
        this.shows = shows;
    }

    public List<String> getStatuses() {
        return statuses;
    }

    public void setStatuses(List<String> statuses) {
        this.statuses = statuses;
    }
}
