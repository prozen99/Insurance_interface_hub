package com.insurancehub.protocol.mq.infrastructure.repository;

import java.util.List;

import com.insurancehub.protocol.mq.domain.entity.MqMessageHistory;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

public interface MqMessageHistoryRepository extends JpaRepository<MqMessageHistory, Long> {

    @EntityGraph(attributePaths = {"execution", "interfaceDefinition", "mqChannelConfig"})
    List<MqMessageHistory> findByExecutionIdOrderByCreatedAtDesc(Long executionId);

    @EntityGraph(attributePaths = {"execution", "interfaceDefinition", "mqChannelConfig"})
    List<MqMessageHistory> findTop20ByOrderByCreatedAtDesc();
}
