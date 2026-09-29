package com.vyapaar.service;

import com.vyapaar.entity.Product;
import com.vyapaar.entity.Purchase;
import com.vyapaar.repository.ProductRepository;
import com.vyapaar.repository.PurchaseRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class PurchaseService {

    private final PurchaseRepository purchaseRepository;
    private final ProductRepository productRepository;

    public PurchaseService(PurchaseRepository purchaseRepository, ProductRepository productRepository) {
        this.purchaseRepository = purchaseRepository;
        this.productRepository = productRepository;
    }

    public List<Purchase> getAllPurchases() {
        return purchaseRepository.findAll();
    }

    public Purchase savePurchase(Purchase purchase) {
        purchase.setTotal(purchase.getQuantity() * purchase.getRate());
        Purchase saved = purchaseRepository.save(purchase);

        Product product = productRepository.findById(saved.getProduct().getId())
                .orElseThrow(() -> new RuntimeException("Product not found: " + saved.getProduct().getId()));
        product.setLastRestockedDate(saved.getPurchaseDate());
        product.setStockQuantity((product.getStockQuantity() != null ? product.getStockQuantity() : 0.0) + saved.getQuantity());
        productRepository.save(product);

        return saved;
    }
}
