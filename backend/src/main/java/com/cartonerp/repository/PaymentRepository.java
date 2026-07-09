package com.cartonerp.repository;

import com.cartonerp.entity.Payment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface PaymentRepository extends JpaRepository<Payment, Long>, JpaSpecificationExecutor<Payment> {
    @Query("SELECT COALESCE(SUM(p.amount), 0) FROM Payment p WHERE p.paymentType = :type")
    Double sumByPaymentType(@Param("type") String paymentType);

    @Query("SELECT COALESCE(SUM(p.amount), 0) FROM Payment p WHERE p.paymentType = :type AND p.partyType = :partyType")
    Double sumByTypeAndParty(@Param("type") String paymentType, @Param("partyType") String partyType);
}
