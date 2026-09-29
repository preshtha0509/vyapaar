package com.vyapaar.controller;

import com.vyapaar.dto.CustomerBalanceRankDTO;
import com.vyapaar.dto.CustomerBalanceTrendDTO;
import com.vyapaar.dto.SalesTrendDTO;
import com.vyapaar.dto.SupplierAgingDTO;
import com.vyapaar.service.ReportsService;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/reports")
public class ReportsController {

    private final ReportsService reportsService;

    public ReportsController(ReportsService reportsService) {
        this.reportsService = reportsService;
    }

    @GetMapping("/customer-balance-trend/{customerId}")
    public List<CustomerBalanceTrendDTO> getCustomerBalanceTrend(@PathVariable Long customerId) {
        return reportsService.getCustomerBalanceTrend(customerId);
    }

    @GetMapping("/sales-trend")
    public List<SalesTrendDTO> getSalesTrend() {
        return reportsService.getSalesTrend();
    }

    @GetMapping("/top-customers-by-balance")
    public List<CustomerBalanceRankDTO> getTopCustomersByBalance(
            @RequestParam(defaultValue = "5") int limit) {
        return reportsService.getTopCustomersByBalance(limit);
    }

    @GetMapping("/supplier-payment-aging")
    public List<SupplierAgingDTO> getSupplierPaymentAging() {
        return reportsService.getSupplierPaymentAging();
    }

    @GetMapping("/summary")
    public Map<String, Object> getSummary(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate from,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate to) {
        return reportsService.getSummary(from, to);
    }

    @GetMapping("/monthly-trend")
    public List<Map<String, Object>> getMonthlyTrend(
            @RequestParam(defaultValue = "6") int months) {
        return reportsService.getMonthlyTrend(months);
    }

    @GetMapping("/supplier-volume")
    public List<Map<String, Object>> getSupplierVolume(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate from,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate to) {
        return reportsService.getSupplierVolume(from, to);
    }
}
