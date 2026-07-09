package com.cartonerp.entity;

import jakarta.persistence.*;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "reconciliations")
public class Reconciliation {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Column(nullable = false, length = 20)
    private String partyType;
    @Column(nullable = false) private Long partyId;
    @Column(length = 120) private String partyName;
    private LocalDate periodStart;
    private LocalDate periodEnd;
    private Double beginBalance = 0.0;
    private Double currentAmount = 0.0;
    private Double paidAmount = 0.0;
    private Double endBalance = 0.0;
    @Column(length = 30) private String status = "未结清";
    private LocalDateTime createdAt = LocalDateTime.now();

    public Reconciliation() {}
    public Long getId() { return id; }
    public String getPartyType() { return partyType; }
    public Long getPartyId() { return partyId; }
    public String getPartyName() { return partyName; }
    public LocalDate getPeriodStart() { return periodStart; }
    public LocalDate getPeriodEnd() { return periodEnd; }
    public Double getBeginBalance() { return beginBalance; }
    public Double getCurrentAmount() { return currentAmount; }
    public Double getPaidAmount() { return paidAmount; }
    public Double getEndBalance() { return endBalance; }
    public String getStatus() { return status; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setId(Long id) { this.id = id; }
    public void setPartyType(String p) { this.partyType = p; }
    public void setPartyId(Long p) { this.partyId = p; }
    public void setPartyName(String p) { this.partyName = p; }
    public void setPeriodStart(LocalDate d) { this.periodStart = d; }
    public void setPeriodEnd(LocalDate d) { this.periodEnd = d; }
    public void setBeginBalance(Double b) { this.beginBalance = b; }
    public void setCurrentAmount(Double c) { this.currentAmount = c; }
    public void setPaidAmount(Double p) { this.paidAmount = p; }
    public void setEndBalance(Double e) { this.endBalance = e; }
    public void setStatus(String s) { this.status = s; }
    public void setCreatedAt(LocalDateTime c) { this.createdAt = c; }
}
