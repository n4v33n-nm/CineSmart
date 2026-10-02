package com.cinesmart;

import com.cinesmart.movie.entity.Movie;
import com.cinesmart.movie.repository.MovieRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@DataJpaTest
@DisplayName("MovieRepository JPA Integration Tests")
class MovieRepositoryTest {

    @Autowired
    private MovieRepository movieRepository;

    @Test
    @DisplayName("Should save and retrieve active movies filtered by genre and title")
    void testMovieFiltering() {
        Movie movie1 = new Movie("Interstellar", "Space voyage", 169, "English", "Sci-Fi", "PG-13", "poster1.jpg");
        Movie movie2 = new Movie("Inception", "Dream heist", 148, "English", "Sci-Fi", "PG-13", "poster2.jpg");
        Movie movie3 = new Movie("Gladiator", "Roman epic", 155, "English", "Action", "R", "poster3.jpg");

        movieRepository.save(movie1);
        movieRepository.save(movie2);
        movieRepository.save(movie3);

        List<Movie> sciFiMovies = movieRepository.findByIsActiveTrueAndGenreIgnoreCase("sci-fi");
        assertEquals(2, sciFiMovies.size());

        List<Movie> searchResult = movieRepository.findByIsActiveTrueAndTitleContainingIgnoreCase("stellar");
        assertEquals(1, searchResult.size());
        assertEquals("Interstellar", searchResult.get(0).getTitle());
    }
}
