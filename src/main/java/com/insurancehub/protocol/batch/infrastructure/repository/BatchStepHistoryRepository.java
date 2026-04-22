package com.insurancehub.protocol.batch.infrastructure.repository;

import java.util.List;

import com.insurancehub.protocol.batch.domain.entity.BatchStepHistory;
import org.springframework.data.jpa.repository.JpaRepository;

public interface BatchStepHistoryRepository extends JpaRepository<BatchStepHistory, Long> {

    List<BatchStepHistory> findByBatchRunHistoryIdOrderByIdAsc(Long batchRunHistoryId);
}
