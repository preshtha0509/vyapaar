package com.vyapaar.controller;

import com.vyapaar.service.LedgerService;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/ledger")
public class LedgerController {

    private final LedgerService ledgerService;

    public LedgerController(LedgerService ledgerService) {
        this.ledgerService = ledgerService;
    }

    @GetMapping("/customer/{id}/balance")
    public Double getCustomerBalance(@PathVariable Long id) {
        return ledgerService.getCustomerBalance(id);
    }

    @GetMapping("/supplier/{id}/balance")
    public Double getSupplierBalance(@PathVariable Long id) {
        return ledgerService.getSupplierBalance(id);
    }
}
