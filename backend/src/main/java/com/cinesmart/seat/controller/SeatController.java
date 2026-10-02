package com.cinesmart.seat.controller;

import com.cinesmart.common.response.ApiResponse;
import com.cinesmart.seat.dto.SeatMapResponseDTO;
import com.cinesmart.seat.service.SeatService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/shows/{showId}/seats")
public class SeatController {

    private final SeatService seatService;

    public SeatController(SeatService seatService) {
        this.seatService = seatService;
    }

    @GetMapping
    public ResponseEntity<ApiResponse<SeatMapResponseDTO>> getSeatMapForShow(@PathVariable Long showId) {
        SeatMapResponseDTO seatMap = seatService.getSeatMapForShow(showId);
        return ResponseEntity.ok(ApiResponse.success("Seat map retrieved successfully", seatMap));
    }
}
