package com.vyapaar.service;

import com.vyapaar.dto.BillResponse;
import com.vyapaar.entity.Sale;
import com.vyapaar.repository.SaleRepository;
import org.springframework.stereotype.Service;

import java.time.format.DateTimeFormatter;

@Service
public class BillService {

    private final SaleRepository saleRepository;

    public BillService(SaleRepository saleRepository) {
        this.saleRepository = saleRepository;
    }

    public BillResponse generateBill(Long saleId, String lang) {
        Sale sale = saleRepository.findById(saleId)
                .orElseThrow(() -> new RuntimeException("Sale not found: " + saleId));

        DateTimeFormatter dateFormat = DateTimeFormatter.ofPattern("dd MMM yyyy");
        boolean hindi = "hi".equalsIgnoreCase(lang);

        BillResponse bill = new BillResponse();
        bill.setCustomerName(sale.getCustomer().getName());
        bill.setCustomerPhone(sale.getCustomer().getPhone());
        bill.setProductName(sale.getProduct().getName());
        bill.setQuantity(sale.getQuantity());
        bill.setUnit(sale.getProduct().getUnit().name());
        bill.setRate(sale.getRate());
        bill.setTotal(sale.getTotal());
        bill.setSaleDate(sale.getSaleDate().format(dateFormat));
        bill.setPaymentStatus(sale.getPaymentStatus().name());
        bill.setBillText(hindi ? buildBillTextHindi(sale, dateFormat) : buildBillTextEnglish(sale, dateFormat));
        return bill;
    }

    private String buildBillTextEnglish(Sale sale, DateTimeFormatter dateFormat) {
        return "VYAPAAR - Bill\n" +
                "Date: " + sale.getSaleDate().format(dateFormat) + "\n" +
                "Customer: " + sale.getCustomer().getName() + "\n" +
                sale.getProduct().getName() + " - " + sale.getQuantity() + " " +
                unitLabel(sale.getProduct().getUnit().name(), false) +
                " @ ₹" + sale.getRate() + "\n" +
                "Total: ₹" + sale.getTotal() + "\n" +
                "Status: " + statusLabel(sale.getPaymentStatus().name(), false) + "\n" +
                "Thank you for your business.";
    }

    private String buildBillTextHindi(Sale sale, DateTimeFormatter dateFormat) {
        return "व्यापार - बिल\n" +
                "दिनांक: " + sale.getSaleDate().format(dateFormat) + "\n" +
                "ग्राहक: " + sale.getCustomer().getName() + "\n" +
                sale.getProduct().getName() + " - " + sale.getQuantity() + " " +
                unitLabel(sale.getProduct().getUnit().name(), true) +
                " @ ₹" + sale.getRate() + "\n" +
                "कुल: ₹" + sale.getTotal() + "\n" +
                "स्थिति: " + statusLabel(sale.getPaymentStatus().name(), true) + "\n" +
                "आपके व्यवसाय के लिए धन्यवाद।";
    }

    private String unitLabel(String unit, boolean hindi) {
        if (!hindi) {
            return unit;
        }
        switch (unit) {
            case "KG":
                return "किलो";
            case "QUINTAL":
                return "क्विंटल";
            case "BAG":
                return "बोरी";
            default:
                return unit;
        }
    }

    private String statusLabel(String status, boolean hindi) {
        if (!hindi) {
            return status;
        }
        switch (status) {
            case "CASH":
                return "नकद";
            case "UDHARI":
                return "उधारी";
            default:
                return status;
        }
    }
}
