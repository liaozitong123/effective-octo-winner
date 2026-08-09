package com.cartonerp.controller;

import com.cartonerp.common.Result;
import com.cartonerp.entity.Customer;
import com.cartonerp.entity.DeliveryNote;
import com.cartonerp.entity.Payment;
import com.cartonerp.entity.ProductionOrder;
import com.cartonerp.entity.PurchaseOrder;
import com.cartonerp.entity.SalesOrder;
import com.cartonerp.entity.Supplier;
import com.cartonerp.repository.DeliveryNoteRepository;
import com.cartonerp.repository.PaymentRepository;
import com.cartonerp.repository.PurchaseOrderRepository;
import com.cartonerp.service.DeliveryNoteService;
import jakarta.persistence.criteria.Predicate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

@RestController
@RequestMapping("/api/payments")
public class PaymentController {
    private static final String RECEIPT_TYPE = "收款";
    private static final String PAYMENT_TYPE = "付款";
    private static final String CUSTOMER_PARTY = "customer";
    private static final String SUPPLIER_PARTY = "supplier";

    @Autowired private PaymentRepository repo;
    @Autowired private DeliveryNoteRepository deliveryNoteRepo;
    @Autowired private PurchaseOrderRepository purchaseOrderRepo;
    @Autowired private DeliveryNoteService deliveryNoteService;

    @GetMapping
    public Result<?> list(@RequestParam(defaultValue = "") String q,
                          @RequestParam(defaultValue = "") String paymentType,
                          @RequestParam(defaultValue = "") String partyType,
                          @RequestParam(defaultValue = "all") String settlementStatus,
                          @RequestParam(defaultValue = "") String month,
                          @RequestParam(defaultValue = "1") int page,
                          @RequestParam(defaultValue = "20") int perPage) {
        if (isCustomerReceivable(paymentType, partyType)) {
            return listCustomerReceivables(q, settlementStatus, month, page, perPage);
        }
        if (isSupplierPayable(paymentType, partyType)) {
            return listSupplierPayables(q, settlementStatus, month, page, perPage);
        }

        Specification<Payment> spec = (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();
            if (!paymentType.isEmpty()) {
                predicates.add(cb.equal(root.get("paymentType"), paymentType));
            }
            if (!partyType.isEmpty()) {
                predicates.add(cb.equal(root.get("partyType"), partyType));
            }
            if (!q.isEmpty()) {
                String p = "%" + q + "%";
                predicates.add(cb.or(cb.like(root.get("paymentNo"), p), cb.like(root.get("partyName"), p)));
            }
            return predicates.isEmpty() ? null : cb.and(predicates.toArray(new Predicate[0]));
        };
        Page<Payment> pg = repo.findAll(spec, PageRequest.of(page - 1, perPage, Sort.by(Sort.Direction.DESC, "id")));
        return Result.okWithTotal(pg.getContent(), pg.getTotalElements());
    }

    @GetMapping("/summary")
    public Result<Map<String, Object>> summary(@RequestParam(defaultValue = "") String q,
                                               @RequestParam(defaultValue = "") String paymentType,
                                               @RequestParam(defaultValue = "") String partyType,
                                               @RequestParam(defaultValue = "all") String settlementStatus,
                                               @RequestParam(defaultValue = "") String month) {
        Map<String, Object> summary = new LinkedHashMap<>();
        if (isCustomerReceivable(paymentType, partyType)) {
            syncCustomerReceivables();
            double total = repo.findByPaymentTypeAndPartyType(RECEIPT_TYPE, CUSTOMER_PARTY).stream()
                .map(this::toReceivableMap)
                .filter(row -> matchesReceivable(row, q))
                .filter(row -> matchesSettlement(row, "unreceivedAmount", settlementStatus))
                .filter(row -> matchesMonth(row, "deliveryDate", month))
                .mapToDouble(row -> rowNumber(row, "unreceivedAmount"))
                .sum();
            summary.put("unreceivedAmount", round2(total));
            return Result.ok(summary);
        }
        if (isSupplierPayable(paymentType, partyType)) {
            syncSupplierPayables();
            double total = repo.findByPaymentTypeAndPartyType(PAYMENT_TYPE, SUPPLIER_PARTY).stream()
                .filter(payment -> !trim(payment.getPurchaseOrderNo()).isEmpty())
                .map(this::toPayableMap)
                .filter(row -> matchesPayable(row, q))
                .filter(row -> matchesSettlement(row, "unpaidAmount", settlementStatus))
                .filter(row -> matchesMonth(row, "signDate", month))
                .mapToDouble(row -> rowNumber(row, "unpaidAmount"))
                .sum();
            summary.put("unpaidAmount", round2(total));
            return Result.ok(summary);
        }
        summary.put("amount", 0.0);
        return Result.ok(summary);
    }

