package com.cartonerp.controller;

import com.cartonerp.common.Result;
import com.cartonerp.entity.Customer;
import com.cartonerp.entity.DeliveryNote;
import com.cartonerp.entity.ProductionOrder;
import com.cartonerp.entity.PurchaseOrder;
import com.cartonerp.entity.SalesOrder;
import com.cartonerp.repository.*;
import com.cartonerp.service.DeliveryNoteService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.*;
import org.springframework.web.bind.annotation.*;
import java.util.*;

@RestController
@RequestMapping("/api/delivery-notes")
public class DeliveryNoteController {
    @Autowired private DeliveryNoteRepository repo;
    @Autowired private DeliveryNoteService deliveryNoteService;

    @GetMapping
    public Result<List<Map<String, Object>>> list(@RequestParam(defaultValue = "") String q,
                                                   @RequestParam(defaultValue = "1") int page,
                                                   @RequestParam(defaultValue = "20") int perPage) {
        deliveryNoteService.syncPendingDeliveryNotes();
        List<Map<String, Object>> rows = repo.findAll(Sort.by(Sort.Direction.DESC, "id")).stream()
            .map(this::toMap)
            .filter(row -> matches(row, q))
            .toList();
        int safePerPage = Math.max(perPage, 1);
        int from = Math.min(Math.max(page - 1, 0) * safePerPage, rows.size());
        int to = Math.min(from + safePerPage, rows.size());
        return Result.okWithTotal(rows.subList(from, to), rows.size());
    }

    @PostMapping
    public Result<Map<String, Object>> create(@RequestBody DeliveryNote d) {
        DeliveryNote saved = deliveryNoteService.saveManualFields(new DeliveryNote(), d);
        return Result.ok(toMap(saved), "创建成功");
    }

    @GetMapping("/{id}")
    public Result<Map<String, Object>> get(@PathVariable Long id) {
        return repo.findById(id).map(d -> Result.ok(toMap(d))).orElse(Result.fail(404, "不存在"));
    }

    @PutMapping("/{id}")
    public Result<Map<String, Object>> update(@PathVariable Long id, @RequestBody DeliveryNote d) {
        DeliveryNote ex = repo.findById(id).orElse(null);
        if (ex == null) return Result.fail(404, "不存在");
        DeliveryNote saved = deliveryNoteService.saveManualFields(ex, d);
        return Result.ok(toMap(saved), "更新成功");
    }

    @PostMapping("/{id}/mark-printed")
    public Result<Map<String, Object>> markPrinted(@PathVariable Long id) {
        DeliveryNote ex = repo.findById(id).orElse(null);
        if (ex == null) return Result.fail(404, "不存在");
        DeliveryNote saved = deliveryNoteService.markPrinted(ex);
        return Result.ok(toMap(saved), "已标记打印");
    }

    @DeleteMapping("/{id}") public Result<?> delete(@PathVariable Long id) { repo.deleteById(id); return Result.ok(null, "删除成功"); }

