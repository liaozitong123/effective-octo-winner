package com.cartonerp.controller;

import com.cartonerp.common.Result;
import com.cartonerp.entity.ProductionOrder;
import com.cartonerp.entity.ProductionRecord;
import com.cartonerp.entity.PurchaseOrder;
import com.cartonerp.entity.SalesOrder;
import com.cartonerp.repository.*;
import com.cartonerp.service.ProductionOrderService;
import com.cartonerp.util.BoardCalculationUtil;
import com.cartonerp.util.OrderNumberUtil;
import jakarta.persistence.criteria.JoinType;
import jakarta.persistence.criteria.Predicate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.*;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.web.bind.annotation.*;

import java.util.*;

@RestController
@RequestMapping("/api/production-orders")
public class ProductionOrderController {
    @Autowired private ProductionOrderRepository repo;
    @Autowired private SalesOrderRepository salesOrderRepo;
    @Autowired private ProductionRecordRepository recordRepo;
    @Autowired private ProductionOrderService productionOrderService;

    @GetMapping
    public Result<List<Map<String, Object>>> list(@RequestParam(defaultValue = "") String q,
                                                   @RequestParam(defaultValue = "all") String printStatus,
                                                   @RequestParam(defaultValue = "1") int page,
                                                   @RequestParam(defaultValue = "20") int perPage) {
        productionOrderService.syncReceivedPurchasesWithoutProduction();
        Specification<ProductionOrder> spec = (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();
            if (q != null && !q.isBlank()) {
                String p = "%" + q.trim() + "%";
                predicates.add(cb.or(
                    cb.like(root.get("orderNo"), p),
                    cb.like(root.get("productName"), p),
                    cb.like(root.join("purchaseOrder", JoinType.LEFT).get("orderNo"), p),
                    cb.like(root.join("customer", JoinType.LEFT).get("name"), p),
                    cb.like(root.join("supplier", JoinType.LEFT).get("name"), p)
                ));
            }
            if ("printed".equals(printStatus)) {
                predicates.add(cb.isTrue(root.get("printed")));
            } else if ("unprinted".equals(printStatus)) {
                predicates.add(cb.or(cb.isFalse(root.get("printed")), cb.isNull(root.get("printed"))));
            }
            return predicates.isEmpty() ? null : cb.and(predicates.toArray(new Predicate[0]));
        };
        Page<ProductionOrder> pg = repo.findAll(spec, PageRequest.of(page - 1, perPage, Sort.by(Sort.Direction.DESC, "id")));
        return Result.okWithTotal(pg.getContent().stream().map(this::toMap).toList(), pg.getTotalElements());
    }

    @GetMapping("/{id}")
    public Result<Map<String, Object>> get(@PathVariable Long id) {
        return repo.findById(id).map(o -> Result.ok(toMap(o))).orElse(Result.fail(404, "not found"));
    }

    @GetMapping("/{id}/progress")
    public Result<Map<String, Object>> progress(@PathVariable Long id) {
        ProductionOrder po = repo.findById(id).orElse(null);
        if (po == null) return Result.fail(404, "not found");
        List<ProductionRecord> records = recordRepo.findByProductionOrderId(id);
        int plannedQty = po.getQty() != null ? po.getQty() : 0;
        long currentOutput = records.stream().mapToLong(r -> r.getOutputQty() != null ? r.getOutputQty() : 0).max().orElse(0);
        long totalWaste = records.stream().mapToLong(r -> r.getWasteQty() != null ? r.getWasteQty() : 0).sum();
        double pct = plannedQty > 0 ? Math.round(currentOutput * 1000.0 / plannedQty) / 10.0 : 0;
        Map<String, Object> m = new LinkedHashMap<>();
        m.put("productionOrderNo", po.getOrderNo());
        m.put("productName", po.getProductName());
        m.put("plannedQty", plannedQty);
        m.put("totalOutput", currentOutput);
        m.put("totalWaste", totalWaste);
        m.put("remaining", plannedQty - currentOutput);
        m.put("progressPct", pct);
        m.put("status", po.getStatus());
        return Result.ok(m);
    }

