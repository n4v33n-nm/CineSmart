package com.cinesmart.recommendation.controller;

import com.cinesmart.common.response.ApiResponse;
import com.cinesmart.recommendation.dto.HomeRecommendationSectionsDTO;
import com.cinesmart.recommendation.dto.MovieRecommendationDTO;
import com.cinesmart.recommendation.dto.UpdatePreferencesRequest;
import com.cinesmart.recommendation.dto.UserPreferencesDTO;
import com.cinesmart.recommendation.service.RecommendationService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/recommendations")
public class RecommendationController {

    private final RecommendationService recommendationService;

    public RecommendationController(RecommendationService recommendationService) {
        this.recommendationService = recommendationService;
    }

    @GetMapping("/personalized")
    public ResponseEntity<ApiResponse<List<MovieRecommendationDTO>>> getPersonalizedRecommendations(
            @RequestParam(name = "limit", defaultValue = "6") int limit,
            @AuthenticationPrincipal UserDetails userDetails) {
        String email = userDetails != null ? userDetails.getUsername() : null;
        List<MovieRecommendationDTO> recommendations = recommendationService.getPersonalizedRecommendations(email, limit);
        return ResponseEntity.ok(ApiResponse.success("Personalized recommendations retrieved successfully", recommendations));
    }

    @GetMapping("/home-sections")
    public ResponseEntity<ApiResponse<HomeRecommendationSectionsDTO>> getHomeSections(
            @AuthenticationPrincipal UserDetails userDetails) {
        String email = userDetails != null ? userDetails.getUsername() : null;
        HomeRecommendationSectionsDTO sections = recommendationService.getHomeSections(email);
        return ResponseEntity.ok(ApiResponse.success("Home recommendation sections retrieved successfully", sections));
    }

    @GetMapping("/movies/{id}/similar")
    public ResponseEntity<ApiResponse<List<MovieRecommendationDTO>>> getSimilarMovies(
            @PathVariable("id") Long id,
            @RequestParam(name = "limit", defaultValue = "4") int limit) {
        List<MovieRecommendationDTO> similar = recommendationService.getSimilarMovies(id, limit);
        return ResponseEntity.ok(ApiResponse.success("Similar movies retrieved successfully", similar));
    }

    @GetMapping("/preferences")
    public ResponseEntity<ApiResponse<UserPreferencesDTO>> getUserPreferences(
            @AuthenticationPrincipal UserDetails userDetails) {
        if (userDetails == null) {
            return ResponseEntity.status(401).body(ApiResponse.error("Authentication required", "UNAUTHORIZED"));
        }
        UserPreferencesDTO prefs = recommendationService.getUserPreferences(userDetails.getUsername());
        return ResponseEntity.ok(ApiResponse.success("User preferences retrieved successfully", prefs));
    }

    @PutMapping("/preferences")
    public ResponseEntity<ApiResponse<UserPreferencesDTO>> updateUserPreferences(
            @RequestBody UpdatePreferencesRequest request,
            @AuthenticationPrincipal UserDetails userDetails) {
        if (userDetails == null) {
            return ResponseEntity.status(401).body(ApiResponse.error("Authentication required", "UNAUTHORIZED"));
        }
        UserPreferencesDTO prefs = recommendationService.updateUserPreferences(
                userDetails.getUsername(),
                request != null ? request.getPreferredGenres() : null
        );
        return ResponseEntity.ok(ApiResponse.success("User preferences updated successfully", prefs));
    }
}
