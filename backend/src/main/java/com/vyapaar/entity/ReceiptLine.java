package com.vyapaar.entity;

import jakarta.persistence.*;

/**
 * One product line on a receipt, copied from a sale row at the moment the receipt was created.
 * The product name and unit are stored as text on purpose: if the product is later renamed or
 * soft-deleted, a receipt that was already given to a customer must not change.
 */
@Entity
@Table(name = "receipt_lines")
public class ReceiptLine {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "receipt_id", nullable = false)
    private Long receiptId;

    @Column(name = "sale_id", nullable = false)
    private Long saleId;

    @Column(nullable = false)
    private String productName;

    @Column(nullable = false, length = 20)
    private String unit;

    @Column(nullable = false)
    private Double quantity;

    @Column(nullable = false)
    private Double rate;

    @Column(nullable = false)
    private Double lineTotal;

    public ReceiptLine() {
    }

    public Long getId() {
        return id;
    }

    public Long getReceiptId() {
        return receiptId;
    }

    public void setReceiptId(Long receiptId) {
        this.receiptId = receiptId;
    }

    public Long getSaleId() {
        return saleId;
    }

    public void setSaleId(Long saleId) {
        this.saleId = saleId;
    }

    public String getProductName() {
        return productName;
    }

    public void setProductName(String productName) {
        this.productName = productName;
    }

    public String getUnit() {
        return unit;
    }

    public void setUnit(String unit) {
        this.unit = unit;
    }

    public Double getQuantity() {
        return quantity;
    }

    public void setQuantity(Double quantity) {
        this.quantity = quantity;
    }

    public Double getRate() {
        return rate;
    }

    public void setRate(Double rate) {
        this.rate = rate;
    }

    public Double getLineTotal() {
        return lineTotal;
    }

    public void setLineTotal(Double lineTotal) {
        this.lineTotal = lineTotal;
    }
}
