package com.cinesmart.movie.controller;

import com.cinesmart.common.response.ApiResponse;
import com.cinesmart.movie.dto.MovieDTO;
import com.cinesmart.movie.service.MovieService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/movies")
public class MovieController {

    private final MovieService movieService;

    public MovieController(MovieService movieService) {
        this.movieService = movieService;
    }

    @GetMapping
    public ResponseEntity<ApiResponse<List<MovieDTO>>> getMovies(
            @RequestParam(required = false) String genre,
            @RequestParam(required = false) String search) {
        List<MovieDTO> movies = movieService.getActiveMovies(genre, search);
        return ResponseEntity.ok(ApiResponse.success("Movies retrieved successfully", movies));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<MovieDTO>> getMovieById(@PathVariable Long id) {
        MovieDTO movie = movieService.getMovieById(id);
        return ResponseEntity.ok(ApiResponse.success(movie));
    }
}
