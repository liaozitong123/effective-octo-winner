package com.cartonerp.repository;

import com.cartonerp.entity.SalesOrder;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import java.util.List;

public interface SalesOrderRepository extends JpaRepository<SalesOrder, Long>, JpaSpecificationExecutor<SalesOrder> {
    long countByCreatedAtBetween(java.time.LocalDateTime start, java.time.LocalDateTime end);
    List<SalesOrder> findByCustomerId(Long customerId);
}
