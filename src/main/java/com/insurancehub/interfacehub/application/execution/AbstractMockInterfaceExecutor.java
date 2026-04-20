package com.insurancehub.interfacehub.application.execution;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import com.insurancehub.interfacehub.domain.ExecutionStepStatus;
import com.insurancehub.interfacehub.domain.ProtocolType;
import org.springframework.util.StringUtils;

public abstract class AbstractMockInterfaceExecutor implements InterfaceExecutor {

    @Override
    public ExecutionResult execute(ExecutionRequest request) {
        LocalDateTime startedAt = LocalDateTime.now();
        boolean shouldFail = containsFail(request.interfaceDefinition().getInterfaceCode())
                || containsFail(request.requestPayload());

        List<ExecutionStepLog> steps = new ArrayList<>();
        steps.add(successStep(1, "Load interface definition", "Interface metadata loaded for mock execution."));
        steps.add(successStep(2, "Resolve " + supports() + " mock adapter", "Protocol strategy selected without external network calls."));

        if (shouldFail) {
            steps.add(new ExecutionStepLog(
                    3,
                    "Execute " + supports() + " mock call",
                    ExecutionStepStatus.FAILED,
                    "Mock failure rule matched because interface code or payload contains FAIL.",
                    startedAt.plusNanos(2_000_000),
                    LocalDateTime.now()
            ));
            return ExecutionResult.failure(
                    "MOCK_EXECUTION_FAILED",
                    supports() + " mock executor returned failure by deterministic rule.",
                    "{\"status\":\"FAILED\",\"protocol\":\"" + supports() + "\"}",
                    steps
            );
        }

        steps.add(successStep(3, "Execute " + supports() + " mock call", mockSuccessMessage()));
        return ExecutionResult.success(
                "{\"status\":\"SUCCESS\",\"protocol\":\"" + supports() + "\",\"message\":\"Mock execution completed\"}",
                steps
        );
    }

    protected abstract String mockSuccessMessage();

    private boolean containsFail(String value) {
        return StringUtils.hasText(value) && value.toUpperCase().contains("FAIL");
    }

    private ExecutionStepLog successStep(int order, String name, String message) {
        LocalDateTime now = LocalDateTime.now();
        return new ExecutionStepLog(order, name, ExecutionStepStatus.SUCCESS, message, now, now);
    }

    @Override
    public abstract ProtocolType supports();
}
