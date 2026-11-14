package com.cinesmart.recommendation.dto;

import java.util.List;

public class UserPreferencesDTO {

    private Long userId;
    private String email;
    private List<String> preferredGenres;
    private List<String> availableGenres;

    public UserPreferencesDTO() {
    }

    public UserPreferencesDTO(Long userId, String email, List<String> preferredGenres, List<String> availableGenres) {
        this.userId = userId;
        this.email = email;
        this.preferredGenres = preferredGenres;
        this.availableGenres = availableGenres;
    }

    public Long getUserId() {
        return userId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public List<String> getPreferredGenres() {
        return preferredGenres;
    }

    public void setPreferredGenres(List<String> preferredGenres) {
        this.preferredGenres = preferredGenres;
    }

    public List<String> getAvailableGenres() {
        return availableGenres;
    }

    public void setAvailableGenres(List<String> availableGenres) {
        this.availableGenres = availableGenres;
    }
}
