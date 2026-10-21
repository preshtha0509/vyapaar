package com.vyapaar.controller;

import com.vyapaar.dto.PartyBalanceDTO;
import com.vyapaar.repository.ReportsRepository;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * The dashboard summary.
 *
 * This used to ask LedgerService for one balance per party, and each of those calls pulled every
 * sale and every payment row for that party into memory to add two numbers -- roughly 3 queries
 * and hundreds of objects per customer. It now reads every balance in TWO queries total, computed
 * in SQL with the same formula LedgerService uses.
 *
 * The response shape is unchanged: { totalToReceive, totalToPay, topCustomers, topSuppliers },
 * with each row as { id, name, balance }, so the frontend needs no change.
 */
@RestController
@RequestMapping("/api/dashboard")
public class DashboardController {

    private static final int TOP_N = 5;

    private final ReportsRepository reportsRepository;

    public DashboardController(ReportsRepository reportsRepository) {
        this.reportsRepository = reportsRepository;
    }

    @GetMapping("/summary")
    public Map<String, Object> getSummary() {
        List<PartyBalanceDTO> customers = reportsRepository.getAllCustomerBalances();
        List<PartyBalanceDTO> suppliers = reportsRepository.getAllSupplierBalances();

        // Both queries already sort by balance descending, so the top N is just the first N.
        Map<String, Object> summary = new LinkedHashMap<>();
        summary.put("totalToReceive", total(customers));
        summary.put("totalToPay", total(suppliers));
        summary.put("topCustomers", rows(customers, TOP_N));
        summary.put("topSuppliers", rows(suppliers, TOP_N));
        return summary;
    }

    private double total(List<PartyBalanceDTO> parties) {
        double sum = 0;
        for (PartyBalanceDTO party : parties) {
            sum += party.getBalance() == null ? 0 : party.getBalance();
        }
        return sum;
    }

    private List<Map<String, Object>> rows(List<PartyBalanceDTO> parties, int limit) {
        List<Map<String, Object>> rows = new ArrayList<>();
        for (PartyBalanceDTO party : parties) {
            if (rows.size() == limit) {
                break;
            }
            Map<String, Object> row = new LinkedHashMap<>();
            row.put("id", party.getId());
            row.put("name", party.getName());
            row.put("balance", party.getBalance());
            rows.add(row);
        }
        return rows;
    }
}
