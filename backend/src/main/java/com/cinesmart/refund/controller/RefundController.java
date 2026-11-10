package com.cinesmart.refund.controller;

import com.cinesmart.common.response.ApiResponse;
import com.cinesmart.refund.dto.BookingCancellationRequest;
import com.cinesmart.refund.dto.CancellationPreviewResponseDTO;
import com.cinesmart.refund.dto.RefundRequestDTO;
import com.cinesmart.refund.service.RefundService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
public class RefundController {

    private final RefundService refundService;

    public RefundController(RefundService refundService) {
        this.refundService = refundService;
    }

    /**
     * Preview cancellation charges, deductions, policy tier, and refund amount.
     */
    @GetMapping("/api/bookings/{id}/cancellation-preview")
    public ResponseEntity<ApiResponse<CancellationPreviewResponseDTO>> previewCancellation(
            @PathVariable("id") Long id,
            @AuthenticationPrincipal UserDetails userDetails) {
        CancellationPreviewResponseDTO preview = refundService.previewCancellation(id, userDetails.getUsername());
        return ResponseEntity.ok(ApiResponse.success("Cancellation preview calculated", preview));
    }

    /**
     * Cancel booking and initiate policy-based refund request.
     */
    @PostMapping("/api/bookings/{id}/cancel-request")
    public ResponseEntity<ApiResponse<RefundRequestDTO>> cancelBookingWithRefund(
            @PathVariable("id") Long id,
            @Valid @RequestBody(required = false) BookingCancellationRequest request,
            @AuthenticationPrincipal UserDetails userDetails) {
        RefundRequestDTO response = refundService.cancelBookingWithRefund(id, userDetails.getUsername(), request);
        return ResponseEntity.ok(ApiResponse.success("Booking cancelled and refund processed", response));
    }

    /**
     * Retrieve all refund records and status tracking for the authenticated customer.
     */
    @GetMapping("/api/refunds/my")
    public ResponseEntity<ApiResponse<List<RefundRequestDTO>>> getMyRefunds(
            @AuthenticationPrincipal UserDetails userDetails) {
        List<RefundRequestDTO> list = refundService.getRefundsForUser(userDetails.getUsername());
        return ResponseEntity.ok(ApiResponse.success("User refunds retrieved successfully", list));
    }

    /**
     * Retrieve specific refund details by ID.
     */
    @GetMapping("/api/refunds/{id}")
    public ResponseEntity<ApiResponse<RefundRequestDTO>> getRefundById(
            @PathVariable("id") Long id,
            @AuthenticationPrincipal UserDetails userDetails) {
        RefundRequestDTO dto = refundService.getRefundById(id, userDetails.getUsername());
        return ResponseEntity.ok(ApiResponse.success(dto));
    }
}
