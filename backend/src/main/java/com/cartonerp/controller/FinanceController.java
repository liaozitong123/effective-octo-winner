package com.cartonerp.controller;

import com.cartonerp.common.Result;
import com.cartonerp.dto.ProfitAnalysisDTO;
import com.cartonerp.entity.DeliveryNote;
import com.cartonerp.entity.ProductionOrder;
import com.cartonerp.entity.SalesOrder;
import com.cartonerp.entity.PurchaseOrder;
import com.cartonerp.entity.Payment;
import com.cartonerp.repository.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.*;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/finance")
public class FinanceController {

    @Autowired private SalesOrderRepository salesOrderRepo;
    @Autowired private PurchaseOrderRepository purchaseOrderRepo;
    @Autowired private DeliveryNoteRepository deliveryNoteRepo;
    @Autowired private PaymentRepository paymentRepo;

    @GetMapping("/profit-analysis")
    public Result<ProfitAnalysisDTO> profitAnalysis(@RequestParam(defaultValue = "0") int year,
                                                      @RequestParam(defaultValue = "0") int month) {
        int y = year > 0 ? year : LocalDate.now().getYear();
        ProfitAnalysisDTO dto = new ProfitAnalysisDTO();
        dto.setYear(y);
        dto.setMonth(month);

        List<DeliveryNote> allDeliveries = deliveryNoteRepo.findAll();
        List<PurchaseOrder> allPurchases = purchaseOrderRepo.findAll();
        Map<Long, PurchaseOrder> purchaseBySalesId = new HashMap<>();
        for (PurchaseOrder purchase : allPurchases) {
            SalesOrder salesOrder = purchase.getSalesOrder();
            if (salesOrder == null || salesOrder.getId() == null) continue;
            purchaseBySalesId.putIfAbsent(salesOrder.getId(), purchase);
        }

        Map<Long, LocalDate> firstDeliveryBySalesId = new HashMap<>();
        for (DeliveryNote note : allDeliveries) {
            SalesOrder salesOrder = sourceSalesOrder(note);
            LocalDate deliveryDate = note.getDeliveryDate();
            if (salesOrder == null || salesOrder.getId() == null || deliveryDate == null) continue;
            firstDeliveryBySalesId.merge(salesOrder.getId(), deliveryDate,
                (oldDate, newDate) -> newDate.isBefore(oldDate) ? newDate : oldDate);
        }

        double totalRevenue = 0, totalCost = 0;
        List<ProfitAnalysisDTO.MonthlyData> monthly = new ArrayList<>();

        for (int m = 1; m <= 12; m++) {
            final int mm = m;
            double rev = allDeliveries.stream()
                .filter(note -> note.getDeliveryDate() != null
                    && note.getDeliveryDate().getYear() == y
                    && note.getDeliveryDate().getMonthValue() == mm)
                .mapToDouble(this::deliveryAmount)
                .sum();
            double cost = firstDeliveryBySalesId.entrySet().stream()
                .filter(entry -> entry.getValue().getYear() == y && entry.getValue().getMonthValue() == mm)
                .map(Map.Entry::getKey)
                .map(purchaseBySalesId::get)
                .filter(Objects::nonNull)
                .mapToDouble(this::purchaseCost)
                .sum();
            totalRevenue += rev;
            totalCost += cost;
            monthly.add(new ProfitAnalysisDTO.MonthlyData(m, Math.round(rev * 100.0) / 100.0, Math.round(cost * 100.0) / 100.0, Math.round((rev - cost) * 100.0) / 100.0));
        }

        if (month > 0) {
            ProfitAnalysisDTO.MonthlyData md = monthly.get(month - 1);
            totalRevenue = md.getRevenue();
            totalCost = md.getCost();
        }

        dto.setRevenue(Math.round(totalRevenue * 100.0) / 100.0);
        dto.setCost(Math.round(totalCost * 100.0) / 100.0);
        dto.setProfit(Math.round((totalRevenue - totalCost) * 100.0) / 100.0);
        dto.setMonthly(monthly);
        return Result.ok(dto);
    }

    private SalesOrder sourceSalesOrder(DeliveryNote note) {
        if (note.getSalesOrder() != null) return note.getSalesOrder();
        ProductionOrder productionOrder = note.getProductionOrder();
        if (productionOrder == null) return null;
        PurchaseOrder purchaseOrder = productionOrder.getPurchaseOrder();
        if (purchaseOrder != null && purchaseOrder.getSalesOrder() != null) return purchaseOrder.getSalesOrder();
        return productionOrder.getSalesOrder();
    }

    private double deliveryAmount(DeliveryNote note) {
        int deliveryQty = note.getQty() != null ? note.getQty() : 0;
        return round2(deliveryQty * boxUnitPrice(sourceSalesOrder(note)));
    }

    private double boxUnitPrice(SalesOrder salesOrder) {
        if (salesOrder == null) return 0.0;
        double boxUnitPrice = numberValue(salesOrder.getBoxUnitPrice());
        double customerUnitPrice = numberValue(salesOrder.getUnitPrice());
        double singleArea = numberValue(salesOrder.getSingleArea());
        if (boxUnitPrice <= 0 && customerUnitPrice > 0 && singleArea > 0) {
            return round2(customerUnitPrice * singleArea);
        }
        return boxUnitPrice;
    }

    private double purchaseCost(PurchaseOrder purchaseOrder) {
        double actualAmount = numberValue(purchaseOrder.getActualAmount());
        if (actualAmount > 0) return round2(actualAmount);
        double boardAmount = numberValue(purchaseOrder.getBoardAmount());
        if (boardAmount > 0) return round2(boardAmount);
        return round2(numberValue(purchaseOrder.getTotalAmount()));
    }

    private double numberValue(Number value) {
        return value != null ? value.doubleValue() : 0.0;
    }

    private double round2(double value) {
        return Math.round(value * 100.0) / 100.0;
    }

    @GetMapping("/payable-receivable")
    public Result<Map<String, Object>> payableReceivable() {
        double totalSupplier = purchaseOrderRepo.findAll().stream()
            .filter(p -> !"已退货".equals(p.getStatus()))
            .mapToDouble(p -> p.getTotalAmount() != null ? p.getTotalAmount() : 0).sum();
        double totalCustomer = salesOrderRepo.findAll().stream()
            .mapToDouble(s -> s.getTotalAmount() != null ? s.getTotalAmount() : 0).sum();
        double paidSupplier = paymentRepo.sumByTypeAndParty("付款", "supplier");
        double receivedCustomer = paymentRepo.sumByTypeAndParty("收款", "customer");

        Map<String, Object> m = new LinkedHashMap<>();
        m.put("totalPayable", Math.round(totalSupplier * 100.0) / 100.0);
        m.put("paidSupplier", Math.round(paidSupplier * 100.0) / 100.0);
        m.put("unpaidSupplier", Math.round((totalSupplier - paidSupplier) * 100.0) / 100.0);
        m.put("totalReceivable", Math.round(totalCustomer * 100.0) / 100.0);
        m.put("receivedCustomer", Math.round(receivedCustomer * 100.0) / 100.0);
        m.put("unreceivedCustomer", Math.round((totalCustomer - receivedCustomer) * 100.0) / 100.0);
        return Result.ok(m);
    }
}
