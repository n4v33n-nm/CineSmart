package com.cinesmart.seat.service;

import com.cinesmart.booking.entity.Booking;
import com.cinesmart.booking.entity.BookingStatus;
import com.cinesmart.booking.repository.BookingRepository;
import com.cinesmart.show.entity.ShowSeat;
import com.cinesmart.show.entity.ShowSeatStatus;
import com.cinesmart.show.repository.ShowSeatRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;

@Service
public class SeatLockCleanupService {

    private static final Logger log = LoggerFactory.getLogger(SeatLockCleanupService.class);

    private final ShowSeatRepository showSeatRepository;
    private final BookingRepository bookingRepository;

    public SeatLockCleanupService(ShowSeatRepository showSeatRepository,
                                  BookingRepository bookingRepository) {
        this.showSeatRepository = showSeatRepository;
        this.bookingRepository = bookingRepository;
    }

    /**
     * Periodic scheduled cleanup of expired seat locks and abandoned bookings.
     * Runs every 15 seconds.
     */
    @Scheduled(fixedDelay = 15000)
    @Transactional
    public void cleanupExpiredLocks() {
        LocalDateTime now = LocalDateTime.now();
        List<ShowSeatStatus> lockStatuses = Arrays.asList(ShowSeatStatus.LOCKED, ShowSeatStatus.HELD);

        // 1. Release expired show seats
        List<ShowSeat> expiredSeats = showSeatRepository.findByStatusInAndLockedUntilBefore(lockStatuses, now);
        if (!expiredSeats.isEmpty()) {
            for (ShowSeat seat : expiredSeats) {
                seat.release();
            }
            showSeatRepository.saveAll(expiredSeats);
            log.info("Released {} expired seat locks across the system.", expiredSeats.size());
        }

        // 2. Mark abandoned pending bookings as EXPIRED
        List<Booking> expiredBookings = bookingRepository.findByStatusAndExpiresAtBefore(BookingStatus.PENDING_PAYMENT, now);
        if (!expiredBookings.isEmpty()) {
            for (Booking booking : expiredBookings) {
                booking.setStatus(BookingStatus.EXPIRED);
            }
            bookingRepository.saveAll(expiredBookings);
            log.info("Marked {} abandoned pending bookings as EXPIRED.", expiredBookings.size());
        }
    }

    /**
     * Eagerly release any expired locks for a specific show before retrieving the seat map.
     */
    @Transactional
    public void releaseExpiredLocksForShow(Long showId) {
        LocalDateTime now = LocalDateTime.now();
        List<ShowSeatStatus> lockStatuses = Arrays.asList(ShowSeatStatus.LOCKED, ShowSeatStatus.HELD);
        List<ShowSeat> expiredSeats = showSeatRepository.findByShowIdAndStatusInAndLockedUntilBefore(showId, lockStatuses, now);

        if (!expiredSeats.isEmpty()) {
            for (ShowSeat seat : expiredSeats) {
                seat.release();
            }
            showSeatRepository.saveAll(expiredSeats);
            log.info("Eagerly released {} expired seat locks for show {}.", expiredSeats.size(), showId);
        }
    }
}

