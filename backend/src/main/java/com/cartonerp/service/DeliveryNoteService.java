package com.cartonerp.service;

import com.cartonerp.entity.Customer;
import com.cartonerp.entity.DeliveryNote;
import com.cartonerp.entity.ProductionOrder;
import com.cartonerp.entity.ProductionRecord;
import com.cartonerp.entity.PurchaseOrder;
import com.cartonerp.entity.SalesOrder;
import com.cartonerp.repository.DeliveryNoteRepository;
import com.cartonerp.repository.ProductionOrderRepository;
import com.cartonerp.repository.ProductionRecordRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Service
public class DeliveryNoteService {
    private static final DateTimeFormatter DELIVERY_NOTE_NO_DATE = DateTimeFormatter.ofPattern("yyyyMMdd");

    @Autowired private DeliveryNoteRepository deliveryNoteRepo;
    @Autowired private ProductionOrderRepository productionOrderRepo;
    @Autowired private ProductionRecordRepository productionRecordRepo;
    @Autowired private ProductionRecordService productionRecordService;

    @Transactional
    public int syncPendingDeliveryNotes() {
        productionRecordService.syncReceivedPurchasesWithoutRecord();
        int created = 0;
        Set<String> processedKeys = new HashSet<>();
        for (ProductionOrder productionOrder : productionOrderRepo.findAll()) {
            if (productionOrder.getId() == null) continue;
            SalesOrder salesOrder = sourceSalesOrder(productionOrder);
            String orderKey = orderKey(productionOrder, salesOrder);
            if (!processedKeys.add(orderKey)) continue;

            int inboundQty = inboundQtyForOrder(productionOrder, salesOrder);
            if (inboundQty <= 0) continue;
            int remainingStock = inboundQty - deliveredQtyForOrder(productionOrder, salesOrder);
            if (remainingStock <= 0) continue;
            boolean hasBlankDraft = notesForOrder(productionOrder, salesOrder).stream()
                .anyMatch(note -> !isPrinted(note) && intValue(note.getQty()) == 0);
            if (hasBlankDraft) continue;

            DeliveryNote note = new DeliveryNote();
            note.setProductionOrder(productionOrder);
            note.setSalesOrder(salesOrder);
            note.setCustomer(sourceCustomer(productionOrder));
            note.setQty(0);
            note.setPrinted(false);
            note.setStatus("未送货");
            deliveryNoteRepo.save(note);
            created++;
        }
        return created;
    }

    @Transactional
    public DeliveryNote saveManualFields(DeliveryNote target, DeliveryNote source) {
        if (source.getProductionOrder() != null && source.getProductionOrder().getId() != null) {
            productionOrderRepo.findById(source.getProductionOrder().getId()).ifPresent(target::setProductionOrder);
        }
        ProductionOrder productionOrder = target.getProductionOrder();
        if (productionOrder != null) {
            target.setSalesOrder(sourceSalesOrder(productionOrder));
            target.setCustomer(sourceCustomer(productionOrder));
        }
        if (source.getQty() != null) target.setQty(source.getQty());
        if (source.getDeliveryDate() != null) target.setDeliveryDate(source.getDeliveryDate());
        if (source.getNotes() != null) target.setNotes(source.getNotes());
        if (source.getDriver() != null) target.setDriver(source.getDriver());
        if (source.getIssuer() != null) target.setIssuer(source.getIssuer());
        if (source.getSalesperson() != null) target.setSalesperson(source.getSalesperson());
        if (source.getReviewCount() != null) target.setReviewCount(source.getReviewCount());
        if (source.getCustomerSignature() != null) target.setCustomerSignature(source.getCustomerSignature());
        normalizePrintStatus(target);
        return deliveryNoteRepo.save(target);
    }

    @Transactional
    public synchronized DeliveryNote markPrinted(DeliveryNote note) {
        if (note.getNoteNo() == null || note.getNoteNo().isBlank()) note.setNoteNo(nextDeliveryNoteNo());
        note.setPrinted(true);
        normalizePrintStatus(note);
        return deliveryNoteRepo.save(note);
    }

    @Transactional
    public synchronized List<DeliveryNote> markPrintedBatch(List<DeliveryNote> notes) {
        if (notes == null || notes.isEmpty()) return List.of();
        String noteNo = notes.stream()
            .map(DeliveryNote::getNoteNo)
            .filter(value -> value != null && !value.isBlank())
            .findFirst()
            .orElseGet(this::nextDeliveryNoteNo);
        return notes.stream().map(note -> {
            note.setNoteNo(noteNo);
            note.setPrinted(true);
            normalizePrintStatus(note);
            return deliveryNoteRepo.save(note);
        }).toList();
    }

