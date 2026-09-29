package com.vyapaar.controller;

import com.vyapaar.dto.BillResponse;
import com.vyapaar.service.BillService;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/sales")
public class BillController {

    private final BillService billService;

    public BillController(BillService billService) {
        this.billService = billService;
    }

    @GetMapping("/{id}/bill")
    public BillResponse getBill(@PathVariable Long id, @RequestParam(defaultValue = "en") String lang) {
        return billService.generateBill(id, lang);
    }
}
