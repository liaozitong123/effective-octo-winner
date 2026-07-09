package com.cartonerp.repository;

import com.cartonerp.entity.ProductionRecord;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import java.util.List;

public interface ProductionRecordRepository extends JpaRepository<ProductionRecord, Long>, JpaSpecificationExecutor<ProductionRecord> {
    List<ProductionRecord> findByProductionOrderId(Long productionOrderId);
}
