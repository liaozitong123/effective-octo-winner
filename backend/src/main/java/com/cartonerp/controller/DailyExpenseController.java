package com.cartonerp.controller;

import com.cartonerp.common.Result;
import com.cartonerp.entity.DailyExpense;
import com.cartonerp.repository.DailyExpenseRepository;
import jakarta.persistence.criteria.Predicate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.time.YearMonth;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/daily-expenses")
public class DailyExpenseController {
    @Autowired private DailyExpenseRepository repo;

    @GetMapping
    public Result<List<DailyExpense>> list(@RequestParam(defaultValue = "") String q,
                                           @RequestParam(defaultValue = "") String month,
                                           @RequestParam(defaultValue = "1") int page,
                                           @RequestParam(defaultValue = "20") int perPage) {
        Specification<DailyExpense> spec = (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();
            if (q != null && !q.isBlank()) {
                String p = "%" + q.trim() + "%";
                predicates.add(cb.or(
                    cb.like(root.get("category"), p),
                    cb.like(root.get("details"), p),
                    cb.like(root.get("handler"), p),
                    cb.like(root.get("notes"), p)
                ));
            }
            if (isMonth(month)) {
                predicates.add(cb.equal(root.get("recordMonth"), month.trim()));
            }
            return predicates.isEmpty() ? null : cb.and(predicates.toArray(new Predicate[0]));
        };
        Page<DailyExpense> pg = repo.findAll(spec, PageRequest.of(Math.max(page - 1, 0), Math.max(perPage, 1), Sort.by(Sort.Direction.DESC, "id")));
        return Result.okWithTotal(pg.getContent(), pg.getTotalElements());
    }

    @GetMapping("/{id}")
    public Result<DailyExpense> get(@PathVariable Long id) {
        return repo.findById(id).map(Result::ok).orElse(Result.fail(404, "not found"));
    }

    @PostMapping
    public Result<DailyExpense> create(@RequestBody Map<String, Object> body) {
        DailyExpense record = new DailyExpense();
        apply(record, body);
        if (!isMonth(record.getRecordMonth())) {
            record.setRecordMonth(YearMonth.now().toString());
        }
        return Result.ok(repo.save(record), "created");
    }

    @PutMapping("/{id}")
    public Result<DailyExpense> update(@PathVariable Long id, @RequestBody Map<String, Object> body) {
        DailyExpense record = repo.findById(id).orElse(null);
        if (record == null) return Result.fail(404, "not found");
        apply(record, body);
        if (!isMonth(record.getRecordMonth())) {
            record.setRecordMonth(YearMonth.now().toString());
        }
        return Result.ok(repo.save(record), "updated");
    }

    @DeleteMapping("/{id}")
    public Result<?> delete(@PathVariable Long id) {
        repo.deleteById(id);
        return Result.ok(null, "deleted");
    }

    private void apply(DailyExpense record, Map<String, Object> body) {
        if (body.containsKey("recordMonth")) record.setRecordMonth(readString(body.get("recordMonth")));
        if (body.containsKey("expenseDate")) record.setExpenseDate(readDate(body.get("expenseDate")));
        if (body.containsKey("category")) record.setCategory(readString(body.get("category")));
        if (body.containsKey("details")) record.setDetails(readString(body.get("details")));
        if (body.containsKey("quantity")) record.setQuantity(readDouble(body.get("quantity")));
        if (body.containsKey("unitPrice")) record.setUnitPrice(readDouble(body.get("unitPrice")));
        if (body.containsKey("handler")) record.setHandler(readString(body.get("handler")));
        if (body.containsKey("notes")) record.setNotes(readString(body.get("notes")));
        if (body.containsKey("settlementDate")) record.setSettlementDate(readDate(body.get("settlementDate")));
        record.setAmount(round2(safe(record.getQuantity()) * safe(record.getUnitPrice())));
    }

    private boolean isMonth(String value) {
        return value != null && value.matches("\\d{4}-\\d{2}");
    }

    private String readString(Object value) {
        return value == null ? "" : String.valueOf(value).trim();
    }

    private Double readDouble(Object value) {
        if (value == null || String.valueOf(value).isBlank()) return 0.0;
        if (value instanceof Number number) return number.doubleValue();
        try {
            return Double.parseDouble(String.valueOf(value).trim());
        } catch (NumberFormatException e) {
            return 0.0;
        }
    }

    private LocalDate readDate(Object value) {
        if (value == null || String.valueOf(value).isBlank()) return null;
        try {
            return LocalDate.parse(String.valueOf(value).trim());
        } catch (RuntimeException e) {
            return null;
        }
    }

    private double safe(Double value) {
        return value == null ? 0.0 : value;
    }

    private double round2(double value) {
        return Math.round(value * 100.0) / 100.0;
    }
}
