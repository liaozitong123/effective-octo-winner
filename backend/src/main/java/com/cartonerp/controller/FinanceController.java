package com.cartonerp.controller;

import com.cartonerp.common.Result;
import com.cartonerp.dto.ProfitAnalysisDTO;
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
    @Autowired private PaymentRepository paymentRepo;

    @GetMapping("/profit-analysis")
    public Result<ProfitAnalysisDTO> profitAnalysis(@RequestParam(defaultValue = "0") int year,
                                                      @RequestParam(defaultValue = "0") int month) {
        int y = year > 0 ? year : LocalDate.now().getYear();
        ProfitAnalysisDTO dto = new ProfitAnalysisDTO();
        dto.setYear(y);
        dto.setMonth(month);

        List<SalesOrder> allSales = salesOrderRepo.findAll();
        List<PurchaseOrder> allPurchases = purchaseOrderRepo.findAll();

        double totalRevenue = 0, totalCost = 0;
        List<ProfitAnalysisDTO.MonthlyData> monthly = new ArrayList<>();

        for (int m = 1; m <= 12; m++) {
            final int mm = m;
            double rev = allSales.stream()
                .filter(s -> s.getCreatedAt() != null && s.getCreatedAt().getYear() == y && s.getCreatedAt().getMonthValue() == mm)
                .mapToDouble(s -> s.getTotalAmount() != null ? s.getTotalAmount() : 0).sum();
            double cost = allPurchases.stream()
                .filter(p -> p.getCreatedAt() != null && p.getCreatedAt().getYear() == y && p.getCreatedAt().getMonthValue() == mm)
                .mapToDouble(p -> p.getTotalAmount() != null ? p.getTotalAmount() : 0).sum();
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
