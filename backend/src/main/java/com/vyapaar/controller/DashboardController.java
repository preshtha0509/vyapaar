package com.vyapaar.controller;

import com.vyapaar.entity.Customer;
import com.vyapaar.entity.Supplier;
import com.vyapaar.repository.CustomerRepository;
import com.vyapaar.repository.SupplierRepository;
import com.vyapaar.service.LedgerService;
import org.springframework.web.bind.annotation.*;

import java.util.*;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/dashboard")
public class DashboardController {

    private final CustomerRepository customerRepository;
    private final SupplierRepository supplierRepository;
    private final LedgerService ledgerService;

    public DashboardController(CustomerRepository customerRepository,
                               SupplierRepository supplierRepository,
                               LedgerService ledgerService) {
        this.customerRepository = customerRepository;
        this.supplierRepository = supplierRepository;
        this.ledgerService = ledgerService;
    }

    @GetMapping("/summary")
    public Map<String, Object> getSummary() {
        List<Customer> customers = customerRepository.findAll();
        List<Supplier> suppliers = supplierRepository.findAll();

        List<Map<String, Object>> customerBalances = new ArrayList<>();
        double totalToReceive = 0;
        for (Customer c : customers) {
            double bal = ledgerService.getCustomerBalance(c.getId());
            Map<String, Object> customerBalance = new HashMap<>();
            customerBalance.put("id", c.getId());
            customerBalance.put("name", c.getName());
            customerBalance.put("balance", bal);
            customerBalances.add(customerBalance);
            totalToReceive += bal;
        }

        List<Map<String, Object>> supplierBalances = new ArrayList<>();
        double totalToPay = 0;
        for (Supplier s : suppliers) {
            double bal = ledgerService.getSupplierBalance(s.getId());
            Map<String, Object> supplierBalance = new HashMap<>();
            supplierBalance.put("id", s.getId());
            supplierBalance.put("name", s.getName());
            supplierBalance.put("balance", bal);
            supplierBalances.add(supplierBalance);
            totalToPay += bal;
        }

        List<Map<String, Object>> topCustomers = customerBalances.stream()
                .sorted((a, b) -> Double.compare((Double) b.get("balance"), (Double) a.get("balance")))
                .limit(5)
                .collect(Collectors.toList());

        List<Map<String, Object>> topSuppliers = supplierBalances.stream()
                .sorted((a, b) -> Double.compare((Double) b.get("balance"), (Double) a.get("balance")))
                .limit(5)
                .collect(Collectors.toList());

        Map<String, Object> summary = new HashMap<>();
        summary.put("totalToReceive", totalToReceive);
        summary.put("totalToPay", totalToPay);
        summary.put("topCustomers", topCustomers);
        summary.put("topSuppliers", topSuppliers);
        return summary;
    }
}
