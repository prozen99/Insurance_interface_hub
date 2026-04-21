package com.insurancehub.interfacehub.application.execution;

import java.util.List;

public record ExecutionResult(
        boolean success,
        String responsePayload,
        String errorCode,
        String errorMessage,
        List<ExecutionStepLog> steps,
        String requestUrl,
        String requestMethod,
        String protocolAction,
        String requestHeaders,
        Integer responseStatusCode,
        String responseHeaders,
        Long latencyMs
) {

    public static ExecutionResult success(String responsePayload, List<ExecutionStepLog> steps) {
        return success(responsePayload, steps, null, null, null, null, null, null);
    }

    public static ExecutionResult success(
            String responsePayload,
            List<ExecutionStepLog> steps,
            String requestUrl,
            String requestMethod,
            String requestHeaders,
            Integer responseStatusCode,
            String responseHeaders,
            Long latencyMs
    ) {
        return success(responsePayload, steps, requestUrl, requestMethod, null, requestHeaders, responseStatusCode, responseHeaders, latencyMs);
    }

    public static ExecutionResult success(
            String responsePayload,
            List<ExecutionStepLog> steps,
            String requestUrl,
            String requestMethod,
            String protocolAction,
            String requestHeaders,
            Integer responseStatusCode,
            String responseHeaders,
            Long latencyMs
    ) {
        return new ExecutionResult(
                true,
                responsePayload,
                null,
                null,
                steps,
                requestUrl,
                requestMethod,
                protocolAction,
                requestHeaders,
                responseStatusCode,
                responseHeaders,
                latencyMs
        );
    }

    public static ExecutionResult failure(String errorCode, String errorMessage, String responsePayload, List<ExecutionStepLog> steps) {
        return failure(errorCode, errorMessage, responsePayload, steps, null, null, null, null, null, null);
    }

    public static ExecutionResult failure(
            String errorCode,
            String errorMessage,
            String responsePayload,
            List<ExecutionStepLog> steps,
            String requestUrl,
            String requestMethod,
            String requestHeaders,
            Integer responseStatusCode,
            String responseHeaders,
            Long latencyMs
    ) {
        return failure(errorCode, errorMessage, responsePayload, steps, requestUrl, requestMethod, null, requestHeaders, responseStatusCode, responseHeaders, latencyMs);
    }

    public static ExecutionResult failure(
            String errorCode,
            String errorMessage,
            String responsePayload,
            List<ExecutionStepLog> steps,
            String requestUrl,
            String requestMethod,
            String protocolAction,
            String requestHeaders,
            Integer responseStatusCode,
            String responseHeaders,
            Long latencyMs
    ) {
        return new ExecutionResult(
                false,
                responsePayload,
                errorCode,
                errorMessage,
                steps,
                requestUrl,
                requestMethod,
                protocolAction,
                requestHeaders,
                responseStatusCode,
                responseHeaders,
                latencyMs
        );
    }
}
