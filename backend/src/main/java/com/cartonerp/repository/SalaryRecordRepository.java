package com.cartonerp.repository;

import com.cartonerp.entity.SalaryRecord;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

public interface SalaryRecordRepository extends JpaRepository<SalaryRecord, Long>, JpaSpecificationExecutor<SalaryRecord> {
}
