package com.cinesmart.admin.controller;

import com.cinesmart.common.response.ApiResponse;
import com.cinesmart.movie.dto.MovieCreateRequest;
import com.cinesmart.movie.dto.MovieDTO;
import com.cinesmart.movie.service.MovieService;
import com.cinesmart.screen.dto.ScreenCreateRequest;
import com.cinesmart.screen.dto.ScreenDTO;
import com.cinesmart.screen.service.ScreenService;
import com.cinesmart.show.dto.ShowCreateRequest;
import com.cinesmart.show.dto.ShowDTO;
import com.cinesmart.show.service.ShowService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/admin")
@PreAuthorize("hasRole('ADMIN')")
public class AdminController {

    private final MovieService movieService;
    private final ShowService showService;
    private final ScreenService screenService;

    public AdminController(MovieService movieService, ShowService showService, ScreenService screenService) {
        this.movieService = movieService;
        this.showService = showService;
        this.screenService = screenService;
    }

    @PostMapping("/movies")
    public ResponseEntity<ApiResponse<MovieDTO>> createMovie(@Valid @RequestBody MovieCreateRequest request) {
        MovieDTO movie = movieService.createMovie(request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Movie created successfully", movie));
    }

    @PutMapping("/movies/{id}")
    public ResponseEntity<ApiResponse<MovieDTO>> updateMovie(
            @PathVariable Long id,
            @Valid @RequestBody MovieCreateRequest request) {
        MovieDTO movie = movieService.updateMovie(id, request);
        return ResponseEntity.ok(ApiResponse.success("Movie updated successfully", movie));
    }

    @DeleteMapping("/movies/{id}")
    public ResponseEntity<ApiResponse<Void>> deactivateMovie(@PathVariable Long id) {
        movieService.deactivateMovie(id);
        return ResponseEntity.ok(ApiResponse.success("Movie deactivated successfully", null));
    }

    @PostMapping("/shows")
    public ResponseEntity<ApiResponse<ShowDTO>> createShow(@Valid @RequestBody ShowCreateRequest request) {
        ShowDTO show = showService.createShow(request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Show created and seats instantiated successfully", show));
    }

    @PutMapping("/shows/{id}/cancel")
    public ResponseEntity<ApiResponse<ShowDTO>> cancelShow(@PathVariable Long id) {
        ShowDTO show = showService.cancelShow(id);
        return ResponseEntity.ok(ApiResponse.success("Show cancelled successfully", show));
    }

    @PostMapping("/screens")
    public ResponseEntity<ApiResponse<ScreenDTO>> createScreen(@Valid @RequestBody ScreenCreateRequest request) {
        ScreenDTO screen = screenService.createScreen(request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Screen created successfully", screen));
    }
}