    @GetMapping("/{id}")
    public Result<Payment> get(@PathVariable Long id) {
        return repo.findById(id).map(Result::ok).orElse(Result.fail(404, "不存在"));
    }

    @PostMapping
    public Result<Payment> create(@RequestBody Payment p) {
        return Result.ok(repo.save(p), "创建成功");
    }

    @PutMapping("/{id}")
    public Result<?> update(@PathVariable Long id, @RequestBody Map<String, Object> body) {
        Payment ex = repo.findById(id).orElse(null);
        if (ex == null) return Result.fail(404, "不存在");

        if (isCustomerReceivable(ex.getPaymentType(), ex.getPartyType())) {
            updateReceivableManualFields(ex, body);
            return Result.ok(toReceivableMap(repo.save(ex)), "更新成功");
        }
        if (isSupplierPayable(ex.getPaymentType(), ex.getPartyType())) {
            updatePayableManualFields(ex, body);
            return Result.ok(toPayableMap(repo.save(ex)), "更新成功");
        }

        if (body.containsKey("paymentNo")) ex.setPaymentNo(readString(body.get("paymentNo")));
        if (body.containsKey("paymentType")) ex.setPaymentType(readString(body.get("paymentType")));
        if (body.containsKey("partyType")) ex.setPartyType(readString(body.get("partyType")));
        if (body.containsKey("partyId")) ex.setPartyId(readLong(body.get("partyId")));
        if (body.containsKey("partyName")) ex.setPartyName(readString(body.get("partyName")));
        if (body.containsKey("amount")) ex.setAmount(readDouble(body.get("amount"), 0.0));
        if (body.containsKey("paymentMethod")) ex.setPaymentMethod(readString(body.get("paymentMethod")));
        if (body.containsKey("paymentDate")) ex.setPaymentDate(readDate(body.get("paymentDate")));
        if (body.containsKey("notes")) ex.setNotes(readString(body.get("notes")));
        return Result.ok(repo.save(ex), "更新成功");
    }

    @DeleteMapping("/{id}")
    public Result<?> delete(@PathVariable Long id) {
        repo.deleteById(id);
        return Result.ok(null, "删除成功");
    }

    private Result<List<Map<String, Object>>> listCustomerReceivables(String q, String settlementStatus, String month, int page, int perPage) {
        syncCustomerReceivables();
        List<Map<String, Object>> rows = repo.findByPaymentTypeAndPartyType(RECEIPT_TYPE, CUSTOMER_PARTY).stream()
            .map(this::toReceivableMap)
            .filter(row -> matchesReceivable(row, q))
            .filter(row -> matchesSettlement(row, "unreceivedAmount", settlementStatus))
            .filter(row -> matchesMonth(row, "deliveryDate", month))
            .sorted(this::compareReceivableRows)
            .toList();

        int safePerPage = Math.max(perPage, 1);
        int from = Math.min(Math.max(page - 1, 0) * safePerPage, rows.size());
        int to = Math.min(from + safePerPage, rows.size());
        return Result.okWithTotal(rows.subList(from, to), rows.size());
    }

