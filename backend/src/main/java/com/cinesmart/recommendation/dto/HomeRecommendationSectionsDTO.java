package com.cinesmart.recommendation.dto;

import java.util.List;

public class HomeRecommendationSectionsDTO {

    private List<MovieRecommendationDTO> recommendedForYou;
    private BecauseYouWatchedDTO becauseYouWatched;
    private List<MovieRecommendationDTO> trendingMovies;
    private List<MovieRecommendationDTO> popularMovies;
    private List<MovieRecommendationDTO> newReleases;
    private List<MovieRecommendationDTO> topRated;
    private boolean isPersonalized;
    private List<String> preferredGenres;

    public HomeRecommendationSectionsDTO() {
    }

    public HomeRecommendationSectionsDTO(List<MovieRecommendationDTO> recommendedForYou,
                                         BecauseYouWatchedDTO becauseYouWatched,
                                         List<MovieRecommendationDTO> trendingMovies,
                                         List<MovieRecommendationDTO> popularMovies,
                                         List<MovieRecommendationDTO> newReleases,
                                         List<MovieRecommendationDTO> topRated,
                                         boolean isPersonalized,
                                         List<String> preferredGenres) {
        this.recommendedForYou = recommendedForYou;
        this.becauseYouWatched = becauseYouWatched;
        this.trendingMovies = trendingMovies;
        this.popularMovies = popularMovies;
        this.newReleases = newReleases;
        this.topRated = topRated;
        this.isPersonalized = isPersonalized;
        this.preferredGenres = preferredGenres;
    }

    public List<MovieRecommendationDTO> getRecommendedForYou() {
        return recommendedForYou;
    }

    public void setRecommendedForYou(List<MovieRecommendationDTO> recommendedForYou) {
        this.recommendedForYou = recommendedForYou;
    }

    public BecauseYouWatchedDTO getBecauseYouWatched() {
        return becauseYouWatched;
    }

    public void setBecauseYouWatched(BecauseYouWatchedDTO becauseYouWatched) {
        this.becauseYouWatched = becauseYouWatched;
    }

    public List<MovieRecommendationDTO> getTrendingMovies() {
        return trendingMovies;
    }

    public void setTrendingMovies(List<MovieRecommendationDTO> trendingMovies) {
        this.trendingMovies = trendingMovies;
    }

    public List<MovieRecommendationDTO> getPopularMovies() {
        return popularMovies;
    }

    public void setPopularMovies(List<MovieRecommendationDTO> popularMovies) {
        this.popularMovies = popularMovies;
    }

    public List<MovieRecommendationDTO> getNewReleases() {
        return newReleases;
    }

    public void setNewReleases(List<MovieRecommendationDTO> newReleases) {
        this.newReleases = newReleases;
    }

    public List<MovieRecommendationDTO> getTopRated() {
        return topRated;
    }

    public void setTopRated(List<MovieRecommendationDTO> topRated) {
        this.topRated = topRated;
    }

    public boolean isPersonalized() {
        return isPersonalized;
    }

    public void setPersonalized(boolean personalized) {
        isPersonalized = personalized;
    }

    public List<String> getPreferredGenres() {
        return preferredGenres;
    }

    public void setPreferredGenres(List<String> preferredGenres) {
        this.preferredGenres = preferredGenres;
    }
}
