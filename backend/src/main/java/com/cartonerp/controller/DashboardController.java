package com.cartonerp.controller;

import com.cartonerp.common.Result;
import com.cartonerp.dto.DashboardDTO;
import com.cartonerp.repository.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;

@RestController
@RequestMapping("/api")
public class DashboardController {

    @Autowired private SalesOrderRepository salesOrderRepo;
    @Autowired private ProductionOrderRepository productionOrderRepo;
    @Autowired private ProductionRecordRepository productionRecordRepo;
    @Autowired private InventoryRepository inventoryRepo;
    @Autowired private CustomerRepository customerRepo;
    @Autowired private SupplierRepository supplierRepo;
    @Autowired private PaymentRepository paymentRepo;
    @Autowired private ReconciliationRepository reconciliationRepo;

    @GetMapping("/dashboard")
    public Result<DashboardDTO> dashboard() {
        LocalDate today = LocalDate.now();
        LocalDateTime todayStart = today.atStartOfDay();
        LocalDateTime todayEnd = today.atTime(LocalTime.MAX);
        DashboardDTO dto = new DashboardDTO();
        dto.setTodayOrders(salesOrderRepo.countByCreatedAtBetween(todayStart, todayEnd));
        // 本月产量 = 产量登记中本月各生产单质检区的产量累加
        LocalDate monthStart = today.withDayOfMonth(1);
        dto.setMonthOutput(productionRecordRepo.findAll().stream()
            .filter(r -> r.getRecordDate() != null && !r.getRecordDate().isBefore(monthStart))
            .filter(r -> "质检区".equals(r.getShift()))
            .collect(java.util.stream.Collectors.groupingBy(
                r -> r.getProductionOrder() != null ? r.getProductionOrder().getId() : 0,
                java.util.stream.Collectors.summingLong(r -> r.getOutputQty() != null ? r.getOutputQty() : 0)))
            .values().stream().mapToLong(Long::longValue).sum());
        dto.setStockAlert(inventoryRepo.findStockAlert().size());
        dto.setTotalCustomers(customerRepo.count());
        dto.setTotalSuppliers(supplierRepo.count());

        double payable = reconciliationRepo.sumUnpaidByPartyType("supplier");       // 应付：欠供应商的
        double receivable = reconciliationRepo.sumUnpaidByPartyType("customer");     // 应收：客户欠我们的
        dto.setReceivable(receivable);
        dto.setPayable(payable);

        return Result.ok(dto);
    }
}
