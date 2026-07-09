package com.cartonerp.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "inventory")
public class Inventory {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Column(nullable = false, length = 30)
    private String itemType;
    @Column(nullable = false, length = 120)
    private String itemName;
    @Column(length = 200) private String spec;
    private Integer qty = 0;
    @Column(length = 20) private String unit;
    @Column(length = 60) private String warehouseLocation;
    private Integer safetyStock = 0;
    private LocalDateTime updatedAt = LocalDateTime.now();

    public Inventory() {}
    public Long getId() { return id; }
    public String getItemType() { return itemType; }
    public String getItemName() { return itemName; }
    public String getSpec() { return spec; }
    public Integer getQty() { return qty; }
    public String getUnit() { return unit; }
    public String getWarehouseLocation() { return warehouseLocation; }
    public Integer getSafetyStock() { return safetyStock; }
    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setId(Long id) { this.id = id; }
    public void setItemType(String t) { this.itemType = t; }
    public void setItemName(String n) { this.itemName = n; }
    public void setSpec(String s) { this.spec = s; }
    public void setQty(Integer q) { this.qty = q; }
    public void setUnit(String u) { this.unit = u; }
    public void setWarehouseLocation(String w) { this.warehouseLocation = w; }
    public void setSafetyStock(Integer s) { this.safetyStock = s; }
    public void setUpdatedAt(LocalDateTime u) { this.updatedAt = u; }
}
