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
    @Column(length = 40) private String deliveryNoteNo;
    private LocalDate deliveryDate;
    private Double receivableAmount = 0.0;
    @Column(length = 40) private String purchaseOrderNo;
    private LocalDate signDate;
    private Double payableAmount = 0.0;
    @Column(length = 30) private String paymentMethod;
    private LocalDate paymentDate;
    @Column(length = 60) private String registrar;
    @Column(length = 60) private String reviewer;
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
    public String getDeliveryNoteNo() { return deliveryNoteNo; }
    public LocalDate getDeliveryDate() { return deliveryDate; }
    public Double getReceivableAmount() { return receivableAmount; }
    public String getPurchaseOrderNo() { return purchaseOrderNo; }
    public LocalDate getSignDate() { return signDate; }
    public Double getPayableAmount() { return payableAmount; }
    public String getPaymentMethod() { return paymentMethod; }
    public LocalDate getPaymentDate() { return paymentDate; }
    public String getRegistrar() { return registrar; }
    public String getReviewer() { return reviewer; }
    public String getNotes() { return notes; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setId(Long id) { this.id = id; }
    public void setPaymentNo(String p) { this.paymentNo = p; }
    public void setPaymentType(String p) { this.paymentType = p; }
    public void setPartyType(String p) { this.partyType = p; }
    public void setPartyId(Long p) { this.partyId = p; }
    public void setPartyName(String p) { this.partyName = p; }
    public void setAmount(Double a) { this.amount = a; }
    public void setDeliveryNoteNo(String d) { this.deliveryNoteNo = d; }
    public void setDeliveryDate(LocalDate d) { this.deliveryDate = d; }
    public void setReceivableAmount(Double r) { this.receivableAmount = r; }
    public void setPurchaseOrderNo(String p) { this.purchaseOrderNo = p; }
    public void setSignDate(LocalDate s) { this.signDate = s; }
    public void setPayableAmount(Double p) { this.payableAmount = p; }
    public void setPaymentMethod(String p) { this.paymentMethod = p; }
    public void setPaymentDate(LocalDate d) { this.paymentDate = d; }
    public void setRegistrar(String r) { this.registrar = r; }
    public void setReviewer(String r) { this.reviewer = r; }
    public void setNotes(String n) { this.notes = n; }
    public void setCreatedAt(LocalDateTime c) { this.createdAt = c; }
}
