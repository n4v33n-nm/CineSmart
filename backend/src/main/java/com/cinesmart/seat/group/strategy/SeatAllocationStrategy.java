package com.cinesmart.seat.group.strategy;

import com.cinesmart.seat.group.dto.GroupSeatingRequest;
import com.cinesmart.seat.group.model.ScoredArrangement;
import com.cinesmart.show.entity.ShowSeat;

import java.util.List;

public interface SeatAllocationStrategy {

    /**
     * Executes the seat allocation strategy on the available inventory.
     *
     * @param availableSeats the list of currently available ShowSeats
     * @param request        the group seating request parameters
     * @param totalRows      the total number of rows in the auditorium
     * @param totalCols      the total number of columns in the auditorium
     * @return candidate scored arrangements satisfying the strategy constraints
     */
    List<ScoredArrangement> allocate(List<ShowSeat> availableSeats,
                                     GroupSeatingRequest request,
                                     int totalRows,
                                     int totalCols);

    /**
     * Checks if this strategy supports the given request.
     */
    boolean supports(GroupSeatingRequest request);
}
