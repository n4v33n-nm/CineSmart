package com.cinesmart.screen.service;

import com.cinesmart.cinema.entity.Cinema;
import com.cinesmart.cinema.repository.CinemaRepository;
import com.cinesmart.common.exception.ResourceNotFoundException;
import com.cinesmart.screen.dto.ScreenCreateRequest;
import com.cinesmart.screen.dto.ScreenDTO;
import com.cinesmart.screen.entity.Screen;
import com.cinesmart.screen.repository.ScreenRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class ScreenService {

    private final ScreenRepository screenRepository;
    private final CinemaRepository cinemaRepository;

    public ScreenService(ScreenRepository screenRepository, CinemaRepository cinemaRepository) {
        this.screenRepository = screenRepository;
        this.cinemaRepository = cinemaRepository;
    }

    public List<ScreenDTO> getScreensByCinema(Long cinemaId) {
        return screenRepository.findByCinemaId(cinemaId).stream()
                .map(ScreenDTO::fromEntity)
                .collect(Collectors.toList());
    }

    public ScreenDTO getScreenById(Long id) {
        Screen screen = screenRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Screen", "id", id));
        return ScreenDTO.fromEntity(screen);
    }

    @Transactional
    public ScreenDTO createScreen(ScreenCreateRequest request) {
        Cinema cinema = cinemaRepository.findById(request.getCinemaId())
                .orElseThrow(() -> new ResourceNotFoundException("Cinema", "id", request.getCinemaId()));

        Screen screen = new Screen(
                cinema,
                request.getScreenNumber(),
                request.getScreenType(),
                request.getTotalCapacity()
        );

        Screen saved = screenRepository.save(screen);
        return ScreenDTO.fromEntity(saved);
    }
}