    @PostMapping
    public Result<Map<String, Object>> create(@RequestBody ProductionOrder o) {
        if (o.getSalesOrder() != null && o.getSalesOrder().getId() != null)
            salesOrderRepo.findById(o.getSalesOrder().getId()).ifPresent(o::setSalesOrder);
        o.setOrderNo(OrderNumberUtil.next("PRD"));
        if (o.getStatus() == null) o.setStatus("待排产");
        applyBoardCalculation(o);
        return Result.ok(toMap(repo.save(o)), "created");
    }

    @PutMapping("/{id}")
    public Result<Map<String, Object>> update(@PathVariable Long id, @RequestBody ProductionOrder o) {
        ProductionOrder ex = repo.findById(id).orElse(null);
        if (ex == null) return Result.fail(404, "not found");
        if (o.getOperator() != null) ex.setOperator(o.getOperator());
        return Result.ok(toMap(repo.save(ex)), "updated");
    }

    @PostMapping("/{id}/mark-printed")
    public Result<Map<String, Object>> markPrinted(@PathVariable Long id) {
        ProductionOrder ex = repo.findById(id).orElse(null);
        if (ex == null) return Result.fail(404, "not found");
        ex.setPrinted(true);
        return Result.ok(toMap(repo.save(ex)), "updated");
    }

    @DeleteMapping("/{id}")
    public Result<?> delete(@PathVariable Long id) {
        try {
            List<ProductionRecord> records = recordRepo.findByProductionOrderId(id);
            recordRepo.deleteAll(records);
            repo.deleteById(id);
            return Result.ok(null, "deleted");
        } catch (Exception e) {
            return Result.fail(400, "delete failed");
        }
    }

    private Map<String, Object> toMap(ProductionOrder o) {
        PurchaseOrder purchaseOrder = o.getPurchaseOrder();
        Map<String, Object> m = new LinkedHashMap<>();
        m.put("id", o.getId());
        m.put("orderNo", o.getOrderNo());
        m.put("printed", isPrinted(o));
        m.put("printStatus", isPrinted(o) ? "已打印" : "未打印");
        m.put("purchaseOrderNo", purchaseOrder != null ? purchaseOrder.getOrderNo() : "");
        m.put("orderDate", purchaseOrder != null ? purchaseOrder.getOrderDate() : "");
        m.put("salesOrderNo", o.getSalesOrder() != null ? o.getSalesOrder().getOrderNo() : "");
        m.put("customerName", sourceCustomerName(o, purchaseOrder));
        m.put("productName", purchaseOrder != null ? purchaseOrder.getProductName() : o.getProductName());
        m.put("spec", purchaseOrder != null ? purchaseOrder.getSpec() : o.getSpec());
        m.put("material", purchaseOrder != null ? purchaseOrder.getMaterial() : o.getMaterial());
        m.put("boxType", purchaseOrder != null ? purchaseOrder.getBoxType() : o.getBoxType());
        m.put("stitchType", purchaseOrder != null ? purchaseOrder.getStitchType() : o.getStitchType());
        m.put("productionStatus", sourceProductionStatus(o, purchaseOrder));
        m.put("unitPrice", purchaseOrder != null ? purchaseOrder.getUnitPrice() : o.getUnitPrice());
        m.put("supplierName", sourceSupplierName(o, purchaseOrder));
        m.put("productionMaterial", purchaseOrder != null ? purchaseOrder.getProductionMaterial() : o.getProductionMaterial());
        m.put("fluteType", purchaseOrder != null ? purchaseOrder.getFluteType() : o.getFluteType());
        m.put("boardLength", purchaseOrder != null ? purchaseOrder.getBoardLength() : o.getBoardLength());
        m.put("boardWidth", purchaseOrder != null ? purchaseOrder.getBoardWidth() : o.getBoardWidth());
        m.put("boardQty", purchaseOrder != null ? purchaseOrder.getBoardQty() : o.getBoardQty());
        m.put("cutCount", purchaseOrder != null ? purchaseOrder.getCutCount() : o.getCutCount());
        m.put("crease", purchaseOrder != null ? purchaseOrder.getCrease() : o.getCrease());
        m.put("boardArea", purchaseOrder != null ? purchaseOrder.getBoardArea() : o.getBoardArea());
        m.put("totalArea", purchaseOrder != null ? purchaseOrder.getTotalArea() : o.getTotalArea());
        m.put("materialBasePrice", purchaseOrder != null ? purchaseOrder.getMaterialBasePrice() : o.getMaterialBasePrice());
        m.put("discountRate", displayDiscountRate(purchaseOrder != null ? purchaseOrder.getDiscountRate() : o.getDiscountRate()));
        m.put("boardUnitPrice", purchaseOrder != null ? purchaseOrder.getBoardUnitPrice() : o.getBoardUnitPrice());
        m.put("profitRate", purchaseOrder != null ? purchaseOrder.getProfitRate() : o.getProfitRate());
        m.put("boardAmount", purchaseOrder != null ? purchaseOrder.getBoardAmount() : o.getBoardAmount());
        m.put("signDate", purchaseOrder != null ? purchaseOrder.getSignDate() : o.getSignDate());
        m.put("actualQty", purchaseOrder != null ? purchaseOrder.getActualQty() : o.getActualQty());
        m.put("actualAmount", purchaseOrder != null ? purchaseOrder.getActualAmount() : o.getActualAmount());
        m.put("specNotes", o.getSpecNotes());
        m.put("urgent", o.getUrgent());
        m.put("orderArea", o.getOrderArea());
        m.put("qty", purchaseOrder != null ? purchaseOrder.getQty() : o.getQty());
        m.put("status", o.getStatus());
        m.put("startDate", o.getStartDate());
        m.put("finishDate", o.getFinishDate());
        m.put("workshop", o.getWorkshop());
        m.put("operator", o.getOperator());
        m.put("notes", sourceNotes(o, purchaseOrder));
        m.put("createdAt", o.getCreatedAt());
        return m;
    }

