package com.cinesmart.cinema.controller;

import com.cinesmart.cinema.dto.CinemaDTO;
import com.cinesmart.cinema.service.CinemaService;
import com.cinesmart.common.response.ApiResponse;
import com.cinesmart.screen.dto.ScreenDTO;
import com.cinesmart.screen.service.ScreenService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/cinemas")
public class CinemaController {

    private final CinemaService cinemaService;
    private final ScreenService screenService;

    public CinemaController(CinemaService cinemaService, ScreenService screenService) {
        this.cinemaService = cinemaService;
        this.screenService = screenService;
    }

    @GetMapping
    public ResponseEntity<ApiResponse<List<CinemaDTO>>> getAllCinemas() {
        List<CinemaDTO> cinemas = cinemaService.getAllCinemas();
        return ResponseEntity.ok(ApiResponse.success("Cinemas retrieved successfully", cinemas));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<CinemaDTO>> getCinemaById(@PathVariable Long id) {
        CinemaDTO cinema = cinemaService.getCinemaById(id);
        return ResponseEntity.ok(ApiResponse.success(cinema));
    }

    @GetMapping("/{id}/screens")
    public ResponseEntity<ApiResponse<List<ScreenDTO>>> getScreensByCinema(@PathVariable Long id) {
        List<ScreenDTO> screens = screenService.getScreensByCinema(id);
        return ResponseEntity.ok(ApiResponse.success("Screens retrieved successfully", screens));
    }
}
