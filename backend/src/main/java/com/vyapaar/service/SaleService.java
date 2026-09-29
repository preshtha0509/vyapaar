package com.vyapaar.service;

import com.vyapaar.entity.Product;
import com.vyapaar.entity.Sale;
import com.vyapaar.repository.ProductRepository;
import com.vyapaar.repository.SaleRepository;
import org.springframework.stereotype.Service;

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

    public Sale saveSale(Sale sale) {
        sale.setTotal(sale.getQuantity() * sale.getRate());
        Sale saved = saleRepository.save(sale);

        if (saved.getProduct() != null && saved.getProduct().getId() != null) {
            Product product = productRepository.findById(saved.getProduct().getId())
                    .orElseThrow(() -> new RuntimeException("Product not found: " + saved.getProduct().getId()));
            product.setStockQuantity((product.getStockQuantity() != null ? product.getStockQuantity() : 0.0) - saved.getQuantity());
            productRepository.save(product);
        }

        return saved;
    }
}
