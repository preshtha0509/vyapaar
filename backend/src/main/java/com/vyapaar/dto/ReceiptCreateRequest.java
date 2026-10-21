package com.vyapaar.dto;

import java.time.LocalDate;

public record ReceiptCreateRequest(Long customerId, LocalDate date) {
}
