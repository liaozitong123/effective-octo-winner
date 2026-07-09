package com.cartonerp.controller;

import com.cartonerp.common.Result;
import com.cartonerp.entity.Supplier;
import com.cartonerp.repository.SupplierRepository;
import jakarta.persistence.criteria.Predicate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.*;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.web.bind.annotation.*;
import java.util.*;

@RestController
@RequestMapping("/api/suppliers")
public class SupplierController {
    @Autowired private SupplierRepository repo;

    @GetMapping
    public Result<List<Supplier>> list(@RequestParam(defaultValue = "") String q,
                                       @RequestParam(defaultValue = "1") int page,
                                       @RequestParam(defaultValue = "20") int perPage) {
        Specification<Supplier> spec = (root, query, cb) -> {
            if (q.isEmpty()) return null;
            String p = "%" + q + "%";
            return cb.or(cb.like(root.get("name"), p), cb.like(root.get("contact"), p), cb.like(root.get("phone"), p));
        };
        Page<Supplier> pg = repo.findAll(spec, PageRequest.of(page - 1, perPage, Sort.by(Sort.Direction.DESC, "id")));
        return Result.okWithTotal(pg.getContent(), pg.getTotalElements());
    }

    @GetMapping("/{id}")
    public Result<Supplier> get(@PathVariable Long id) {
        return repo.findById(id).map(Result::ok).orElse(Result.fail(404, "不存在"));
    }

    @PostMapping public Result<Supplier> create(@RequestBody Supplier s) { return Result.ok(repo.save(s), "创建成功"); }

    @PutMapping("/{id}")
    public Result<Supplier> update(@PathVariable Long id, @RequestBody Supplier s) {
        Supplier ex = repo.findById(id).orElse(null);
        if (ex == null) return Result.fail(404, "不存在");
        if (s.getName() != null) ex.setName(s.getName());
        if (s.getContact() != null) ex.setContact(s.getContact());
        if (s.getPhone() != null) ex.setPhone(s.getPhone());
        if (s.getEmail() != null) ex.setEmail(s.getEmail());
        if (s.getAddress() != null) ex.setAddress(s.getAddress());
        if (s.getSupplyType() != null) ex.setSupplyType(s.getSupplyType());
        if (s.getNotes() != null) ex.setNotes(s.getNotes());
        return Result.ok(repo.save(ex), "更新成功");
    }

    @DeleteMapping("/{id}") public Result<?> delete(@PathVariable Long id) { repo.deleteById(id); return Result.ok(null, "删除成功"); }
}
