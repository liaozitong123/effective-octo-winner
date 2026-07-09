package com.cartonerp.controller;

import com.cartonerp.common.Result;
import com.cartonerp.entity.ProductionOrder;
import com.cartonerp.entity.PurchaseOrder;
import com.cartonerp.entity.SalesOrder;
import com.cartonerp.repository.CustomerRepository;
import com.cartonerp.repository.DeliveryNoteRepository;
import com.cartonerp.repository.ProductionOrderRepository;
import com.cartonerp.repository.PurchaseOrderRepository;
import com.cartonerp.repository.SalesOrderRepository;
import jakarta.persistence.criteria.Predicate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.web.bind.annotation.*;
import java.util.*;

@RestController
@RequestMapping("/api/sales-orders")
public class SalesOrderController {
    @Autowired private SalesOrderRepository repo;
    @Autowired private CustomerRepository customerRepo;
    @Autowired private ProductionOrderRepository productionOrderRepo;
    @Autowired private DeliveryNoteRepository deliveryNoteRepo;
    @Autowired private PurchaseOrderRepository purchaseOrderRepo;

    @GetMapping
    public Result<List<Map<String, Object>>> list(@RequestParam(defaultValue = "") String q,
                                                   @RequestParam(defaultValue = "1") int page,
                                                   @RequestParam(defaultValue = "20") int perPage) {
        Specification<SalesOrder> spec = (root, query, cb) -> {
            if (q.isEmpty()) return null;
            String p = "%" + q + "%";
            return cb.or(cb.like(root.get("orderNo"), p), cb.like(root.get("productName"), p));
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
        // Auto-generate order number: SO-YYYYMMDDHHmm
        o.setOrderNo("SO-" + java.time.LocalDateTime.now().format(java.time.format.DateTimeFormatter.ofPattern("yyyyMMddHHmm")));
        SalesOrder saved = repo.save(o);

        // Auto-create purchase order
        PurchaseOrder puo = new PurchaseOrder();
        puo.setOrderNo(saved.getOrderNo());
        puo.setProductName(saved.getProductName());
        puo.setSpec(saved.getSpec());
        puo.setMaterial(saved.getMaterial());
        puo.setBoxType(saved.getBoxType());
        puo.setFluteType(saved.getFluteType());
        puo.setQty(saved.getQty());
        puo.setUnitPrice(saved.getUnitPrice());
        puo.setMaterialType("纸板");
        puo.setMaterialName(saved.getProductName());
        if (saved.getCustomer() != null) puo.setCustomer(saved.getCustomer());
        puo.setStatus("待收货");
        PurchaseOrder savedPuo = purchaseOrderRepo.save(puo);

        // Auto-create production order from purchase order
        ProductionOrder po = new ProductionOrder();
        po.setOrderNo(savedPuo.getOrderNo());
        po.setProductName(savedPuo.getProductName());
        po.setCustomer(saved.getCustomer());
        po.setSupplier(savedPuo.getSupplier());
        po.setQty(savedPuo.getQty());
        po.setUnit(savedPuo.getUnit() != null ? savedPuo.getUnit() : "个");
        po.setProductionMaterial(savedPuo.getProductionMaterial());
        po.setFluteType(savedPuo.getFluteType());
        po.setBoardLength(savedPuo.getBoardLength());
        po.setBoardWidth(savedPuo.getBoardWidth());
        po.setBoardQty(savedPuo.getBoardQty());
        po.setCutCount(savedPuo.getCutCount());
        po.setCrease(savedPuo.getCrease());
        po.setStatus("待排产");
        productionOrderRepo.save(po);

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
        SalesOrder updated = repo.save(ex);

        // Sync linked production orders
        productionOrderRepo.findAll().stream()
            .filter(po -> po.getSalesOrder() != null && po.getSalesOrder().getId().equals(id))
            .forEach(po -> {
                po.setProductName(updated.getProductName());
                po.setSpec(updated.getSpec());
                po.setMaterial(updated.getMaterial());
                po.setBoxType(updated.getBoxType());
                po.setFluteType(updated.getFluteType());
                po.setUnitPrice(updated.getUnitPrice());
                po.setQty(updated.getQty());
                productionOrderRepo.save(po);
            });

        return Result.ok(toMap(updated), "更新成功");
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
}
