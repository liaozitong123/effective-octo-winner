package com.cartonerp.controller;

import com.cartonerp.common.Result;
import com.cartonerp.entity.SalaryRecord;
import com.cartonerp.repository.SalaryRecordRepository;
import jakarta.persistence.criteria.Predicate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/salary-records")
public class SalaryRecordController {
    @Autowired private SalaryRecordRepository repo;

    @GetMapping
    public Result<List<SalaryRecord>> list(@RequestParam(defaultValue = "") String q,
                                           @RequestParam(defaultValue = "") String month,
                                           @RequestParam(defaultValue = "1") int page,
                                           @RequestParam(defaultValue = "20") int perPage) {
        Specification<SalaryRecord> spec = (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();
            if (q != null && !q.isBlank()) {
                String p = "%" + q.trim() + "%";
                predicates.add(cb.or(
                    cb.like(root.get("employeeName"), p),
                    cb.like(root.get("position"), p),
                    cb.like(root.get("notes"), p)
                ));
            }
            if (month != null && month.matches("\\d{4}-\\d{2}")) {
                LocalDate start = LocalDate.parse(month + "-01");
                LocalDate end = start.plusMonths(1);
                predicates.add(cb.greaterThanOrEqualTo(root.get("paymentDate"), start));
                predicates.add(cb.lessThan(root.get("paymentDate"), end));
            }
            return predicates.isEmpty() ? null : cb.and(predicates.toArray(new Predicate[0]));
        };
        Page<SalaryRecord> pg = repo.findAll(spec, PageRequest.of(Math.max(page - 1, 0), Math.max(perPage, 1), Sort.by(Sort.Direction.DESC, "id")));
        return Result.okWithTotal(pg.getContent(), pg.getTotalElements());
    }

    @GetMapping("/{id}")
    public Result<SalaryRecord> get(@PathVariable Long id) {
        return repo.findById(id).map(Result::ok).orElse(Result.fail(404, "不存在"));
    }

    @PostMapping
    public Result<SalaryRecord> create(@RequestBody Map<String, Object> body) {
        SalaryRecord record = new SalaryRecord();
        apply(record, body);
        return Result.ok(repo.save(record), "创建成功");
    }

    @PutMapping("/{id}")
    public Result<SalaryRecord> update(@PathVariable Long id, @RequestBody Map<String, Object> body) {
        SalaryRecord record = repo.findById(id).orElse(null);
        if (record == null) return Result.fail(404, "不存在");
        apply(record, body);
        return Result.ok(repo.save(record), "更新成功");
    }

    @DeleteMapping("/{id}")
    public Result<?> delete(@PathVariable Long id) {
        repo.deleteById(id);
        return Result.ok(null, "删除成功");
    }

    private void apply(SalaryRecord record, Map<String, Object> body) {
        if (body.containsKey("employeeName")) record.setEmployeeName(readString(body.get("employeeName")));
        if (body.containsKey("position")) record.setPosition(readString(body.get("position")));
        if (body.containsKey("attendanceShifts")) record.setAttendanceShifts(readDouble(body.get("attendanceShifts")));
        if (body.containsKey("baseSalary")) record.setBaseSalary(readDouble(body.get("baseSalary")));
        if (body.containsKey("overtimeSalary")) record.setOvertimeSalary(readDouble(body.get("overtimeSalary")));
        if (body.containsKey("allowance")) record.setAllowance(readDouble(body.get("allowance")));
        if (body.containsKey("socialSecuritySubsidy")) record.setSocialSecuritySubsidy(readDouble(body.get("socialSecuritySubsidy")));
        if (body.containsKey("payableSalary")) record.setPayableSalary(readDouble(body.get("payableSalary")));
        if (body.containsKey("socialSecurityWithheld")) record.setSocialSecurityWithheld(readDouble(body.get("socialSecurityWithheld")));
        if (body.containsKey("netSalary")) record.setNetSalary(readDouble(body.get("netSalary")));
        if (body.containsKey("paymentDate")) record.setPaymentDate(readDate(body.get("paymentDate")));
        if (body.containsKey("notes")) record.setNotes(readString(body.get("notes")));
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
}
