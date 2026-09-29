package com.vyapaar.dto;

public interface SupplierAgingDTO {
    String getSupplierName();
    Double getDays0To30();
    Double getDays31To60();
    Double getDays61To90();
    Double getDays90Plus();
    Double getTotalOutstanding();
}
