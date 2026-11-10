package com.cinesmart.loyalty.controller;

import com.cinesmart.common.response.ApiResponse;
import com.cinesmart.loyalty.dto.LoyaltyAccountDTO;
import com.cinesmart.loyalty.dto.LoyaltyRedeemRequest;
import com.cinesmart.loyalty.dto.LoyaltyRedeemResponseDTO;
import com.cinesmart.loyalty.dto.LoyaltyTransactionDTO;
import com.cinesmart.loyalty.service.LoyaltyService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/loyalty")
public class LoyaltyController {

    private final LoyaltyService loyaltyService;

    public LoyaltyController(LoyaltyService loyaltyService) {
        this.loyaltyService = loyaltyService;
    }

    /**
     * Get current authenticated user's loyalty account balance, rupee value and rate specs.
     */
    @GetMapping("/balance")
    public ResponseEntity<ApiResponse<LoyaltyAccountDTO>> getBalance(
            @AuthenticationPrincipal UserDetails userDetails) {
        LoyaltyAccountDTO account = loyaltyService.getAccountForUser(userDetails.getUsername());
        return ResponseEntity.ok(ApiResponse.success("Loyalty balance retrieved", account));
    }

    /**
     * Get transaction history (earned, redeemed, reversed) for the authenticated user.
     */
    @GetMapping("/transactions")
    public ResponseEntity<ApiResponse<List<LoyaltyTransactionDTO>>> getTransactions(
            @AuthenticationPrincipal UserDetails userDetails) {
        List<LoyaltyTransactionDTO> list = loyaltyService.getTransactionsForUser(userDetails.getUsername());
        return ResponseEntity.ok(ApiResponse.success("Loyalty transactions retrieved", list));
    }

    /**
     * Pre-calculate loyalty redemption discount for checkout order amount.
     */
    @PostMapping("/calculate-redemption")
    public ResponseEntity<ApiResponse<LoyaltyRedeemResponseDTO>> calculateRedemption(
            @Valid @RequestBody LoyaltyRedeemRequest request,
            @AuthenticationPrincipal UserDetails userDetails) {
        LoyaltyRedeemResponseDTO result = loyaltyService.calculateRedemption(
                userDetails.getUsername(),
                request.getPointsToRedeem(),
                request.getCurrentAmount()
        );
        return ResponseEntity.ok(ApiResponse.success(result.getMessage(), result));
    }
}

