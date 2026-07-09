package com.cartonerp.repository;

import com.cartonerp.entity.DeliveryNote;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

public interface DeliveryNoteRepository extends JpaRepository<DeliveryNote, Long>, JpaSpecificationExecutor<DeliveryNote> {
}
