package com.cartonerp.controller;

import com.cartonerp.common.Result;
import com.cartonerp.entity.Inventory;
import com.cartonerp.entity.ProductionOrder;
import com.cartonerp.entity.ProductionRecord;
import com.cartonerp.entity.PurchaseOrder;
import com.cartonerp.entity.SalesOrder;
import com.cartonerp.repository.InventoryRepository;
import com.cartonerp.repository.ProductionRecordRepository;
import com.cartonerp.service.DeliveryNoteService;
import com.cartonerp.service.ProductionRecordService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.*;
import org.springframework.web.bind.annotation.*;
import java.time.LocalDate;
import java.util.*;

@RestController
@RequestMapping("/api/inventory")
public class InventoryController {
    @Autowired private InventoryRepository repo;
    @Autowired private ProductionRecordRepository productionRecordRepo;
    @Autowired private DeliveryNoteService deliveryNoteService;
    @Autowired private ProductionRecordService productionRecordService;

    @GetMapping
    public Result<List<Map<String, Object>>> list(@RequestParam(defaultValue = "") String q,
                                                   @RequestParam(defaultValue = "1") int page,
                                                   @RequestParam(defaultValue = "20") int perPage) {
        deliveryNoteService.syncPendingDeliveryNotes();
        List<Map<String, Object>> rows = buildProductionStockSummary();
        if (q != null && !q.isBlank()) {
            String keyword = q.trim().toLowerCase();
            rows = rows.stream().filter(row -> matches(row, keyword)).toList();
        }
        int safePerPage = Math.max(perPage, 1);
        int from = Math.min(Math.max(page - 1, 0) * safePerPage, rows.size());
        int to = Math.min(from + safePerPage, rows.size());
        return Result.okWithTotal(rows.subList(from, to), rows.size());
    }

    @GetMapping("/{id}") public Result<Inventory> get(@PathVariable Long id) { return repo.findById(id).map(Result::ok).orElse(Result.fail(404, "不存在")); }
    @PostMapping public Result<Inventory> create(@RequestBody Inventory i) { return Result.ok(repo.save(i), "创建成功"); }
    @PutMapping("/{id}")
    public Result<Inventory> update(@PathVariable Long id, @RequestBody Inventory i) {
        Inventory ex = repo.findById(id).orElse(null);
        if (ex == null) return Result.fail(404, "不存在");
        if (i.getItemType() != null) ex.setItemType(i.getItemType());
        if (i.getItemName() != null) ex.setItemName(i.getItemName());
        if (i.getSpec() != null) ex.setSpec(i.getSpec());
        if (i.getQty() != null) ex.setQty(i.getQty());
        if (i.getUnit() != null) ex.setUnit(i.getUnit());
        if (i.getWarehouseLocation() != null) ex.setWarehouseLocation(i.getWarehouseLocation());
        if (i.getSafetyStock() != null) ex.setSafetyStock(i.getSafetyStock());
        return Result.ok(repo.save(ex), "更新成功");
    }
    @DeleteMapping("/{id}") public Result<?> delete(@PathVariable Long id) { repo.deleteById(id); return Result.ok(null, "删除成功"); }

    private List<Map<String, Object>> buildProductionStockSummary() {
        Map<String, Summary> grouped = new LinkedHashMap<>();
        for (ProductionRecord record : productionRecordRepo.findAll(Sort.by(Sort.Direction.DESC, "id"))) {
            ProductionOrder productionOrder = record.getProductionOrder();
            if (productionOrder == null) continue;
            PurchaseOrder purchaseOrder = productionOrder.getPurchaseOrder();
            SalesOrder salesOrder = purchaseOrder != null && purchaseOrder.getSalesOrder() != null
                ? purchaseOrder.getSalesOrder()
                : productionOrder.getSalesOrder();
            String key = summaryKey(productionOrder, purchaseOrder, salesOrder);
            Summary summary = grouped.computeIfAbsent(key, k -> new Summary(productionOrder, purchaseOrder, salesOrder));
            summary.inboundQty += intValue(record.getOutputQty());
        }
        return grouped.values().stream().map(Summary::toMap).toList();
    }

    private String summaryKey(ProductionOrder productionOrder, PurchaseOrder purchaseOrder, SalesOrder salesOrder) {
        return String.join("|",
            firstNonBlank(salesOrder != null ? salesOrder.getOrderNo() : null, ""),
            sourceCustomerName(productionOrder, purchaseOrder, salesOrder),
            firstNonBlank(
                productionOrder.getProductName(),
                purchaseOrder != null ? purchaseOrder.getProductName() : null,
                salesOrder != null ? salesOrder.getProductName() : null
            ),
            firstNonBlank(
                productionOrder.getSpec(),
                purchaseOrder != null ? purchaseOrder.getSpec() : null,
                salesOrder != null ? salesOrder.getSpec() : null
            )
        );
    }

