package com.cartonerp.service;

import com.cartonerp.entity.ProductionOrder;
import com.cartonerp.entity.ProductionRecord;
import com.cartonerp.entity.PurchaseOrder;
import com.cartonerp.repository.ProductionOrderRepository;
import com.cartonerp.repository.ProductionRecordRepository;
import com.cartonerp.repository.PurchaseOrderRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class ProductionRecordService {
    @Autowired private ProductionOrderRepository productionOrderRepo;
    @Autowired private ProductionRecordRepository productionRecordRepo;
    @Autowired private PurchaseOrderRepository purchaseOrderRepo;
    @Autowired private ProductionOrderService productionOrderService;

    @Transactional
    public int ensureForReceivedPurchase(PurchaseOrder purchaseOrder) {
        if (purchaseOrder == null || purchaseOrder.getId() == null) return 0;
        if (!"已收货".equals(purchaseOrder.getStatus())) return 0;

        int created = 0;
        List<ProductionOrder> productionOrders = productionOrderRepo.findByPurchaseOrderId(purchaseOrder.getId());
        for (ProductionOrder productionOrder : productionOrders) {
            if (!productionRecordRepo.findByProductionOrderId(productionOrder.getId()).isEmpty()) continue;
            ProductionRecord record = new ProductionRecord();
            record.setProductionOrder(productionOrder);
            record.setOutputQty(0);
            record.setWasteQty(0);
            record.setDeliveryQty(0);
            record.setRemainingStock(0);
            productionRecordRepo.save(record);
            created++;
        }
        return created;
    }

    @Transactional
    public int syncReceivedPurchasesWithoutRecord() {
        int created = 0;
        for (PurchaseOrder purchaseOrder : purchaseOrderRepo.findAll()) {
            if (!"已收货".equals(purchaseOrder.getStatus())) continue;
            productionOrderService.createOrUpdateFromSignedPurchase(purchaseOrder);
            created += ensureForReceivedPurchase(purchaseOrder);
        }
        return created;
    }
}
