package com.cinesmart.analytics.dto;

import java.math.BigDecimal;

public class CategoryDataPointDTO {

    private String name;
    private long count;
    private BigDecimal value;
    private double percentage;
    private String extra;

    public CategoryDataPointDTO() {
    }

    public CategoryDataPointDTO(String name, long count, BigDecimal value, double percentage, String extra) {
        this.name = name;
        this.count = count;
        this.value = value != null ? value : BigDecimal.ZERO;
        this.percentage = percentage;
        this.extra = extra;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public long getCount() {
        return count;
    }

    public void setCount(long count) {
        this.count = count;
    }

    public BigDecimal getValue() {
        return value != null ? value : BigDecimal.ZERO;
    }

    public void setValue(BigDecimal value) {
        this.value = value;
    }

    public double getPercentage() {
        return percentage;
    }

    public void setPercentage(double percentage) {
        this.percentage = percentage;
    }

    public String getExtra() {
        return extra;
    }

    public void setExtra(String extra) {
        this.extra = extra;
    }
}
