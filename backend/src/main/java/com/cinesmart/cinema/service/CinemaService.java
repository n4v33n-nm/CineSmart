package com.cinesmart.cinema.service;

import com.cinesmart.cinema.dto.CinemaDTO;
import com.cinesmart.cinema.entity.Cinema;
import com.cinesmart.cinema.repository.CinemaRepository;
import com.cinesmart.common.exception.ResourceNotFoundException;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class CinemaService {

    private final CinemaRepository cinemaRepository;

    public CinemaService(CinemaRepository cinemaRepository) {
        this.cinemaRepository = cinemaRepository;
    }

    public List<CinemaDTO> getAllCinemas() {
        return cinemaRepository.findAll().stream()
                .map(CinemaDTO::fromEntity)
                .collect(Collectors.toList());
    }

    public CinemaDTO getCinemaById(Long id) {
        Cinema cinema = cinemaRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Cinema", "id", id));
        return CinemaDTO.fromEntity(cinema);
    }
}
