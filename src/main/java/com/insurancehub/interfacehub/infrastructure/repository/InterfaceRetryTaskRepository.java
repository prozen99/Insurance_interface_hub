package com.insurancehub.interfacehub.infrastructure.repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import com.insurancehub.interfacehub.domain.RetryStatus;
import com.insurancehub.interfacehub.domain.entity.InterfaceRetryTask;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

public interface InterfaceRetryTaskRepository extends JpaRepository<InterfaceRetryTask, Long> {

    long countByRetryStatus(RetryStatus retryStatus);

    long countByRetryStatusAndCreatedAtBetween(
            RetryStatus retryStatus,
            LocalDateTime startInclusive,
            LocalDateTime endExclusive
    );

    long countByRetryStatusAndLastRetriedAtBetween(
            RetryStatus retryStatus,
            LocalDateTime startInclusive,
            LocalDateTime endExclusive
    );

    Optional<InterfaceRetryTask> findFirstByExecutionIdAndRetryStatusOrderByCreatedAtDesc(Long executionId, RetryStatus retryStatus);

    List<InterfaceRetryTask> findByExecutionIdOrderByCreatedAtDesc(Long executionId);

    @EntityGraph(attributePaths = {"execution", "interfaceDefinition"})
    List<InterfaceRetryTask> findTop10ByOrderByCreatedAtDesc();

    @EntityGraph(attributePaths = {"execution", "interfaceDefinition"})
    List<InterfaceRetryTask> findTop20ByRetryStatusOrderByCreatedAtDesc(RetryStatus retryStatus);

    @EntityGraph(attributePaths = {"execution", "interfaceDefinition"})
    List<InterfaceRetryTask> findByRetryStatusOrderByCreatedAtDesc(RetryStatus retryStatus);
}
