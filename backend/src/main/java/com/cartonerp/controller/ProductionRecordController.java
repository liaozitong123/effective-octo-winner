package com.cartonerp.controller;

import com.cartonerp.common.Result;
import com.cartonerp.entity.*;
import com.cartonerp.repository.*;
import com.cartonerp.service.DeliveryNoteService;
import com.cartonerp.service.ProductionRecordService;
import jakarta.persistence.criteria.Predicate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.*;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.web.bind.annotation.*;
import java.util.*;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/production-records")
public class ProductionRecordController {
    @Autowired private ProductionRecordRepository repo;
    @Autowired private ProductionOrderRepository productionOrderRepo;
    @Autowired private com.cartonerp.service.BusinessService businessService;
    @Autowired private ProductionRecordService productionRecordService;
    @Autowired private DeliveryNoteService deliveryNoteService;

    @GetMapping
    public Result<List<Map<String, Object>>> list(@RequestParam(defaultValue = "") String q,
                                                   @RequestParam(defaultValue = "1") int page,
                                                   @RequestParam(defaultValue = "20") int perPage) {
        productionRecordService.syncReceivedPurchasesWithoutRecord();
        Specification<ProductionRecord> spec = (root, query, cb) -> {
            if (q.isEmpty()) return null;
            String p = "%" + q + "%";
            var productionOrder = root.join("productionOrder", jakarta.persistence.criteria.JoinType.LEFT);
            var purchaseOrder = productionOrder.join("purchaseOrder", jakarta.persistence.criteria.JoinType.LEFT);
            var salesOrder = productionOrder.join("salesOrder", jakarta.persistence.criteria.JoinType.LEFT);
            var customer = productionOrder.join("customer", jakarta.persistence.criteria.JoinType.LEFT);
            var supplier = productionOrder.join("supplier", jakarta.persistence.criteria.JoinType.LEFT);
            return cb.or(
                cb.like(root.get("operator"), p),
                cb.like(root.get("nailer"), p),
                cb.like(productionOrder.get("orderNo"), p),
                cb.like(purchaseOrder.get("orderNo"), p),
                cb.like(salesOrder.get("orderNo"), p),
                cb.like(customer.get("name"), p),
                cb.like(supplier.get("name"), p),
                cb.like(productionOrder.get("productName"), p)
            );
        };
        Page<ProductionRecord> pg = repo.findAll(spec, PageRequest.of(page - 1, perPage, Sort.by(Sort.Direction.DESC, "id")));
        return Result.okWithTotal(pg.getContent().stream().map(this::toMap).collect(Collectors.toList()), pg.getTotalElements());
    }

    @PostMapping
    public Result<Map<String, Object>> create(@RequestBody ProductionRecord r) {
        if (r.getProductionOrder() != null && r.getProductionOrder().getId() != null)
            productionOrderRepo.findById(r.getProductionOrder().getId()).ifPresent(r::setProductionOrder);
        applyStockCalculation(r);
        ProductionRecord saved = repo.save(r);
        businessService.onProductionRecordAdded(saved);
        deliveryNoteService.syncPendingDeliveryNotes();
        return Result.ok(toMap(saved), "创建成功");
    }

    @PutMapping("/{id}")
    public Result<Map<String, Object>> update(@PathVariable Long id, @RequestBody ProductionRecord r) {
        ProductionRecord ex = repo.findById(id).orElse(null);
        if (ex == null) return Result.fail(404, "不存在");
        if (r.getProductionOrder() != null && r.getProductionOrder().getId() != null)
            productionOrderRepo.findById(r.getProductionOrder().getId()).ifPresent(ex::setProductionOrder);
        if (r.getOutputQty() != null) ex.setOutputQty(r.getOutputQty());
        if (r.getWasteQty() != null) ex.setWasteQty(r.getWasteQty());
        if (r.getOperator() != null) ex.setOperator(r.getOperator());
        if (r.getNailer() != null) ex.setNailer(r.getNailer());
        if (r.getShift() != null) ex.setShift(r.getShift());
        if (r.getRecordDate() != null) ex.setRecordDate(r.getRecordDate());
        if (r.getProductionDate() != null) ex.setProductionDate(r.getProductionDate());
        if (r.getDeliveryQty() != null) ex.setDeliveryQty(r.getDeliveryQty());
        if (r.getRemainingStock() != null) ex.setRemainingStock(r.getRemainingStock());
        if (r.getDeliveryDate() != null) ex.setDeliveryDate(r.getDeliveryDate());
        if (r.getNotes() != null) ex.setNotes(r.getNotes());
        applyStockCalculation(ex);
        ProductionRecord saved = repo.save(ex);
        businessService.onProductionRecordAdded(saved);
        deliveryNoteService.syncPendingDeliveryNotes();
        return Result.ok(toMap(saved), "更新成功");
    }

    @GetMapping("/{id}")
    public Result<Map<String, Object>> get(@PathVariable Long id) {
        return repo.findById(id).map(r -> Result.ok(toMap(r))).orElse(Result.fail(404, "不存在"));
    }

    @DeleteMapping("/{id}") public Result<?> delete(@PathVariable Long id) { repo.deleteById(id); return Result.ok(null, "删除成功"); }

