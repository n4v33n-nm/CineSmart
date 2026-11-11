package com.cinesmart.refund.controller;

import com.cinesmart.common.response.ApiResponse;
import com.cinesmart.refund.dto.CancellationPolicyDTO;
import com.cinesmart.refund.service.CancellationPolicyService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/cancellation-policies")
public class CancellationPolicyController {

    private final CancellationPolicyService cancellationPolicyService;

    public CancellationPolicyController(CancellationPolicyService cancellationPolicyService) {
        this.cancellationPolicyService = cancellationPolicyService;
    }

    /**
     * Public endpoint to view active cinema cancellation rules and refund tiers.
     */
    @GetMapping
    public ResponseEntity<ApiResponse<List<CancellationPolicyDTO>>> getActivePolicies() {
        List<CancellationPolicyDTO> policies = cancellationPolicyService.getActivePolicies();
        return ResponseEntity.ok(ApiResponse.success("Active cancellation policies retrieved", policies));
    }
}

