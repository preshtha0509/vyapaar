package com.vyapaar.entity;

import jakarta.persistence.*;
import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * One receipt handed to one customer for one day's sales.
 *
 * Sales are stored one row per product, so a customer who buys three products on one day produces
 * three sales rows but only ONE receipt, with three lines.
 *
 * Design note: the lines of a receipt are a snapshot taken when the receipt is created, and
 * `subtotal` is the total of that snapshot. This does NOT break the project rule against storing
 * computed values. A balance is a live fact about a relationship and must always be recomputed from
 * history by LedgerService. A receipt is a document that was given to a customer at a point in
 * time, and it has to keep saying exactly what it said when it was sent, even if the underlying
 * sale is corrected later.
 */
@Entity
@Table(
        name = "receipts",
        uniqueConstraints = @UniqueConstraint(
                name = "uk_receipts_customer_date",
                columnNames = {"customer_id", "receipt_date"}))
public class Receipt {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "customer_id", nullable = false)
    private Long customerId;

    @Column(name = "receipt_date", nullable = false)
    private LocalDate receiptDate;

    @Column(nullable = false, unique = true, length = 40)
    private String receiptNumber;

    @Column(nullable = false, unique = true, length = 64)
    private String publicToken;

    @Column(nullable = false)
    private Double subtotal;

    @Column(nullable = false)
    private LocalDateTime createdAt;

    public Receipt() {
    }

    public Long getId() {
        return id;
    }

    public Long getCustomerId() {
        return customerId;
    }

    public void setCustomerId(Long customerId) {
        this.customerId = customerId;
    }

    public LocalDate getReceiptDate() {
        return receiptDate;
    }

    public void setReceiptDate(LocalDate receiptDate) {
        this.receiptDate = receiptDate;
    }

    public String getReceiptNumber() {
        return receiptNumber;
    }

    public void setReceiptNumber(String receiptNumber) {
        this.receiptNumber = receiptNumber;
    }

    public String getPublicToken() {
        return publicToken;
    }

    public void setPublicToken(String publicToken) {
        this.publicToken = publicToken;
    }

    public Double getSubtotal() {
        return subtotal;
    }

    public void setSubtotal(Double subtotal) {
        this.subtotal = subtotal;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }
}