    private Result<List<Map<String, Object>>> listSupplierPayables(String q, String settlementStatus, String month, int page, int perPage) {
        syncSupplierPayables();
        List<Map<String, Object>> rows = repo.findByPaymentTypeAndPartyType(PAYMENT_TYPE, SUPPLIER_PARTY).stream()
            .filter(payment -> !trim(payment.getPurchaseOrderNo()).isEmpty())
            .map(this::toPayableMap)
            .filter(row -> matchesPayable(row, q))
            .filter(row -> matchesSettlement(row, "unpaidAmount", settlementStatus))
            .filter(row -> matchesMonth(row, "signDate", month))
            .sorted(this::comparePayableRows)
            .toList();

        int safePerPage = Math.max(perPage, 1);
        int from = Math.min(Math.max(page - 1, 0) * safePerPage, rows.size());
        int to = Math.min(from + safePerPage, rows.size());
        return Result.okWithTotal(rows.subList(from, to), rows.size());
    }

    private void syncCustomerReceivables() {
        deliveryNoteService.syncPendingDeliveryNotes();
        Map<String, List<DeliveryNote>> notesByNo = new LinkedHashMap<>();
        for (DeliveryNote note : deliveryNoteRepo.findAll(Sort.by(Sort.Direction.ASC, "id"))) {
            String noteNo = trim(note.getNoteNo());
            if (noteNo.isEmpty()) continue;
            notesByNo.computeIfAbsent(noteNo, key -> new ArrayList<>()).add(note);
        }

        for (Map.Entry<String, List<DeliveryNote>> entry : notesByNo.entrySet()) {
            String deliveryNoteNo = entry.getKey();
            List<DeliveryNote> notes = entry.getValue();
            Payment payment = repo.findFirstByPaymentTypeAndPartyTypeAndDeliveryNoteNo(RECEIPT_TYPE, CUSTOMER_PARTY, deliveryNoteNo)
                .or(() -> repo.findFirstByPaymentTypeAndPartyTypeAndPaymentNo(RECEIPT_TYPE, CUSTOMER_PARTY, receivablePaymentNo(deliveryNoteNo)))
                .orElseGet(() -> {
                    Payment p = new Payment();
                    p.setPaymentType(RECEIPT_TYPE);
                    p.setPartyType(CUSTOMER_PARTY);
                    p.setPaymentNo(receivablePaymentNo(deliveryNoteNo));
                    p.setAmount(0.0);
                    return p;
                });

            Customer customer = firstCustomer(notes);
            payment.setDeliveryNoteNo(deliveryNoteNo);
            payment.setDeliveryDate(firstDeliveryDate(notes));
            payment.setPartyId(customer != null && customer.getId() != null ? customer.getId() : 0L);
            payment.setPartyName(customer != null ? customer.getName() : "");
            payment.setReceivableAmount(round2(notes.stream().mapToDouble(this::deliveryAmount).sum()));
            repo.save(payment);
        }
    }

    private void syncSupplierPayables() {
        for (PurchaseOrder purchaseOrder : purchaseOrderRepo.findAll(Sort.by(Sort.Direction.ASC, "id"))) {
            if (!isReceivedPurchase(purchaseOrder)) continue;
            String purchaseOrderNo = trim(purchaseOrder.getOrderNo());
            if (purchaseOrderNo.isEmpty()) continue;

            Payment payment = repo.findFirstByPaymentTypeAndPartyTypeAndPurchaseOrderNo(PAYMENT_TYPE, SUPPLIER_PARTY, purchaseOrderNo)
                .or(() -> repo.findFirstByPaymentTypeAndPartyTypeAndPaymentNo(PAYMENT_TYPE, SUPPLIER_PARTY, payablePaymentNo(purchaseOrderNo)))
                .orElseGet(() -> {
                    Payment p = new Payment();
                    p.setPaymentType(PAYMENT_TYPE);
                    p.setPartyType(SUPPLIER_PARTY);
                    p.setPaymentNo(payablePaymentNo(purchaseOrderNo));
                    p.setAmount(0.0);
                    return p;
                });

            Supplier supplier = purchaseOrder.getSupplier();
            payment.setPurchaseOrderNo(purchaseOrderNo);
            payment.setSignDate(purchaseOrder.getSignDate());
            payment.setPartyId(supplier != null && supplier.getId() != null ? supplier.getId() : 0L);
            payment.setPartyName(supplier != null ? supplier.getName() : "");
            payment.setPayableAmount(payableAmount(purchaseOrder));
            repo.save(payment);
        }
    }

