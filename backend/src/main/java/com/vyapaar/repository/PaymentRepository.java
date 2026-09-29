package com.vyapaar.repository;

import com.vyapaar.entity.Payment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface PaymentRepository extends JpaRepository<Payment, Long> {

    List<Payment> findByPartyTypeAndPartyId(Payment.PartyType partyType, Long partyId);
}
