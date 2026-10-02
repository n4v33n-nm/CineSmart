package com.cinesmart.show.controller;

import com.cinesmart.common.response.ApiResponse;
import com.cinesmart.show.dto.ShowDTO;
import com.cinesmart.show.service.ShowService;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

@RestController
public class ShowController {

    private final ShowService showService;

    public ShowController(ShowService showService) {
        this.showService = showService;
    }

    @GetMapping("/api/shows")
    public ResponseEntity<ApiResponse<List<ShowDTO>>> getShows(
            @RequestParam(required = false) Long movieId,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date) {
        List<ShowDTO> shows = showService.getShows(movieId, date);
        return ResponseEntity.ok(ApiResponse.success("Shows retrieved successfully", shows));
    }

    @GetMapping("/api/shows/{id}")
    public ResponseEntity<ApiResponse<ShowDTO>> getShowById(@PathVariable Long id) {
        ShowDTO show = showService.getShowById(id);
        return ResponseEntity.ok(ApiResponse.success(show));
    }

    @GetMapping("/api/movies/{movieId}/shows")
    public ResponseEntity<ApiResponse<List<ShowDTO>>> getShowsForMovie(@PathVariable Long movieId) {
        List<ShowDTO> shows = showService.getShowsByMovie(movieId);
        return ResponseEntity.ok(ApiResponse.success("Shows for movie retrieved successfully", shows));
    }
}