    private boolean matches(Map<String, Object> row, String keyword) {
        return List.of("salesOrderNo", "customerName", "productName", "spec", "customerMaterial", "fluteType").stream()
            .map(row::get)
            .filter(Objects::nonNull)
            .map(value -> String.valueOf(value).toLowerCase())
            .anyMatch(value -> value.contains(keyword));
    }

    private String sourceCustomerName(ProductionOrder productionOrder, PurchaseOrder purchaseOrder, SalesOrder salesOrder) {
        if (purchaseOrder != null && purchaseOrder.getCustomer() != null) return purchaseOrder.getCustomer().getName();
        if (productionOrder != null && productionOrder.getCustomer() != null) return productionOrder.getCustomer().getName();
        if (salesOrder != null && salesOrder.getCustomer() != null) return salesOrder.getCustomer().getName();
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

    private int intValue(Number value) {
        return value != null ? value.intValue() : 0;
    }

    private double round2(double value) {
        return Math.round(value * 100.0) / 100.0;
    }

    private double round4(double value) {
        return Math.round(value * 10000.0) / 10000.0;
    }

    private class Summary {
        private final ProductionOrder productionOrder;
        private final PurchaseOrder purchaseOrder;
        private final SalesOrder salesOrder;
        private int inboundQty;
        private int deliveryQty;

        private Summary(ProductionOrder productionOrder, PurchaseOrder purchaseOrder, SalesOrder salesOrder) {
            this.productionOrder = productionOrder;
            this.purchaseOrder = purchaseOrder;
            this.salesOrder = salesOrder;
        }

        private Map<String, Object> toMap() {
            double customerUnitPrice = firstNumber(
                salesOrder != null ? salesOrder.getUnitPrice() : null,
                purchaseOrder != null ? purchaseOrder.getUnitPrice() : null,
                productionOrder.getUnitPrice()
            );
            double singleArea = firstNumber(salesOrder != null ? salesOrder.getSingleArea() : null);
            double boxUnitPrice = firstNumber(salesOrder != null ? salesOrder.getBoxUnitPrice() : null);
            if (boxUnitPrice <= 0 && customerUnitPrice > 0 && singleArea > 0) {
                boxUnitPrice = round2(customerUnitPrice * singleArea);
            }
            double area = round4(singleArea * inboundQty);
            deliveryQty = deliveryNoteService.deliveredQtyForOrder(productionOrder, salesOrder);
            int remainingStock = inboundQty - deliveryQty;

            Map<String, Object> m = new LinkedHashMap<>();
            m.put("id", summaryKey(productionOrder, purchaseOrder, salesOrder));
            m.put("deliveryStatus", remainingStock == 0 ? "已送货" : "未送货");
            m.put("salesOrderNo", salesOrder != null ? salesOrder.getOrderNo() : "");
            m.put("orderDate", sourceOrderDate(salesOrder));
            m.put("customerName", sourceCustomerName(productionOrder, purchaseOrder, salesOrder));
            m.put("productName", firstNonBlank(
                productionOrder.getProductName(),
                purchaseOrder != null ? purchaseOrder.getProductName() : null,
                salesOrder != null ? salesOrder.getProductName() : null
            ));
            m.put("spec", firstNonBlank(
                productionOrder.getSpec(),
                purchaseOrder != null ? purchaseOrder.getSpec() : null,
                salesOrder != null ? salesOrder.getSpec() : null
            ));
            m.put("customerUnitPrice", customerUnitPrice);
            m.put("customerMaterial", firstNonBlank(
                salesOrder != null ? salesOrder.getMaterial() : null,
                productionOrder.getMaterial(),
                purchaseOrder != null ? purchaseOrder.getMaterial() : null
            ));
            m.put("fluteType", firstNonBlank(
                salesOrder != null ? salesOrder.getFluteType() : null,
                productionOrder.getFluteType(),
                purchaseOrder != null ? purchaseOrder.getFluteType() : null
            ));
            m.put("singleArea", singleArea);
            m.put("area", area);
            m.put("inboundAmount", round2(customerUnitPrice * area));
            m.put("inboundQty", inboundQty);
            m.put("boxUnitPrice", boxUnitPrice);
            m.put("deliveryQty", deliveryQty);
            m.put("amount", round2(boxUnitPrice * deliveryQty));
            m.put("remainingStock", remainingStock);
            return m;
        }

        private LocalDate sourceOrderDate(SalesOrder salesOrder) {
            return salesOrder != null && salesOrder.getCreatedAt() != null ? salesOrder.getCreatedAt().toLocalDate() : null;
        }
    }
}
