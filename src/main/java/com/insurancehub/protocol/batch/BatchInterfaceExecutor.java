package com.insurancehub.protocol.batch;

import com.insurancehub.interfacehub.application.execution.ExecutionRequest;
import com.insurancehub.interfacehub.application.execution.ExecutionResult;
import com.insurancehub.interfacehub.application.execution.InterfaceExecutor;
import com.insurancehub.interfacehub.domain.ProtocolType;
import com.insurancehub.protocol.batch.application.BatchExecutionService;
import org.springframework.stereotype.Component;

@Component
public class BatchInterfaceExecutor implements InterfaceExecutor {

    private final BatchExecutionService batchExecutionService;

    public BatchInterfaceExecutor(BatchExecutionService batchExecutionService) {
        this.batchExecutionService = batchExecutionService;
    }

    @Override
    public ProtocolType supports() {
        return ProtocolType.BATCH;
    }

    @Override
    public ExecutionResult execute(ExecutionRequest request) {
        return batchExecutionService.execute(request);
    }
}
