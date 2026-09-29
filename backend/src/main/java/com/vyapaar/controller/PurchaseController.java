package com.vyapaar.controller;

import com.vyapaar.entity.Purchase;
import com.vyapaar.service.PurchaseService;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/purchases")
public class PurchaseController {

    private final PurchaseService purchaseService;

    public PurchaseController(PurchaseService purchaseService) {
        this.purchaseService = purchaseService;
    }

    @GetMapping
    public List<Purchase> getAllPurchases() {
        return purchaseService.getAllPurchases();
    }

    @PostMapping
    public Purchase addPurchase(@RequestBody Purchase purchase) {
        return purchaseService.savePurchase(purchase);
    }
}
