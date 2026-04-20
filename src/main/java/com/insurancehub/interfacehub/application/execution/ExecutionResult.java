package com.insurancehub.interfacehub.application.execution;

import java.util.List;

public record ExecutionResult(
        boolean success,
        String responsePayload,
        String errorCode,
        String errorMessage,
        List<ExecutionStepLog> steps
) {

    public static ExecutionResult success(String responsePayload, List<ExecutionStepLog> steps) {
        return new ExecutionResult(true, responsePayload, null, null, steps);
    }

    public static ExecutionResult failure(String errorCode, String errorMessage, String responsePayload, List<ExecutionStepLog> steps) {
        return new ExecutionResult(false, responsePayload, errorCode, errorMessage, steps);
    }
}
