package com.cinesmart.booking.repository;

import com.cinesmart.booking.entity.Booking;
import com.cinesmart.booking.entity.BookingStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface BookingRepository extends JpaRepository<Booking, Long> {

    List<Booking> findByUserIdOrderByCreatedAtDesc(Long userId);

    Optional<Booking> findByBookingReference(String bookingReference);

    List<Booking> findByShowId(Long showId);

    List<Booking> findByShowIdAndStatus(Long showId, BookingStatus status);
}
