package com.cartonerp.controller;

import com.cartonerp.common.Result;
import com.cartonerp.entity.ProductionOrder;
import com.cartonerp.entity.ProductionRecord;
import com.cartonerp.repository.*;
import com.cartonerp.util.OrderNumberUtil;
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

    @GetMapping
    public Result<List<Map<String, Object>>> list(@RequestParam(defaultValue = "") String q,
                                                   @RequestParam(defaultValue = "1") int page,
                                                   @RequestParam(defaultValue = "20") int perPage) {
        Specification<ProductionOrder> spec = (root, query, cb) -> {
            if (q.isEmpty()) return null;
            String p = "%" + q + "%";
            return cb.or(cb.like(root.get("orderNo"), p), cb.like(root.get("productName"), p));
        };
        Page<ProductionOrder> pg = repo.findAll(spec, PageRequest.of(page - 1, perPage, Sort.by(Sort.Direction.DESC, "id")));
        return Result.okWithTotal(pg.getContent().stream().map(this::toMap).toList(), pg.getTotalElements());
    }

    @GetMapping("/{id}")
    public Result<Map<String, Object>> get(@PathVariable Long id) {
        return repo.findById(id).map(o -> Result.ok(toMap(o))).orElse(Result.fail(404, "不存在"));
    }

    @GetMapping("/{id}/progress")
    public Result<Map<String, Object>> progress(@PathVariable Long id) {
        ProductionOrder po = repo.findById(id).orElse(null);
        if (po == null) return Result.fail(404, "不存在");
        List<ProductionRecord> records = recordRepo.findByProductionOrderId(id);
        long currentOutput = records.stream().mapToLong(r -> r.getOutputQty() != null ? r.getOutputQty() : 0).max().orElse(0);
        long totalWaste = records.stream().mapToLong(r -> r.getWasteQty() != null ? r.getWasteQty() : 0).sum();
        double pct = po.getQty() > 0 ? Math.round(currentOutput * 1000.0 / po.getQty()) / 10.0 : 0;
        Map<String, Object> m = new LinkedHashMap<>();
        m.put("productionOrderNo", po.getOrderNo());
        m.put("productName", po.getProductName());
        m.put("plannedQty", po.getQty());
        m.put("totalOutput", currentOutput);
        m.put("totalWaste", totalWaste);
        m.put("remaining", (po.getQty() != null ? po.getQty() : 0) - currentOutput);
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
        return Result.ok(toMap(repo.save(o)), "创建成功");
    }

    @PutMapping("/{id}")
    public Result<Map<String, Object>> update(@PathVariable Long id, @RequestBody ProductionOrder o) {
        ProductionOrder ex = repo.findById(id).orElse(null);
        if (ex == null) return Result.fail(404, "不存在");
        if (o.getOrderNo() != null) ex.setOrderNo(o.getOrderNo());
        if (o.getSalesOrder() != null && o.getSalesOrder().getId() != null)
            salesOrderRepo.findById(o.getSalesOrder().getId()).ifPresent(ex::setSalesOrder);
        if (o.getProductName() != null) ex.setProductName(o.getProductName());
        if (o.getSpec() != null) ex.setSpec(o.getSpec());
        if (o.getQty() != null) ex.setQty(o.getQty());
        if (o.getMaterial() != null) ex.setMaterial(o.getMaterial());
        if (o.getBoxType() != null) ex.setBoxType(o.getBoxType());
        if (o.getUnitPrice() != null) ex.setUnitPrice(o.getUnitPrice());
        if (o.getProductionMaterial() != null) ex.setProductionMaterial(o.getProductionMaterial());
        if (o.getFluteType() != null) ex.setFluteType(o.getFluteType());
        if (o.getBoardLength() != null) ex.setBoardLength(o.getBoardLength());
        if (o.getBoardWidth() != null) ex.setBoardWidth(o.getBoardWidth());
        if (o.getBoardQty() != null) ex.setBoardQty(o.getBoardQty());
        if (o.getCutCount() != null) ex.setCutCount(o.getCutCount());
        if (o.getCrease() != null) ex.setCrease(o.getCrease());
        if (o.getBoardArea() != null) ex.setBoardArea(o.getBoardArea());
        if (o.getTotalArea() != null) ex.setTotalArea(o.getTotalArea());
        if (o.getMaterialBasePrice() != null) ex.setMaterialBasePrice(o.getMaterialBasePrice());
        if (o.getDiscountRate() != null) ex.setDiscountRate(o.getDiscountRate());
        if (o.getBoardUnitPrice() != null) ex.setBoardUnitPrice(o.getBoardUnitPrice());
        if (o.getProfitRate() != null) ex.setProfitRate(o.getProfitRate());
        if (o.getBoardAmount() != null) ex.setBoardAmount(o.getBoardAmount());
        if (o.getSignDate() != null) ex.setSignDate(o.getSignDate());
        if (o.getActualQty() != null) ex.setActualQty(o.getActualQty());
        if (o.getActualAmount() != null) ex.setActualAmount(o.getActualAmount());
        if (o.getSpecNotes() != null) ex.setSpecNotes(o.getSpecNotes());
        if (o.getUrgent() != null) ex.setUrgent(o.getUrgent());
        if (o.getOrderArea() != null) ex.setOrderArea(o.getOrderArea());
        if (o.getWorkshop() != null) ex.setWorkshop(o.getWorkshop());
        if (o.getNotes() != null) ex.setNotes(o.getNotes());
        return Result.ok(toMap(repo.save(ex)), "更新成功");
    }

    @DeleteMapping("/{id}")
    public Result<?> delete(@PathVariable Long id) {
        try {
            // Delete child records first
            List<ProductionRecord> records = recordRepo.findByProductionOrderId(id);
            recordRepo.deleteAll(records);
            repo.deleteById(id);
            return Result.ok(null, "删除成功");
        } catch (Exception e) {
            return Result.fail(400, "删除失败：请先删除关联数据");
        }
    }

    private Map<String, Object> toMap(ProductionOrder o) {
        Map<String, Object> m = new LinkedHashMap<>();
        m.put("id", o.getId()); m.put("orderNo", o.getOrderNo());
        m.put("salesOrderNo", o.getSalesOrder() != null ? o.getSalesOrder().getOrderNo() : "");
        m.put("customerName", o.getCustomer() != null ? o.getCustomer().getName() : "");
        m.put("productName", o.getProductName()); m.put("spec", o.getSpec());
        m.put("material", o.getMaterial()); m.put("boxType", o.getBoxType());
        m.put("unitPrice", o.getUnitPrice());
        m.put("supplierName", o.getSupplier() != null ? o.getSupplier().getName() : "");
        m.put("productionMaterial", o.getProductionMaterial()); m.put("fluteType", o.getFluteType());
        m.put("boardLength", o.getBoardLength()); m.put("boardWidth", o.getBoardWidth());
        m.put("boardQty", o.getBoardQty()); m.put("cutCount", o.getCutCount());
        m.put("crease", o.getCrease()); m.put("boardArea", o.getBoardArea());
        m.put("totalArea", o.getTotalArea()); m.put("materialBasePrice", o.getMaterialBasePrice());
        m.put("discountRate", o.getDiscountRate()); m.put("boardUnitPrice", o.getBoardUnitPrice());
        m.put("profitRate", o.getProfitRate()); m.put("boardAmount", o.getBoardAmount());
        m.put("signDate", o.getSignDate()); m.put("actualQty", o.getActualQty());
        m.put("actualAmount", o.getActualAmount());
        m.put("specNotes", o.getSpecNotes()); m.put("urgent", o.getUrgent());
        m.put("orderArea", o.getOrderArea());
        m.put("qty", o.getQty()); m.put("status", o.getStatus());
        m.put("startDate", o.getStartDate()); m.put("finishDate", o.getFinishDate());
        m.put("workshop", o.getWorkshop()); m.put("notes", o.getNotes()); m.put("createdAt", o.getCreatedAt());
        return m;
    }
}