    public int inboundQty(ProductionOrder productionOrder) {
        if (productionOrder == null || productionOrder.getId() == null) return 0;
        return productionRecordRepo.findByProductionOrderId(productionOrder.getId()).stream()
            .mapToInt(record -> intValue(record.getOutputQty()))
            .sum();
    }

    public int inboundQtyForOrder(ProductionOrder productionOrder, SalesOrder salesOrder) {
        if (salesOrder != null && salesOrder.getId() != null) {
            return productionOrderRepo.findBySalesOrderId(salesOrder.getId()).stream()
                .mapToInt(this::inboundQty)
                .sum();
        }
        return inboundQty(productionOrder);
    }

    public int deliveredQty(ProductionOrder productionOrder) {
        return deliveredQty(productionOrder, null);
    }

    public int deliveredQtyForOrder(ProductionOrder productionOrder, SalesOrder salesOrder) {
        return notesForOrder(productionOrder, salesOrder).stream()
            .mapToInt(note -> intValue(note.getQty()))
            .sum();
    }

    public int deliveredQty(ProductionOrder productionOrder, Long excludeNoteId) {
        if (productionOrder == null || productionOrder.getId() == null) return 0;
        return deliveryNoteRepo.findByProductionOrderId(productionOrder.getId()).stream()
            .filter(note -> excludeNoteId == null || !excludeNoteId.equals(note.getId()))
            .mapToInt(note -> intValue(note.getQty()))
            .sum();
    }

    public boolean isPrinted(DeliveryNote note) {
        return Boolean.TRUE.equals(note.getPrinted());
    }

    public void normalizePrintStatus(DeliveryNote note) {
        note.setStatus(isPrinted(note) ? "已送货" : "未送货");
    }

    public SalesOrder sourceSalesOrder(ProductionOrder productionOrder) {
        if (productionOrder == null) return null;
        PurchaseOrder purchaseOrder = productionOrder.getPurchaseOrder();
        if (purchaseOrder != null && purchaseOrder.getSalesOrder() != null) return purchaseOrder.getSalesOrder();
        return productionOrder.getSalesOrder();
    }

    public Customer sourceCustomer(ProductionOrder productionOrder) {
        if (productionOrder == null) return null;
        PurchaseOrder purchaseOrder = productionOrder.getPurchaseOrder();
        if (purchaseOrder != null && purchaseOrder.getCustomer() != null) return purchaseOrder.getCustomer();
        if (productionOrder.getCustomer() != null) return productionOrder.getCustomer();
        SalesOrder salesOrder = sourceSalesOrder(productionOrder);
        return salesOrder != null ? salesOrder.getCustomer() : null;
    }

    private List<DeliveryNote> notesForOrder(ProductionOrder productionOrder, SalesOrder salesOrder) {
        if (salesOrder != null && salesOrder.getId() != null) return deliveryNoteRepo.findBySalesOrderId(salesOrder.getId());
        if (productionOrder != null && productionOrder.getId() != null) return deliveryNoteRepo.findByProductionOrderId(productionOrder.getId());
        return List.of();
    }

    private String orderKey(ProductionOrder productionOrder, SalesOrder salesOrder) {
        if (salesOrder != null && salesOrder.getId() != null) return "SO:" + salesOrder.getId();
        return "PO:" + productionOrder.getId();
    }

    private String nextDeliveryNoteNo() {
        String prefix = LocalDate.now().format(DELIVERY_NOTE_NO_DATE);
        int next = deliveryNoteRepo.findByNoteNoStartingWith(prefix).stream()
            .map(DeliveryNote::getNoteNo)
            .mapToInt(noteNo -> deliveryNoteNoSuffix(prefix, noteNo))
            .max()
            .orElse(0) + 1;
        if (next > 999) throw new IllegalStateException("当天送货单号已超过999");
        return prefix + String.format("%03d", next);
    }

    private int deliveryNoteNoSuffix(String prefix, String noteNo) {
        if (noteNo == null || !noteNo.startsWith(prefix) || noteNo.length() != prefix.length() + 3) return 0;
        try {
            return Integer.parseInt(noteNo.substring(prefix.length()));
        } catch (NumberFormatException e) {
            return 0;
        }
    }

    private int intValue(Number value) {
        return value != null ? value.intValue() : 0;
    }
}
