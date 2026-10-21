package com.vyapaar.repository;

import com.vyapaar.entity.ReceiptSend;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

/**
 * Append-only. Only save(...) is ever called on this repository.
 */
@Repository
public interface ReceiptSendRepository extends JpaRepository<ReceiptSend, Long> {
}
