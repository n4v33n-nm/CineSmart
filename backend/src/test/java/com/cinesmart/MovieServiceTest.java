package com.cinesmart;

import com.cinesmart.common.exception.ResourceNotFoundException;
import com.cinesmart.movie.dto.MovieCreateRequest;
import com.cinesmart.movie.dto.MovieDTO;
import com.cinesmart.movie.entity.Movie;
import com.cinesmart.movie.repository.MovieRepository;
import com.cinesmart.movie.service.MovieService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("MovieService Unit Tests")
class MovieServiceTest {

    @Mock
    private MovieRepository movieRepository;

    @InjectMocks
    private MovieService movieService;

    private Movie sampleMovie;

    @BeforeEach
    void setUp() {
        sampleMovie = new Movie("Inception", "Dream heist", 148, "English", "Sci-Fi", "PG-13", "poster.jpg");
        sampleMovie.setId(10L);
    }

    @Test
    @DisplayName("Should retrieve movie by ID successfully")
    void testGetMovieByIdSuccess() {
        when(movieRepository.findById(10L)).thenReturn(Optional.of(sampleMovie));

        MovieDTO dto = movieService.getMovieById(10L);

        assertNotNull(dto);
        assertEquals(10L, dto.getId());
        assertEquals("Inception", dto.getTitle());
        verify(movieRepository, times(1)).findById(10L);
    }

    @Test
    @DisplayName("Should throw ResourceNotFoundException when movie ID does not exist")
    void testGetMovieByIdNotFound() {
        when(movieRepository.findById(999L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> movieService.getMovieById(999L));
    }

    @Test
    @DisplayName("Should create movie successfully from request")
    void testCreateMovie() {
        MovieCreateRequest request = new MovieCreateRequest("Avatar", "Pandora", 162, "English", "Sci-Fi", "PG-13", "avatar.jpg");
        when(movieRepository.save(any(Movie.class))).thenReturn(sampleMovie);

        MovieDTO created = movieService.createMovie(request);

        assertNotNull(created);
        verify(movieRepository, times(1)).save(any(Movie.class));
    }
}
