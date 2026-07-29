package com.cartonerp.repository;

import com.cartonerp.entity.DeliveryNote;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import java.util.List;

public interface DeliveryNoteRepository extends JpaRepository<DeliveryNote, Long>, JpaSpecificationExecutor<DeliveryNote> {
    List<DeliveryNote> findByProductionOrderId(Long productionOrderId);
    List<DeliveryNote> findBySalesOrderId(Long salesOrderId);
}
