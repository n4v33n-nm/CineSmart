package com.cinesmart.movie.dto;

import com.cinesmart.movie.entity.Movie;

public class MovieDTO {

    private Long id;
    private String title;
    private String synopsis;
    private int durationMinutes;
    private String language;
    private String genre;
    private String ageRating;
    private String posterUrl;
    private boolean active;

    public MovieDTO() {
    }

    public MovieDTO(Long id, String title, String synopsis, int durationMinutes, String language, String genre, String ageRating, String posterUrl, boolean active) {
        this.id = id;
        this.title = title;
        this.synopsis = synopsis;
        this.durationMinutes = durationMinutes;
        this.language = language;
        this.genre = genre;
        this.ageRating = ageRating;
        this.posterUrl = posterUrl;
        this.active = active;
    }

    public static MovieDTO fromEntity(Movie movie) {
        if (movie == null) return null;
        return new MovieDTO(
                movie.getId(),
                movie.getTitle(),
                movie.getSynopsis(),
                movie.getDurationMinutes(),
                movie.getLanguage(),
                movie.getGenre(),
                movie.getAgeRating(),
                movie.getPosterUrl(),
                movie.isActive()
        );
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
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

    public boolean isActive() {
        return active;
    }

    public void setActive(boolean active) {
        this.active = active;
    }
}
