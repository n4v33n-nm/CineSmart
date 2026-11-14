package com.cinesmart.recommendation.dto;

import com.cinesmart.movie.dto.MovieDTO;
import java.util.List;

public class BecauseYouWatchedDTO {

    private MovieDTO sourceMovie;
    private List<MovieRecommendationDTO> recommendations;

    public BecauseYouWatchedDTO() {
    }

    public BecauseYouWatchedDTO(MovieDTO sourceMovie, List<MovieRecommendationDTO> recommendations) {
        this.sourceMovie = sourceMovie;
        this.recommendations = recommendations;
    }

    public MovieDTO getSourceMovie() {
        return sourceMovie;
    }

    public void setSourceMovie(MovieDTO sourceMovie) {
        this.sourceMovie = sourceMovie;
    }

    public List<MovieRecommendationDTO> getRecommendations() {
        return recommendations;
    }

    public void setRecommendations(List<MovieRecommendationDTO> recommendations) {
        this.recommendations = recommendations;
    }
}
