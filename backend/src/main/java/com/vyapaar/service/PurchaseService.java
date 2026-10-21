package com.vyapaar.service;

import com.vyapaar.entity.Purchase;
import com.vyapaar.repository.ProductRepository;
import com.vyapaar.repository.PurchaseRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

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

    /**
     * Records a purchase, increases stock, and moves the product's restock date forward.
     * Same reasoning as SaleService.saveSale: one transaction, and the arithmetic happens in the
     * database so two simultaneous purchases cannot lose an update.
     */
    @Transactional
    public Purchase savePurchase(Purchase purchase) {
        if (purchase.getProduct() == null || purchase.getProduct().getId() == null) {
            throw new IllegalArgumentException("A purchase needs a product.");
        }

        purchase.setTotal(purchase.getQuantity() * purchase.getRate());
        Purchase saved = purchaseRepository.save(purchase);

        int changed = productRepository.adjustStockAndRestockDate(
                saved.getProduct().getId(), saved.getQuantity(), saved.getPurchaseDate());
        if (changed == 0) {
            throw new IllegalArgumentException("Product not found: " + saved.getProduct().getId());
        }

        return saved;
    }
}
