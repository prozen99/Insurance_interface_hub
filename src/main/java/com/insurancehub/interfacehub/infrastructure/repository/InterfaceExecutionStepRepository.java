package com.insurancehub.interfacehub.infrastructure.repository;

import java.util.List;

import com.insurancehub.interfacehub.domain.entity.InterfaceExecutionStep;
import org.springframework.data.jpa.repository.JpaRepository;

public interface InterfaceExecutionStepRepository extends JpaRepository<InterfaceExecutionStep, Long> {

    List<InterfaceExecutionStep> findByExecutionIdOrderByStepOrderAsc(Long executionId);
}