    private void updateReceivableManualFields(Payment payment, Map<String, Object> body) {
        if (body.containsKey("amount")) payment.setAmount(readDouble(body.get("amount"), 0.0));
        if (body.containsKey("receivedAmount")) payment.setAmount(readDouble(body.get("receivedAmount"), 0.0));
        if (body.containsKey("paymentDate")) payment.setPaymentDate(readDate(body.get("paymentDate")));
        if (body.containsKey("registrar")) payment.setRegistrar(readString(body.get("registrar")));
        if (body.containsKey("reviewer")) payment.setReviewer(readString(body.get("reviewer")));
    }

    private void updatePayableManualFields(Payment payment, Map<String, Object> body) {
        if (body.containsKey("amount")) payment.setAmount(readDouble(body.get("amount"), 0.0));
        if (body.containsKey("paidAmount")) payment.setAmount(readDouble(body.get("paidAmount"), 0.0));
        if (body.containsKey("paymentDate")) payment.setPaymentDate(readDate(body.get("paymentDate")));
        if (body.containsKey("registrar")) payment.setRegistrar(readString(body.get("registrar")));
        if (body.containsKey("reviewer")) payment.setReviewer(readString(body.get("reviewer")));
    }

    private Map<String, Object> toReceivableMap(Payment p) {
        double receivableAmount = numberValue(p.getReceivableAmount());
        double receivedAmount = numberValue(p.getAmount());
        Map<String, Object> m = new LinkedHashMap<>();
        m.put("id", p.getId());
        m.put("deliveryDate", p.getDeliveryDate());
        m.put("customerName", p.getPartyName());
        m.put("partyName", p.getPartyName());
        m.put("deliveryNoteNo", p.getDeliveryNoteNo());
        m.put("paymentNo", p.getPaymentNo());
        m.put("receivableAmount", round2(receivableAmount));
        m.put("amount", round2(receivedAmount));
        m.put("receivedAmount", round2(receivedAmount));
        m.put("unreceivedAmount", round2(receivableAmount - receivedAmount));
        m.put("paymentDate", p.getPaymentDate());
        m.put("registrar", p.getRegistrar());
        m.put("reviewer", p.getReviewer());
        m.put("createdAt", p.getCreatedAt());
        return m;
    }

    private Map<String, Object> toPayableMap(Payment p) {
        double payableAmount = numberValue(p.getPayableAmount());
        double paidAmount = numberValue(p.getAmount());
        Map<String, Object> m = new LinkedHashMap<>();
        m.put("id", p.getId());
        m.put("signDate", p.getSignDate());
        m.put("supplierName", p.getPartyName());
        m.put("partyName", p.getPartyName());
        m.put("purchaseOrderNo", p.getPurchaseOrderNo());
        m.put("paymentNo", p.getPaymentNo());
        m.put("payableAmount", round2(payableAmount));
        m.put("amount", round2(paidAmount));
        m.put("paidAmount", round2(paidAmount));
        m.put("unpaidAmount", round2(payableAmount - paidAmount));
        m.put("paymentDate", p.getPaymentDate());
        m.put("registrar", p.getRegistrar());
        m.put("reviewer", p.getReviewer());
        m.put("createdAt", p.getCreatedAt());
        return m;
    }

