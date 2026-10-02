package com.cinesmart.movie.repository;

import com.cinesmart.movie.entity.Movie;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface MovieRepository extends JpaRepository<Movie, Long> {

    List<Movie> findByIsActiveTrue();

    List<Movie> findByIsActiveTrueAndGenreIgnoreCase(String genre);

    List<Movie> findByIsActiveTrueAndTitleContainingIgnoreCase(String title);
}
