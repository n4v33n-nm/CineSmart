package com.cinesmart.analytics.controller;

import com.cinesmart.analytics.dto.*;
import com.cinesmart.analytics.service.AnalyticsService;
import com.cinesmart.common.response.ApiResponse;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/admin/analytics")
@PreAuthorize("hasRole('ADMIN')")
public class AdminAnalyticsController {

    private final AnalyticsService analyticsService;

    public AdminAnalyticsController(AnalyticsService analyticsService) {
        this.analyticsService = analyticsService;
    }

    @GetMapping("/overview")
    public ResponseEntity<ApiResponse<AdminAnalyticsOverviewDTO>> getOverview(
            @ModelAttribute AnalyticsFilterRequest filter) {
        AdminAnalyticsOverviewDTO overview = analyticsService.getOverview(filter);
        return ResponseEntity.ok(ApiResponse.success("Analytics overview retrieved successfully", overview));
    }

    @GetMapping("/kpi")
    public ResponseEntity<ApiResponse<DashboardKPIDTO>> getKPI(
            @ModelAttribute AnalyticsFilterRequest filter) {
        DashboardKPIDTO kpi = analyticsService.getKPI(filter);
        return ResponseEntity.ok(ApiResponse.success("Analytics KPI metrics retrieved successfully", kpi));
    }

    @GetMapping("/revenue")
    public ResponseEntity<ApiResponse<RevenueAnalyticsDTO>> getRevenueAnalytics(
            @ModelAttribute AnalyticsFilterRequest filter) {
        RevenueAnalyticsDTO revenue = analyticsService.getRevenueAnalytics(filter);
        return ResponseEntity.ok(ApiResponse.success("Revenue analytics retrieved successfully", revenue));
    }

    @GetMapping("/bookings")
    public ResponseEntity<ApiResponse<BookingAnalyticsDTO>> getBookingAnalytics(
            @ModelAttribute AnalyticsFilterRequest filter) {
        BookingAnalyticsDTO bookings = analyticsService.getBookingAnalytics(filter);
        return ResponseEntity.ok(ApiResponse.success("Booking analytics retrieved successfully", bookings));
    }

    @GetMapping("/movies")
    public ResponseEntity<ApiResponse<MovieAnalyticsDTO>> getMovieAnalytics(
            @ModelAttribute AnalyticsFilterRequest filter) {
        MovieAnalyticsDTO movies = analyticsService.getMovieAnalytics(filter);
        return ResponseEntity.ok(ApiResponse.success("Movie analytics retrieved successfully", movies));
    }

    @GetMapping("/theatres")
    public ResponseEntity<ApiResponse<TheatreAnalyticsDTO>> getTheatreAnalytics(
            @ModelAttribute AnalyticsFilterRequest filter) {
        TheatreAnalyticsDTO theatres = analyticsService.getTheatreAnalytics(filter);
        return ResponseEntity.ok(ApiResponse.success("Theatre analytics retrieved successfully", theatres));
    }

    @GetMapping("/marketing-refunds")
    public ResponseEntity<ApiResponse<MarketingRefundAnalyticsDTO>> getMarketingRefundAnalytics(
            @ModelAttribute AnalyticsFilterRequest filter) {
        MarketingRefundAnalyticsDTO marketingRefunds = analyticsService.getMarketingRefundAnalytics(filter);
        return ResponseEntity.ok(ApiResponse.success("Marketing and refund analytics retrieved successfully", marketingRefunds));
    }

    @GetMapping("/filter-options")
    public ResponseEntity<ApiResponse<AnalyticsFilterOptionsDTO>> getFilterOptions() {
        AnalyticsFilterOptionsDTO options = analyticsService.getFilterOptions();
        return ResponseEntity.ok(ApiResponse.success("Analytics filter options retrieved successfully", options));
    }
}
