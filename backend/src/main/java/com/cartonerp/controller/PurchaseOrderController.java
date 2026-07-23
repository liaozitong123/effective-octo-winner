package com.cartonerp.controller;

import com.cartonerp.common.Result;
import com.cartonerp.entity.PurchaseOrder;
import com.cartonerp.repository.*;
import com.cartonerp.service.ProductionOrderService;
import com.cartonerp.util.BoardCalculationUtil;
import com.cartonerp.util.OrderNumberUtil;
import jakarta.persistence.criteria.JoinType;
import jakarta.persistence.criteria.Predicate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.*;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.web.bind.annotation.*;
import java.time.LocalDate;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.*;

@RestController
@RequestMapping("/api/purchase-orders")
public class PurchaseOrderController {
    @Autowired private PurchaseOrderRepository repo;
    @Autowired private SupplierRepository supplierRepo;
    @Autowired private SalesOrderRepository salesOrderRepo;
    @Autowired private com.cartonerp.service.BusinessService businessService;
    @Autowired private ProductionOrderService productionOrderService;
    @Autowired private JdbcTemplate jdbcTemplate;

    @GetMapping
    public Result<List<Map<String, Object>>> list(@RequestParam(defaultValue = "") String q,
                                                   @RequestParam(defaultValue = "all") String signStatus,
                                                   @RequestParam(defaultValue = "1") int page,
                                                   @RequestParam(defaultValue = "20") int perPage) {
        List<Object> params = new ArrayList<>();
        String where = buildListWhere(q, signStatus, params);
        Long total = jdbcTemplate.queryForObject(
            "select count(*) from purchase_orders po "
                + "left join sales_orders so on po.sales_order_id = so.id "
                + "left join customers c on po.customer_id = c.id "
                + where,
            Long.class,
            params.toArray()
        );

        List<Object> listParams = new ArrayList<>(params);
        listParams.add(Math.max(perPage, 1));
        listParams.add(Math.max(page - 1, 0) * Math.max(perPage, 1));
        List<Map<String, Object>> rows = jdbcTemplate.query(
            "select po.id, po.order_no, po.sales_order_id, so.order_no as sales_order_no, "
                + "po.order_date as order_date, s.name as supplier_name, po.supplier_id, c.name as customer_name, "
                + "po.customer_id, po.material_type, po.material_name, po.spec, po.qty, po.unit, "
                + "po.unit_price, po.total_amount, po.status, po.expected_date as expected_date, po.notes, "
                + "po.created_at as created_at, po.product_name, po.material, po.box_type, po.stitch_type, "
                + "po.production_status, po.production_material, po.flute_type, po.board_length, po.board_width, "
                + "po.board_qty, po.cut_count, po.crease, po.board_area, po.total_area, "
                + "po.material_base_price, po.discount_rate, po.board_unit_price, po.profit_rate, "
                + "po.board_amount, po.sign_date as sign_date, po.actual_qty, po.actual_amount, "
                + "po.acceptance_notes, po.signer "
                + "from purchase_orders po "
                + "left join sales_orders so on po.sales_order_id = so.id "
                + "left join suppliers s on po.supplier_id = s.id "
                + "left join customers c on po.customer_id = c.id "
                + where
                + " order by po.id desc limit ? offset ?",
            (rs, rowNum) -> toListMap(rs),
            listParams.toArray()
        );
        return Result.okWithTotal(rows, total != null ? total : 0);
    }

    @GetMapping("/sign-summary")
    public Result<Map<String, Object>> signSummary() {
        Map<String, Object> summary = jdbcTemplate.queryForObject(
            "select "
                + "coalesce(sum(case when sign_date is null then 1 else 0 end), 0) as unsigned_count, "
                + "coalesce(sum(case when sign_date is null then coalesce(total_area, 0) else 0 end), 0) as unsigned_total_area, "
                + "coalesce(sum(case when sign_date is not null then coalesce(total_area, 0) else 0 end), 0) as signed_total_area "
                + "from purchase_orders",
            (rs, rowNum) -> {
                Map<String, Object> m = new LinkedHashMap<>();
                m.put("unsignedCount", numberValue(rs, "unsigned_count"));
                m.put("unsignedTotalArea", numberValue(rs, "unsigned_total_area"));
                m.put("signedTotalArea", numberValue(rs, "signed_total_area"));
                return m;
            }
        );
        return Result.ok(summary);
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
        productionOrderService.createOrUpdateFromSignedPurchase(saved);

        return Result.ok(toMap(saved), "创建成功");
    }

