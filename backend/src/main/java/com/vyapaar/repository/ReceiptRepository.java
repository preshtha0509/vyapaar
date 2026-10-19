package com.vyapaar.repository;

import com.vyapaar.entity.Receipt;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.Optional;

@Repository
public interface ReceiptRepository extends JpaRepository<Receipt, Long> {

    Optional<Receipt> findByPublicToken(String publicToken);

    Optional<Receipt> findByCustomerIdAndReceiptDate(Long customerId, LocalDate receiptDate);

    long countByReceiptDateBetween(LocalDate from, LocalDate to);
}
