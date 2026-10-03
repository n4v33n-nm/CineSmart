package com.cinesmart;

import com.cinesmart.seat.entity.Seat;
import com.cinesmart.seat.entity.SeatTier;
import com.cinesmart.seat.group.dto.GroupSeatingRequest;
import com.cinesmart.seat.group.model.ArrangementType;
import com.cinesmart.seat.group.model.ScoredArrangement;
import com.cinesmart.seat.group.service.SeatScoringService;
import com.cinesmart.show.entity.Show;
import com.cinesmart.show.entity.ShowSeat;
import com.cinesmart.show.entity.ShowSeatStatus;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("SeatScoringService Unit Tests")
class SeatScoringServiceTest {

    private SeatScoringService scoringService;
    private Show testShow;

    @BeforeEach
    void setUp() {
        scoringService = new SeatScoringService();
        testShow = new Show();
        testShow.setId(101L);
    }

    private ShowSeat createSeat(String row, int col, int gridY, SeatTier tier) {
        Seat physical = new Seat(null, row, col, tier, col, gridY);
        physical.setId((long) (gridY * 100 + col));
        ShowSeat ss = new ShowSeat(testShow, physical, BigDecimal.valueOf(15.00));
        ss.setId((long) (gridY * 1000 + col));
        ss.setStatus(ShowSeatStatus.AVAILABLE);
        return ss;
    }

    @Test
    @DisplayName("Scenario 11: Documented scoring formula produces 100 score for prime optical center")
    void testPerfectScoreInOptimalSweetSpot() {
        // Total rows = 8, Total cols = 12
        // X_mid = (12+1)/2 = 6.5
        // Y_opt = round(0.65 * 8) = 5 (Row E)
        // Group of 4 seats: Row E, cols 5, 6, 7, 8 -> mean col = (5+6+7+8)/4 = 6.5!
        List<ShowSeat> seats = Arrays.asList(
                createSeat("E", 5, 5, SeatTier.PREMIUM),
                createSeat("E", 6, 5, SeatTier.PREMIUM),
                createSeat("E", 7, 5, SeatTier.PREMIUM),
                createSeat("E", 8, 5, SeatTier.PREMIUM)
        );

        GroupSeatingRequest request = new GroupSeatingRequest(101L, 4);
        request.setPreferredTier(SeatTier.PREMIUM);

        ScoredArrangement scored = scoringService.scoreArrangement(
                seats,
                ArrangementType.CONTIGUOUS_ROW,
                request,
                8,
                12,
                1,
                0.0
        );

        assertEquals(100.0, scored.getScore(), 0.001);
        assertEquals(0.0, scored.getCenterDeviation(), 0.001);
        assertEquals(0.0, scored.getRowDeviation(), 0.001);
        assertEquals(0.0, scored.getTierPenalty(), 0.001);
        assertEquals(0.0, scored.getSplitPenalty(), 0.001);
        assertEquals(0.0, scored.getMisalignmentPenalty(), 0.001);
    }

    @Test
    @DisplayName("Scenario 10: Preferred tier influence: matching tier has 0 penalty, discordant tier penalizes 40 points")
    void testPreferredTierInfluencesScore() {
        List<ShowSeat> seats = Arrays.asList(
                createSeat("E", 5, 5, SeatTier.STANDARD),
                createSeat("E", 6, 5, SeatTier.STANDARD)
        );

        // Matching preference
        GroupSeatingRequest matchingRequest = new GroupSeatingRequest(101L, 2);
        matchingRequest.setPreferredTier(SeatTier.STANDARD);
        ScoredArrangement matchScore = scoringService.scoreArrangement(
                seats, ArrangementType.CONTIGUOUS_ROW, matchingRequest, 8, 12, 1, 0.0
        );

        // Discordant preference: requested RECLINER but got STANDARD
        GroupSeatingRequest discordRequest = new GroupSeatingRequest(101L, 2);
        discordRequest.setPreferredTier(SeatTier.RECLINER);
        ScoredArrangement discordScore = scoringService.scoreArrangement(
                seats, ArrangementType.CONTIGUOUS_ROW, discordRequest, 8, 12, 1, 0.0
        );

        assertTrue(matchScore.getScore() > discordScore.getScore());
        assertEquals(SeatScoringService.PENALTY_TIER_DISCORDANT, discordScore.getTierPenalty(), 0.001);
    }

