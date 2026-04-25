package com.insurancehub.protocol.batch.infrastructure.repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import com.insurancehub.protocol.batch.domain.entity.BatchRunHistory;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

public interface BatchRunHistoryRepository extends JpaRepository<BatchRunHistory, Long> {

    long countByBatchStatusAndCreatedAtBetween(
            String batchStatus,
            LocalDateTime startInclusive,
            LocalDateTime endExclusive
    );

    @EntityGraph(attributePaths = {"execution", "interfaceDefinition", "batchJobConfig"})
    List<BatchRunHistory> findTop50ByOrderByCreatedAtDesc();

    @EntityGraph(attributePaths = {"execution", "interfaceDefinition", "batchJobConfig"})
    List<BatchRunHistory> findByExecutionIdOrderByCreatedAtDesc(Long executionId);

    @EntityGraph(attributePaths = {"execution", "interfaceDefinition", "batchJobConfig"})
    Optional<BatchRunHistory> findDetailById(Long id);
}
