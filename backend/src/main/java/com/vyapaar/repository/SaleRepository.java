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

    long countByProductId(Long productId);
}
