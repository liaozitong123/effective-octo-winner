package com.cartonerp.entity;

import jakarta.persistence.*;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "production_records")
public class ProductionRecord {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "production_order_id")
    private ProductionOrder productionOrder;
    @Column(nullable = false) private Integer outputQty = 0;
    private Integer wasteQty = 0;
    @Column(length = 60) private String operator;
    @Column(length = 60) private String nailer;
    @Column(length = 20) private String shift;
    private LocalDate recordDate;
    private LocalDate productionDate;
    private Integer deliveryQty = 0;
    private Integer remainingStock = 0;
    private LocalDate deliveryDate;
    @Column(length = 60) private String driver;
    @Column(columnDefinition = "TEXT") private String notes;
    private LocalDateTime createdAt = LocalDateTime.now();

    public ProductionRecord() {}
    public Long getId() { return id; }
    public ProductionOrder getProductionOrder() { return productionOrder; }
    public Integer getOutputQty() { return outputQty; }
    public Integer getWasteQty() { return wasteQty; }
    public String getOperator() { return operator; }
    public String getNailer() { return nailer; }
    public String getShift() { return shift; }
    public LocalDate getRecordDate() { return recordDate; }
    public LocalDate getProductionDate() { return productionDate; }
    public Integer getDeliveryQty() { return deliveryQty; }
    public Integer getRemainingStock() { return remainingStock; }
    public LocalDate getDeliveryDate() { return deliveryDate; }
    public String getDriver() { return driver; }
    public String getNotes() { return notes; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setId(Long id) { this.id = id; }
    public void setProductionOrder(ProductionOrder p) { this.productionOrder = p; }
    public void setOutputQty(Integer q) { this.outputQty = q; }
    public void setWasteQty(Integer w) { this.wasteQty = w; }
    public void setOperator(String o) { this.operator = o; }
    public void setNailer(String n) { this.nailer = n; }
    public void setShift(String s) { this.shift = s; }
    public void setRecordDate(LocalDate d) { this.recordDate = d; }
    public void setProductionDate(LocalDate d) { this.productionDate = d; }
    public void setDeliveryQty(Integer q) { this.deliveryQty = q; }
    public void setRemainingStock(Integer q) { this.remainingStock = q; }
    public void setDeliveryDate(LocalDate d) { this.deliveryDate = d; }
    public void setDriver(String d) { this.driver = d; }
    public void setNotes(String n) { this.notes = n; }
    public void setCreatedAt(LocalDateTime c) { this.createdAt = c; }
}
