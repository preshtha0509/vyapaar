package com.vyapaar.service;

import com.vyapaar.dto.CustomerBalanceRankDTO;
import com.vyapaar.dto.CustomerBalanceTrendDTO;
import com.vyapaar.dto.SalesTrendDTO;
import com.vyapaar.dto.SupplierAgingDTO;
import com.vyapaar.entity.Purchase;
import com.vyapaar.entity.Sale;
import com.vyapaar.repository.PurchaseRepository;
import com.vyapaar.repository.ReportsRepository;
import com.vyapaar.repository.SaleRepository;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.YearMonth;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class ReportsService {

    private final SaleRepository saleRepository;
    private final PurchaseRepository purchaseRepository;
    private final ReportsRepository reportsRepository;

    public ReportsService(SaleRepository saleRepository,
                          PurchaseRepository purchaseRepository,
                          ReportsRepository reportsRepository) {
        this.saleRepository = saleRepository;
        this.purchaseRepository = purchaseRepository;
        this.reportsRepository = reportsRepository;
    }

    public List<CustomerBalanceTrendDTO> getCustomerBalanceTrend(Long customerId) {
        return reportsRepository.getCustomerBalanceTrend(customerId);
    }

    public List<SalesTrendDTO> getSalesTrend() {
        return reportsRepository.getSalesTrend();
    }

    public List<CustomerBalanceRankDTO> getTopCustomersByBalance(int limit) {
        return reportsRepository.getTopCustomersByBalance(limit);
    }

    public List<SupplierAgingDTO> getSupplierPaymentAging() {
        return reportsRepository.getSupplierPaymentAging();
    }

    public Map<String, Object> getSummary(LocalDate from, LocalDate to) {
        List<Sale> sales = saleRepository.findBySaleDateBetween(from, to);
        List<Purchase> purchases = purchaseRepository.findByPurchaseDateBetween(from, to);

        double totalSales = sales.stream().mapToDouble(Sale::getTotal).sum();
        double totalPurchases = purchases.stream().mapToDouble(Purchase::getTotal).sum();

        Map<String, Object> summary = new HashMap<>();
        summary.put("totalSales", totalSales);
        summary.put("totalPurchases", totalPurchases);
        summary.put("grossDifference", totalSales - totalPurchases);
        return summary;
    }

    public List<Map<String, Object>> getMonthlyTrend(int monthsBack) {
        YearMonth currentMonth = YearMonth.now();
        LocalDate start = currentMonth.minusMonths(monthsBack - 1).atDay(1);

        List<Sale> sales = saleRepository.findBySaleDateBetween(start, LocalDate.now());
        List<Purchase> purchases = purchaseRepository.findByPurchaseDateBetween(start, LocalDate.now());

        Map<YearMonth, Double> salesByMonth = sales.stream().collect(Collectors.groupingBy(
                s -> YearMonth.from(s.getSaleDate()),
                Collectors.summingDouble(Sale::getTotal)));

        Map<YearMonth, Double> purchasesByMonth = purchases.stream().collect(Collectors.groupingBy(
                p -> YearMonth.from(p.getPurchaseDate()),
                Collectors.summingDouble(Purchase::getTotal)));

        List<Map<String, Object>> trend = new ArrayList<>();
        DateTimeFormatter labelFormat = DateTimeFormatter.ofPattern("MMM yyyy");

        for (int i = monthsBack - 1; i >= 0; i--) {
            YearMonth month = currentMonth.minusMonths(i);
            Map<String, Object> row = new LinkedHashMap<>();
            row.put("month", month.format(labelFormat));
            row.put("sales", salesByMonth.getOrDefault(month, 0.0));
            row.put("purchases", purchasesByMonth.getOrDefault(month, 0.0));
            trend.add(row);
        }
        return trend;
    }

    public List<Map<String, Object>> getSupplierVolume(LocalDate from, LocalDate to) {
        List<Purchase> purchases = purchaseRepository.findByPurchaseDateBetween(from, to);

        Map<String, Double> bySupplier = purchases.stream().collect(Collectors.groupingBy(
                p -> p.getSupplier().getName(),
                Collectors.summingDouble(Purchase::getTotal)));

        return bySupplier.entrySet().stream()
                .sorted((a, b) -> Double.compare(b.getValue(), a.getValue()))
                .map(e -> {
                    Map<String, Object> row = new LinkedHashMap<>();
                    row.put("supplier", e.getKey());
                    row.put("total", e.getValue());
                    return row;
                })
                .collect(Collectors.toList());
    }
}
