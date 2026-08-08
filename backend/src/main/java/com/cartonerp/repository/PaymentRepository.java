package com.cartonerp.repository;

import com.cartonerp.entity.Payment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.util.List;
import java.util.Optional;

public interface PaymentRepository extends JpaRepository<Payment, Long>, JpaSpecificationExecutor<Payment> {
    List<Payment> findByPaymentTypeAndPartyType(String paymentType, String partyType);

    Optional<Payment> findFirstByPaymentTypeAndPartyTypeAndDeliveryNoteNo(String paymentType, String partyType, String deliveryNoteNo);

    Optional<Payment> findFirstByPaymentTypeAndPartyTypeAndPurchaseOrderNo(String paymentType, String partyType, String purchaseOrderNo);

    Optional<Payment> findFirstByPaymentTypeAndPartyTypeAndPaymentNo(String paymentType, String partyType, String paymentNo);

    @Query("SELECT COALESCE(SUM(p.amount), 0) FROM Payment p WHERE p.paymentType = :type")
    Double sumByPaymentType(@Param("type") String paymentType);

    @Query("SELECT COALESCE(SUM(p.amount), 0) FROM Payment p WHERE p.paymentType = :type AND p.partyType = :partyType")
    Double sumByTypeAndParty(@Param("type") String paymentType, @Param("partyType") String partyType);
}
