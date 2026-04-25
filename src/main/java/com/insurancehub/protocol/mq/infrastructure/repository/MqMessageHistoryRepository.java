package com.insurancehub.protocol.mq.infrastructure.repository;

import java.time.LocalDateTime;
import java.util.List;

import com.insurancehub.protocol.mq.domain.MqProcessingStatus;
import com.insurancehub.protocol.mq.domain.entity.MqMessageHistory;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

public interface MqMessageHistoryRepository extends JpaRepository<MqMessageHistory, Long> {

    long countByPublishStatusAndCreatedAtBetween(
            MqProcessingStatus publishStatus,
            LocalDateTime startInclusive,
            LocalDateTime endExclusive
    );

    long countByConsumeStatusAndCreatedAtBetween(
            MqProcessingStatus consumeStatus,
            LocalDateTime startInclusive,
            LocalDateTime endExclusive
    );

    @EntityGraph(attributePaths = {"execution", "interfaceDefinition", "mqChannelConfig"})
    List<MqMessageHistory> findByExecutionIdOrderByCreatedAtDesc(Long executionId);

    @EntityGraph(attributePaths = {"execution", "interfaceDefinition", "mqChannelConfig"})
    List<MqMessageHistory> findTop20ByOrderByCreatedAtDesc();
}
