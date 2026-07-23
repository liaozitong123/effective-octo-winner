package com.cartonerp.repository;

import com.cartonerp.entity.ProductionOrder;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import java.util.List;

public interface ProductionOrderRepository extends JpaRepository<ProductionOrder, Long>, JpaSpecificationExecutor<ProductionOrder> {
    List<ProductionOrder> findByPurchaseOrderId(Long purchaseOrderId);
    List<ProductionOrder> findBySalesOrderId(Long salesOrderId);
}