    @PutMapping("/{id}")
    public Result<Map<String, Object>> update(@PathVariable Long id, @RequestBody PurchaseOrder o) {
        PurchaseOrder ex = repo.findById(id).orElse(null);
        if (ex == null) return Result.fail(404, "不存在");
        boolean wasReceived = "已收货".equals(ex.getStatus());
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
        if (o.getProductionStatus() != null) {
            ex.setProductionStatus(o.getProductionStatus());
            if (ex.getSalesOrder() != null) {
                ex.getSalesOrder().setProductionStatus(o.getProductionStatus());
                salesOrderRepo.save(ex.getSalesOrder());
            }
        }
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
        if (o.getNotes() != null) ex.setNotes(o.getNotes());
        applyBoardCalculation(ex);
        PurchaseOrder saved = repo.save(ex);
        if (!wasReceived && "已收货".equals(saved.getStatus())) {
            businessService.onPurchaseReceived(saved);
        }
        productionOrderService.createOrUpdateFromSignedPurchase(saved);

        return Result.ok(toMap(saved), "更新成功");
    }

    @PutMapping("/{id}/receipt")
    public Result<Map<String, Object>> updateReceipt(@PathVariable Long id, @RequestBody Map<String, Object> body) {
        PurchaseOrder ex = repo.findById(id).orElse(null);
        if (ex == null) return Result.fail(404, "不存在");
        boolean wasReceived = "已收货".equals(ex.getStatus());

        if (body.containsKey("status")) ex.setStatus(stringValue(body.get("status"), "待收货"));
        if (body.containsKey("actualQty")) ex.setActualQty(intValue(body.get("actualQty")));
        if (body.containsKey("signDate")) ex.setSignDate(dateValue(body.get("signDate")));
        if (body.containsKey("acceptanceNotes")) ex.setAcceptanceNotes(stringValue(body.get("acceptanceNotes"), ""));
        if (body.containsKey("signer")) ex.setSigner(stringValue(body.get("signer"), ""));

        applyBoardCalculation(ex);
        PurchaseOrder saved = repo.save(ex);
        List<String> warnings = syncReceiptSideEffects(saved, wasReceived);

        return Result.ok(toMap(saved), warnings.isEmpty() ? "收货信息已更新" : "收货信息已保存，自动同步未完全完成：" + String.join("；", warnings));
    }

    @DeleteMapping("/{id}") public Result<?> delete(@PathVariable Long id) { repo.deleteById(id); return Result.ok(null, "删除成功"); }

    private String buildListWhere(String q, String signStatus, List<Object> params) {
        List<String> clauses = new ArrayList<>();
        if (q != null && !q.isBlank()) {
            String p = "%" + q.trim() + "%";
            clauses.add("(po.order_no like ? or po.material_name like ? or po.product_name like ? or so.order_no like ? or c.name like ?)");
            params.add(p);
            params.add(p);
            params.add(p);
            params.add(p);
            params.add(p);
        }
        if ("signed".equals(signStatus)) {
            clauses.add("po.sign_date is not null");
        } else if ("unsigned".equals(signStatus)) {
            clauses.add("po.sign_date is null");
        }
        return clauses.isEmpty() ? "" : " where " + String.join(" and ", clauses);
    }

