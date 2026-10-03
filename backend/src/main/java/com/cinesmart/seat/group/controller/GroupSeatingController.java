package com.cinesmart.seat.group.controller;

import com.cinesmart.common.response.ApiResponse;
import com.cinesmart.seat.group.dto.GroupSeatingRequest;
import com.cinesmart.seat.group.dto.GroupSeatingResponseDTO;
import com.cinesmart.seat.group.service.GroupSeatingService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
public class GroupSeatingController {

    private final GroupSeatingService groupSeatingService;

    public GroupSeatingController(GroupSeatingService groupSeatingService) {
        this.groupSeatingService = groupSeatingService;
    }

    /**
     * Primary endpoint: Request intelligent group seating recommendations for a show.
     */
    @PostMapping("/api/shows/{showId}/group-seating/recommendations")
    public ResponseEntity<ApiResponse<GroupSeatingResponseDTO>> getGroupSeatingRecommendations(
            @PathVariable Long showId,
            @Valid @RequestBody GroupSeatingRequest request) {
        request.setShowId(showId);
        GroupSeatingResponseDTO response = groupSeatingService.getRecommendations(request);
        return ResponseEntity.ok(ApiResponse.success("Group seating recommendations generated successfully", response));
    }

    /**
     * Secondary compatible alias: /api/shows/{showId}/recommendations
     */
    @PostMapping("/api/shows/{showId}/recommendations")
    public ResponseEntity<ApiResponse<GroupSeatingResponseDTO>> getRecommendationsAlias(
            @PathVariable Long showId,
            @Valid @RequestBody GroupSeatingRequest request) {
        request.setShowId(showId);
        GroupSeatingResponseDTO response = groupSeatingService.getRecommendations(request);
        return ResponseEntity.ok(ApiResponse.success("Group seating recommendations generated successfully", response));
    }

    /**
     * Global endpoint compatible with Phase 1 specs: /api/recommendations/group-seats
     */
    @PostMapping("/api/recommendations/group-seats")
    public ResponseEntity<ApiResponse<GroupSeatingResponseDTO>> getGlobalRecommendations(
            @Valid @RequestBody GroupSeatingRequest request) {
        GroupSeatingResponseDTO response = groupSeatingService.getRecommendations(request);
        return ResponseEntity.ok(ApiResponse.success("Group seating recommendations generated successfully", response));
    }
}
