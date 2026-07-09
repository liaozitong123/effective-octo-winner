package com.cartonerp.repository;

import com.cartonerp.entity.Inventory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import java.util.List;

public interface InventoryRepository extends JpaRepository<Inventory, Long>, JpaSpecificationExecutor<Inventory> {
    @Query("SELECT i FROM Inventory i WHERE i.safetyStock > 0 AND i.qty <= i.safetyStock")
    List<Inventory> findStockAlert();
}
