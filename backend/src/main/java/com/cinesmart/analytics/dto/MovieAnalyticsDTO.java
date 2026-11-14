package com.cinesmart.analytics.dto;

import java.util.List;

public class MovieAnalyticsDTO {

    private List<CategoryDataPointDTO> mostBookedMovies;
    private List<CategoryDataPointDTO> popularGenres;
    private List<CategoryDataPointDTO> movieWiseRevenue;

    public MovieAnalyticsDTO() {
    }

    public MovieAnalyticsDTO(List<CategoryDataPointDTO> mostBookedMovies,
                             List<CategoryDataPointDTO> popularGenres,
                             List<CategoryDataPointDTO> movieWiseRevenue) {
        this.mostBookedMovies = mostBookedMovies;
        this.popularGenres = popularGenres;
        this.movieWiseRevenue = movieWiseRevenue;
    }

    public List<CategoryDataPointDTO> getMostBookedMovies() {
        return mostBookedMovies;
    }

    public void setMostBookedMovies(List<CategoryDataPointDTO> mostBookedMovies) {
        this.mostBookedMovies = mostBookedMovies;
    }

    public List<CategoryDataPointDTO> getPopularGenres() {
        return popularGenres;
    }

    public void setPopularGenres(List<CategoryDataPointDTO> popularGenres) {
        this.popularGenres = popularGenres;
    }

    public List<CategoryDataPointDTO> getMovieWiseRevenue() {
        return movieWiseRevenue;
    }

    public void setMovieWiseRevenue(List<CategoryDataPointDTO> movieWiseRevenue) {
        this.movieWiseRevenue = movieWiseRevenue;
    }
}
