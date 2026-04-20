package com.insurancehub.protocol.batch;

import com.insurancehub.interfacehub.application.execution.AbstractMockInterfaceExecutor;
import com.insurancehub.interfacehub.domain.ProtocolType;
import org.springframework.stereotype.Component;

@Component
public class BatchMockInterfaceExecutor extends AbstractMockInterfaceExecutor {

    @Override
    public ProtocolType supports() {
        return ProtocolType.BATCH;
    }

    @Override
    protected String mockSuccessMessage() {
        return "Mock batch run accepted without launching a real job.";
    }
}
