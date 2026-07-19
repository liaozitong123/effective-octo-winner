package com.cartonerp.controller;

import com.cartonerp.common.Result;
import com.cartonerp.entity.PurchaseOrder;
import com.cartonerp.entity.SalesOrder;
import com.cartonerp.repository.CustomerRepository;
import com.cartonerp.repository.DeliveryNoteRepository;
import com.cartonerp.repository.ProductionOrderRepository;
import com.cartonerp.repository.PurchaseOrderRepository;
import com.cartonerp.repository.SalesOrderRepository;
import com.cartonerp.service.ProductionOrderService;
import com.cartonerp.util.OrderNumberUtil;
import jakarta.persistence.criteria.JoinType;
import jakarta.persistence.criteria.Predicate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.web.bind.annotation.*;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;

@RestController
@RequestMapping("/api/sales-orders")
public class SalesOrderController {
    @Autowired private SalesOrderRepository repo;
    @Autowired private CustomerRepository customerRepo;
    @Autowired private ProductionOrderRepository productionOrderRepo;
    @Autowired private DeliveryNoteRepository deliveryNoteRepo;
    @Autowired private PurchaseOrderRepository purchaseOrderRepo;
    @Autowired private ProductionOrderService productionOrderService;

    @GetMapping
    public Result<List<Map<String, Object>>> list(@RequestParam(defaultValue = "") String q,
                                                   @RequestParam(defaultValue = "1") int page,
                                                   @RequestParam(defaultValue = "20") int perPage) {
        Specification<SalesOrder> spec = (root, query, cb) -> {
            if (q.isEmpty()) return null;
            String p = "%" + q + "%";
            return cb.or(
                cb.like(root.get("orderNo"), p),
                cb.like(root.get("productName"), p),
                cb.like(root.join("customer", JoinType.LEFT).get("name"), p)
            );
        };
        Page<SalesOrder> pg = repo.findAll(spec, PageRequest.of(page - 1, perPage, Sort.by(Sort.Direction.DESC, "id")));
        List<Map<String, Object>> list = pg.getContent().stream().map(this::toMap).toList();
        return Result.okWithTotal(list, pg.getTotalElements());
    }

    @GetMapping("/{id}")
    public Result<Map<String, Object>> get(@PathVariable Long id) {
        return repo.findById(id).map(o -> Result.ok(toMap(o))).orElse(Result.fail(404, "不存在"));
    }

    @PostMapping
    public Result<Map<String, Object>> create(@RequestBody SalesOrder o) {
        if (o.getCustomer() != null && o.getCustomer().getId() != null)
            customerRepo.findById(o.getCustomer().getId()).ifPresent(o::setCustomer);
        // Auto-generate order number: SO-YYYYMMDDHHmmss
        o.setOrderNo(OrderNumberUtil.next("SO"));
        applyAmountCalculation(o);
        SalesOrder saved = repo.save(o);

        // Auto-create purchase order
        PurchaseOrder puo = new PurchaseOrder();
        puo.setOrderNo(OrderNumberUtil.next("PO"));
        puo.setSalesOrder(saved);
        puo.setProductName(saved.getProductName());
        puo.setSpec(saved.getSpec());
        puo.setMaterial(saved.getMaterial());
        puo.setBoxType(saved.getBoxType());
        puo.setFluteType(saved.getFluteType());
        puo.setQty(saved.getQty());
        puo.setUnit(saved.getUnit());
        puo.setUnitPrice(saved.getUnitPrice());
        puo.setOrderDate(toCreatedDate(saved.getCreatedAt()));
        puo.setMaterialType("纸板");
        puo.setMaterialName(saved.getProductName());
        if (saved.getCustomer() != null) puo.setCustomer(saved.getCustomer());
        puo.setStatus("待收货");
        purchaseOrderRepo.save(puo);

        return Result.ok(toMap(saved), "创建成功");
    }

    @PutMapping("/{id}")
    public Result<Map<String, Object>> update(@PathVariable Long id, @RequestBody SalesOrder o) {
        SalesOrder ex = repo.findById(id).orElse(null);
        if (ex == null) return Result.fail(404, "不存在");
        if (o.getOrderNo() != null) ex.setOrderNo(o.getOrderNo());
        if (o.getCustomer() != null && o.getCustomer().getId() != null)
            customerRepo.findById(o.getCustomer().getId()).ifPresent(ex::setCustomer);
        if (o.getProductName() != null) ex.setProductName(o.getProductName());
        if (o.getSpec() != null) ex.setSpec(o.getSpec());
        if (o.getMaterial() != null) ex.setMaterial(o.getMaterial());
        if (o.getQty() != null) ex.setQty(o.getQty());
        if (o.getUnit() != null) ex.setUnit(o.getUnit());
        if (o.getBoxType() != null) ex.setBoxType(o.getBoxType());
        if (o.getFluteType() != null) ex.setFluteType(o.getFluteType());
        if (o.getSingleArea() != null) ex.setSingleArea(o.getSingleArea());
        if (o.getUnitPrice() != null) ex.setUnitPrice(o.getUnitPrice());
        if (o.getBoxUnitPrice() != null) ex.setBoxUnitPrice(o.getBoxUnitPrice());
        if (o.getTotalAmount() != null) ex.setTotalAmount(o.getTotalAmount());
        if (o.getStatus() != null) ex.setStatus(o.getStatus());
        if (o.getDeliveryDate() != null) ex.setDeliveryDate(o.getDeliveryDate());
        if (o.getNotes() != null) ex.setNotes(o.getNotes());
        applyAmountCalculation(ex);
        SalesOrder updated = repo.save(ex);

        syncLinkedPurchaseOrders(updated);

        return Result.ok(toMap(updated), "更新成功");
    }

