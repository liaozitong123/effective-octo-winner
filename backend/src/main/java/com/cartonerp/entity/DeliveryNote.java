package com.cartonerp.entity;

import jakarta.persistence.*;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "delivery_notes")
public class DeliveryNote {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Column(length = 40)
    private String noteNo;
    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "sales_order_id")
    private SalesOrder salesOrder;
    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "production_order_id")
    private ProductionOrder productionOrder;
    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "customer_id")
    private Customer customer;
    private LocalDate deliveryDate;
    private Integer qty = 0;
    @Column(length = 30) private String status = "未送货";
    private Boolean printed = false;
    @Column(length = 60) private String carrier;
    @Column(length = 60) private String trackingNo;
    @Column(length = 60) private String driver;
    @Column(length = 60) private String issuer;
    @Column(length = 60) private String salesperson;
    @Column(length = 60) private String reviewCount;
    @Column(length = 120) private String customerSignature;
    @Column(columnDefinition = "TEXT") private String notes;
    private LocalDateTime createdAt = LocalDateTime.now();

    @PrePersist
    public void prePersist() {
        if (createdAt == null) createdAt = LocalDateTime.now();
        if (status == null || status.isBlank()) status = "未送货";
        if (printed == null) printed = false;
    }

    public DeliveryNote() {}
    public Long getId() { return id; }
    public String getNoteNo() { return noteNo; }
    public SalesOrder getSalesOrder() { return salesOrder; }
    public ProductionOrder getProductionOrder() { return productionOrder; }
    public Customer getCustomer() { return customer; }
    public LocalDate getDeliveryDate() { return deliveryDate; }
    public Integer getQty() { return qty; }
    public String getStatus() { return status; }
    public Boolean getPrinted() { return printed; }
    public String getCarrier() { return carrier; }
    public String getTrackingNo() { return trackingNo; }
    public String getDriver() { return driver; }
    public String getIssuer() { return issuer; }
    public String getSalesperson() { return salesperson; }
    public String getReviewCount() { return reviewCount; }
    public String getCustomerSignature() { return customerSignature; }
    public String getNotes() { return notes; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setId(Long id) { this.id = id; }
    public void setNoteNo(String n) { this.noteNo = n; }
    public void setSalesOrder(SalesOrder s) { this.salesOrder = s; }
    public void setProductionOrder(ProductionOrder p) { this.productionOrder = p; }
    public void setCustomer(Customer c) { this.customer = c; }
    public void setDeliveryDate(LocalDate d) { this.deliveryDate = d; }
    public void setQty(Integer q) { this.qty = q; }
    public void setStatus(String s) { this.status = s; }
    public void setPrinted(Boolean p) { this.printed = p; }
    public void setCarrier(String c) { this.carrier = c; }
    public void setTrackingNo(String t) { this.trackingNo = t; }
    public void setDriver(String d) { this.driver = d; }
    public void setIssuer(String i) { this.issuer = i; }
    public void setSalesperson(String s) { this.salesperson = s; }
    public void setReviewCount(String r) { this.reviewCount = r; }
    public void setCustomerSignature(String c) { this.customerSignature = c; }
    public void setNotes(String n) { this.notes = n; }
    public void setCreatedAt(LocalDateTime c) { this.createdAt = c; }
}
