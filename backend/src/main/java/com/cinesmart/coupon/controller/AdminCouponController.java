package com.cinesmart.coupon.controller;

import com.cinesmart.common.response.ApiResponse;
import com.cinesmart.coupon.dto.CouponCreateRequest;
import com.cinesmart.coupon.dto.CouponDTO;
import com.cinesmart.coupon.service.CouponService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/admin/coupons")
@PreAuthorize("hasRole('ADMIN')")
public class AdminCouponController {

    private final CouponService couponService;

    public AdminCouponController(CouponService couponService) {
        this.couponService = couponService;
    }

    @GetMapping
    public ResponseEntity<ApiResponse<List<CouponDTO>>> getAllCoupons() {
        List<CouponDTO> list = couponService.getAllCoupons();
        return ResponseEntity.ok(ApiResponse.success("All coupons retrieved", list));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<CouponDTO>> getCouponById(@PathVariable Long id) {
        CouponDTO coupon = couponService.getCouponById(id);
        return ResponseEntity.ok(ApiResponse.success(coupon));
    }

    @PostMapping
    public ResponseEntity<ApiResponse<CouponDTO>> createCoupon(@Valid @RequestBody CouponCreateRequest request) {
        CouponDTO created = couponService.createCoupon(request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Coupon created successfully", created));
    }

    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<CouponDTO>> updateCoupon(
            @PathVariable Long id,
            @Valid @RequestBody CouponCreateRequest request) {
        CouponDTO updated = couponService.updateCoupon(id, request);
        return ResponseEntity.ok(ApiResponse.success("Coupon updated successfully", updated));
    }

    @PatchMapping("/{id}/toggle")
    public ResponseEntity<ApiResponse<CouponDTO>> toggleCouponStatus(@PathVariable Long id) {
        CouponDTO toggled = couponService.toggleCouponStatus(id);
        return ResponseEntity.ok(ApiResponse.success("Coupon status toggled", toggled));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> deleteCoupon(@PathVariable Long id) {
        couponService.deleteCoupon(id);
        return ResponseEntity.ok(ApiResponse.success("Coupon deactivated successfully", null));
    }
}
