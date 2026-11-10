package com.cinesmart.refund.controller;

import com.cinesmart.common.response.ApiResponse;
import com.cinesmart.refund.dto.*;
import com.cinesmart.refund.entity.RefundStatus;
import com.cinesmart.refund.service.CancellationPolicyService;
import com.cinesmart.refund.service.RefundService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/admin")
@PreAuthorize("hasRole('ADMIN')")
public class AdminRefundController {

    private final RefundService refundService;
    private final CancellationPolicyService cancellationPolicyService;

    public AdminRefundController(RefundService refundService,
                                 CancellationPolicyService cancellationPolicyService) {
        this.refundService = refundService;
        this.cancellationPolicyService = cancellationPolicyService;
    }

    // -------------------------------------------------------------
    // Cancellation Policy Management
    // -------------------------------------------------------------

    @GetMapping("/cancellation-policies")
    public ResponseEntity<ApiResponse<List<CancellationPolicyDTO>>> getAllPolicies() {
        List<CancellationPolicyDTO> list = cancellationPolicyService.getAllPolicies();
        return ResponseEntity.ok(ApiResponse.success("All cancellation policies retrieved", list));
    }

    @GetMapping("/cancellation-policies/{id}")
    public ResponseEntity<ApiResponse<CancellationPolicyDTO>> getPolicyById(@PathVariable("id") Long id) {
        CancellationPolicyDTO dto = cancellationPolicyService.getPolicyById(id);
        return ResponseEntity.ok(ApiResponse.success(dto));
    }

    @PostMapping("/cancellation-policies")
    public ResponseEntity<ApiResponse<CancellationPolicyDTO>> createPolicy(
            @Valid @RequestBody CancellationPolicyCreateRequest request) {
        CancellationPolicyDTO created = cancellationPolicyService.createPolicy(request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Cancellation policy created successfully", created));
    }

    @PutMapping("/cancellation-policies/{id}")
    public ResponseEntity<ApiResponse<CancellationPolicyDTO>> updatePolicy(
            @PathVariable("id") Long id,
            @Valid @RequestBody CancellationPolicyCreateRequest request) {
        CancellationPolicyDTO updated = cancellationPolicyService.updatePolicy(id, request);
        return ResponseEntity.ok(ApiResponse.success("Cancellation policy updated successfully", updated));
    }

    @RequestMapping(value = "/cancellation-policies/{id}/toggle", method = {RequestMethod.PUT, RequestMethod.PATCH})
    public ResponseEntity<ApiResponse<CancellationPolicyDTO>> togglePolicyStatus(@PathVariable("id") Long id) {
        CancellationPolicyDTO toggled = cancellationPolicyService.togglePolicyStatus(id);
        return ResponseEntity.ok(ApiResponse.success("Cancellation policy status toggled", toggled));
    }

    @DeleteMapping("/cancellation-policies/{id}")
    public ResponseEntity<ApiResponse<Void>> deletePolicy(@PathVariable("id") Long id) {
        cancellationPolicyService.deletePolicy(id);
        return ResponseEntity.ok(ApiResponse.success("Cancellation policy deactivated successfully", null));
    }

    // -------------------------------------------------------------
    // Refund Request Administration
    // -------------------------------------------------------------

    @GetMapping("/refunds")
    public ResponseEntity<ApiResponse<List<RefundRequestDTO>>> getRefundRequests(
            @RequestParam(name = "status", required = false) RefundStatus status) {
        List<RefundRequestDTO> list = refundService.getAllRefunds(status);
        return ResponseEntity.ok(ApiResponse.success("Refund requests retrieved successfully", list));
    }

    @GetMapping("/refunds/stats")
    public ResponseEntity<ApiResponse<RefundStatsDTO>> getRefundStats() {
        RefundStatsDTO stats = refundService.getRefundStats();
        return ResponseEntity.ok(ApiResponse.success("Refund statistics retrieved successfully", stats));
    }

    @PostMapping("/refunds/{id}/approve")
    public ResponseEntity<ApiResponse<RefundRequestDTO>> approveRefund(
            @PathVariable("id") Long id,
            @Valid @RequestBody(required = false) RefundAdminActionRequest request,
            @AuthenticationPrincipal UserDetails userDetails) {
        RefundRequestDTO response = refundService.approveRefund(id, userDetails.getUsername(), request);
        return ResponseEntity.ok(ApiResponse.success("Refund request approved", response));
    }

    @PostMapping("/refunds/{id}/process")
    public ResponseEntity<ApiResponse<RefundRequestDTO>> processRefund(
            @PathVariable("id") Long id,
            @Valid @RequestBody(required = false) RefundAdminActionRequest request,
            @AuthenticationPrincipal UserDetails userDetails) {
        RefundRequestDTO response = refundService.processRefund(id, userDetails.getUsername(), request);
        return ResponseEntity.ok(ApiResponse.success("Refund processed successfully", response));
    }

    @PostMapping("/refunds/{id}/reject")
    public ResponseEntity<ApiResponse<RefundRequestDTO>> rejectRefund(
            @PathVariable("id") Long id,
            @Valid @RequestBody(required = false) RefundAdminActionRequest request,
            @AuthenticationPrincipal UserDetails userDetails) {
        RefundRequestDTO response = refundService.rejectRefund(id, userDetails.getUsername(), request);
        return ResponseEntity.ok(ApiResponse.success("Refund request rejected", response));
    }
}
