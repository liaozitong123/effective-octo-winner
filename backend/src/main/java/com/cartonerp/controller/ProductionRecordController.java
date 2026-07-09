package com.cartonerp.controller;

import com.cartonerp.common.Result;
import com.cartonerp.entity.ProductionRecord;
import com.cartonerp.repository.*;
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

    @GetMapping
    public Result<List<Map<String, Object>>> list(@RequestParam(defaultValue = "") String q,
                                                   @RequestParam(defaultValue = "1") int page,
                                                   @RequestParam(defaultValue = "20") int perPage) {
        Specification<ProductionRecord> spec = (root, query, cb) -> {
            if (q.isEmpty()) return null;
            String p = "%" + q + "%";
            return cb.or(cb.like(root.get("operator"), p));
        };
        Page<ProductionRecord> pg = repo.findAll(spec, PageRequest.of(page - 1, perPage, Sort.by(Sort.Direction.DESC, "id")));
        return Result.okWithTotal(pg.getContent().stream().map(this::toMap).collect(Collectors.toList()), pg.getTotalElements());
    }

    @PostMapping
    public Result<Map<String, Object>> create(@RequestBody ProductionRecord r) {
        if (r.getProductionOrder() != null && r.getProductionOrder().getId() != null)
            productionOrderRepo.findById(r.getProductionOrder().getId()).ifPresent(r::setProductionOrder);
        ProductionRecord saved = repo.save(r);
        businessService.onProductionRecordAdded(saved);
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
        if (r.getShift() != null) ex.setShift(r.getShift());
        if (r.getRecordDate() != null) ex.setRecordDate(r.getRecordDate());
        if (r.getNotes() != null) ex.setNotes(r.getNotes());
        ProductionRecord saved = repo.save(ex);
        businessService.onProductionRecordAdded(saved);
        return Result.ok(toMap(saved), "更新成功");
    }

    @GetMapping("/{id}")
    public Result<Map<String, Object>> get(@PathVariable Long id) {
        return repo.findById(id).map(r -> Result.ok(toMap(r))).orElse(Result.fail(404, "不存在"));
    }

    @DeleteMapping("/{id}") public Result<?> delete(@PathVariable Long id) { repo.deleteById(id); return Result.ok(null, "删除成功"); }

    private Map<String, Object> toMap(ProductionRecord r) {
        Map<String, Object> m = new LinkedHashMap<>();
        m.put("id", r.getId());
        m.put("productionOrderNo", r.getProductionOrder() != null ? r.getProductionOrder().getOrderNo() : "");
        m.put("productName", r.getProductionOrder() != null ? r.getProductionOrder().getProductName() : "");
        m.put("outputQty", r.getOutputQty()); m.put("wasteQty", r.getWasteQty());
        m.put("operator", r.getOperator()); m.put("shift", r.getShift());
        m.put("recordDate", r.getRecordDate()); m.put("notes", r.getNotes()); m.put("createdAt", r.getCreatedAt());
        return m;
    }
}
