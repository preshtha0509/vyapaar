package com.vyapaar.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;

/**
 * An audit record of one attempt to send a receipt to a customer.
 * This table is append-only: rows are inserted and never updated or deleted, the same way the
 * ledger keeps its full history.
 */
@Entity
@Table(name = "receipt_sends")
public class ReceiptSend {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "receipt_id", nullable = false)
    private Long receiptId;

    @Column(nullable = false, length = 20)
    private String channel;

    @Column(length = 40)
    private String recipient;

    @Column(nullable = false, length = 20)
    private String status;

    @Column(length = 255)
    private String error;

    @Column(nullable = false)
    private LocalDateTime createdAt;

    public ReceiptSend() {
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

    public String getChannel() {
        return channel;
    }

    public void setChannel(String channel) {
        this.channel = channel;
    }

    public String getRecipient() {
        return recipient;
    }

    public void setRecipient(String recipient) {
        this.recipient = recipient;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getError() {
        return error;
    }

    public void setError(String error) {
        this.error = error;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }
}
