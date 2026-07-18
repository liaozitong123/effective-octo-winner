package com.cartonerp.controller;

import com.cartonerp.common.Result;
import com.cartonerp.entity.ProductionOrder;
import com.cartonerp.entity.PurchaseOrder;
import com.cartonerp.repository.*;
import com.cartonerp.util.BoardCalculationUtil;
import com.cartonerp.util.OrderNumberUtil;
import jakarta.persistence.criteria.Predicate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.*;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.web.bind.annotation.*;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;

@RestController
@RequestMapping("/api/purchase-orders")
public class PurchaseOrderController {
    @Autowired private PurchaseOrderRepository repo;
    @Autowired private SupplierRepository supplierRepo;
    @Autowired private ProductionOrderRepository productionOrderRepo;
    @Autowired private com.cartonerp.service.BusinessService businessService;

    @GetMapping
    public Result<List<Map<String, Object>>> list(@RequestParam(defaultValue = "") String q,
                                                   @RequestParam(defaultValue = "1") int page,
                                                   @RequestParam(defaultValue = "20") int perPage) {
        Specification<PurchaseOrder> spec = (root, query, cb) -> {
            if (q.isEmpty()) return null;
            String p = "%" + q + "%";
            return cb.or(cb.like(root.get("orderNo"), p), cb.like(root.get("materialName"), p));
        };
        Page<PurchaseOrder> pg = repo.findAll(spec, PageRequest.of(page - 1, perPage, Sort.by(Sort.Direction.DESC, "id")));
        return Result.okWithTotal(pg.getContent().stream().map(this::toMap).toList(), pg.getTotalElements());
    }

    @GetMapping("/{id}")
    public Result<Map<String, Object>> get(@PathVariable Long id) {
        return repo.findById(id).map(o -> Result.ok(toMap(o))).orElse(Result.fail(404, "不存在"));
    }

    @PostMapping
    public Result<Map<String, Object>> create(@RequestBody PurchaseOrder o) {
        if (o.getSupplier() != null && o.getSupplier().getId() != null)
            supplierRepo.findById(o.getSupplier().getId()).ifPresent(o::setSupplier);
        // Auto-generate order number: PO-YYYYMMDDHHmmss
        o.setOrderNo(OrderNumberUtil.next("PO"));
        applyBoardCalculation(o);
        PurchaseOrder saved = repo.save(o);
        businessService.onPurchaseReceived(saved);

        // Auto-create production order
        ProductionOrder po = new ProductionOrder();
        po.setOrderNo(OrderNumberUtil.next("PRD"));
        po.setProductName(saved.getProductName() != null ? saved.getProductName() : saved.getMaterialName());
        po.setSpec(saved.getSpec());
        po.setMaterial(saved.getMaterial());
        po.setBoxType(saved.getBoxType());
        po.setStitchType(saved.getStitchType());
        po.setSupplier(saved.getSupplier());
        po.setQty(saved.getQty());
        po.setUnit(saved.getUnit() != null ? saved.getUnit() : "个");
        po.setProductionMaterial(saved.getProductionMaterial());
        po.setFluteType(saved.getFluteType());
        po.setBoardLength(saved.getBoardLength());
        po.setBoardWidth(saved.getBoardWidth());
        po.setBoardQty(saved.getBoardQty());
        po.setCutCount(saved.getCutCount());
        po.setCrease(saved.getCrease());
        po.setBoardArea(saved.getBoardArea());
        po.setTotalArea(saved.getTotalArea());
        po.setMaterialBasePrice(saved.getMaterialBasePrice());
        po.setDiscountRate(saved.getDiscountRate());
        po.setBoardUnitPrice(saved.getBoardUnitPrice());
        po.setProfitRate(saved.getProfitRate());
        po.setBoardAmount(saved.getBoardAmount());
        po.setSignDate(saved.getSignDate());
        po.setActualQty(saved.getActualQty());
        po.setActualAmount(saved.getActualAmount());
        po.setStatus("待排产");
        productionOrderRepo.save(po);

        return Result.ok(toMap(saved), "创建成功");
    }

