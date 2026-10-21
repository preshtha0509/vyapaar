package com.vyapaar.dto;

public record ReceiptLineResponse(
        String productName,
        String unit,
        Double quantity,
        Double rate,
        Double lineTotal) {
}
