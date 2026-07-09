package com.cartonerp.repository;

import com.cartonerp.entity.Reconciliation;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface ReconciliationRepository extends JpaRepository<Reconciliation, Long>, JpaSpecificationExecutor<Reconciliation> {
    @Query("SELECT COALESCE(SUM(r.endBalance), 0) FROM Reconciliation r WHERE r.partyType = :type AND r.status = '未结清'")
    Double sumUnpaidByPartyType(@Param("type") String partyType);
}
