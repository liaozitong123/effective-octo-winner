package com.cartonerp.entity;

import jakarta.persistence.*;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "payments")
public class Payment {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Column(unique = true, nullable = false, length = 40)
    private String paymentNo;
    @Column(nullable = false, length = 20)
    private String paymentType;    // 收款/付款
    @Column(nullable = false, length = 20)
    private String partyType;      // customer/supplier
    @Column(nullable = false)
    private Long partyId;
    @Column(length = 120) private String partyName;
    @Column(nullable = false) private Double amount = 0.0;
    @Column(length = 30) private String paymentMethod;
    private LocalDate paymentDate;
    @Column(columnDefinition = "TEXT") private String notes;
    private LocalDateTime createdAt = LocalDateTime.now();

    public Payment() {}
    public Long getId() { return id; }
    public String getPaymentNo() { return paymentNo; }
    public String getPaymentType() { return paymentType; }
    public String getPartyType() { return partyType; }
    public Long getPartyId() { return partyId; }
    public String getPartyName() { return partyName; }
    public Double getAmount() { return amount; }
    public String getPaymentMethod() { return paymentMethod; }
    public LocalDate getPaymentDate() { return paymentDate; }
    public String getNotes() { return notes; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setId(Long id) { this.id = id; }
    public void setPaymentNo(String p) { this.paymentNo = p; }
    public void setPaymentType(String p) { this.paymentType = p; }
    public void setPartyType(String p) { this.partyType = p; }
    public void setPartyId(Long p) { this.partyId = p; }
    public void setPartyName(String p) { this.partyName = p; }
    public void setAmount(Double a) { this.amount = a; }
    public void setPaymentMethod(String p) { this.paymentMethod = p; }
    public void setPaymentDate(LocalDate d) { this.paymentDate = d; }
    public void setNotes(String n) { this.notes = n; }
    public void setCreatedAt(LocalDateTime c) { this.createdAt = c; }
}
