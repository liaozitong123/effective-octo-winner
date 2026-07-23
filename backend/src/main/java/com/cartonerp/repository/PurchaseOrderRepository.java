package com.cartonerp.repository;

import com.cartonerp.entity.PurchaseOrder;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import java.util.List;

public interface PurchaseOrderRepository extends JpaRepository<PurchaseOrder, Long>, JpaSpecificationExecutor<PurchaseOrder> {
    List<PurchaseOrder> findBySalesOrderId(Long salesOrderId);

    @Query("""
        select p from PurchaseOrder p
        where (p.signDate is not null or p.status = '已收货')
          and (p.status is null or p.status <> '已退货')
          and not exists (
              select po.id from ProductionOrder po
              where po.purchaseOrder = p
          )
        """)
    List<PurchaseOrder> findReceivedWithoutProductionOrder();
}
