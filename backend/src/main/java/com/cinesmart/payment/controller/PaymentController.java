package com.cinesmart.payment.controller;

import com.cinesmart.common.response.ApiResponse;
import com.cinesmart.payment.dto.*;
import com.cinesmart.payment.service.PaymentService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/payments")
public class PaymentController {

    private final PaymentService paymentService;

    public PaymentController(PaymentService paymentService) {
        this.paymentService = paymentService;
    }

    /**
     * Standard Payment Endpoint:
     * Initiates and processes payment in a single, robust client operation.
     */
    @PostMapping
    public ResponseEntity<ApiResponse<PaymentResponseDTO>> makePayment(
            @Valid @RequestBody PaymentInitiateRequest request,
            @RequestHeader(value = "Idempotency-Key", required = false) String idempotencyHeader,
            @AuthenticationPrincipal UserDetails userDetails) {
        if (idempotencyHeader != null && !idempotencyHeader.trim().isEmpty() && request.getIdempotencyKey() == null) {
            request.setIdempotencyKey(idempotencyHeader);
        }
        PaymentResponseDTO response = paymentService.initiatePayment(userDetails.getUsername(), request, true);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Payment executed successfully", response));
    }

    /**
     * 2-Step Payment Initiation: Creates an INITIATED payment transaction.
     */
    @PostMapping("/initiate")
    public ResponseEntity<ApiResponse<PaymentResponseDTO>> initiatePayment(
            @Valid @RequestBody PaymentInitiateRequest request,
            @RequestHeader(value = "Idempotency-Key", required = false) String idempotencyHeader,
            @AuthenticationPrincipal UserDetails userDetails) {
        if (idempotencyHeader != null && !idempotencyHeader.trim().isEmpty() && request.getIdempotencyKey() == null) {
            request.setIdempotencyKey(idempotencyHeader);
        }
        PaymentResponseDTO response = paymentService.initiatePayment(userDetails.getUsername(), request, false);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Payment transaction initiated", response));
    }

    /**
     * 2-Step Payment Process: Processes an existing INITIATED payment transaction.
     */
    @PostMapping("/{id}/process")
    public ResponseEntity<ApiResponse<PaymentResponseDTO>> processPayment(
            @PathVariable("id") Long id,
            @RequestBody(required = false) PaymentProcessRequest request,
            @AuthenticationPrincipal UserDetails userDetails) {
        PaymentResponseDTO response = paymentService.processPayment(id, userDetails.getUsername(), request);
        return ResponseEntity.ok(ApiResponse.success("Payment transaction processed", response));
    }

    /**
     * Cancel an initiated payment transaction.
     */
    @PostMapping("/{id}/cancel")
    public ResponseEntity<ApiResponse<PaymentResponseDTO>> cancelPayment(
            @PathVariable("id") Long id,
            @AuthenticationPrincipal UserDetails userDetails) {
        PaymentResponseDTO response = paymentService.cancelPayment(id, userDetails.getUsername());
        return ResponseEntity.ok(ApiResponse.success("Payment transaction cancelled", response));
    }

    /**
     * Retrieve payment details by transaction ID.
     */
    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<PaymentResponseDTO>> getPaymentById(
            @PathVariable("id") Long id,
            @AuthenticationPrincipal UserDetails userDetails) {
        PaymentResponseDTO response = paymentService.getPaymentById(id, userDetails.getUsername());
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    /**
     * Retrieve payment transactions associated with a booking.
     */
    @GetMapping("/booking/{bookingId}")
    public ResponseEntity<ApiResponse<List<PaymentResponseDTO>>> getPaymentsForBooking(
            @PathVariable("bookingId") Long bookingId,
            @AuthenticationPrincipal UserDetails userDetails) {
        List<PaymentResponseDTO> payments = paymentService.getPaymentsForBooking(bookingId, userDetails.getUsername());
        return ResponseEntity.ok(ApiResponse.success("Booking payments retrieved successfully", payments));
    }

    /**
     * Process refund for a completed payment transaction.
     */
    @PostMapping("/{id}/refund")
    public ResponseEntity<ApiResponse<RefundResponseDTO>> refundPayment(
            @PathVariable("id") Long id,
            @RequestBody(required = false) RefundRequestDTO request,
            @AuthenticationPrincipal UserDetails userDetails) {
        RefundResponseDTO response = paymentService.refundPayment(id, userDetails.getUsername(), request);
        return ResponseEntity.ok(ApiResponse.success("Refund processed successfully", response));
    }
}
