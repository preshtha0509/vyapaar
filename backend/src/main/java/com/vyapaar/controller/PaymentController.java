package com.vyapaar.controller;

import com.vyapaar.entity.Payment;
import com.vyapaar.service.PaymentService;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/payments")
public class PaymentController {

    private final PaymentService paymentService;

    public PaymentController(PaymentService paymentService) {
        this.paymentService = paymentService;
    }

    @GetMapping
    public List<Payment> getAllPayments() {
        return paymentService.getAllPayments();
    }

    @PostMapping
    public Payment addPayment(@RequestBody Payment payment) {
        return paymentService.savePayment(payment);
    }
}
