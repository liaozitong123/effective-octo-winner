package com.cartonerp.controller;

import com.cartonerp.common.Result;
import com.cartonerp.entity.DeliveryNote;
import com.cartonerp.repository.*;
import jakarta.persistence.criteria.Predicate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.*;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.web.bind.annotation.*;
import java.util.*;

@RestController
@RequestMapping("/api/delivery-notes")
public class DeliveryNoteController {
    @Autowired private DeliveryNoteRepository repo;
    @Autowired private SalesOrderRepository salesOrderRepo;
    @Autowired private CustomerRepository customerRepo;
    @Autowired private com.cartonerp.service.BusinessService businessService;

    @GetMapping
    public Result<List<Map<String, Object>>> list(@RequestParam(defaultValue = "") String q,
                                                   @RequestParam(defaultValue = "1") int page,
                                                   @RequestParam(defaultValue = "20") int perPage) {
        Specification<DeliveryNote> spec = (root, query, cb) -> {
            if (q.isEmpty()) return null;
            return cb.like(root.get("noteNo"), "%" + q + "%");
        };
        Page<DeliveryNote> pg = repo.findAll(spec, PageRequest.of(page - 1, perPage, Sort.by(Sort.Direction.DESC, "id")));
        return Result.okWithTotal(pg.getContent().stream().map(this::toMap).toList(), pg.getTotalElements());
    }

    @PostMapping
    public Result<Map<String, Object>> create(@RequestBody DeliveryNote d) {
        if (d.getSalesOrder() != null && d.getSalesOrder().getId() != null)
            salesOrderRepo.findById(d.getSalesOrder().getId()).ifPresent(d::setSalesOrder);
        if (d.getCustomer() != null && d.getCustomer().getId() != null)
            customerRepo.findById(d.getCustomer().getId()).ifPresent(d::setCustomer);
        DeliveryNote saved = repo.save(d);
        businessService.onDeliveryNoteCreated(saved);
        return Result.ok(toMap(saved), "创建成功");
    }

    @GetMapping("/{id}")
    public Result<Map<String, Object>> get(@PathVariable Long id) {
        return repo.findById(id).map(d -> Result.ok(toMap(d))).orElse(Result.fail(404, "不存在"));
    }

    @PutMapping("/{id}")
    public Result<Map<String, Object>> update(@PathVariable Long id, @RequestBody DeliveryNote d) {
        DeliveryNote ex = repo.findById(id).orElse(null);
        if (ex == null) return Result.fail(404, "不存在");
        if (d.getNoteNo() != null) ex.setNoteNo(d.getNoteNo());
        if (d.getSalesOrder() != null && d.getSalesOrder().getId() != null) salesOrderRepo.findById(d.getSalesOrder().getId()).ifPresent(ex::setSalesOrder);
        if (d.getCustomer() != null && d.getCustomer().getId() != null) customerRepo.findById(d.getCustomer().getId()).ifPresent(ex::setCustomer);
        if (d.getQty() != null) ex.setQty(d.getQty());
        if (d.getDeliveryDate() != null) ex.setDeliveryDate(d.getDeliveryDate());
        if (d.getStatus() != null) ex.setStatus(d.getStatus());
        if (d.getCarrier() != null) ex.setCarrier(d.getCarrier());
        if (d.getTrackingNo() != null) ex.setTrackingNo(d.getTrackingNo());
        if (d.getNotes() != null) ex.setNotes(d.getNotes());
        DeliveryNote saved = repo.save(ex);
        businessService.onDeliveryNoteCreated(saved);
        return Result.ok(toMap(saved), "更新成功");
    }

    @DeleteMapping("/{id}") public Result<?> delete(@PathVariable Long id) { repo.deleteById(id); return Result.ok(null, "删除成功"); }

    private Map<String, Object> toMap(DeliveryNote d) {
        Map<String, Object> m = new LinkedHashMap<>();
        m.put("id", d.getId()); m.put("noteNo", d.getNoteNo());
        m.put("customerName", d.getCustomer() != null ? d.getCustomer().getName() : "");
        m.put("salesOrderNo", d.getSalesOrder() != null ? d.getSalesOrder().getOrderNo() : "");
        m.put("qty", d.getQty()); m.put("deliveryDate", d.getDeliveryDate());
        m.put("status", d.getStatus()); m.put("carrier", d.getCarrier());
        m.put("trackingNo", d.getTrackingNo()); m.put("notes", d.getNotes()); m.put("createdAt", d.getCreatedAt());
        return m;
    }
}