    private void syncLinkedPurchaseOrders(SalesOrder updated) {
        List<PurchaseOrder> linkedOrders = purchaseOrderRepo.findBySalesOrderId(updated.getId());
        if (linkedOrders.isEmpty()) linkedOrders = findLegacyPurchaseOrders(updated);

        for (PurchaseOrder purchaseOrder : linkedOrders) {
            purchaseOrder.setSalesOrder(updated);
            purchaseOrder.setQty(updated.getQty());
            purchaseOrder.setUnitPrice(updated.getUnitPrice());
            if (updated.getUnit() != null) purchaseOrder.setUnit(updated.getUnit());
            PurchaseOrder savedPurchase = purchaseOrderRepo.save(purchaseOrder);
            productionOrderService.createOrUpdateFromSignedPurchase(savedPurchase);
        }
    }

    private List<PurchaseOrder> findLegacyPurchaseOrders(SalesOrder salesOrder) {
        List<PurchaseOrder> candidates = purchaseOrderRepo.findAll().stream()
            .filter(p -> p.getSalesOrder() == null)
            .filter(p -> sameValue(p.getMaterialType(), "纸板"))
            .filter(p -> sameCustomer(p, salesOrder))
            .filter(p -> sameValue(p.getProductName(), salesOrder.getProductName())
                || sameValue(p.getMaterialName(), salesOrder.getProductName()))
            .filter(p -> sameValue(p.getSpec(), salesOrder.getSpec()))
            .filter(p -> sameValue(p.getMaterial(), salesOrder.getMaterial()))
            .filter(p -> sameValue(p.getBoxType(), salesOrder.getBoxType()))
            .filter(p -> sameValue(p.getFluteType(), salesOrder.getFluteType()))
            .toList();

        List<PurchaseOrder> sameStampOrders = candidates.stream()
            .filter(p -> sameOrderNumberStamp(p.getOrderNo(), salesOrder.getOrderNo()))
            .toList();
        if (!sameStampOrders.isEmpty()) return sameStampOrders;
        return candidates.size() == 1 ? candidates : List.of();
    }

    private boolean sameCustomer(PurchaseOrder purchaseOrder, SalesOrder salesOrder) {
        Long purchaseCustomerId = purchaseOrder.getCustomer() != null ? purchaseOrder.getCustomer().getId() : null;
        Long salesCustomerId = salesOrder.getCustomer() != null ? salesOrder.getCustomer().getId() : null;
        return Objects.equals(purchaseCustomerId, salesCustomerId);
    }

    private boolean sameValue(String a, String b) {
        return Objects.equals(normalize(a), normalize(b));
    }

    private String normalize(String value) {
        return value == null || value.isBlank() ? "" : value.trim();
    }

    private boolean sameOrderNumberStamp(String purchaseOrderNo, String salesOrderNo) {
        String purchaseStamp = orderNumberStamp(purchaseOrderNo);
        String salesStamp = orderNumberStamp(salesOrderNo);
        return !purchaseStamp.isEmpty() && purchaseStamp.equals(salesStamp);
    }

    private String orderNumberStamp(String orderNo) {
        if (orderNo == null) return "";
        int dashIndex = orderNo.indexOf("-");
        return dashIndex >= 0 && dashIndex + 1 < orderNo.length() ? orderNo.substring(dashIndex + 1) : "";
    }

    @DeleteMapping("/{id}")
    public Result<?> delete(@PathVariable Long id) {
        try {
            productionOrderRepo.findAll().stream()
                .filter(p -> p.getSalesOrder() != null && p.getSalesOrder().getId().equals(id))
                .forEach(p -> productionOrderRepo.delete(p));
            deliveryNoteRepo.findAll().stream()
                .filter(d -> d.getSalesOrder() != null && d.getSalesOrder().getId().equals(id))
                .forEach(d -> deliveryNoteRepo.delete(d));
            repo.deleteById(id);
            return Result.ok(null, "删除成功");
        } catch (Exception e) {
            return Result.fail(400, "删除失败：" + e.getMessage());
        }
    }

    private Map<String, Object> toMap(SalesOrder o) {
        Map<String, Object> m = new LinkedHashMap<>();
        m.put("id", o.getId()); m.put("orderNo", o.getOrderNo());
        m.put("createdDate", toCreatedDate(o.getCreatedAt()));
        m.put("customerName", o.getCustomer() != null ? o.getCustomer().getName() : "");
        m.put("customerId", o.getCustomer() != null ? o.getCustomer().getId() : null);
        m.put("productName", o.getProductName()); m.put("spec", o.getSpec());
        m.put("material", o.getMaterial()); m.put("qty", o.getQty());
        m.put("unit", o.getUnit()); m.put("boxType", o.getBoxType());
        m.put("fluteType", o.getFluteType()); m.put("singleArea", o.getSingleArea());
        m.put("unitPrice", o.getUnitPrice()); m.put("boxUnitPrice", o.getBoxUnitPrice());
        m.put("totalAmount", o.getTotalAmount()); m.put("status", o.getStatus());
        m.put("deliveryDate", o.getDeliveryDate()); m.put("notes", o.getNotes());
        m.put("createdAt", o.getCreatedAt());
        return m;
    }

    private void applyAmountCalculation(SalesOrder o) {
        double qty = o.getQty() != null ? o.getQty() : 0;
        double boxUnitPrice = o.getBoxUnitPrice() != null ? o.getBoxUnitPrice() : 0.0;
        o.setTotalAmount(round2(qty * boxUnitPrice));
    }

    private double round2(double value) {
        return Math.round(value * 100.0) / 100.0;
    }

    private LocalDate toCreatedDate(LocalDateTime createdAt) {
        return createdAt != null ? createdAt.toLocalDate() : null;
    }
}
