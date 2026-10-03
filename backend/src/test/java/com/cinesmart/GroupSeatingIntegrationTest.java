package com.cinesmart;

import com.cinesmart.seat.group.controller.GroupSeatingController;
import com.cinesmart.seat.group.dto.GroupSeatingRecommendationDTO;
import com.cinesmart.seat.group.dto.GroupSeatingResponseDTO;
import com.cinesmart.seat.group.service.GroupSeatingService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.FilterType;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.util.Collections;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(controllers = GroupSeatingController.class,
        excludeFilters = @ComponentScan.Filter(type = FilterType.ASSIGNABLE_TYPE,
                classes = {com.cinesmart.config.SecurityConfig.class, com.cinesmart.config.JwtAuthenticationFilter.class}))
@AutoConfigureMockMvc(addFilters = false)
@DisplayName("GroupSeating REST API Integration Tests")
class GroupSeatingIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private GroupSeatingService groupSeatingService;

    @Test
    @DisplayName("POST /api/shows/{showId}/group-seating/recommendations should return 200 OK and recommendation payload")
    void testGroupSeatingRecommendationSuccess() throws Exception {
        GroupSeatingRecommendationDTO rec = new GroupSeatingRecommendationDTO(
                1,
                "CONTIGUOUS_ROW",
                96.5,
                "Row E, Seats 5 to 8 (Contiguous)",
                "Prime center optical alignment.",
                BigDecimal.valueOf(60.00),
                Collections.emptyList()
        );

        GroupSeatingResponseDTO mockResponse = new GroupSeatingResponseDTO(
                101L,
                "Interstellar Odyssey",
                "Screen 1",
                "CineSmart Central",
                4,
                "Found 1 intelligent group seating recommendations.",
                Collections.singletonList(rec)
        );

        when(groupSeatingService.getRecommendations(any())).thenReturn(mockResponse);

        String requestBody = "{\"partySize\": 4, \"preferredTier\": \"STANDARD\", \"allowSplitRows\": true}";

        mockMvc.perform(post("/api/shows/101/group-seating/recommendations")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestBody))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.showId").value(101))
                .andExpect(jsonPath("$.data.partySize").value(4))
                .andExpect(jsonPath("$.data.candidateCount").value(1))
                .andExpect(jsonPath("$.data.recommendations[0].rank").value(1))
                .andExpect(jsonPath("$.data.recommendations[0].score").value(96.5))
                .andExpect(jsonPath("$.data.recommendations[0].arrangementType").value("CONTIGUOUS_ROW"));
    }

    @Test
    @DisplayName("POST /api/shows/{showId}/group-seating/recommendations with invalid group size (0) returns 400 Bad Request")
    void testInvalidGroupSizeReturns400() throws Exception {
        String requestBody = "{\"partySize\": 0}";

        mockMvc.perform(post("/api/shows/101/group-seating/recommendations")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestBody))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false));
    }

    @Test
    @DisplayName("POST /api/shows/{showId}/group-seating/recommendations with excessive group size (> 10) returns 400 Bad Request")
    void testOversizedGroupSizeReturns400() throws Exception {
        String requestBody = "{\"partySize\": 15}";

        mockMvc.perform(post("/api/shows/101/group-seating/recommendations")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestBody))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false));
    }
}
