package com.cinesmart.movie.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Positive;

public class MovieCreateRequest {

    @NotBlank(message = "Movie title is required")
    private String title;

    private String synopsis;

    @Positive(message = "Duration must be positive")
    private int durationMinutes;

    @NotBlank(message = "Language is required")
    private String language;

    @NotBlank(message = "Genre is required")
    private String genre;

    @NotBlank(message = "Age rating is required")
    private String ageRating;

    private String posterUrl;

    public MovieCreateRequest() {
    }

    public MovieCreateRequest(String title, String synopsis, int durationMinutes, String language, String genre, String ageRating, String posterUrl) {
        this.title = title;
        this.synopsis = synopsis;
        this.durationMinutes = durationMinutes;
        this.language = language;
        this.genre = genre;
        this.ageRating = ageRating;
        this.posterUrl = posterUrl;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getSynopsis() {
        return synopsis;
    }

    public void setSynopsis(String synopsis) {
        this.synopsis = synopsis;
    }

    public int getDurationMinutes() {
        return durationMinutes;
    }

    public void setDurationMinutes(int durationMinutes) {
        this.durationMinutes = durationMinutes;
    }

    public String getLanguage() {
        return language;
    }

    public void setLanguage(String language) {
        this.language = language;
    }

    public String getGenre() {
        return genre;
    }

    public void setGenre(String genre) {
        this.genre = genre;
    }

    public String getAgeRating() {
        return ageRating;
    }

    public void setAgeRating(String ageRating) {
        this.ageRating = ageRating;
    }

    public String getPosterUrl() {
        return posterUrl;
    }

    public void setPosterUrl(String posterUrl) {
        this.posterUrl = posterUrl;
    }
}
