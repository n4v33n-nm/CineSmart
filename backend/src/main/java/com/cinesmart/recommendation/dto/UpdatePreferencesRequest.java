package com.cinesmart.recommendation.dto;

import java.util.List;

public class UpdatePreferencesRequest {

    private List<String> preferredGenres;

    public UpdatePreferencesRequest() {
    }

    public UpdatePreferencesRequest(List<String> preferredGenres) {
        this.preferredGenres = preferredGenres;
    }

    public List<String> getPreferredGenres() {
        return preferredGenres;
    }

    public void setPreferredGenres(List<String> preferredGenres) {
        this.preferredGenres = preferredGenres;
    }
}
