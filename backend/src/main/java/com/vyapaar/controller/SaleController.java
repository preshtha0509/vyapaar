package com.vyapaar.controller;

import com.vyapaar.entity.Sale;
import com.vyapaar.service.SaleService;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/sales")
public class SaleController {

    private final SaleService saleService;

    public SaleController(SaleService saleService) {
        this.saleService = saleService;
    }

    @GetMapping
    public List<Sale> getAllSales() {
        return saleService.getAllSales();
    }

    @PostMapping
    public Sale addSale(@RequestBody Sale sale) {
        return saleService.saveSale(sale);
    }
}
