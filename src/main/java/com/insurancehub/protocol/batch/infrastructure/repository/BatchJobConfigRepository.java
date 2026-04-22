package com.insurancehub.protocol.batch.infrastructure.repository;

import java.util.List;
import java.util.Optional;

import com.insurancehub.protocol.batch.domain.entity.BatchJobConfig;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

public interface BatchJobConfigRepository extends JpaRepository<BatchJobConfig, Long> {

    Optional<BatchJobConfig> findByInterfaceDefinitionId(Long interfaceDefinitionId);

    @EntityGraph(attributePaths = {"interfaceDefinition"})
    List<BatchJobConfig> findByActiveTrueAndEnabledTrueOrderByUpdatedAtAsc();
}
