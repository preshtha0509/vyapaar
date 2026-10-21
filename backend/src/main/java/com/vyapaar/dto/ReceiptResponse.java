package com.vyapaar.dto;

import java.util.List;

/**
 * whatsAppLink is null when the customer has no usable mobile number on record.
 */
public record ReceiptResponse(
        Long id,
        String receiptNumber,
        String receiptDate,
        String customerName,
        String publicToken,
        String receiptUrl,
        String pdfUrl,
        Double subtotal,
        String whatsAppLink,
        List<ReceiptLineResponse> lines) {
}
