package com.vyapaar.entity;

import jakarta.persistence.*;
import java.time.LocalDate;

@Entity
@Table(name = "products")
public class Product {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String name;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Unit unit;

    private LocalDate lastRestockedDate;

    @Column(nullable = false)
    private Double stockQuantity = 0.0;

    @Column(nullable = false)
    private Boolean active = true;

    public Double getStockQuantity() {
        return stockQuantity;
    }

    public void setStockQuantity(Double stockQuantity) {
        this.stockQuantity = stockQuantity != null ? stockQuantity : 0.0;
    }

    public Boolean getActive() {
        return active;
    }

    public void setActive(Boolean active) {
        this.active = active != null ? active : true;
    }

    public enum Unit {
        KG, QUINTAL, BAG
    }

    public Product() {
    }

    public Long getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Unit getUnit() {
        return unit;
    }

    public void setUnit(Unit unit) {
        this.unit = unit;
    }

    public LocalDate getLastRestockedDate() {
        return lastRestockedDate;
    }

    public void setLastRestockedDate(LocalDate lastRestockedDate) {
        this.lastRestockedDate = lastRestockedDate;
    }
}
