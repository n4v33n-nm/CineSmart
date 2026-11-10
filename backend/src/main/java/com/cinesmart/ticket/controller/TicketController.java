package com.cinesmart.ticket.controller;

import com.cinesmart.common.response.ApiResponse;
import com.cinesmart.ticket.dto.TicketDTO;
import com.cinesmart.ticket.dto.TicketStatsDTO;
import com.cinesmart.ticket.dto.TicketVerificationRequest;
import com.cinesmart.ticket.dto.TicketVerificationResponseDTO;
import com.cinesmart.ticket.service.TicketService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
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

    @PostMapping("/verify")
    @PreAuthorize("hasAnyRole('STAFF', 'ADMIN')")
    public ResponseEntity<ApiResponse<TicketVerificationResponseDTO>> verifyTicket(
            @Valid @RequestBody TicketVerificationRequest request,
            @AuthenticationPrincipal UserDetails userDetails) {
        TicketVerificationResponseDTO response = ticketService.verifyTicket(
                request.getTicketCode(),
                request.getVerificationHash(),
                userDetails.getUsername()
        );
        return ResponseEntity.ok(ApiResponse.success("Ticket verified successfully", response));
    }

    @GetMapping("/stats")
    @PreAuthorize("hasAnyRole('STAFF', 'ADMIN')")
    public ResponseEntity<ApiResponse<TicketStatsDTO>> getTicketStats() {
        TicketStatsDTO stats = ticketService.getTicketStats();
        return ResponseEntity.ok(ApiResponse.success("Ticket verification statistics retrieved", stats));
    }
}
