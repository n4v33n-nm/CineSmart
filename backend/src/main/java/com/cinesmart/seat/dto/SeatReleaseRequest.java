package com.cinesmart.seat.dto;

import jakarta.validation.constraints.NotEmpty;
import java.util.List;

public class SeatReleaseRequest {

    @NotEmpty(message = "showSeatIds cannot be empty")
    private List<Long> showSeatIds;

    public SeatReleaseRequest() {}

    public SeatReleaseRequest(List<Long> showSeatIds) {
        this.showSeatIds = showSeatIds;
    }

    public List<Long> getShowSeatIds() {
        return showSeatIds;
    }

    public void setShowSeatIds(List<Long> showSeatIds) {
        this.showSeatIds = showSeatIds;
    }
}

