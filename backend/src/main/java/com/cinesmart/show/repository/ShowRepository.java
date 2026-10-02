package com.cinesmart.show.repository;

import com.cinesmart.show.entity.Show;
import com.cinesmart.show.entity.ShowStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface ShowRepository extends JpaRepository<Show, Long> {

    List<Show> findByMovieId(Long movieId);

    List<Show> findByMovieIdAndStatus(Long movieId, ShowStatus status);

    List<Show> findByMovieIdAndStartTimeAfterOrderByStartTimeAsc(Long movieId, LocalDateTime startTime);

    List<Show> findByScreenId(Long screenId);

    List<Show> findByStartTimeBetweenOrderByStartTimeAsc(LocalDateTime start, LocalDateTime end);
}