    private Map<String, Object> toMap(DeliveryNote d) {
        ProductionOrder productionOrder = d.getProductionOrder();
        PurchaseOrder purchaseOrder = productionOrder != null ? productionOrder.getPurchaseOrder() : null;
        SalesOrder salesOrder = sourceSalesOrder(d, productionOrder, purchaseOrder);
        int inboundQty = deliveryNoteService.inboundQtyForOrder(productionOrder, salesOrder);
        int deliveryQty = d.getQty() != null ? d.getQty() : 0;
        double customerUnitPrice = firstNumber(
            salesOrder != null ? salesOrder.getUnitPrice() : null,
            purchaseOrder != null ? purchaseOrder.getUnitPrice() : null,
            productionOrder != null ? productionOrder.getUnitPrice() : null
        );
        double singleArea = firstNumber(salesOrder != null ? salesOrder.getSingleArea() : null);
        double boxUnitPrice = firstNumber(salesOrder != null ? salesOrder.getBoxUnitPrice() : null);
        if (boxUnitPrice <= 0 && customerUnitPrice > 0 && singleArea > 0) {
            boxUnitPrice = round2(customerUnitPrice * singleArea);
        }
        double area = round4(singleArea * deliveryQty);
        int totalDeliveryQty = deliveryNoteService.deliveredQtyForOrder(productionOrder, salesOrder);
        int remainingStock = inboundQty - totalDeliveryQty;
        Customer customer = sourceCustomer(d, productionOrder, purchaseOrder, salesOrder);

        Map<String, Object> m = new LinkedHashMap<>();
        m.put("id", d.getId());
        m.put("deliveryStatus", deliveryNoteService.isPrinted(d) ? "已送货" : "未送货");
        m.put("status", deliveryNoteService.isPrinted(d) ? "已送货" : "未送货");
        m.put("salesOrderNo", salesOrder != null ? salesOrder.getOrderNo() : "");
        m.put("orderDate", sourceOrderDate(purchaseOrder, salesOrder));
        m.put("customerName", customer != null ? customer.getName() : "");
        m.put("customerContact", customer != null ? customer.getContact() : "");
        m.put("customerPhone", customer != null ? customer.getPhone() : "");
        m.put("customerAddress", customer != null ? customer.getAddress() : "");
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
        m.put("customerMaterial", firstNonBlank(
            salesOrder != null ? salesOrder.getMaterial() : null,
            productionOrder != null ? productionOrder.getMaterial() : null,
            purchaseOrder != null ? purchaseOrder.getMaterial() : null
        ));
        m.put("fluteType", firstNonBlank(
            salesOrder != null ? salesOrder.getFluteType() : null,
            productionOrder != null ? productionOrder.getFluteType() : null,
            purchaseOrder != null ? purchaseOrder.getFluteType() : null
        ));
        m.put("singleArea", singleArea);
        m.put("inboundAmount", round2(customerUnitPrice * singleArea * inboundQty));
        m.put("inboundQty", inboundQty);
        m.put("boxUnitPrice", boxUnitPrice);
        m.put("deliveryQty", deliveryQty);
        m.put("qty", deliveryQty);
        m.put("area", area);
        m.put("amount", round2(area * boxUnitPrice));
        m.put("remainingStock", remainingStock);
        m.put("otherDeliveredQty", totalDeliveryQty - deliveryQty);
        m.put("notes", d.getNotes());
        m.put("driver", d.getDriver());
        m.put("carrier", d.getDriver());
        m.put("noteNo", d.getNoteNo());
        m.put("deliveryDate", d.getDeliveryDate());
        m.put("issuer", d.getIssuer());
        m.put("salesperson", d.getSalesperson());
        m.put("reviewCount", d.getReviewCount());
        m.put("customerSignature", d.getCustomerSignature());
        m.put("productionOrderId", productionOrder != null ? productionOrder.getId() : null);
        m.put("salesOrderId", salesOrder != null ? salesOrder.getId() : null);
        m.put("customerId", customer != null ? customer.getId() : null);
        m.put("printed", deliveryNoteService.isPrinted(d));
        m.put("createdAt", d.getCreatedAt());
        return m;
    }

    private boolean matches(Map<String, Object> row, String q) {
        if (q == null || q.isBlank()) return true;
        String keyword = q.trim().toLowerCase();
        return List.of("noteNo", "salesOrderNo", "customerName", "productName", "spec", "driver", "issuer", "salesperson").stream()
            .map(row::get)
            .filter(Objects::nonNull)
            .map(value -> String.valueOf(value).toLowerCase())
            .anyMatch(value -> value.contains(keyword));
    }

    private SalesOrder sourceSalesOrder(DeliveryNote note, ProductionOrder productionOrder, PurchaseOrder purchaseOrder) {
        if (purchaseOrder != null && purchaseOrder.getSalesOrder() != null) return purchaseOrder.getSalesOrder();
        if (productionOrder != null && productionOrder.getSalesOrder() != null) return productionOrder.getSalesOrder();
        return note.getSalesOrder();
    }

    private Object sourceOrderDate(PurchaseOrder purchaseOrder, SalesOrder salesOrder) {
        if (purchaseOrder != null && purchaseOrder.getOrderDate() != null) return purchaseOrder.getOrderDate();
        return salesOrder != null && salesOrder.getCreatedAt() != null ? salesOrder.getCreatedAt().toLocalDate() : null;
    }

    private Customer sourceCustomer(DeliveryNote note, ProductionOrder productionOrder, PurchaseOrder purchaseOrder, SalesOrder salesOrder) {
        if (note.getCustomer() != null) return note.getCustomer();
        if (purchaseOrder != null && purchaseOrder.getCustomer() != null) return purchaseOrder.getCustomer();
        if (productionOrder != null && productionOrder.getCustomer() != null) return productionOrder.getCustomer();
        if (salesOrder != null && salesOrder.getCustomer() != null) return salesOrder.getCustomer();
        return null;
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

    private double round2(double value) {
        return Math.round(value * 100.0) / 100.0;
    }

    private double round4(double value) {
        return Math.round(value * 10000.0) / 10000.0;
    }
}
