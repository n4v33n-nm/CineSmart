package com.cinesmart.seat.controller;

import com.cinesmart.common.response.ApiResponse;
import com.cinesmart.seat.dto.SeatLockRequest;
import com.cinesmart.seat.dto.SeatLockResponseDTO;
import com.cinesmart.seat.dto.SeatMapResponseDTO;
import com.cinesmart.seat.dto.SeatReleaseRequest;
import com.cinesmart.seat.service.SeatService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/shows/{showId}/seats")
public class SeatController {

    private final SeatService seatService;

    public SeatController(SeatService seatService) {
        this.seatService = seatService;
    }

    @GetMapping
    public ResponseEntity<ApiResponse<SeatMapResponseDTO>> getSeatMapForShow(@PathVariable("showId") Long showId) {
        SeatMapResponseDTO seatMap = seatService.getSeatMapForShow(showId);
        return ResponseEntity.ok(ApiResponse.success("Seat map retrieved successfully", seatMap));
    }

    @PostMapping("/lock")
    public ResponseEntity<ApiResponse<SeatLockResponseDTO>> lockSeats(
            @PathVariable("showId") Long showId,
            @Valid @RequestBody SeatLockRequest request,
            @AuthenticationPrincipal UserDetails userDetails) {
        SeatLockResponseDTO response = seatService.lockSeats(showId, request.getShowSeatIds(), userDetails.getUsername());
        return ResponseEntity.ok(ApiResponse.success("Seats locked successfully", response));
    }

    @PostMapping("/release")
    public ResponseEntity<ApiResponse<SeatLockResponseDTO>> releaseSeats(
            @PathVariable("showId") Long showId,
            @Valid @RequestBody SeatReleaseRequest request,
            @AuthenticationPrincipal UserDetails userDetails) {
        SeatLockResponseDTO response = seatService.releaseSeats(showId, request.getShowSeatIds(), userDetails.getUsername());
        return ResponseEntity.ok(ApiResponse.success("Seats released successfully", response));
    }
}
