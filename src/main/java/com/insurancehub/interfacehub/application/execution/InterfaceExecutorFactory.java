package com.insurancehub.interfacehub.application.execution;

import java.util.EnumMap;
import java.util.List;
import java.util.Map;

import com.insurancehub.interfacehub.domain.ProtocolType;
import org.springframework.stereotype.Component;

@Component
public class InterfaceExecutorFactory {

    private final Map<ProtocolType, InterfaceExecutor> executors;

    public InterfaceExecutorFactory(List<InterfaceExecutor> executors) {
        this.executors = new EnumMap<>(ProtocolType.class);
        for (InterfaceExecutor executor : executors) {
            this.executors.put(executor.supports(), executor);
        }
    }

    public InterfaceExecutor getExecutor(ProtocolType protocolType) {
        InterfaceExecutor executor = executors.get(protocolType);
        if (executor == null) {
            throw new ExecutionNotAllowedException("No executor registered for protocol: " + protocolType);
        }
        return executor;
    }
}
