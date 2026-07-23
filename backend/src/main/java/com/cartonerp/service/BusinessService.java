package com.cartonerp.service;

import com.cartonerp.entity.*;
import com.cartonerp.repository.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;

@Service
public class BusinessService {

    @Autowired private InventoryRepository inventoryRepo;
    @Autowired private SalesOrderRepository salesOrderRepo;
    @Autowired private ProductionOrderRepository productionOrderRepo;
    @Autowired private ProductionRecordRepository productionRecordRepo;

    // Stage progression order - fixed sequence, cannot skip
    public static final List<String> STAGES = Arrays.asList(
        "待排产", "印刷", "模切", "糊盒", "打包", "质检", "已入库", "已出库", "已送货"
    );

    // Each workshop completes exactly ONE stage
    public static final Map<String, Integer> WORKSHOP_TO_STAGE = Map.of(
        "印刷区", STAGES.indexOf("印刷"),
        "模切区", STAGES.indexOf("模切"),
        "糊盒区", STAGES.indexOf("糊盒"),
        "打包区", STAGES.indexOf("打包"),
        "质检区", STAGES.indexOf("质检")
    );

    /**
     * 采购单收货 → 自动增加库存
     */
    @Transactional
    public void onPurchaseReceived(PurchaseOrder po) {
        if (!"已收货".equals(po.getStatus())) return;
        String materialType = firstNonBlank(po.getMaterialType(), "纸板");
        String materialName = firstNonBlank(po.getMaterialName(), po.getProductName(), po.getOrderNo(), "未命名物料");
        String spec = po.getSpec() != null ? po.getSpec() : "";
        Optional<Inventory> opt = inventoryRepo.findAll().stream()
            .filter(i -> Objects.equals(i.getItemName(), materialName)
                && Objects.equals(i.getSpec() != null ? i.getSpec() : "", spec))
            .findFirst();
        if (opt.isPresent()) {
            Inventory inv = opt.get();
            inv.setQty(inv.getQty() + (po.getQty() != null ? po.getQty() : 0));
            inv.setUpdatedAt(LocalDateTime.now());
            inventoryRepo.save(inv);
        } else {
            Inventory inv = new Inventory();
            inv.setItemType(materialType);
            inv.setItemName(materialName);
            inv.setSpec(spec);
            inv.setQty(po.getQty() != null ? po.getQty() : 0);
            inv.setUnit(po.getUnit() != null ? po.getUnit() : "");
            inv.setUpdatedAt(LocalDateTime.now());
            inventoryRepo.save(inv);
        }
    }

    private String firstNonBlank(String... values) {
        for (String value : values) {
            if (value != null && !value.isBlank()) return value;
        }
        return "";
    }

    /**
     * Scan report / production record → advance production stage (strict order)
     */
    @Transactional
    public void onProductionRecordAdded(ProductionRecord record) {
        ProductionOrder po = record.getProductionOrder();
        if (po == null) return;
        String workshop = record.getShift();

        if (workshop == null || !WORKSHOP_TO_STAGE.containsKey(workshop)) {
            productionOrderRepo.save(po);
            return;
        }

        String curStatus = po.getStatus();
        int curIdx = STAGES.indexOf(curStatus);
        int expectedIdx = WORKSHOP_TO_STAGE.get(workshop);

        // Auto-start from 待排产 → advance through 印刷 to 模切
        if (curIdx == 0 && expectedIdx == STAGES.indexOf("印刷")) {
            po.setStatus("模切");  // 待排产 → 印刷 → 模切 (skip setting 印刷 intermediate)
            po.setWorkshop(workshop);
            productionOrderRepo.save(po);
            return;
        }

        // Must be at exactly the stage this workshop handles (or one before, for first time)
        if (curIdx < expectedIdx) {
            throw new RuntimeException("请先完成上一道工序：" + (expectedIdx > 0 ? STAGES.get(expectedIdx - 1) : ""));
        }
        if (curIdx > expectedIdx) {
            // Already past this stage, allow reporting anyway but don't regress
            po.setWorkshop(workshop);
            productionOrderRepo.save(po);
            return;
        }

        // curIdx == expectedIdx: advance to next stage
        int nextIdx = expectedIdx + 1;
        if (nextIdx < STAGES.size()) {
            po.setStatus(STAGES.get(nextIdx));
        }
        po.setWorkshop(workshop);

        // Only after 质检区 completes, check if output meets target → 已入库
        // Use the single-stage output (same batch moves through each stage, don't sum)
        long currentOutput = record.getOutputQty() != null ? record.getOutputQty() : 0;
        long plannedQty = po.getQty() != null ? po.getQty() : 0;
        if (currentOutput >= plannedQty && expectedIdx == STAGES.indexOf("质检")) {
            po.setStatus("已入库");
            po.setFinishDate(LocalDate.now());
        }

        productionOrderRepo.save(po);
    }

    /**
     * 送货单创建/打印 → 出库 + 送货
     */
    @Transactional
    public void onDeliveryNoteCreated(DeliveryNote note) {
        // Update sales order status
        SalesOrder so = note.getSalesOrder();
        if (so != null) {
            so.setStatus("已送货");
            if (note.getDeliveryDate() != null) {
                so.setDeliveryDate(note.getDeliveryDate());
            }
            salesOrderRepo.save(so);
        }

        // Update related production orders to 已出库
        if (so != null) {
            List<ProductionOrder> pos = productionOrderRepo.findAll().stream()
                .filter(p -> p.getSalesOrder() != null && p.getSalesOrder().getId().equals(so.getId()))
                .toList();
            for (ProductionOrder po : pos) {
                po.setStatus("已出库");
                productionOrderRepo.save(po);
            }
        }
    }

    /**
     * Get stage index for progress display
     */
    public static int getStageIndex(String status) {
        int idx = STAGES.indexOf(status);
        return idx >= 0 ? idx : 0;
    }

    public static String getNextStage(String current) {
        int idx = STAGES.indexOf(current);
        return (idx >= 0 && idx + 1 < STAGES.size()) ? STAGES.get(idx + 1) : current;
    }
}
