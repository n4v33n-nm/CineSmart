package com.cinesmart.analytics.dto;

public class AdminAnalyticsOverviewDTO {

    private DashboardKPIDTO kpi;
    private RevenueAnalyticsDTO revenue;
    private BookingAnalyticsDTO bookings;
    private MovieAnalyticsDTO movies;
    private TheatreAnalyticsDTO theatres;
    private MarketingRefundAnalyticsDTO marketingRefunds;
    private AnalyticsFilterOptionsDTO filterOptions;

    public AdminAnalyticsOverviewDTO() {
    }

    public AdminAnalyticsOverviewDTO(DashboardKPIDTO kpi,
                                     RevenueAnalyticsDTO revenue,
                                     BookingAnalyticsDTO bookings,
                                     MovieAnalyticsDTO movies,
                                     TheatreAnalyticsDTO theatres,
                                     MarketingRefundAnalyticsDTO marketingRefunds,
                                     AnalyticsFilterOptionsDTO filterOptions) {
        this.kpi = kpi;
        this.revenue = revenue;
        this.bookings = bookings;
        this.movies = movies;
        this.theatres = theatres;
        this.marketingRefunds = marketingRefunds;
        this.filterOptions = filterOptions;
    }

    public DashboardKPIDTO getKpi() {
        return kpi;
    }

    public void setKpi(DashboardKPIDTO kpi) {
        this.kpi = kpi;
    }

    public RevenueAnalyticsDTO getRevenue() {
        return revenue;
    }

    public void setRevenue(RevenueAnalyticsDTO revenue) {
        this.revenue = revenue;
    }

    public BookingAnalyticsDTO getBookings() {
        return bookings;
    }

    public void setBookings(BookingAnalyticsDTO bookings) {
        this.bookings = bookings;
    }

    public MovieAnalyticsDTO getMovies() {
        return movies;
    }

    public void setMovies(MovieAnalyticsDTO movies) {
        this.movies = movies;
    }

    public TheatreAnalyticsDTO getTheatres() {
        return theatres;
    }

    public void setTheatres(TheatreAnalyticsDTO theatres) {
        this.theatres = theatres;
    }

    public MarketingRefundAnalyticsDTO getMarketingRefunds() {
        return marketingRefunds;
    }

    public void setMarketingRefunds(MarketingRefundAnalyticsDTO marketingRefunds) {
        this.marketingRefunds = marketingRefunds;
    }

    public AnalyticsFilterOptionsDTO getFilterOptions() {
        return filterOptions;
    }

    public void setFilterOptions(AnalyticsFilterOptionsDTO filterOptions) {
        this.filterOptions = filterOptions;
    }
}
