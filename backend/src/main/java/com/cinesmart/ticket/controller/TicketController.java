package com.cinesmart.ticket.controller;

import com.cinesmart.common.response.ApiResponse;
import com.cinesmart.ticket.dto.TicketDTO;
import com.cinesmart.ticket.service.TicketService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/tickets")
public class TicketController {

    private final TicketService ticketService;

    public TicketController(TicketService ticketService) {
        this.ticketService = ticketService;
    }

    @GetMapping("/booking/{bookingId}")
    public ResponseEntity<ApiResponse<List<TicketDTO>>> getTicketsByBooking(
            @PathVariable Long bookingId,
            @AuthenticationPrincipal UserDetails userDetails) {
        List<TicketDTO> tickets = ticketService.getTicketsByBookingId(bookingId, userDetails.getUsername());
        return ResponseEntity.ok(ApiResponse.success("Tickets retrieved successfully", tickets));
    }

    @GetMapping("/{ticketCode}")
    public ResponseEntity<ApiResponse<TicketDTO>> getTicketByCode(@PathVariable String ticketCode) {
        TicketDTO ticket = ticketService.getTicketByCode(ticketCode);
        return ResponseEntity.ok(ApiResponse.success(ticket));
    }
}
