package com.cinesmart;

import com.cinesmart.cinema.entity.Cinema;
import com.cinesmart.cinema.entity.ScreenType;
import com.cinesmart.common.exception.BadRequestException;
import com.cinesmart.common.exception.ResourceNotFoundException;
import com.cinesmart.movie.entity.Movie;
import com.cinesmart.screen.entity.Screen;
import com.cinesmart.seat.entity.Seat;
import com.cinesmart.seat.entity.SeatTier;
import com.cinesmart.seat.group.dto.GroupSeatingRequest;
import com.cinesmart.seat.group.dto.GroupSeatingResponseDTO;
import com.cinesmart.seat.group.service.GroupSeatingService;
import com.cinesmart.seat.group.service.SeatScoringService;
import com.cinesmart.seat.group.strategy.AccessibleSeatAllocationStrategy;
import com.cinesmart.seat.group.strategy.ContiguousSeatAllocationStrategy;
import com.cinesmart.seat.group.strategy.FlexibleSeatAllocationStrategy;
import com.cinesmart.show.entity.Show;
import com.cinesmart.show.entity.ShowSeat;
import com.cinesmart.show.entity.ShowSeatStatus;
import com.cinesmart.show.repository.ShowRepository;
import com.cinesmart.show.repository.ShowSeatRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@DisplayName("GroupSeatingService Unit Tests")
class GroupSeatingServiceTest {

    @Mock
    private ShowRepository showRepository;

    @Mock
    private ShowSeatRepository showSeatRepository;

    private GroupSeatingService groupSeatingService;
    private Show testShow;
    private List<ShowSeat> sampleShowSeats;

    @BeforeEach
    void setUp() {
        SeatScoringService scoringService = new SeatScoringService();
        ContiguousSeatAllocationStrategy contiguousStrategy = new ContiguousSeatAllocationStrategy(scoringService);
        FlexibleSeatAllocationStrategy flexibleStrategy = new FlexibleSeatAllocationStrategy(scoringService);
        AccessibleSeatAllocationStrategy accessibleStrategy = new AccessibleSeatAllocationStrategy(scoringService);

        groupSeatingService = new GroupSeatingService(
                showRepository,
                showSeatRepository,
                scoringService,
                contiguousStrategy,
                flexibleStrategy,
                accessibleStrategy
        );

        Cinema cinema = new Cinema("CineSmart Downtown", "Metropolis", "100 Broadway", "+1-555-1234");
        Screen screen = new Screen(cinema, 1, ScreenType.STANDARD, 48);
        Movie movie = new Movie("Interstellar", "Space voyage", 169, "English", "Sci-Fi", "PG-13", "url");

        testShow = new Show(movie, screen, LocalDateTime.now(), LocalDateTime.now().plusHours(2), BigDecimal.valueOf(15.00));
        testShow.setId(200L);

        // Generate 48 sample seats: Rows A-F, Cols 1-8
        sampleShowSeats = new ArrayList<>();
        String[] rows = {"A", "B", "C", "D", "E", "F"};
        for (int r = 0; r < rows.length; r++) {
            String row = rows[r];
            for (int col = 1; col <= 8; col++) {
                SeatTier tier;
                if (r == 0 && (col == 1 || col == 2)) {
                    tier = SeatTier.ACCESSIBLE_WHEELCHAIR;
                } else if (r == 0 && (col == 3 || col == 4)) {
                    tier = SeatTier.ACCESSIBLE_COMPANION;
                } else {
                    tier = SeatTier.STANDARD;
                }

                Seat seat = new Seat(screen, row, col, tier, col, r + 1);
                seat.setId((long) (r * 100 + col));

                ShowSeat ss = new ShowSeat(testShow, seat, BigDecimal.valueOf(15.00));
                ss.setId((long) (r * 1000 + col));
                ss.setStatus(ShowSeatStatus.AVAILABLE);
                sampleShowSeats.add(ss);
            }
        }
    }

