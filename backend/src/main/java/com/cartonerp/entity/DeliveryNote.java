package com.cartonerp.entity;

import jakarta.persistence.*;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "delivery_notes")
public class DeliveryNote {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Column(unique = true, nullable = false, length = 40)
    private String noteNo;
    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "sales_order_id")
    private SalesOrder salesOrder;
    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "customer_id")
    private Customer customer;
    private LocalDate deliveryDate;
    private Integer qty = 0;
    @Column(length = 30) private String status = "已发货";
    @Column(length = 60) private String carrier;
    @Column(length = 60) private String trackingNo;
    @Column(columnDefinition = "TEXT") private String notes;
    private LocalDateTime createdAt = LocalDateTime.now();

    public DeliveryNote() {}
    public Long getId() { return id; }
    public String getNoteNo() { return noteNo; }
    public SalesOrder getSalesOrder() { return salesOrder; }
    public Customer getCustomer() { return customer; }
    public LocalDate getDeliveryDate() { return deliveryDate; }
    public Integer getQty() { return qty; }
    public String getStatus() { return status; }
    public String getCarrier() { return carrier; }
    public String getTrackingNo() { return trackingNo; }
    public String getNotes() { return notes; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setId(Long id) { this.id = id; }
    public void setNoteNo(String n) { this.noteNo = n; }
    public void setSalesOrder(SalesOrder s) { this.salesOrder = s; }
    public void setCustomer(Customer c) { this.customer = c; }
    public void setDeliveryDate(LocalDate d) { this.deliveryDate = d; }
    public void setQty(Integer q) { this.qty = q; }
    public void setStatus(String s) { this.status = s; }
    public void setCarrier(String c) { this.carrier = c; }
    public void setTrackingNo(String t) { this.trackingNo = t; }
    public void setNotes(String n) { this.notes = n; }
    public void setCreatedAt(LocalDateTime c) { this.createdAt = c; }
}
