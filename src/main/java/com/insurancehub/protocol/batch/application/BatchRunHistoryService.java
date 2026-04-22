package com.insurancehub.protocol.batch.application;

import java.util.List;

import com.insurancehub.protocol.batch.domain.entity.BatchRunHistory;
import com.insurancehub.protocol.batch.domain.entity.BatchStepHistory;
import com.insurancehub.protocol.batch.infrastructure.repository.BatchRunHistoryRepository;
import com.insurancehub.protocol.batch.infrastructure.repository.BatchStepHistoryRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

@Service
public class BatchRunHistoryService {

    private final BatchRunHistoryRepository batchRunHistoryRepository;
    private final BatchStepHistoryRepository batchStepHistoryRepository;

    public BatchRunHistoryService(
            BatchRunHistoryRepository batchRunHistoryRepository,
            BatchStepHistoryRepository batchStepHistoryRepository
    ) {
        this.batchRunHistoryRepository = batchRunHistoryRepository;
        this.batchStepHistoryRepository = batchStepHistoryRepository;
    }

    @Transactional(readOnly = true)
    public List<BatchRunHistory> recentRuns() {
        return batchRunHistoryRepository.findTop50ByOrderByCreatedAtDesc();
    }

    @Transactional(readOnly = true)
    public List<BatchRunHistory> findByExecutionId(Long executionId) {
        return batchRunHistoryRepository.findByExecutionIdOrderByCreatedAtDesc(executionId);
    }

    @Transactional(readOnly = true)
    public BatchRunHistory getDetail(Long batchRunId) {
        return batchRunHistoryRepository.findDetailById(batchRunId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Batch run not found"));
    }

    @Transactional(readOnly = true)
    public List<BatchStepHistory> getSteps(Long batchRunId) {
        return batchStepHistoryRepository.findByBatchRunHistoryIdOrderByIdAsc(batchRunId);
    }
}