    @Test
    @DisplayName("Scenario 7: Invalid group size (<= 0 or > 10) throws BadRequestException")
    void testInvalidGroupSize() {
        GroupSeatingRequest zeroReq = new GroupSeatingRequest(200L, 0);
        assertThrows(BadRequestException.class, () -> groupSeatingService.getRecommendations(zeroReq));

        GroupSeatingRequest negativeReq = new GroupSeatingRequest(200L, -3);
        assertThrows(BadRequestException.class, () -> groupSeatingService.getRecommendations(negativeReq));

        GroupSeatingRequest oversizedReq = new GroupSeatingRequest(200L, 11);
        assertThrows(BadRequestException.class, () -> groupSeatingService.getRecommendations(oversizedReq));
    }

    @Test
    @DisplayName("Scenario 12: Group size larger than total available capacity is rejected with informative response")
    void testGroupSizeExceedsAvailableCapacity() {
        // Mark 46 seats as BOOKED, leaving only 2 AVAILABLE
        for (int i = 0; i < 46; i++) {
            sampleShowSeats.get(i).setStatus(ShowSeatStatus.BOOKED);
        }

        when(showRepository.findById(200L)).thenReturn(Optional.of(testShow));
        when(showSeatRepository.findByShowId(200L)).thenReturn(sampleShowSeats);

        GroupSeatingRequest request = new GroupSeatingRequest(200L, 4); // asks for 4 when only 2 exist
        GroupSeatingResponseDTO response = groupSeatingService.getRecommendations(request);

        assertNotNull(response);
        assertEquals(0, response.getCandidateCount());
        assertTrue(response.getMessage().contains("exceeds available capacity"));
    }

    @Test
    @DisplayName("Scenario 1 & 6: Successful recommendations do not alter seat status (read-only)")
    void testSuccessfulRecommendationDoesNotReserveSeats() {
        when(showRepository.findById(200L)).thenReturn(Optional.of(testShow));
        when(showSeatRepository.findByShowId(200L)).thenReturn(sampleShowSeats);

        GroupSeatingRequest request = new GroupSeatingRequest(200L, 4);
        GroupSeatingResponseDTO response = groupSeatingService.getRecommendations(request);

        assertNotNull(response);
        assertTrue(response.getCandidateCount() > 0);
        assertTrue(response.getCandidateCount() <= 3);

        // Verification: Recommendation is purely advisory; seats MUST remain AVAILABLE in memory!
        for (ShowSeat ss : sampleShowSeats) {
            assertEquals(ShowSeatStatus.AVAILABLE, ss.getStatus());
        }
    }

    @Test
    @DisplayName("Scenario Hard Gate: Wheelchair accessible request returns certified accessible cluster")
    void testWheelchairAccessibleRecommendation() {
        when(showRepository.findById(200L)).thenReturn(Optional.of(testShow));
        when(showSeatRepository.findByShowId(200L)).thenReturn(sampleShowSeats);

        GroupSeatingRequest request = new GroupSeatingRequest(200L, 3);
        request.setRequireAccessibility(true);

        GroupSeatingResponseDTO response = groupSeatingService.getRecommendations(request);

        assertNotNull(response);
        assertTrue(response.getCandidateCount() > 0);
        assertEquals("ACCESSIBLE_CLUSTER", response.getRecommendations().get(0).getArrangementType());

        // Verify that the recommended seats include wheelchair space
        boolean hasWheelchair = response.getRecommendations().get(0).getSeats().stream()
                .anyMatch(s -> s.getTier() == SeatTier.ACCESSIBLE_WHEELCHAIR);
        assertTrue(hasWheelchair, "Must include certified wheelchair space");
    }

    @Test
    @DisplayName("Invalid show ID throws ResourceNotFoundException")
    void testShowNotFound() {
        when(showRepository.findById(999L)).thenReturn(Optional.empty());

        GroupSeatingRequest request = new GroupSeatingRequest(999L, 4);
        assertThrows(ResourceNotFoundException.class, () -> groupSeatingService.getRecommendations(request));
    }
}
