package com.cartonerp.controller;

import com.cartonerp.common.Result;
import com.cartonerp.entity.Reconciliation;
import com.cartonerp.repository.ReconciliationRepository;
import jakarta.persistence.criteria.Predicate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.*;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.web.bind.annotation.*;
import java.util.*;

@RestController
@RequestMapping("/api/reconciliations")
public class ReconciliationController {
    @Autowired private ReconciliationRepository repo;

    @GetMapping
    public Result<List<Reconciliation>> list(@RequestParam(defaultValue = "") String q,
                                               @RequestParam(defaultValue = "1") int page,
                                               @RequestParam(defaultValue = "20") int perPage) {
        Specification<Reconciliation> spec = (root, query, cb) -> {
            if (q.isEmpty()) return null;
            return cb.like(root.get("partyName"), "%" + q + "%");
        };
        Page<Reconciliation> pg = repo.findAll(spec, PageRequest.of(page - 1, perPage, Sort.by(Sort.Direction.DESC, "id")));
        return Result.okWithTotal(pg.getContent(), pg.getTotalElements());
    }

    @GetMapping("/{id}") public Result<Reconciliation> get(@PathVariable Long id) { return repo.findById(id).map(Result::ok).orElse(Result.fail(404, "不存在")); }
    @PostMapping public Result<Reconciliation> create(@RequestBody Reconciliation r) { return Result.ok(repo.save(r), "创建成功"); }
    @PutMapping("/{id}")
    public Result<Reconciliation> update(@PathVariable Long id, @RequestBody Reconciliation r) {
        Reconciliation ex = repo.findById(id).orElse(null);
        if (ex == null) return Result.fail(404, "不存在");
        if (r.getPartyType() != null) ex.setPartyType(r.getPartyType());
        if (r.getPartyId() != null) ex.setPartyId(r.getPartyId());
        if (r.getPartyName() != null) ex.setPartyName(r.getPartyName());
        if (r.getPeriodStart() != null) ex.setPeriodStart(r.getPeriodStart());
        if (r.getPeriodEnd() != null) ex.setPeriodEnd(r.getPeriodEnd());
        if (r.getBeginBalance() != null) ex.setBeginBalance(r.getBeginBalance());
        if (r.getCurrentAmount() != null) ex.setCurrentAmount(r.getCurrentAmount());
        if (r.getPaidAmount() != null) ex.setPaidAmount(r.getPaidAmount());
        if (r.getEndBalance() != null) ex.setEndBalance(r.getEndBalance());
        if (r.getStatus() != null) ex.setStatus(r.getStatus());
        return Result.ok(repo.save(ex), "更新成功");
    }
    @DeleteMapping("/{id}") public Result<?> delete(@PathVariable Long id) { repo.deleteById(id); return Result.ok(null, "删除成功"); }
}