    private boolean isPrinted(ProductionOrder productionOrder) {
        return Boolean.TRUE.equals(productionOrder.getPrinted());
    }

    private String sourceNotes(ProductionOrder productionOrder, PurchaseOrder purchaseOrder) {
        SalesOrder salesOrder = purchaseOrder != null ? purchaseOrder.getSalesOrder() : productionOrder.getSalesOrder();
        if (salesOrder != null && salesOrder.getNotes() != null) return salesOrder.getNotes();
        return productionOrder.getNotes();
    }

    private String sourceProductionStatus(ProductionOrder productionOrder, PurchaseOrder purchaseOrder) {
        SalesOrder salesOrder = purchaseOrder != null ? purchaseOrder.getSalesOrder() : productionOrder.getSalesOrder();
        if (salesOrder != null && salesOrder.getProductionStatus() != null) return salesOrder.getProductionStatus();
        if (purchaseOrder != null && purchaseOrder.getProductionStatus() != null) return purchaseOrder.getProductionStatus();
        return productionOrder.getProductionStatus();
    }

    private String sourceCustomerName(ProductionOrder productionOrder, PurchaseOrder purchaseOrder) {
        if (purchaseOrder != null && purchaseOrder.getCustomer() != null) return purchaseOrder.getCustomer().getName();
        if (productionOrder.getCustomer() != null) return productionOrder.getCustomer().getName();
        return "";
    }

    private String sourceSupplierName(ProductionOrder productionOrder, PurchaseOrder purchaseOrder) {
        if (purchaseOrder != null && purchaseOrder.getSupplier() != null) return purchaseOrder.getSupplier().getName();
        if (productionOrder.getSupplier() != null) return productionOrder.getSupplier().getName();
        return "";
    }

    private void applyBoardCalculation(ProductionOrder o) {
        BoardCalculationUtil.Result result = BoardCalculationUtil.calculate(
            o.getBoardLength(), o.getBoardWidth(), o.getBoardQty(), o.getMaterialBasePrice(), o.getDiscountRate(),
            o.getBoardUnitPrice(), o.getUnitPrice(), o.getActualQty()
        );
        o.setBoardArea(result.boardArea());
        o.setTotalArea(result.totalArea());
        o.setBoardUnitPrice(result.boardUnitPrice());
        o.setProfitRate(result.profitRate());
        o.setBoardAmount(result.boardAmount());
        o.setActualAmount(result.actualAmount());
        o.setOrderArea(result.totalArea());
    }

    private Double displayDiscountRate(Double rate) {
        return rate != null && rate > 0 && rate <= 2 ? rate * 100 : rate;
    }
}
