package com.cinesmart.booking.repository;

import com.cinesmart.booking.entity.Booking;
import com.cinesmart.booking.entity.BookingStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface BookingRepository extends JpaRepository<Booking, Long> {

    List<Booking> findByUserIdOrderByCreatedAtDesc(Long userId);

    Optional<Booking> findByBookingReference(String bookingReference);

    List<Booking> findByShowId(Long showId);

    List<Booking> findByShowIdAndStatus(Long showId, BookingStatus status);

    List<Booking> findByStatusAndExpiresAtBefore(BookingStatus status, java.time.LocalDateTime time);

    List<Booking> findByStatusInAndExpiresAtBefore(List<BookingStatus> statuses, java.time.LocalDateTime time);

    @Query("SELECT b.show.movie.id, COUNT(b) FROM Booking b WHERE b.status = com.cinesmart.booking.entity.BookingStatus.CONFIRMED GROUP BY b.show.movie.id")
    List<Object[]> countConfirmedBookingsGroupByMovieId();

    @Query("SELECT b.show.movie.id, COUNT(b) FROM Booking b WHERE b.status = com.cinesmart.booking.entity.BookingStatus.CONFIRMED AND b.createdAt >= :since GROUP BY b.show.movie.id")
    List<Object[]> countConfirmedBookingsSinceGroupByMovieId(@Param("since") LocalDateTime since);

    @Query("SELECT b FROM Booking b WHERE b.user.id = :userId AND b.status = com.cinesmart.booking.entity.BookingStatus.CONFIRMED ORDER BY b.createdAt DESC")
    List<Booking> findConfirmedByUserIdOrderByCreatedAtDesc(@Param("userId") Long userId);
}
