package com.insurancehub.protocol.mq.application;

import java.util.List;

import com.insurancehub.protocol.mq.domain.entity.MqMessageHistory;
import com.insurancehub.protocol.mq.infrastructure.repository.MqMessageHistoryRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class MqMessageHistoryService {

    private final MqMessageHistoryRepository mqMessageHistoryRepository;

    public MqMessageHistoryService(MqMessageHistoryRepository mqMessageHistoryRepository) {
        this.mqMessageHistoryRepository = mqMessageHistoryRepository;
    }

    @Transactional(readOnly = true)
    public List<MqMessageHistory> findByExecutionId(Long executionId) {
        return mqMessageHistoryRepository.findByExecutionIdOrderByCreatedAtDesc(executionId);
    }

    @Transactional(readOnly = true)
    public List<MqMessageHistory> recentMessages() {
        return mqMessageHistoryRepository.findTop20ByOrderByCreatedAtDesc();
    }
}
