package com.cartonerp.entity;

import jakarta.persistence.*;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "daily_expenses")
public class DailyExpense {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Column(length = 7) private String recordMonth;
    private LocalDate expenseDate;
    @Column(length = 80) private String category;
    @Column(length = 255) private String details;
    private Double quantity = 0.0;
    private Double unitPrice = 0.0;
    private Double amount = 0.0;
    @Column(length = 80) private String handler;
    @Column(columnDefinition = "TEXT") private String notes;
    private LocalDate settlementDate;
    private LocalDateTime createdAt = LocalDateTime.now();

    public Long getId() { return id; }
    public String getRecordMonth() { return recordMonth; }
    public LocalDate getExpenseDate() { return expenseDate; }
    public String getCategory() { return category; }
    public String getDetails() { return details; }
    public Double getQuantity() { return quantity; }
    public Double getUnitPrice() { return unitPrice; }
    public Double getAmount() { return amount; }
    public String getHandler() { return handler; }
    public String getNotes() { return notes; }
    public LocalDate getSettlementDate() { return settlementDate; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setId(Long id) { this.id = id; }
    public void setRecordMonth(String recordMonth) { this.recordMonth = recordMonth; }
    public void setExpenseDate(LocalDate expenseDate) { this.expenseDate = expenseDate; }
    public void setCategory(String category) { this.category = category; }
    public void setDetails(String details) { this.details = details; }
    public void setQuantity(Double quantity) { this.quantity = quantity; }
    public void setUnitPrice(Double unitPrice) { this.unitPrice = unitPrice; }
    public void setAmount(Double amount) { this.amount = amount; }
    public void setHandler(String handler) { this.handler = handler; }
    public void setNotes(String notes) { this.notes = notes; }
    public void setSettlementDate(LocalDate settlementDate) { this.settlementDate = settlementDate; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
}
