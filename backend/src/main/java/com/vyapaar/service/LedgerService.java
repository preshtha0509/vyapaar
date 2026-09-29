package com.vyapaar.service;

import com.vyapaar.entity.Payment;
import com.vyapaar.repository.CustomerRepository;
import com.vyapaar.repository.PaymentRepository;
import com.vyapaar.repository.PurchaseRepository;
import com.vyapaar.repository.SaleRepository;
import com.vyapaar.repository.SupplierRepository;
import org.springframework.stereotype.Service;

@Service
public class LedgerService {

    private final SaleRepository saleRepository;
    private final PurchaseRepository purchaseRepository;
    private final PaymentRepository paymentRepository;
    private final CustomerRepository customerRepository;
    private final SupplierRepository supplierRepository;

    public LedgerService(SaleRepository saleRepository,
                         PurchaseRepository purchaseRepository,
                         PaymentRepository paymentRepository,
                         CustomerRepository customerRepository,
                         SupplierRepository supplierRepository) {
        this.saleRepository = saleRepository;
        this.purchaseRepository = purchaseRepository;
        this.paymentRepository = paymentRepository;
        this.customerRepository = customerRepository;
        this.supplierRepository = supplierRepository;
    }

    public Double getCustomerBalance(Long customerId) {
        double openingBalance = customerRepository.findById(customerId)
                .map(c -> c.getOpeningBalance())
                .orElse(0.0);

        double totalSales = saleRepository.findByCustomerId(customerId).stream()
                .mapToDouble(s -> s.getTotal())
                .sum();

        double totalReceived = paymentRepository
                .findByPartyTypeAndPartyId(Payment.PartyType.CUSTOMER, customerId).stream()
                .mapToDouble(p -> p.getAmount())
                .sum();

        return openingBalance + totalSales - totalReceived;
    }

    public Double getSupplierBalance(Long supplierId) {
        double openingBalance = supplierRepository.findById(supplierId)
                .map(s -> s.getOpeningBalance())
                .orElse(0.0);

        double totalPurchases = purchaseRepository.findBySupplierId(supplierId).stream()
                .mapToDouble(p -> p.getTotal())
                .sum();

        double totalPaid = paymentRepository
                .findByPartyTypeAndPartyId(Payment.PartyType.SUPPLIER, supplierId).stream()
                .mapToDouble(p -> p.getAmount())
                .sum();

        return openingBalance + totalPurchases - totalPaid;
    }
}
