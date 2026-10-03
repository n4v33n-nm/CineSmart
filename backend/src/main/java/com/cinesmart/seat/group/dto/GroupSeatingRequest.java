package com.cinesmart.seat.group.dto;

import com.cinesmart.seat.entity.SeatTier;
import com.fasterxml.jackson.annotation.JsonAlias;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

public class GroupSeatingRequest {

    public static final int MIN_GROUP_SIZE = 1;
    public static final int MAX_GROUP_SIZE = 10;

    private Long showId;

    @NotNull(message = "Group size is required")
    @Min(value = MIN_GROUP_SIZE, message = "Group size must be at least " + MIN_GROUP_SIZE)
    @Max(value = MAX_GROUP_SIZE, message = "Group size cannot exceed maximum limit of " + MAX_GROUP_SIZE)
    @JsonAlias({"groupSize", "numberOfPeople"})
    private Integer partySize;

    private SeatTier preferredTier;

    private String preferredRow;

    private Boolean allowSplitRows = true;

    private Boolean requireAccessibility = false;

    private Integer maxSplits = 2;

    public GroupSeatingRequest() {
    }

    public GroupSeatingRequest(Long showId, Integer partySize) {
        this.showId = showId;
        this.partySize = partySize;
    }

    public GroupSeatingRequest(Long showId, Integer partySize, SeatTier preferredTier, Boolean allowSplitRows) {
        this.showId = showId;
        this.partySize = partySize;
        this.preferredTier = preferredTier;
        this.allowSplitRows = allowSplitRows != null ? allowSplitRows : true;
    }

    public Long getShowId() {
        return showId;
    }

    public void setShowId(Long showId) {
        this.showId = showId;
    }

    public Integer getPartySize() {
        return partySize;
    }

    public void setPartySize(Integer partySize) {
        this.partySize = partySize;
    }

    public Integer getGroupSize() {
        return partySize;
    }

    public void setGroupSize(Integer groupSize) {
        this.partySize = groupSize;
    }

    public SeatTier getPreferredTier() {
        return preferredTier;
    }

    public void setPreferredTier(SeatTier preferredTier) {
        this.preferredTier = preferredTier;
    }

    public String getPreferredRow() {
        return preferredRow;
    }

    public void setPreferredRow(String preferredRow) {
        this.preferredRow = preferredRow;
    }

    public Boolean getAllowSplitRows() {
        return allowSplitRows != null ? allowSplitRows : true;
    }

    public void setAllowSplitRows(Boolean allowSplitRows) {
        this.allowSplitRows = allowSplitRows;
    }

    public Boolean getRequireAccessibility() {
        return requireAccessibility != null ? requireAccessibility : false;
    }

    public void setRequireAccessibility(Boolean requireAccessibility) {
        this.requireAccessibility = requireAccessibility;
    }

    public Integer getMaxSplits() {
        return maxSplits != null ? maxSplits : 2;
    }

    public void setMaxSplits(Integer maxSplits) {
        this.maxSplits = maxSplits;
    }
}
