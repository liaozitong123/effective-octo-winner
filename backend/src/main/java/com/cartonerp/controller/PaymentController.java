package com.cartonerp.controller;

import com.cartonerp.common.Result;
import com.cartonerp.entity.Payment;
import com.cartonerp.repository.PaymentRepository;
import jakarta.persistence.criteria.Predicate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.*;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.web.bind.annotation.*;
import java.util.*;

@RestController
@RequestMapping("/api/payments")
public class PaymentController {
    @Autowired private PaymentRepository repo;

    @GetMapping
    public Result<List<Payment>> list(@RequestParam(defaultValue = "") String q,
                                       @RequestParam(defaultValue = "1") int page,
                                       @RequestParam(defaultValue = "20") int perPage) {
        Specification<Payment> spec = (root, query, cb) -> {
            if (q.isEmpty()) return null;
            String p = "%" + q + "%";
            return cb.or(cb.like(root.get("paymentNo"), p), cb.like(root.get("partyName"), p));
        };
        Page<Payment> pg = repo.findAll(spec, PageRequest.of(page - 1, perPage, Sort.by(Sort.Direction.DESC, "id")));
        return Result.okWithTotal(pg.getContent(), pg.getTotalElements());
    }

    @GetMapping("/{id}") public Result<Payment> get(@PathVariable Long id) { return repo.findById(id).map(Result::ok).orElse(Result.fail(404, "不存在")); }
    @PostMapping public Result<Payment> create(@RequestBody Payment p) { return Result.ok(repo.save(p), "创建成功"); }
    @PutMapping("/{id}")
    public Result<Payment> update(@PathVariable Long id, @RequestBody Payment p) {
        Payment ex = repo.findById(id).orElse(null);
        if (ex == null) return Result.fail(404, "不存在");
        if (p.getPaymentNo() != null) ex.setPaymentNo(p.getPaymentNo());
        if (p.getPaymentType() != null) ex.setPaymentType(p.getPaymentType());
        if (p.getPartyType() != null) ex.setPartyType(p.getPartyType());
        if (p.getPartyId() != null) ex.setPartyId(p.getPartyId());
        if (p.getPartyName() != null) ex.setPartyName(p.getPartyName());
        if (p.getAmount() != null) ex.setAmount(p.getAmount());
        if (p.getPaymentMethod() != null) ex.setPaymentMethod(p.getPaymentMethod());
        if (p.getPaymentDate() != null) ex.setPaymentDate(p.getPaymentDate());
        if (p.getNotes() != null) ex.setNotes(p.getNotes());
        return Result.ok(repo.save(ex), "更新成功");
    }
    @DeleteMapping("/{id}") public Result<?> delete(@PathVariable Long id) { repo.deleteById(id); return Result.ok(null, "删除成功"); }
}
