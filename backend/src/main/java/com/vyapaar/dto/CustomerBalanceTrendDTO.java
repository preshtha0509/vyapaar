package com.vyapaar.dto;

import java.time.LocalDate;

public interface CustomerBalanceTrendDTO {
    LocalDate getDate();
    Double getTransactionAmount();
    Double getRunningBalance();
}
