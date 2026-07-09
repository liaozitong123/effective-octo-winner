package com.cartonerp.controller;

import com.cartonerp.common.Result;
import com.cartonerp.entity.Inventory;
import com.cartonerp.repository.InventoryRepository;
import jakarta.persistence.criteria.Predicate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.*;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.web.bind.annotation.*;
import java.util.*;

@RestController
@RequestMapping("/api/inventory")
public class InventoryController {
    @Autowired private InventoryRepository repo;

    @GetMapping
    public Result<List<Inventory>> list(@RequestParam(defaultValue = "") String q,
                                         @RequestParam(defaultValue = "1") int page,
                                         @RequestParam(defaultValue = "20") int perPage) {
        Specification<Inventory> spec = (root, query, cb) -> {
            if (q.isEmpty()) return null;
            String p = "%" + q + "%";
            return cb.or(cb.like(root.get("itemName"), p), cb.like(root.get("itemType"), p), cb.like(root.get("spec"), p));
        };
        Page<Inventory> pg = repo.findAll(spec, PageRequest.of(page - 1, perPage, Sort.by(Sort.Direction.DESC, "id")));
        return Result.okWithTotal(pg.getContent(), pg.getTotalElements());
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
}
