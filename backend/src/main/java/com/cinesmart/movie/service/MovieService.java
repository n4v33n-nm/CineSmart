package com.cinesmart.movie.service;

import com.cinesmart.common.exception.ResourceNotFoundException;
import com.cinesmart.movie.dto.MovieCreateRequest;
import com.cinesmart.movie.dto.MovieDTO;
import com.cinesmart.movie.entity.Movie;
import com.cinesmart.movie.repository.MovieRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class MovieService {

    private final MovieRepository movieRepository;

    public MovieService(MovieRepository movieRepository) {
        this.movieRepository = movieRepository;
    }

    public List<MovieDTO> getActiveMovies(String genre, String search) {
        List<Movie> movies;
        if (genre != null && !genre.isBlank()) {
            movies = movieRepository.findByIsActiveTrueAndGenreIgnoreCase(genre.trim());
        } else if (search != null && !search.isBlank()) {
            movies = movieRepository.findByIsActiveTrueAndTitleContainingIgnoreCase(search.trim());
        } else {
            movies = movieRepository.findByIsActiveTrue();
        }

        return movies.stream().map(MovieDTO::fromEntity).collect(Collectors.toList());
    }

    public MovieDTO getMovieById(Long id) {
        Movie movie = movieRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Movie", "id", id));
        return MovieDTO.fromEntity(movie);
    }

    @Transactional
    public MovieDTO createMovie(MovieCreateRequest request) {
        Movie movie = new Movie(
                request.getTitle().trim(),
                request.getSynopsis(),
                request.getDurationMinutes(),
                request.getLanguage().trim(),
                request.getGenre().trim(),
                request.getAgeRating().trim(),
                request.getPosterUrl()
        );
        Movie saved = movieRepository.save(movie);
        return MovieDTO.fromEntity(saved);
    }

    @Transactional
    public MovieDTO updateMovie(Long id, MovieCreateRequest request) {
        Movie movie = movieRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Movie", "id", id));

        movie.setTitle(request.getTitle().trim());
        movie.setSynopsis(request.getSynopsis());
        movie.setDurationMinutes(request.getDurationMinutes());
        movie.setLanguage(request.getLanguage().trim());
        movie.setGenre(request.getGenre().trim());
        movie.setAgeRating(request.getAgeRating().trim());
        if (request.getPosterUrl() != null) {
            movie.setPosterUrl(request.getPosterUrl());
        }

        Movie updated = movieRepository.save(movie);
        return MovieDTO.fromEntity(updated);
    }

    @Transactional
    public void deactivateMovie(Long id) {
        Movie movie = movieRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Movie", "id", id));
        movie.setActive(false);
        movieRepository.save(movie);
    }
}
