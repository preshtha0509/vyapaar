package com.vyapaar.repository;

import com.vyapaar.entity.ReceiptLine;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ReceiptLineRepository extends JpaRepository<ReceiptLine, Long> {

    List<ReceiptLine> findByReceiptIdOrderByIdAsc(Long receiptId);
}