    private boolean matchesReceivable(Map<String, Object> row, String q) {
        if (q == null || q.isBlank()) return true;
        String keyword = q.trim().toLowerCase();
        return List.of("deliveryDate", "customerName", "deliveryNoteNo").stream()
            .map(row::get)
            .filter(Objects::nonNull)
            .map(value -> String.valueOf(value).toLowerCase())
            .anyMatch(value -> value.contains(keyword));
    }

    private boolean matchesPayable(Map<String, Object> row, String q) {
        if (q == null || q.isBlank()) return true;
        String keyword = q.trim().toLowerCase();
        return List.of("signDate", "supplierName", "purchaseOrderNo").stream()
            .map(row::get)
            .filter(Objects::nonNull)
            .map(value -> String.valueOf(value).toLowerCase())
            .anyMatch(value -> value.contains(keyword));
    }

    private boolean matchesSettlement(Map<String, Object> row, String amountKey, String settlementStatus) {
        double remaining = rowNumber(row, amountKey);
        if ("unsettled".equals(settlementStatus)) return remaining > 0;
        if ("settled".equals(settlementStatus)) return remaining <= 0;
        return true;
    }

    private boolean matchesMonth(Map<String, Object> row, String dateKey, String month) {
        if (month == null || !month.matches("\\d{4}-\\d{2}")) return true;
        Object value = row.get(dateKey);
        return value != null && String.valueOf(value).startsWith(month);
    }

    private int compareReceivableRows(Map<String, Object> a, Map<String, Object> b) {
        String dateA = Objects.toString(a.get("deliveryDate"), "");
        String dateB = Objects.toString(b.get("deliveryDate"), "");
        int dateCompare = dateB.compareTo(dateA);
        if (dateCompare != 0) return dateCompare;
        Long idA = readLong(a.get("id"));
        Long idB = readLong(b.get("id"));
        return Long.compare(idB != null ? idB : 0L, idA != null ? idA : 0L);
    }

    private int comparePayableRows(Map<String, Object> a, Map<String, Object> b) {
        String dateA = Objects.toString(a.get("signDate"), "");
        String dateB = Objects.toString(b.get("signDate"), "");
        int dateCompare = dateB.compareTo(dateA);
        if (dateCompare != 0) return dateCompare;
        Long idA = readLong(a.get("id"));
        Long idB = readLong(b.get("id"));
        return Long.compare(idB != null ? idB : 0L, idA != null ? idA : 0L);
    }

    private boolean isCustomerReceivable(String paymentType, String partyType) {
        return RECEIPT_TYPE.equals(paymentType) && CUSTOMER_PARTY.equals(partyType);
    }

    private boolean isSupplierPayable(String paymentType, String partyType) {
        return PAYMENT_TYPE.equals(paymentType) && SUPPLIER_PARTY.equals(partyType);
    }

    private String receivablePaymentNo(String deliveryNoteNo) {
        return "AR-" + deliveryNoteNo;
    }

    private String payablePaymentNo(String purchaseOrderNo) {
        return "AP-" + purchaseOrderNo;
    }

    private boolean isReceivedPurchase(PurchaseOrder purchaseOrder) {
        return purchaseOrder.getSignDate() != null || "已收货".equals(purchaseOrder.getStatus());
    }

    private double payableAmount(PurchaseOrder purchaseOrder) {
        double actualAmount = firstNumber(purchaseOrder.getActualAmount());
        if (actualAmount > 0) return round2(actualAmount);
        double boardAmount = firstNumber(purchaseOrder.getBoardAmount());
        if (boardAmount > 0) return round2(boardAmount);
        return round2(firstNumber(purchaseOrder.getTotalAmount()));
    }

