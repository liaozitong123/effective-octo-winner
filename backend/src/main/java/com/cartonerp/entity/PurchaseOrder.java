package com.cartonerp.entity;

import jakarta.persistence.*;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "purchase_orders")
public class PurchaseOrder {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Column(unique = true, nullable = false, length = 40)
    private String orderNo;
    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "supplier_id")
    private Supplier supplier;
    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "customer_id")
    private Customer customer;
    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "sales_order_id")
    private SalesOrder salesOrder;
    @Column(nullable = false, length = 60)
    private String materialType;
    @Column(nullable = false, length = 120)
    private String materialName;
    @Column(length = 200) private String spec;
    private Integer qty = 0;
    @Column(length = 20) private String unit;
    @Column(nullable = false) private Double unitPrice = 0.0;
    @Column(nullable = false) private Double totalAmount = 0.0;
    @Column(length = 30) private String status = "待收货";
    @Column(length = 120) private String productName;
    @Column(length = 120) private String material;
    @Column(length = 30) private String boxType;
    @Column(length = 20) private String stitchType;
    @Column(length = 120) private String productionMaterial;
    @Column(length = 30) private String fluteType;
    private Double boardLength = 0.0;
    private Double boardWidth = 0.0;
    private Integer boardQty = 0;
    private Integer cutCount = 0;
    @Column(length = 50) private String crease;
    private Double boardArea = 0.0;
    private Double totalArea = 0.0;
    private Double materialBasePrice = 0.0;
    private Double discountRate = 100.0;
    private Double boardUnitPrice = 0.0;
    private Double profitRate = 0.0;
    private Double boardAmount = 0.0;
    private java.time.LocalDate signDate;
    private Integer actualQty = 0;
    private Double actualAmount = 0.0;
    private LocalDate orderDate;
    private LocalDate expectedDate;
    @Column(columnDefinition = "TEXT") private String notes;
    private LocalDateTime createdAt = LocalDateTime.now();

    public PurchaseOrder() {}
    public Long getId() { return id; }
    public String getOrderNo() { return orderNo; }
    public Supplier getSupplier() { return supplier; }
    public Customer getCustomer() { return customer; }
    public SalesOrder getSalesOrder() { return salesOrder; }
    public String getMaterialType() { return materialType; }
    public String getMaterialName() { return materialName; }
    public String getSpec() { return spec; }
    public Integer getQty() { return qty; }
    public String getUnit() { return unit; }
    public Double getUnitPrice() { return unitPrice; }
    public Double getTotalAmount() { return totalAmount; }
    public String getStatus() { return status; }
    public LocalDate getOrderDate() { return orderDate; }
    public LocalDate getExpectedDate() { return expectedDate; }
    public String getNotes() { return notes; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setId(Long id) { this.id = id; }
    public void setOrderNo(String o) { this.orderNo = o; }
    public void setSupplier(Supplier s) { this.supplier = s; }
    public void setCustomer(Customer c) { this.customer = c; }
    public void setSalesOrder(SalesOrder s) { this.salesOrder = s; }
    public void setMaterialType(String m) { this.materialType = m; }
    public void setMaterialName(String m) { this.materialName = m; }
    public void setSpec(String s) { this.spec = s; }
    public void setQty(Integer q) { this.qty = q; }
    public void setUnit(String u) { this.unit = u; }
    public void setUnitPrice(Double u) { this.unitPrice = u; }
    public void setTotalAmount(Double t) { this.totalAmount = t; }
    public void setStatus(String s) { this.status = s; }
    public void setOrderDate(LocalDate d) { this.orderDate = d; }
    public void setExpectedDate(LocalDate d) { this.expectedDate = d; }
    public void setNotes(String n) { this.notes = n; }
    public void setCreatedAt(LocalDateTime c) { this.createdAt = c; }
    // New production fields
    public String getProductName() { return productName; }
    public String getMaterial() { return material; }
    public String getBoxType() { return boxType; }
    public String getStitchType() { return stitchType; }
    public String getProductionMaterial() { return productionMaterial; }
    public String getFluteType() { return fluteType; }
    public Double getBoardLength() { return boardLength; }
    public Double getBoardWidth() { return boardWidth; }
    public Integer getBoardQty() { return boardQty; }
    public Integer getCutCount() { return cutCount; }
    public String getCrease() { return crease; }
    public Double getBoardArea() { return boardArea; }
    public Double getTotalArea() { return totalArea; }
    public Double getMaterialBasePrice() { return materialBasePrice; }
    public Double getDiscountRate() { return discountRate; }
    public Double getBoardUnitPrice() { return boardUnitPrice; }
    public Double getProfitRate() { return profitRate; }
    public Double getBoardAmount() { return boardAmount; }
    public java.time.LocalDate getSignDate() { return signDate; }
    public Integer getActualQty() { return actualQty; }
    public Double getActualAmount() { return actualAmount; }
    public void setProductName(String p) { this.productName = p; }
    public void setMaterial(String m) { this.material = m; }
    public void setBoxType(String b) { this.boxType = b; }
    public void setStitchType(String s) { this.stitchType = s; }
    public void setProductionMaterial(String m) { this.productionMaterial = m; }
    public void setFluteType(String f) { this.fluteType = f; }
    public void setBoardLength(Double l) { this.boardLength = l; }
    public void setBoardWidth(Double w) { this.boardWidth = w; }
    public void setBoardQty(Integer q) { this.boardQty = q; }
    public void setCutCount(Integer c) { this.cutCount = c; }
    public void setCrease(String c) { this.crease = c; }
    public void setBoardArea(Double a) { this.boardArea = a; }
    public void setTotalArea(Double a) { this.totalArea = a; }
    public void setMaterialBasePrice(Double p) { this.materialBasePrice = p; }
    public void setDiscountRate(Double r) { this.discountRate = r; }
    public void setBoardUnitPrice(Double p) { this.boardUnitPrice = p; }
    public void setProfitRate(Double r) { this.profitRate = r; }
    public void setBoardAmount(Double a) { this.boardAmount = a; }
    public void setSignDate(java.time.LocalDate d) { this.signDate = d; }
    public void setActualQty(Integer q) { this.actualQty = q; }
    public void setActualAmount(Double a) { this.actualAmount = a; }
}
