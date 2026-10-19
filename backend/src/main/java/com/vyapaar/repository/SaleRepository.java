package com.vyapaar.repository;

import com.vyapaar.entity.Sale;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

@Repository
public interface SaleRepository extends JpaRepository<Sale, Long> {

    List<Sale> findByCustomerId(Long customerId);

    List<Sale> findBySaleDateBetween(LocalDate from, LocalDate to);

    // Module 15: one receipt covers all of a customer's sales on a single day.
    List<Sale> findByCustomerIdAndSaleDateOrderByIdAsc(Long customerId, LocalDate saleDate);

    long countByProductId(Long productId);
}
