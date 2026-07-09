package com.cartonerp.controller;

import com.cartonerp.common.Result;
import com.cartonerp.entity.Customer;
import com.cartonerp.repository.CustomerRepository;
import com.cartonerp.repository.SalesOrderRepository;
import jakarta.persistence.criteria.Predicate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.web.bind.annotation.*;
import java.util.ArrayList;
import java.util.List;

@RestController
@RequestMapping("/api/customers")
public class CustomerController {
    @Autowired private CustomerRepository repo;
    @Autowired private SalesOrderRepository salesOrderRepo;

    @GetMapping
    public Result<List<Customer>> list(@RequestParam(defaultValue = "") String q,
                                       @RequestParam(defaultValue = "1") int page,
                                       @RequestParam(defaultValue = "20") int perPage) {
        Specification<Customer> spec = (root, query, cb) -> {
            if (q.isEmpty()) return null;
            String pattern = "%" + q + "%";
            List<Predicate> preds = new ArrayList<>();
            preds.add(cb.like(root.get("name"), pattern));
            preds.add(cb.like(root.get("contact"), pattern));
            preds.add(cb.like(root.get("phone"), pattern));
            return cb.or(preds.toArray(new Predicate[0]));
        };
        Page<Customer> pg = repo.findAll(spec, PageRequest.of(page - 1, perPage, Sort.by(Sort.Direction.DESC, "id")));
        return Result.okWithTotal(pg.getContent(), pg.getTotalElements());
    }

    @GetMapping("/{id}")
    public Result<Customer> get(@PathVariable Long id) {
        return repo.findById(id).map(Result::ok).orElse(Result.fail(404, "不存在"));
    }

    @PostMapping
    public Result<Customer> create(@RequestBody Customer c) {
        return Result.ok(repo.save(c), "创建成功");
    }

    @PutMapping("/{id}")
    public Result<Customer> update(@PathVariable Long id, @RequestBody Customer c) {
        Customer existing = repo.findById(id).orElse(null);
        if (existing == null) return Result.fail(404, "不存在");
        if (c.getName() != null) existing.setName(c.getName());
        if (c.getContact() != null) existing.setContact(c.getContact());
        if (c.getPhone() != null) existing.setPhone(c.getPhone());
        if (c.getEmail() != null) existing.setEmail(c.getEmail());
        if (c.getAddress() != null) existing.setAddress(c.getAddress());
        if (c.getNotes() != null) existing.setNotes(c.getNotes());
        return Result.ok(repo.save(existing), "更新成功");
    }

    @DeleteMapping("/{id}")
    public Result<?> delete(@PathVariable Long id) {
        try {
            salesOrderRepo.findAll().stream()
                .filter(s -> s.getCustomer() != null && s.getCustomer().getId().equals(id))
                .forEach(s -> salesOrderRepo.delete(s));
            repo.deleteById(id);
            return Result.ok(null, "删除成功");
        } catch (Exception e) {
            return Result.fail(400, "删除失败：" + e.getMessage());
        }
    }
}
