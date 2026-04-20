package com.insurancehub.interfacehub.application.execution;

import java.time.LocalDateTime;

import com.insurancehub.interfacehub.domain.ExecutionStepStatus;

public record ExecutionStepLog(
        int stepOrder,
        String stepName,
        ExecutionStepStatus stepStatus,
        String message,
        LocalDateTime startedAt,
        LocalDateTime finishedAt
) {
}
