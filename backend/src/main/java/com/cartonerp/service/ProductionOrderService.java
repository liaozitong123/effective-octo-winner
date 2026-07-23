package com.cartonerp.service;

import com.cartonerp.entity.Customer;
import com.cartonerp.entity.ProductionOrder;
import com.cartonerp.entity.PurchaseOrder;
import com.cartonerp.entity.SalesOrder;
import com.cartonerp.repository.ProductionOrderRepository;
import com.cartonerp.util.OrderNumberUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Objects;
import java.util.Optional;

@Service
public class ProductionOrderService {
    @Autowired private ProductionOrderRepository productionOrderRepo;

    @Transactional
    public void createOrUpdateFromSignedPurchase(PurchaseOrder purchaseOrder) {
        if (purchaseOrder == null || purchaseOrder.getId() == null || !shouldSyncToProduction(purchaseOrder)) return;

        ProductionOrder productionOrder = findExistingProductionOrder(purchaseOrder)
            .orElseGet(() -> {
                ProductionOrder po = new ProductionOrder();
                po.setOrderNo(OrderNumberUtil.next("PRD"));
                po.setStatus("待排产");
                return po;
            });

        copyPurchaseFields(purchaseOrder, productionOrder);
        productionOrderRepo.save(productionOrder);
    }

    private boolean shouldSyncToProduction(PurchaseOrder purchaseOrder) {
        return purchaseOrder.getSignDate() != null || "已收货".equals(purchaseOrder.getStatus());
    }

    private Optional<ProductionOrder> findExistingProductionOrder(PurchaseOrder purchaseOrder) {
        List<ProductionOrder> linkedOrders = productionOrderRepo.findByPurchaseOrderId(purchaseOrder.getId());
        if (!linkedOrders.isEmpty()) return Optional.of(linkedOrders.get(0));

        SalesOrder salesOrder = purchaseOrder.getSalesOrder();
        if (salesOrder == null || salesOrder.getId() == null) return Optional.empty();

        return productionOrderRepo.findBySalesOrderId(salesOrder.getId()).stream()
            .filter(po -> po.getPurchaseOrder() == null)
            .filter(po -> sameValue(po.getProductName(), firstNonBlank(purchaseOrder.getProductName(), purchaseOrder.getMaterialName())))
            .filter(po -> sameValue(po.getSpec(), purchaseOrder.getSpec()))
            .filter(po -> sameValue(po.getMaterial(), purchaseOrder.getMaterial()))
            .filter(po -> sameValue(po.getBoxType(), purchaseOrder.getBoxType()))
            .filter(po -> sameValue(po.getStitchType(), purchaseOrder.getStitchType()))
            .findFirst();
    }

    private void copyPurchaseFields(PurchaseOrder source, ProductionOrder target) {
        target.setPurchaseOrder(source);
        target.setSalesOrder(source.getSalesOrder());
        target.setCustomer(resolveCustomer(source));
        target.setSupplier(source.getSupplier());
        target.setProductName(firstNonBlank(source.getProductName(), source.getMaterialName(), source.getOrderNo()));
        target.setSpec(source.getSpec());
        target.setMaterial(source.getMaterial());
        target.setBoxType(source.getBoxType());
        target.setStitchType(source.getStitchType());
        target.setProductionStatus(source.getProductionStatus());
        target.setUnitPrice(source.getUnitPrice());
        target.setQty(source.getQty());
        target.setUnit(source.getUnit() != null ? source.getUnit() : "个");
        target.setProductionMaterial(source.getProductionMaterial());
        target.setFluteType(source.getFluteType());
        target.setBoardLength(source.getBoardLength());
        target.setBoardWidth(source.getBoardWidth());
        target.setBoardQty(source.getBoardQty());
        target.setCutCount(source.getCutCount());
        target.setCrease(source.getCrease());
        target.setBoardArea(source.getBoardArea());
        target.setTotalArea(source.getTotalArea());
        target.setOrderArea(source.getTotalArea());
        target.setMaterialBasePrice(source.getMaterialBasePrice());
        target.setDiscountRate(source.getDiscountRate());
        target.setBoardUnitPrice(source.getBoardUnitPrice());
        target.setProfitRate(source.getProfitRate());
        target.setBoardAmount(source.getBoardAmount());
        target.setSignDate(source.getSignDate());
        target.setActualQty(source.getActualQty());
        target.setActualAmount(source.getActualAmount());
        target.setNotes(resolveNotes(source));
        if (target.getStatus() == null || target.getStatus().isBlank()) target.setStatus("待排产");
    }

    private String resolveNotes(PurchaseOrder purchaseOrder) {
        SalesOrder salesOrder = purchaseOrder.getSalesOrder();
        if (salesOrder != null && salesOrder.getNotes() != null) return salesOrder.getNotes();
        return purchaseOrder.getNotes();
    }

    private Customer resolveCustomer(PurchaseOrder purchaseOrder) {
        if (purchaseOrder.getCustomer() != null) return purchaseOrder.getCustomer();
        SalesOrder salesOrder = purchaseOrder.getSalesOrder();
        return salesOrder != null ? salesOrder.getCustomer() : null;
    }

    private boolean sameValue(String a, String b) {
        return Objects.equals(normalize(a), normalize(b));
    }

    private String normalize(String value) {
        return value == null || value.isBlank() ? "" : value.trim();
    }

    private String firstNonBlank(String... values) {
        for (String value : values) {
            if (value != null && !value.isBlank()) return value;
        }
        return "";
    }
}
