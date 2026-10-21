package com.vyapaar.service;

import com.vyapaar.entity.Sale;
import com.vyapaar.repository.ProductRepository;
import com.vyapaar.repository.SaleRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class SaleService {

    private final SaleRepository saleRepository;
    private final ProductRepository productRepository;

    public SaleService(SaleRepository saleRepository, ProductRepository productRepository) {
        this.saleRepository = saleRepository;
        this.productRepository = productRepository;
    }

    public List<Sale> getAllSales() {
        return saleRepository.findAll();
    }

    /**
     * Records a sale and reduces stock by the same quantity.
     *
     * @Transactional makes the two writes one unit: if the stock adjustment fails, the sale is
     * rolled back too. Without it they committed separately, so a failure in between left a sale
     * on the books with the stock never reduced.
     *
     * The total is calculated here and never taken from the request, so a tampered payload cannot
     * set its own price.
     */
    @Transactional
    public Sale saveSale(Sale sale) {
        if (sale.getProduct() == null || sale.getProduct().getId() == null) {
            throw new IllegalArgumentException("A sale needs a product.");
        }

        sale.setTotal(sale.getQuantity() * sale.getRate());
        Sale saved = saleRepository.save(sale);

        // Negative delta: a sale takes stock out. Stock may go negative, which the app allows.
        int changed = productRepository.adjustStock(saved.getProduct().getId(), -saved.getQuantity());
        if (changed == 0) {
            throw new IllegalArgumentException("Product not found: " + saved.getProduct().getId());
        }

        return saved;
    }
}
