package com.cartonerp.controller;

import com.cartonerp.common.Result;
import com.cartonerp.entity.ProductionOrder;
import com.cartonerp.entity.ProductionRecord;
import com.cartonerp.repository.ProductionOrderRepository;
import com.cartonerp.repository.ProductionRecordRepository;
import com.cartonerp.service.BusinessService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.*;
import java.util.ArrayList;
import java.util.List;

@RestController
@RequestMapping("/api/public")
public class PublicApiController {

    @Autowired private ProductionOrderRepository productionOrderRepo;
    @Autowired private ProductionRecordRepository productionRecordRepo;
    @Autowired private BusinessService businessService;

    /**
     * Get all active production orders for scan selection (public)
     */
    @GetMapping("/production-orders")
    public Result<List<Map<String, Object>>> getActiveOrders() {
        List<Map<String, Object>> list = new ArrayList<>();
        for (ProductionOrder po : productionOrderRepo.findAll()) {
            long currentOutput = productionRecordRepo.findByProductionOrderId(po.getId()).stream()
                .mapToLong(r -> r.getOutputQty() != null ? r.getOutputQty() : 0)
                .max().orElse(0);
            int qty = po.getQty() != null ? po.getQty() : 0;
            Map<String, Object> m = new LinkedHashMap<>();
            m.put("id", po.getId());
            m.put("orderNo", po.getOrderNo());
            m.put("productName", po.getProductName());
            m.put("spec", po.getSpec());
            m.put("qty", qty);
            m.put("status", po.getStatus());
            m.put("progressPct", qty > 0 ? Math.round(currentOutput * 1000.0 / qty) / 10.0 : 0);
            list.add(m);
        }
        return Result.ok(list);
    }

    /**
     * Get production order info (public, no login needed for QR scan)
     */
    @GetMapping("/production-order/{id}")
    public Result<Map<String, Object>> getOrderInfo(@PathVariable Long id) {
        ProductionOrder po = productionOrderRepo.findById(id).orElse(null);
        if (po == null) return Result.fail(404, "生产单不存在");
        long currentOutput = productionRecordRepo.findByProductionOrderId(id).stream()
            .mapToLong(r -> r.getOutputQty() != null ? r.getOutputQty() : 0).max().orElse(0);
        long totalWaste = productionRecordRepo.findByProductionOrderId(id).stream()
            .mapToLong(r -> r.getWasteQty() != null ? r.getWasteQty() : 0).sum();
        Map<String, Object> m = new LinkedHashMap<>();
        m.put("id", po.getId());
        m.put("orderNo", po.getOrderNo());
        m.put("productName", po.getProductName());
        m.put("spec", po.getSpec());
        m.put("qty", po.getQty());
        m.put("totalOutput", currentOutput);
        m.put("totalWaste", totalWaste);
        m.put("status", po.getStatus());
        return Result.ok(m);
    }

    /**
     * Worker submits work report via QR scan (public, no login needed)
     */
    @PostMapping("/report")
    public Result<Map<String, Object>> report(@RequestBody Map<String, Object> body) {
        Long productionOrderId = Long.valueOf(body.get("productionOrderId").toString());
        ProductionOrder po = productionOrderRepo.findById(productionOrderId).orElse(null);
        if (po == null) return Result.fail(404, "生产单不存在");

        ProductionRecord record = new ProductionRecord();
        record.setProductionOrder(po);
        record.setOutputQty(body.get("outputQty") != null ? Integer.valueOf(body.get("outputQty").toString()) : 0);
        record.setWasteQty(body.get("wasteQty") != null ? Integer.valueOf(body.get("wasteQty").toString()) : 0);
        record.setOperator(body.get("operator") != null ? body.get("operator").toString() : "匿名");
        record.setShift(body.get("workshop") != null ? body.get("workshop").toString() : "");
        record.setRecordDate(LocalDate.now());
        ProductionRecord saved = productionRecordRepo.save(record);

        // Auto-update production order progress
        try {
            businessService.onProductionRecordAdded(saved);
        } catch (RuntimeException e) {
            return Result.fail(400, e.getMessage());
        }

        Map<String, Object> result = new LinkedHashMap<>();
        result.put("id", saved.getId());
        result.put("outputQty", saved.getOutputQty());
        result.put("message", "报工成功");
        return Result.ok(result);
    }
}
