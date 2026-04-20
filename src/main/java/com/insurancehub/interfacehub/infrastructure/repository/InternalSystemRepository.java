package com.insurancehub.interfacehub.infrastructure.repository;

import java.util.List;

import com.insurancehub.interfacehub.domain.MasterStatus;
import com.insurancehub.interfacehub.domain.entity.InternalSystem;
import org.springframework.data.jpa.repository.JpaRepository;

public interface InternalSystemRepository extends JpaRepository<InternalSystem, Long> {

    List<InternalSystem> findAllByOrderBySystemCodeAsc();

    List<InternalSystem> findByStatusOrderBySystemCodeAsc(MasterStatus status);

    boolean existsBySystemCode(String systemCode);

    boolean existsBySystemCodeAndIdNot(String systemCode, Long id);

    long countByStatus(MasterStatus status);
}