    private Map<String, Object> toMap(ProductionRecord r) {
        ProductionOrder productionOrder = r.getProductionOrder();
        PurchaseOrder purchaseOrder = productionOrder != null ? productionOrder.getPurchaseOrder() : null;
        SalesOrder salesOrder = purchaseOrder != null && purchaseOrder.getSalesOrder() != null
            ? purchaseOrder.getSalesOrder()
            : (productionOrder != null ? productionOrder.getSalesOrder() : null);

        double customerUnitPrice = firstNumber(
            salesOrder != null ? salesOrder.getUnitPrice() : null,
            purchaseOrder != null ? purchaseOrder.getUnitPrice() : null,
            productionOrder != null ? productionOrder.getUnitPrice() : null
        );
        double singleArea = firstNumber(salesOrder != null ? salesOrder.getSingleArea() : null, 0.0);
        int inboundQty = r.getOutputQty() != null ? r.getOutputQty() : 0;
        double boardArea = firstNumber(
            purchaseOrder != null ? purchaseOrder.getBoardArea() : null,
            productionOrder != null ? productionOrder.getBoardArea() : null
        );
        int actualQty = firstInt(
            purchaseOrder != null ? purchaseOrder.getActualQty() : null,
            productionOrder != null ? productionOrder.getActualQty() : null
        );

        Map<String, Object> m = new LinkedHashMap<>();
        m.put("id", r.getId());
        m.put("productionOrderId", productionOrder != null ? productionOrder.getId() : null);
        m.put("productionOrderNo", productionOrder != null ? productionOrder.getOrderNo() : "");
        m.put("receiptStatus", purchaseOrder != null ? purchaseOrder.getStatus() : "");
        m.put("purchaseOrderNo", purchaseOrder != null ? purchaseOrder.getOrderNo() : "");
        m.put("salesOrderNo", salesOrder != null ? salesOrder.getOrderNo() : "");
        m.put("receivedDate", salesOrder != null && salesOrder.getCreatedAt() != null ? salesOrder.getCreatedAt().toLocalDate() : null);
        m.put("customerName", sourceCustomerName(productionOrder, purchaseOrder, salesOrder));
        m.put("productName", firstNonBlank(
            productionOrder != null ? productionOrder.getProductName() : null,
            purchaseOrder != null ? purchaseOrder.getProductName() : null,
            salesOrder != null ? salesOrder.getProductName() : null
        ));
        m.put("spec", firstNonBlank(
            productionOrder != null ? productionOrder.getSpec() : null,
            purchaseOrder != null ? purchaseOrder.getSpec() : null,
            salesOrder != null ? salesOrder.getSpec() : null
        ));
        m.put("customerUnitPrice", customerUnitPrice);
        m.put("fluteType", firstNonBlank(
            productionOrder != null ? productionOrder.getFluteType() : null,
            purchaseOrder != null ? purchaseOrder.getFluteType() : null,
            salesOrder != null ? salesOrder.getFluteType() : null
        ));
        m.put("singleArea", singleArea);
        m.put("inboundAmount", round2(customerUnitPrice * singleArea * inboundQty));
        m.put("inboundQty", inboundQty);
        m.put("outputQty", r.getOutputQty());
        m.put("inboundDate", r.getRecordDate());
        m.put("recordDate", r.getRecordDate());
        m.put("nailer", r.getNailer());
        m.put("orderDate", purchaseOrder != null ? purchaseOrder.getOrderDate() : null);
        m.put("supplierName", sourceSupplierName(productionOrder, purchaseOrder));
        m.put("boardArea", boardArea);
        m.put("actualTotalArea", round4(boardArea * actualQty));
        m.put("actualQty", actualQty);
        m.put("actualAmount", firstNumber(
            purchaseOrder != null ? purchaseOrder.getActualAmount() : null,
            productionOrder != null ? productionOrder.getActualAmount() : null
        ));
        m.put("operator", r.getOperator());
        m.put("productionDate", r.getProductionDate());
        m.put("deliveryQty", r.getDeliveryQty());
        m.put("remainingStock", remainingStock(r));
        m.put("deliveryDate", r.getDeliveryDate());
        m.put("wasteQty", r.getWasteQty());
        m.put("shift", r.getShift());
        m.put("notes", r.getNotes());
        m.put("createdAt", r.getCreatedAt());
        return m;
    }

    private String sourceCustomerName(ProductionOrder productionOrder, PurchaseOrder purchaseOrder, SalesOrder salesOrder) {
        if (purchaseOrder != null && purchaseOrder.getCustomer() != null) return purchaseOrder.getCustomer().getName();
        if (productionOrder != null && productionOrder.getCustomer() != null) return productionOrder.getCustomer().getName();
        if (salesOrder != null && salesOrder.getCustomer() != null) return salesOrder.getCustomer().getName();
        return "";
    }

    private String sourceSupplierName(ProductionOrder productionOrder, PurchaseOrder purchaseOrder) {
        if (purchaseOrder != null && purchaseOrder.getSupplier() != null) return purchaseOrder.getSupplier().getName();
        if (productionOrder != null && productionOrder.getSupplier() != null) return productionOrder.getSupplier().getName();
        return "";
    }

    private String firstNonBlank(String... values) {
        for (String value : values) {
            if (value != null && !value.isBlank()) return value;
        }
        return "";
    }

    private double firstNumber(Number... values) {
        for (Number value : values) {
            if (value != null) return value.doubleValue();
        }
        return 0.0;
    }

    private int firstInt(Number... values) {
        for (Number value : values) {
            if (value != null) return value.intValue();
        }
        return 0;
    }

    private double round2(double value) {
        return Math.round(value * 100.0) / 100.0;
    }

    private double round4(double value) {
        return Math.round(value * 10000.0) / 10000.0;
    }

    private void applyStockCalculation(ProductionRecord record) {
        record.setRemainingStock(remainingStock(record));
    }

    private int remainingStock(ProductionRecord record) {
        int inboundQty = record.getOutputQty() != null ? record.getOutputQty() : 0;
        int deliveryQty = record.getDeliveryQty() != null ? record.getDeliveryQty() : 0;
        return inboundQty - deliveryQty;
    }
}
