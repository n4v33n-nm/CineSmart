package com.cinesmart;

import com.cinesmart.seat.entity.Seat;
import com.cinesmart.seat.entity.SeatTier;
import com.cinesmart.seat.group.dto.GroupSeatingRequest;
import com.cinesmart.seat.group.model.ArrangementType;
import com.cinesmart.seat.group.model.ScoredArrangement;
import com.cinesmart.seat.group.service.SeatScoringService;
import com.cinesmart.seat.group.strategy.FlexibleSeatAllocationStrategy;
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

@DisplayName("FlexibleSeatAllocationStrategy Unit Tests")
class FlexibleSeatAllocationStrategyTest {

    private SeatScoringService seatScoringService;
    private FlexibleSeatAllocationStrategy strategy;
    private Show testShow;

    @BeforeEach
    void setUp() {
        seatScoringService = new SeatScoringService();
        strategy = new FlexibleSeatAllocationStrategy(seatScoringService);
        testShow = new Show();
        testShow.setId(101L);
    }

    private ShowSeat createSeat(String row, int col, int gridY, SeatTier tier, ShowSeatStatus status) {
        Seat physical = new Seat(null, row, col, tier, col, gridY);
        physical.setId((long) (gridY * 100 + col));
        ShowSeat ss = new ShowSeat(testShow, physical, BigDecimal.valueOf(15.00));
        ss.setId((long) (gridY * 1000 + col));
        ss.setStatus(status);
        return ss;
    }

    @Test
    @DisplayName("Scenario 4: When full contiguous block is unavailable, flexible arrangement finds adjacent split 2+2")
    void testFlexibleSplitTwoPlusTwo() {
        List<ShowSeat> seats = new ArrayList<>();
        // Row D (gridY=4): only seats 5, 6 available (2 seats)
        seats.add(createSeat("D", 5, 4, SeatTier.STANDARD, ShowSeatStatus.AVAILABLE));
        seats.add(createSeat("D", 6, 4, SeatTier.STANDARD, ShowSeatStatus.AVAILABLE));

        // Row E (gridY=5): only seats 5, 6 available (2 seats)
        seats.add(createSeat("E", 5, 5, SeatTier.STANDARD, ShowSeatStatus.AVAILABLE));
        seats.add(createSeat("E", 6, 5, SeatTier.STANDARD, ShowSeatStatus.AVAILABLE));

        // Group of 4 cannot fit in a single row, but fits perfectly in 2+2 across Row D & E
        GroupSeatingRequest request = new GroupSeatingRequest(101L, 4);
        request.setAllowSplitRows(true);

        List<ScoredArrangement> arrangements = strategy.allocate(seats, request, 8, 12);

        assertFalse(arrangements.isEmpty(), "Flexible allocation should find 2+2 split arrangement");
        ScoredArrangement arrangement = arrangements.get(0);
        assertEquals(4, arrangement.getSeats().size());
        assertEquals(ArrangementType.ADJACENT_SPLIT_ROW, arrangement.getArrangementType());
        assertEquals(2, arrangement.getClusterCount());
        // Perfectly aligned columns (both col 5-6) have 0 misalignment penalty
        assertEquals(0.0, arrangement.getMisalignmentPenalty(), 0.001);
    }

    @Test
    @DisplayName("Scenario 4b: Flexible split with odd party size (3+2 = 5)")
    void testFlexibleSplitThreePlusTwo() {
        List<ShowSeat> seats = new ArrayList<>();
        // Row D: seats 4, 5, 6 available
        seats.add(createSeat("D", 4, 4, SeatTier.STANDARD, ShowSeatStatus.AVAILABLE));
        seats.add(createSeat("D", 5, 4, SeatTier.STANDARD, ShowSeatStatus.AVAILABLE));
        seats.add(createSeat("D", 6, 4, SeatTier.STANDARD, ShowSeatStatus.AVAILABLE));

        // Row E: seats 4, 5 available
        seats.add(createSeat("E", 4, 5, SeatTier.STANDARD, ShowSeatStatus.AVAILABLE));
        seats.add(createSeat("E", 5, 5, SeatTier.STANDARD, ShowSeatStatus.AVAILABLE));

        GroupSeatingRequest request = new GroupSeatingRequest(101L, 5);
        request.setAllowSplitRows(true);

        List<ScoredArrangement> arrangements = strategy.allocate(seats, request, 8, 12);

        assertFalse(arrangements.isEmpty());
        ScoredArrangement arrangement = arrangements.get(0);
        assertEquals(5, arrangement.getSeats().size());
        assertEquals(2, arrangement.getClusterCount());
    }

    @Test
    @DisplayName("Scenario 5: Non-adjacent rows (e.g. Row B and Row F) are rejected")
    void testNonAdjacentRowsRejected() {
        List<ShowSeat> seats = new ArrayList<>();
        // Row B (gridY=2)
        seats.add(createSeat("B", 5, 2, SeatTier.STANDARD, ShowSeatStatus.AVAILABLE));
        seats.add(createSeat("B", 6, 2, SeatTier.STANDARD, ShowSeatStatus.AVAILABLE));

        // Row F (gridY=6) - 4 rows apart!
        seats.add(createSeat("F", 5, 6, SeatTier.STANDARD, ShowSeatStatus.AVAILABLE));
        seats.add(createSeat("F", 6, 6, SeatTier.STANDARD, ShowSeatStatus.AVAILABLE));

        GroupSeatingRequest request = new GroupSeatingRequest(101L, 4);
        request.setAllowSplitRows(true);

        List<ScoredArrangement> arrangements = strategy.allocate(seats, request, 8, 12);
        assertTrue(arrangements.isEmpty(), "Non-adjacent rows must not be allocated together");
    }

    @Test
    @DisplayName("Scenario 5b: High column offset across rows is rejected")
    void testExtremeColumnOffsetRejected() {
        List<ShowSeat> seats = new ArrayList<>();
        // Row D: left side (col 1, 2)
        seats.add(createSeat("D", 1, 4, SeatTier.STANDARD, ShowSeatStatus.AVAILABLE));
        seats.add(createSeat("D", 2, 4, SeatTier.STANDARD, ShowSeatStatus.AVAILABLE));

        // Row E: far right side (col 11, 12)
        seats.add(createSeat("E", 11, 5, SeatTier.STANDARD, ShowSeatStatus.AVAILABLE));
        seats.add(createSeat("E", 12, 5, SeatTier.STANDARD, ShowSeatStatus.AVAILABLE));

        GroupSeatingRequest request = new GroupSeatingRequest(101L, 4);
        request.setAllowSplitRows(true);

        List<ScoredArrangement> arrangements = strategy.allocate(seats, request, 8, 12);
        assertTrue(arrangements.isEmpty(), "Arrangements with column offset > 3.5 must be rejected");
    }
}