    private Map<String, Object> toListMap(ResultSet rs) throws SQLException {
        Map<String, Object> m = new LinkedHashMap<>();
        m.put("id", numberValue(rs, "id"));
        m.put("orderNo", safeString(rs, "order_no"));
        m.put("salesOrderId", numberValue(rs, "sales_order_id"));
        m.put("salesOrderNo", safeString(rs, "sales_order_no"));
        m.put("orderDate", safeString(rs, "order_date"));
        m.put("supplierName", safeString(rs, "supplier_name"));
        m.put("supplierId", numberValue(rs, "supplier_id"));
        m.put("customerName", safeString(rs, "customer_name"));
        m.put("customerId", numberValue(rs, "customer_id"));
        m.put("materialType", safeString(rs, "material_type"));
        m.put("materialName", safeString(rs, "material_name"));
        m.put("spec", safeString(rs, "spec"));
        m.put("qty", numberValue(rs, "qty"));
        m.put("unit", safeString(rs, "unit"));
        m.put("unitPrice", numberValue(rs, "unit_price"));
        m.put("totalAmount", numberValue(rs, "total_amount"));
        m.put("status", safeString(rs, "status"));
        m.put("expectedDate", safeString(rs, "expected_date"));
        m.put("notes", safeString(rs, "notes"));
        m.put("createdAt", safeString(rs, "created_at"));
        m.put("productName", safeString(rs, "product_name"));
        m.put("material", safeString(rs, "material"));
        m.put("boxType", safeString(rs, "box_type"));
        m.put("stitchType", safeString(rs, "stitch_type"));
        m.put("productionStatus", safeString(rs, "production_status"));
        m.put("productionMaterial", safeString(rs, "production_material"));
        m.put("fluteType", safeString(rs, "flute_type"));
        m.put("boardLength", numberValue(rs, "board_length"));
        m.put("boardWidth", numberValue(rs, "board_width"));
        m.put("boardQty", numberValue(rs, "board_qty"));
        m.put("cutCount", numberValue(rs, "cut_count"));
        m.put("crease", safeString(rs, "crease"));
        m.put("boardArea", numberValue(rs, "board_area"));
        m.put("totalArea", numberValue(rs, "total_area"));
        m.put("materialBasePrice", numberValue(rs, "material_base_price"));
        m.put("discountRate", displayDiscountRate(doubleValue(rs, "discount_rate")));
        Number unitPrice = numberValue(rs, "unit_price");
        Number boardUnitPrice = numberValue(rs, "board_unit_price");
        m.put("boardUnitPrice", boardUnitPrice);
        m.put("profitRate", displayProfitRate(numberValue(rs, "profit_rate"), unitPrice, boardUnitPrice));
        m.put("boardAmount", numberValue(rs, "board_amount"));
        m.put("signDate", safeString(rs, "sign_date"));
        m.put("actualQty", numberValue(rs, "actual_qty"));
        m.put("actualAmount", numberValue(rs, "actual_amount"));
        m.put("acceptanceNotes", safeString(rs, "acceptance_notes"));
        m.put("signer", safeString(rs, "signer"));
        return m;
    }

    private String safeString(ResultSet rs, String column) {
        try {
            String value = rs.getString(column);
            return value != null ? value : "";
        } catch (SQLException e) {
            return "";
        }
    }

    private Number numberValue(ResultSet rs, String column) {
        try {
            Object value = rs.getObject(column);
            return value instanceof Number number ? number : null;
        } catch (SQLException e) {
            return null;
        }
    }

    private Double doubleValue(ResultSet rs, String column) {
        Number value = numberValue(rs, column);
        return value != null ? value.doubleValue() : null;
    }

