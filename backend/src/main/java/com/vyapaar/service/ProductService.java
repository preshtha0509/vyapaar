package com.vyapaar.service;

import com.vyapaar.dto.ProductResponse;
import com.vyapaar.entity.Product;
import com.vyapaar.repository.ProductRepository;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class ProductService {

    private final ProductRepository productRepository;

    public ProductService(ProductRepository productRepository) {
        this.productRepository = productRepository;
    }

    public List<ProductResponse> getAllProducts() {
        return productRepository.findByActiveTrue().stream().map(p -> {
            ProductResponse r = new ProductResponse();
            r.setId(p.getId());
            r.setName(p.getName());
            r.setUnit(p.getUnit().name());
            r.setStockQuantity(p.getStockQuantity());
            if (p.getLastRestockedDate() != null) {
                r.setAgeInDays(ChronoUnit.DAYS.between(p.getLastRestockedDate(), LocalDate.now()));
            }
            return r;
        }).collect(Collectors.toList());
    }

    public Product saveProduct(Product product) {
        if (product.getStockQuantity() == null) {
            product.setStockQuantity(0.0);
        }
        if (product.getActive() == null) {
            product.setActive(true);
        }
        return productRepository.save(product);
    }

    public void deleteProduct(Long id) {
        Product product = productRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Product not found: " + id));
        product.setActive(false);
        productRepository.save(product);
    }
}