    @PutMapping("/{id}")
    public Result<Map<String, Object>> update(@PathVariable Long id, @RequestBody PurchaseOrder o) {
        PurchaseOrder ex = repo.findById(id).orElse(null);
        if (ex == null) return Result.fail(404, "不存在");
        if (o.getOrderNo() != null) ex.setOrderNo(o.getOrderNo());
        if (o.getSupplier() != null && o.getSupplier().getId() != null)
            supplierRepo.findById(o.getSupplier().getId()).ifPresent(ex::setSupplier);
        if (o.getMaterialType() != null) ex.setMaterialType(o.getMaterialType());
        if (o.getMaterialName() != null) ex.setMaterialName(o.getMaterialName());
        if (o.getSpec() != null) ex.setSpec(o.getSpec());
        if (o.getQty() != null) ex.setQty(o.getQty());
        if (o.getUnit() != null) ex.setUnit(o.getUnit());
        if (o.getUnitPrice() != null) ex.setUnitPrice(o.getUnitPrice());
        if (o.getTotalAmount() != null) ex.setTotalAmount(o.getTotalAmount());
        if (o.getStatus() != null) ex.setStatus(o.getStatus());
        if (o.getOrderDate() != null) ex.setOrderDate(o.getOrderDate());
        if (o.getExpectedDate() != null) ex.setExpectedDate(o.getExpectedDate());
        if (o.getProductName() != null) ex.setProductName(o.getProductName());
        if (o.getMaterial() != null) ex.setMaterial(o.getMaterial());
        if (o.getBoxType() != null) ex.setBoxType(o.getBoxType());
        if (o.getStitchType() != null) ex.setStitchType(o.getStitchType());
        if (o.getProductionMaterial() != null) ex.setProductionMaterial(o.getProductionMaterial());
        if (o.getFluteType() != null) ex.setFluteType(o.getFluteType());
        if (o.getBoardLength() != null) ex.setBoardLength(o.getBoardLength());
        if (o.getBoardWidth() != null) ex.setBoardWidth(o.getBoardWidth());
        if (o.getBoardQty() != null) ex.setBoardQty(o.getBoardQty());
        if (o.getCutCount() != null) ex.setCutCount(o.getCutCount());
        if (o.getCrease() != null) ex.setCrease(o.getCrease());
        if (o.getBoardArea() != null) ex.setBoardArea(o.getBoardArea());
        if (o.getTotalArea() != null) ex.setTotalArea(o.getTotalArea());
        if (o.getMaterialBasePrice() != null) ex.setMaterialBasePrice(o.getMaterialBasePrice());
        if (o.getDiscountRate() != null) ex.setDiscountRate(o.getDiscountRate());
        if (o.getBoardUnitPrice() != null) ex.setBoardUnitPrice(o.getBoardUnitPrice());
        if (o.getProfitRate() != null) ex.setProfitRate(o.getProfitRate());
        if (o.getBoardAmount() != null) ex.setBoardAmount(o.getBoardAmount());
        if (o.getSignDate() != null) ex.setSignDate(o.getSignDate());
        if (o.getActualQty() != null) ex.setActualQty(o.getActualQty());
        if (o.getActualAmount() != null) ex.setActualAmount(o.getActualAmount());
        if (o.getNotes() != null) ex.setNotes(o.getNotes());
        applyBoardCalculation(ex);
        PurchaseOrder saved = repo.save(ex);
        businessService.onPurchaseReceived(saved);

        // Sync supplier to linked production orders
        if (saved.getSupplier() != null) {
            productionOrderRepo.findAll().stream()
                .filter(po -> po.getOrderNo() != null && po.getOrderNo().equals(saved.getOrderNo()))
                .forEach(po -> { po.setSupplier(saved.getSupplier()); productionOrderRepo.save(po); });
        }

        return Result.ok(toMap(saved), "更新成功");
    }

    @DeleteMapping("/{id}") public Result<?> delete(@PathVariable Long id) { repo.deleteById(id); return Result.ok(null, "删除成功"); }

    private Map<String, Object> toMap(PurchaseOrder o) {
        Map<String, Object> m = new LinkedHashMap<>();
        m.put("id", o.getId()); m.put("orderNo", o.getOrderNo());
        m.put("createdDate", toCreatedDate(o.getCreatedAt()));
        m.put("supplierName", o.getSupplier() != null ? o.getSupplier().getName() : "");
        m.put("supplierId", o.getSupplier() != null ? o.getSupplier().getId() : null);
        m.put("customerName", o.getCustomer() != null ? o.getCustomer().getName() : "");
        m.put("materialType", o.getMaterialType()); m.put("materialName", o.getMaterialName());
        m.put("spec", o.getSpec()); m.put("qty", o.getQty()); m.put("unit", o.getUnit());
        m.put("unitPrice", o.getUnitPrice()); m.put("totalAmount", o.getTotalAmount());
        m.put("status", o.getStatus()); m.put("orderDate", o.getOrderDate());
        m.put("expectedDate", o.getExpectedDate()); m.put("notes", o.getNotes());
        m.put("createdAt", o.getCreatedAt());
        m.put("productName", o.getProductName()); m.put("material", o.getMaterial());
        m.put("boxType", o.getBoxType()); m.put("stitchType", o.getStitchType());
        m.put("productionMaterial", o.getProductionMaterial());
        m.put("fluteType", o.getFluteType()); m.put("boardLength", o.getBoardLength());
        m.put("boardWidth", o.getBoardWidth()); m.put("boardQty", o.getBoardQty());
        m.put("cutCount", o.getCutCount()); m.put("crease", o.getCrease());
        m.put("boardArea", o.getBoardArea()); m.put("totalArea", o.getTotalArea());
        m.put("materialBasePrice", o.getMaterialBasePrice()); m.put("discountRate", displayDiscountRate(o.getDiscountRate()));
        m.put("boardUnitPrice", o.getBoardUnitPrice()); m.put("profitRate", o.getProfitRate());
        m.put("boardAmount", o.getBoardAmount()); m.put("signDate", o.getSignDate());
        m.put("actualQty", o.getActualQty()); m.put("actualAmount", o.getActualAmount());
        return m;
    }

    private void applyBoardCalculation(PurchaseOrder o) {
        BoardCalculationUtil.Result result = BoardCalculationUtil.calculate(
            o.getBoardLength(), o.getBoardWidth(), o.getBoardQty(), o.getMaterialBasePrice(), o.getDiscountRate(),
            o.getBoardUnitPrice(), o.getUnitPrice(), o.getActualQty()
        );
        o.setBoardArea(result.boardArea());
        o.setTotalArea(result.totalArea());
        o.setBoardUnitPrice(result.boardUnitPrice());
        o.setProfitRate(result.profitRate());
        o.setBoardAmount(result.boardAmount());
        o.setActualAmount(result.actualAmount());
    }

    private Double displayDiscountRate(Double rate) {
        return rate != null && rate > 0 && rate <= 2 ? rate * 100 : rate;
    }

    private LocalDate toCreatedDate(LocalDateTime createdAt) {
        return createdAt != null ? createdAt.toLocalDate() : null;
    }
}
