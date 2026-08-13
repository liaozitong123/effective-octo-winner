package com.cartonerp.repository;

import com.cartonerp.entity.DailyExpense;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

public interface DailyExpenseRepository extends JpaRepository<DailyExpense, Long>, JpaSpecificationExecutor<DailyExpense> {
}
