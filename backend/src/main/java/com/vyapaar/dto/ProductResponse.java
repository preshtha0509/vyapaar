package com.vyapaar.dto;

public class ProductResponse {

    private Long id;
    private String name;
    private String unit;
    private Long ageInDays;
    private Double stockQuantity;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getUnit() {
        return unit;
    }

    public void setUnit(String unit) {
        this.unit = unit;
    }

    public Long getAgeInDays() {
        return ageInDays;
    }

    public void setAgeInDays(Long ageInDays) {
        this.ageInDays = ageInDays;
    }

    public Double getStockQuantity() {
        return stockQuantity;
    }

    public void setStockQuantity(Double stockQuantity) {
        this.stockQuantity = stockQuantity;
    }
}
