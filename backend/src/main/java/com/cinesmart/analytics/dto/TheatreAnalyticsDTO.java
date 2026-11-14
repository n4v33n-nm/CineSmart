package com.cinesmart.analytics.dto;

import java.util.List;

public class TheatreAnalyticsDTO {

    private List<CategoryDataPointDTO> theatreWiseBookings;
    private List<CategoryDataPointDTO> theatreWiseRevenue;
    private List<CategoryDataPointDTO> screenOccupancy;
    private List<CategoryDataPointDTO> showPerformance;

    public TheatreAnalyticsDTO() {
    }

    public TheatreAnalyticsDTO(List<CategoryDataPointDTO> theatreWiseBookings,
                               List<CategoryDataPointDTO> theatreWiseRevenue,
                               List<CategoryDataPointDTO> screenOccupancy,
                               List<CategoryDataPointDTO> showPerformance) {
        this.theatreWiseBookings = theatreWiseBookings;
        this.theatreWiseRevenue = theatreWiseRevenue;
        this.screenOccupancy = screenOccupancy;
        this.showPerformance = showPerformance;
    }

    public List<CategoryDataPointDTO> getTheatreWiseBookings() {
        return theatreWiseBookings;
    }

    public void setTheatreWiseBookings(List<CategoryDataPointDTO> theatreWiseBookings) {
        this.theatreWiseBookings = theatreWiseBookings;
    }

    public List<CategoryDataPointDTO> getTheatreWiseRevenue() {
        return theatreWiseRevenue;
    }

    public void setTheatreWiseRevenue(List<CategoryDataPointDTO> theatreWiseRevenue) {
        this.theatreWiseRevenue = theatreWiseRevenue;
    }

    public List<CategoryDataPointDTO> getScreenOccupancy() {
        return screenOccupancy;
    }

    public void setScreenOccupancy(List<CategoryDataPointDTO> screenOccupancy) {
        this.screenOccupancy = screenOccupancy;
    }

    public List<CategoryDataPointDTO> getShowPerformance() {
        return showPerformance;
    }

    public void setShowPerformance(List<CategoryDataPointDTO> showPerformance) {
        this.showPerformance = showPerformance;
    }
}
