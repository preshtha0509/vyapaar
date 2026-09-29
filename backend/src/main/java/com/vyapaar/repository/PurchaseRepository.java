package com.vyapaar.repository;

import com.vyapaar.entity.Purchase;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

@Repository
public interface PurchaseRepository extends JpaRepository<Purchase, Long> {

    List<Purchase> findBySupplierId(Long supplierId);

    List<Purchase> findByPurchaseDateBetween(LocalDate from, LocalDate to);

    long countByProductId(Long productId);
}