    @Test
    @DisplayName("Scenario 3: Deterministic tie-breaking: minimal cluster count (K=1 beats K=2)")
    void testTieBreakerClusterCount() {
        // Candidate A: Contiguous single row (K=1), score 80
        ScoredArrangement arr1 = new ScoredArrangement(
                Arrays.asList(createSeat("E", 1, 5, SeatTier.STANDARD), createSeat("E", 2, 5, SeatTier.STANDARD)),
                80.0, ArrangementType.CONTIGUOUS_ROW, "Contig", "Exp", 1, 10.0, 0.0, 0.0, 0.0, 0.0
        );

        // Candidate B: Split row (K=2), score 80
        ScoredArrangement arr2 = new ScoredArrangement(
                Arrays.asList(createSeat("D", 5, 4, SeatTier.STANDARD), createSeat("E", 5, 5, SeatTier.STANDARD)),
                80.0, ArrangementType.ADJACENT_SPLIT_ROW, "Split", "Exp", 2, 0.0, 0.5, 0.0, 25.0, 0.0
        );

        List<ScoredArrangement> list = new ArrayList<>(Arrays.asList(arr2, arr1));
        list.sort(scoringService.getDeterministicComparator());

        assertEquals(arr1, list.get(0), "Single cluster K=1 must win over K=2 when scores are equal");
    }

    @Test
    @DisplayName("Scenario 3b: Deterministic tie-breaker: symmetric left vs right center blocks resolved by column index")
    void testTieBreakerLeftVsRightSymmetric() {
        // Option 1: Seats 3, 4, 5 (distance to center = 2.5)
        ScoredArrangement opt1 = new ScoredArrangement(
                Arrays.asList(
                        createSeat("E", 3, 5, SeatTier.STANDARD),
                        createSeat("E", 4, 5, SeatTier.STANDARD),
                        createSeat("E", 5, 5, SeatTier.STANDARD)
                ),
                86.6, ArrangementType.CONTIGUOUS_ROW, "Option 1", "Exp", 1, 13.4, 0.0, 0.0, 0.0, 0.0
        );

        // Option 2: Seats 8, 9, 10 (distance to center = 2.5)
        ScoredArrangement opt2 = new ScoredArrangement(
                Arrays.asList(
                        createSeat("E", 8, 5, SeatTier.STANDARD),
                        createSeat("E", 9, 5, SeatTier.STANDARD),
                        createSeat("E", 10, 5, SeatTier.STANDARD)
                ),
                86.6, ArrangementType.CONTIGUOUS_ROW, "Option 2", "Exp", 1, 13.4, 0.0, 0.0, 0.0, 0.0
        );

        List<ScoredArrangement> list = new ArrayList<>(Arrays.asList(opt2, opt1));
        list.sort(scoringService.getDeterministicComparator());

        assertEquals(opt1, list.get(0), "Lower starting column (Col 3) must win tie-breaker over Col 8");
    }

    @Test
    @DisplayName("Scenario 3c: Deterministic tie-breaker: alphabetical row order wins (Row D before Row E)")
    void testTieBreakerAlphabeticalRow() {
        ScoredArrangement rowD = new ScoredArrangement(
                Arrays.asList(createSeat("D", 5, 4, SeatTier.STANDARD), createSeat("D", 6, 4, SeatTier.STANDARD)),
                85.0, ArrangementType.CONTIGUOUS_ROW, "Row D", "Exp", 1, 0.0, 1.0, 0.0, 0.0, 0.0
        );

        ScoredArrangement rowF = new ScoredArrangement(
                Arrays.asList(createSeat("F", 5, 6, SeatTier.STANDARD), createSeat("F", 6, 6, SeatTier.STANDARD)),
                85.0, ArrangementType.CONTIGUOUS_ROW, "Row F", "Exp", 1, 0.0, 1.0, 0.0, 0.0, 0.0
        );

        List<ScoredArrangement> list = new ArrayList<>(Arrays.asList(rowF, rowD));
        list.sort(scoringService.getDeterministicComparator());

        assertEquals(rowD, list.get(0), "Alphabetical row D must win over row F when scores and distances match");
    }
}
