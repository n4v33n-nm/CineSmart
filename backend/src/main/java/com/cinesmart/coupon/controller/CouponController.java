package com.cinesmart.coupon.controller;

import com.cinesmart.common.response.ApiResponse;
import com.cinesmart.coupon.dto.CouponDTO;
import com.cinesmart.coupon.dto.CouponValidateRequest;
import com.cinesmart.coupon.dto.CouponValidateResponseDTO;
import com.cinesmart.coupon.service.CouponService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/coupons")
public class CouponController {

    private final CouponService couponService;

    public CouponController(CouponService couponService) {
        this.couponService = couponService;
    }

    /**
     * Get all active promotional coupons available to customers.
     */
    @GetMapping("/active")
    public ResponseEntity<ApiResponse<List<CouponDTO>>> getActiveCoupons() {
        List<CouponDTO> coupons = couponService.getActiveCoupons();
        return ResponseEntity.ok(ApiResponse.success("Active coupons retrieved", coupons));
    }

    /**
     * Validate coupon eligibility and calculate instant discount for current user & order.
     */
    @PostMapping("/validate")
    public ResponseEntity<ApiResponse<CouponValidateResponseDTO>> validateCoupon(
            @Valid @RequestBody CouponValidateRequest request,
            @AuthenticationPrincipal UserDetails userDetails) {
        String email = userDetails != null ? userDetails.getUsername() : null;
        CouponValidateResponseDTO result = couponService.validateCoupon(
                request.getCode(),
                request.getBookingAmount(),
                email
        );
        return ResponseEntity.ok(ApiResponse.success(result.getMessage(), result));
    }
}
