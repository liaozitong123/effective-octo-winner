package com.cartonerp.entity;

import jakarta.persistence.*;
import org.hibernate.annotations.NotFound;
import org.hibernate.annotations.NotFoundAction;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "production_orders")
public class ProductionOrder {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Column(unique = true, nullable = false, length = 40)
    private String orderNo;
    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "sales_order_id")
    private SalesOrder salesOrder;
    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "purchase_order_id")
    @NotFound(action = NotFoundAction.IGNORE)
    private PurchaseOrder purchaseOrder;
    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "customer_id")
    private Customer customer;
    @Column(nullable = false, length = 120)
    private String productName;
    @Column(length = 200) private String spec;
    @Column(length = 120) private String material;     // 客户材质
    @Column(length = 30) private String boxType;        // 盒式
    @Column(length = 20) private String stitchType;      // 钉口
    private Double unitPrice = 0.0;                      // 客户平方单价
    private Integer qty = 0;
    @Column(length = 20) private String productionStatus;
    private Boolean printed = false;
    @Column(length = 20) private String unit = "个";
    @Column(length = 30) private String status = "待排产";

    // 生产相关字段
    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "supplier_id")
    private Supplier supplier;                              // 供应商
    @Column(length = 120) private String productionMaterial;// 生产材质
    @Column(length = 30) private String fluteType;         // 楞别
    private Double boardLength = 0.0;                       // 纸板长度
    private Double boardWidth = 0.0;                        // 纸板宽度
    private Integer boardQty = 0;                           // 纸板数量
    private Integer cutCount = 0;                           // 开数
    @Column(length = 50) private String crease;            // 凹压线
    @Column(length = 200) private String specNotes;         // 纸箱规格备注栏
    @Column(length = 10) private String urgent = "否";      // 急单
    private Double orderArea = 0.0;                          // 下单面积
    private Double boardArea = 0.0;                         // 纸板面积
    private Double totalArea = 0.0;                         // 总面积 = 纸板面积 × 纸板数量
    private Double materialBasePrice = 0.0;                 // 材质基价
    private Double discountRate = 100.0;                    // 折率(%)
    private Double boardUnitPrice = 0.0;                    // 纸板平方单价 = 材质基价 × 折率
    private Double profitRate = 0.0;                        // 毛利率
    private Double boardAmount = 0.0;                       // 纸板金额
    private java.time.LocalDate signDate;                   // 签收日期
    private Integer actualQty = 0;                          // 实收数量
    private Double actualAmount = 0.0;                      // 实收金额
    private LocalDate startDate;
    private LocalDate finishDate;
    @Column(length = 60) private String workshop;
    @Column(name = "production_operator", length = 60) private String operator;
    @Column(columnDefinition = "TEXT") private String notes;
    private LocalDateTime createdAt = LocalDateTime.now();

    public ProductionOrder() {}
    public Long getId() { return id; }
    public String getOrderNo() { return orderNo; }
    public SalesOrder getSalesOrder() { return salesOrder; }
    public PurchaseOrder getPurchaseOrder() { return purchaseOrder; }
    public Customer getCustomer() { return customer; }
    public String getProductName() { return productName; }
    public String getSpec() { return spec; }
    public String getMaterial() { return material; }
    public String getBoxType() { return boxType; }
    public String getStitchType() { return stitchType; }
    public Double getUnitPrice() { return unitPrice; }
    public Integer getQty() { return qty; }
    public String getProductionStatus() { return productionStatus; }
    public String getStatus() { return status; }
    public Boolean getPrinted() { return printed; }
    public LocalDate getStartDate() { return startDate; }
    public LocalDate getFinishDate() { return finishDate; }
    public String getWorkshop() { return workshop; }
    public String getOperator() { return operator; }
    public String getNotes() { return notes; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setId(Long id) { this.id = id; }
    public void setOrderNo(String o) { this.orderNo = o; }
    public void setSalesOrder(SalesOrder s) { this.salesOrder = s; }
    public void setPurchaseOrder(PurchaseOrder p) { this.purchaseOrder = p; }
    public void setCustomer(Customer c) { this.customer = c; }
    public void setProductName(String p) { this.productName = p; }
    public void setSpec(String s) { this.spec = s; }
    public void setMaterial(String m) { this.material = m; }
    public void setBoxType(String b) { this.boxType = b; }
    public void setStitchType(String s) { this.stitchType = s; }
    public String getUnit() { return unit; }
    public void setUnit(String u) { this.unit = u; }
    public void setUnitPrice(Double u) { this.unitPrice = u; }
    public void setQty(Integer q) { this.qty = q; }
    public void setProductionStatus(String p) { this.productionStatus = p; }
    public void setStatus(String s) { this.status = s; }
    public void setPrinted(Boolean p) { this.printed = p; }
    public void setStartDate(LocalDate d) { this.startDate = d; }
    public void setFinishDate(LocalDate d) { this.finishDate = d; }
    public void setWorkshop(String w) { this.workshop = w; }
    public void setOperator(String o) { this.operator = o; }
    public void setNotes(String n) { this.notes = n; }
    public void setCreatedAt(LocalDateTime c) { this.createdAt = c; }
    // New production fields
    public Supplier getSupplier() { return supplier; }
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
    public void setSupplier(Supplier s) { this.supplier = s; }
    public void setProductionMaterial(String m) { this.productionMaterial = m; }
    public void setFluteType(String f) { this.fluteType = f; }
    public void setBoardLength(Double l) { this.boardLength = l; }
    public void setBoardWidth(Double w) { this.boardWidth = w; }
    public void setBoardQty(Integer q) { this.boardQty = q; }
    public void setCutCount(Integer c) { this.cutCount = c; }
    public void setCrease(String c) { this.crease = c; }
    public String getSpecNotes() { return specNotes; }
    public String getUrgent() { return urgent; }
    public Double getOrderArea() { return orderArea; }
    public void setSpecNotes(String s) { this.specNotes = s; }
    public void setUrgent(String u) { this.urgent = u; }
    public void setOrderArea(Double a) { this.orderArea = a; }
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