    private double deliveryAmount(DeliveryNote note) {
        int deliveryQty = note.getQty() != null ? note.getQty() : 0;
        double boxUnitPrice = boxUnitPrice(note);
        return round2(deliveryQty * boxUnitPrice);
    }

    private double boxUnitPrice(DeliveryNote note) {
        SalesOrder salesOrder = sourceSalesOrder(note);
        double customerUnitPrice = firstNumber(salesOrder != null ? salesOrder.getUnitPrice() : null);
        double singleArea = firstNumber(salesOrder != null ? salesOrder.getSingleArea() : null);
        double boxUnitPrice = firstNumber(salesOrder != null ? salesOrder.getBoxUnitPrice() : null);
        if (boxUnitPrice <= 0 && customerUnitPrice > 0 && singleArea > 0) {
            boxUnitPrice = round2(customerUnitPrice * singleArea);
        }
        return boxUnitPrice;
    }

    private Customer firstCustomer(List<DeliveryNote> notes) {
        for (DeliveryNote note : notes) {
            ProductionOrder productionOrder = note.getProductionOrder();
            PurchaseOrder purchaseOrder = productionOrder != null ? productionOrder.getPurchaseOrder() : null;
            SalesOrder salesOrder = sourceSalesOrder(note);
            if (note.getCustomer() != null) return note.getCustomer();
            if (purchaseOrder != null && purchaseOrder.getCustomer() != null) return purchaseOrder.getCustomer();
            if (productionOrder != null && productionOrder.getCustomer() != null) return productionOrder.getCustomer();
            if (salesOrder != null && salesOrder.getCustomer() != null) return salesOrder.getCustomer();
        }
        return null;
    }

    private LocalDate firstDeliveryDate(List<DeliveryNote> notes) {
        return notes.stream()
            .map(DeliveryNote::getDeliveryDate)
            .filter(Objects::nonNull)
            .max(Comparator.naturalOrder())
            .orElse(null);
    }

    private SalesOrder sourceSalesOrder(DeliveryNote note) {
        ProductionOrder productionOrder = note.getProductionOrder();
        PurchaseOrder purchaseOrder = productionOrder != null ? productionOrder.getPurchaseOrder() : null;
        if (purchaseOrder != null && purchaseOrder.getSalesOrder() != null) return purchaseOrder.getSalesOrder();
        if (productionOrder != null && productionOrder.getSalesOrder() != null) return productionOrder.getSalesOrder();
        return note.getSalesOrder();
    }

    private String readString(Object value) {
        return value == null ? null : String.valueOf(value).trim();
    }

    private Long readLong(Object value) {
        if (value == null || String.valueOf(value).isBlank()) return null;
        if (value instanceof Number number) return number.longValue();
        try {
            return Long.parseLong(String.valueOf(value).trim());
        } catch (NumberFormatException e) {
            return null;
        }
    }

    private Double readDouble(Object value, double defaultValue) {
        if (value == null || String.valueOf(value).isBlank()) return defaultValue;
        if (value instanceof Number number) return number.doubleValue();
        try {
            return Double.parseDouble(String.valueOf(value).trim());
        } catch (NumberFormatException e) {
            return defaultValue;
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

    private String trim(String value) {
        return value == null ? "" : value.trim();
    }

    private double firstNumber(Number... values) {
        for (Number value : values) {
            if (value != null) return value.doubleValue();
        }
        return 0.0;
    }

    private double numberValue(Number value) {
        return value != null ? value.doubleValue() : 0.0;
    }

    private double rowNumber(Map<String, Object> row, String key) {
        Object value = row.get(key);
        if (value instanceof Number number) return number.doubleValue();
        if (value == null || String.valueOf(value).isBlank()) return 0.0;
        try {
            return Double.parseDouble(String.valueOf(value));
        } catch (NumberFormatException e) {
            return 0.0;
        }
    }

    private double round2(double value) {
        return Math.round(value * 100.0) / 100.0;
    }
}
