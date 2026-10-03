package com.cinesmart;

import com.cinesmart.seat.entity.Seat;
import com.cinesmart.seat.entity.SeatTier;
import com.cinesmart.seat.group.dto.GroupSeatingRequest;
import com.cinesmart.seat.group.model.ArrangementType;
import com.cinesmart.seat.group.model.ScoredArrangement;
import com.cinesmart.seat.group.service.SeatScoringService;
import com.cinesmart.seat.group.strategy.ContiguousSeatAllocationStrategy;
import com.cinesmart.show.entity.Show;
import com.cinesmart.show.entity.ShowSeat;
import com.cinesmart.show.entity.ShowSeatStatus;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("ContiguousSeatAllocationStrategy Unit Tests")
class ContiguousSeatAllocationStrategyTest {

    private SeatScoringService seatScoringService;
    private ContiguousSeatAllocationStrategy strategy;
    private Show testShow;

    @BeforeEach
    void setUp() {
        seatScoringService = new SeatScoringService();
        strategy = new ContiguousSeatAllocationStrategy(seatScoringService);
        testShow = new Show();
        testShow.setId(101L);
    }

    private ShowSeat createSeat(String row, int col, SeatTier tier, ShowSeatStatus status) {
        Seat physical = new Seat(null, row, col, tier, col, row.charAt(0) - 'A' + 1);
        physical.setId((long) (row.charAt(0) * 100 + col));
        ShowSeat ss = new ShowSeat(testShow, physical, BigDecimal.valueOf(15.00));
        ss.setId((long) (row.charAt(0) * 1000 + col));
        ss.setStatus(status);
        return ss;
    }

    @Test
    @DisplayName("Scenario 1: A group can be allocated four consecutive seats in the same row")
    void testAllocateFourConsecutiveSeats() {
        List<ShowSeat> seats = new ArrayList<>();
        // Row E: Seats 1 to 6 all AVAILABLE
        for (int c = 1; c <= 6; c++) {
            seats.add(createSeat("E", c, SeatTier.STANDARD, ShowSeatStatus.AVAILABLE));
        }

        GroupSeatingRequest request = new GroupSeatingRequest(101L, 4);
        List<ScoredArrangement> arrangements = strategy.allocate(seats, request, 8, 12);

        assertFalse(arrangements.isEmpty());
        // For 6 consecutive seats and group size 4, there are 3 possible sliding windows (1-4, 2-5, 3-6)
        assertEquals(3, arrangements.size());

        for (ScoredArrangement arr : arrangements) {
            assertEquals(4, arr.getSeats().size());
            assertEquals(ArrangementType.CONTIGUOUS_ROW, arr.getArrangementType());
            assertEquals(1, arr.getClusterCount());

            // Verify seats are strictly consecutive
            for (int i = 0; i < 3; i++) {
                assertEquals(
                        arr.getSeats().get(i).getSeat().getColumnNumber() + 1,
                        arr.getSeats().get(i + 1).getSeat().getColumnNumber()
                );
            }
        }
    }

    @Test
    @DisplayName("Scenario 2: A group can be allocated two consecutive seats")
    void testAllocateTwoConsecutiveSeats() {
        List<ShowSeat> seats = new ArrayList<>();
        seats.add(createSeat("C", 3, SeatTier.STANDARD, ShowSeatStatus.AVAILABLE));
        seats.add(createSeat("C", 4, SeatTier.STANDARD, ShowSeatStatus.AVAILABLE));

        GroupSeatingRequest request = new GroupSeatingRequest(101L, 2);
        List<ScoredArrangement> arrangements = strategy.allocate(seats, request, 8, 12);

        assertEquals(1, arrangements.size());
        assertEquals(2, arrangements.get(0).getSeats().size());
        assertEquals(3, arrangements.get(0).getSeats().get(0).getSeat().getColumnNumber());
        assertEquals(4, arrangements.get(0).getSeats().get(1).getSeat().getColumnNumber());
    }

    @Test
    @DisplayName("Scenario 3: Non-consecutive seats or aisle gaps are not allocated as contiguous")
    void testNonConsecutiveSeatsNotAllocatedAsContiguous() {
        List<ShowSeat> seats = new ArrayList<>();
        seats.add(createSeat("D", 1, SeatTier.STANDARD, ShowSeatStatus.AVAILABLE));
        seats.add(createSeat("D", 2, SeatTier.STANDARD, ShowSeatStatus.AVAILABLE));
        // Seat 3 is missing / gap!
        seats.add(createSeat("D", 4, SeatTier.STANDARD, ShowSeatStatus.AVAILABLE));
        seats.add(createSeat("D", 5, SeatTier.STANDARD, ShowSeatStatus.AVAILABLE));

        // Group size 3 cannot fit in (1,2) or (4,5)
        GroupSeatingRequest request = new GroupSeatingRequest(101L, 3);
        List<ScoredArrangement> arrangements = strategy.allocate(seats, request, 8, 12);

        assertTrue(arrangements.isEmpty(), "Should not form contiguous arrangement across gap");
    }

    @Test
    @DisplayName("Scenario 9: Unavailable (BOOKED, HELD, BLOCKED) seats are never recommended")
    void testUnavailableSeatsNeverRecommended() {
        List<ShowSeat> seats = new ArrayList<>();
        seats.add(createSeat("E", 1, SeatTier.STANDARD, ShowSeatStatus.AVAILABLE));
        seats.add(createSeat("E", 2, SeatTier.STANDARD, ShowSeatStatus.BOOKED)); // BOOKED
        seats.add(createSeat("E", 3, SeatTier.STANDARD, ShowSeatStatus.AVAILABLE));
        seats.add(createSeat("E", 4, SeatTier.STANDARD, ShowSeatStatus.HELD)); // HELD
        seats.add(createSeat("E", 5, SeatTier.STANDARD, ShowSeatStatus.AVAILABLE));
        seats.add(createSeat("E", 6, SeatTier.STANDARD, ShowSeatStatus.BLOCKED)); // BLOCKED

        GroupSeatingRequest request = new GroupSeatingRequest(101L, 2);
        List<ScoredArrangement> arrangements = strategy.allocate(seats, request, 8, 12);

        // No two adjacent seats are AVAILABLE
        assertTrue(arrangements.isEmpty());
    }

    @Test
    @DisplayName("Scenario 6: All seats occupied returns empty candidates")
    void testAllSeatsOccupied() {
        List<ShowSeat> seats = new ArrayList<>();
        for (int c = 1; c <= 8; c++) {
            seats.add(createSeat("E", c, SeatTier.STANDARD, ShowSeatStatus.BOOKED));
        }

        GroupSeatingRequest request = new GroupSeatingRequest(101L, 4);
        List<ScoredArrangement> arrangements = strategy.allocate(seats, request, 8, 12);

        assertTrue(arrangements.isEmpty());
    }
}