    private Map<String, Object> toMap(PurchaseOrder o) {
        Map<String, Object> m = new LinkedHashMap<>();
        m.put("id", o.getId()); m.put("orderNo", o.getOrderNo());
        m.put("salesOrderId", o.getSalesOrder() != null ? o.getSalesOrder().getId() : null);
        m.put("salesOrderNo", o.getSalesOrder() != null ? o.getSalesOrder().getOrderNo() : "");
        m.put("orderDate", o.getOrderDate());
        m.put("supplierName", o.getSupplier() != null ? o.getSupplier().getName() : "");
        m.put("supplierId", o.getSupplier() != null ? o.getSupplier().getId() : null);
        m.put("customerName", o.getCustomer() != null ? o.getCustomer().getName() : "");
        m.put("materialType", o.getMaterialType()); m.put("materialName", o.getMaterialName());
        m.put("spec", o.getSpec()); m.put("qty", o.getQty()); m.put("unit", o.getUnit());
        m.put("unitPrice", o.getUnitPrice()); m.put("totalAmount", o.getTotalAmount());
        m.put("status", o.getStatus());
        m.put("expectedDate", o.getExpectedDate()); m.put("notes", o.getNotes());
        m.put("createdAt", o.getCreatedAt());
        m.put("productName", o.getProductName()); m.put("material", o.getMaterial());
        m.put("boxType", o.getBoxType()); m.put("stitchType", o.getStitchType());
        m.put("productionStatus", o.getProductionStatus());
        m.put("productionMaterial", o.getProductionMaterial());
        m.put("fluteType", o.getFluteType()); m.put("boardLength", o.getBoardLength());
        m.put("boardWidth", o.getBoardWidth()); m.put("boardQty", o.getBoardQty());
        m.put("cutCount", o.getCutCount()); m.put("crease", o.getCrease());
        m.put("boardArea", o.getBoardArea()); m.put("totalArea", o.getTotalArea());
        m.put("materialBasePrice", o.getMaterialBasePrice()); m.put("discountRate", displayDiscountRate(o.getDiscountRate()));
        m.put("boardUnitPrice", o.getBoardUnitPrice()); m.put("profitRate", displayProfitRate(o.getProfitRate(), o.getUnitPrice(), o.getBoardUnitPrice()));
        m.put("boardAmount", o.getBoardAmount()); m.put("signDate", o.getSignDate());
        m.put("actualQty", o.getActualQty()); m.put("actualAmount", o.getActualAmount());
        m.put("acceptanceNotes", o.getAcceptanceNotes()); m.put("signer", o.getSigner());
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

    private Double displayProfitRate(Number storedRate, Number unitPrice, Number boardUnitPrice) {
        double customerPrice = unitPrice != null ? unitPrice.doubleValue() : 0.0;
        double boardPrice = boardUnitPrice != null ? boardUnitPrice.doubleValue() : 0.0;
        if (customerPrice > 0) {
            return Math.round((customerPrice - boardPrice) / customerPrice * 10000.0) / 100.0;
        }
        return storedRate != null ? storedRate.doubleValue() : null;
    }

    private String stringValue(Object value, String fallback) {
        return value != null ? String.valueOf(value) : fallback;
    }

    private Integer intValue(Object value) {
        if (value instanceof Number number) return number.intValue();
        if (value == null || String.valueOf(value).isBlank()) return 0;
        try {
            return Integer.parseInt(String.valueOf(value));
        } catch (NumberFormatException e) {
            try {
                return (int) Math.round(Double.parseDouble(String.valueOf(value)));
            } catch (NumberFormatException ignored) {
                return 0;
            }
        }
    }

    private LocalDate dateValue(Object value) {
        if (value == null || String.valueOf(value).isBlank()) return null;
        String text = String.valueOf(value).trim();
        int timeSeparator = text.indexOf('T');
        if (timeSeparator > 0) text = text.substring(0, timeSeparator);
        try {
            return LocalDate.parse(text);
        } catch (RuntimeException e) {
            return null;
        }
    }

    private List<String> syncReceiptSideEffects(PurchaseOrder saved, boolean wasReceived) {
        List<String> warnings = new ArrayList<>();
        if (!wasReceived && "已收货".equals(saved.getStatus())) {
            try {
                businessService.onPurchaseReceived(saved);
            } catch (RuntimeException e) {
                warnings.add("库存同步失败");
                e.printStackTrace();
            }
        }
        try {
            productionOrderService.createOrUpdateFromSignedPurchase(saved);
        } catch (RuntimeException e) {
            warnings.add("生产单同步失败");
            e.printStackTrace();
        }
        return warnings;
    }

}
