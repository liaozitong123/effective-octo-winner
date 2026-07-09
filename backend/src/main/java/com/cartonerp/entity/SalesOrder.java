package com.cartonerp.entity;

import jakarta.persistence.*;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "sales_orders")
public class SalesOrder {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Column(unique = true, nullable = false, length = 40)
    private String orderNo;
    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "customer_id")
    private Customer customer;
    @Column(nullable = false, length = 120)
    private String productName;
    @Column(length = 200) private String spec;
    @Column(length = 120) private String material;
    private Integer qty = 0;
    @Column(length = 20) private String unit = "个";
    @Column(length = 30) private String boxType;              // 盒式
    @Column(length = 30) private String fluteType;            // 楞别
    private Double singleArea = 0.0;                           // 单个面积
    @Column(nullable = false) private Double unitPrice = 0.0; // 平方单价
    private Double boxUnitPrice = 0.0;                         // 纸箱单价 = 单个面积 × 平方单价
    @Column(nullable = false) private Double totalAmount = 0.0;
    @Column(length = 30) private String status = "待生产";
    private LocalDate deliveryDate;
    @Column(columnDefinition = "TEXT") private String notes;
    private LocalDateTime createdAt = LocalDateTime.now();

    public SalesOrder() {}
    public Long getId() { return id; }
    public String getOrderNo() { return orderNo; }
    public Customer getCustomer() { return customer; }
    public String getProductName() { return productName; }
    public String getSpec() { return spec; }
    public String getMaterial() { return material; }
    public Integer getQty() { return qty; }
    public String getUnit() { return unit; }
    public String getBoxType() { return boxType; }
    public String getFluteType() { return fluteType; }
    public Double getSingleArea() { return singleArea; }
    public Double getUnitPrice() { return unitPrice; }
    public Double getBoxUnitPrice() { return boxUnitPrice; }
    public Double getTotalAmount() { return totalAmount; }
    public String getStatus() { return status; }
    public LocalDate getDeliveryDate() { return deliveryDate; }
    public String getNotes() { return notes; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setId(Long id) { this.id = id; }
    public void setOrderNo(String o) { this.orderNo = o; }
    public void setCustomer(Customer c) { this.customer = c; }
    public void setProductName(String p) { this.productName = p; }
    public void setSpec(String s) { this.spec = s; }
    public void setMaterial(String m) { this.material = m; }
    public void setQty(Integer q) { this.qty = q; }
    public void setUnit(String u) { this.unit = u; }
    public void setBoxType(String b) { this.boxType = b; }
    public void setFluteType(String f) { this.fluteType = f; }
    public void setSingleArea(Double s) { this.singleArea = s; }
    public void setUnitPrice(Double u) { this.unitPrice = u; }
    public void setBoxUnitPrice(Double b) { this.boxUnitPrice = b; }
    public void setTotalAmount(Double t) { this.totalAmount = t; }
    public void setStatus(String s) { this.status = s; }
    public void setDeliveryDate(LocalDate d) { this.deliveryDate = d; }
    public void setNotes(String n) { this.notes = n; }
    public void setCreatedAt(LocalDateTime c) { this.createdAt = c; }
}
