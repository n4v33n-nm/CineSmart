package com.cinesmart.seat.repository;

import com.cinesmart.seat.entity.Seat;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface SeatRepository extends JpaRepository<Seat, Long> {

    List<Seat> findByScreenIdAndIsActiveTrueOrderByRowIdentifierAscColumnNumberAsc(Long screenId);

    List<Seat> findByScreenId(Long screenId);
}
