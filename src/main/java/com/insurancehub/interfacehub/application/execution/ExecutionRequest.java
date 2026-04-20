package com.insurancehub.interfacehub.application.execution;

import com.insurancehub.interfacehub.domain.ExecutionTriggerType;
import com.insurancehub.interfacehub.domain.entity.InterfaceDefinition;
import com.insurancehub.interfacehub.domain.entity.InterfaceExecution;

public record ExecutionRequest(
        InterfaceDefinition interfaceDefinition,
        InterfaceExecution execution,
        ExecutionTriggerType triggerType,
        String requestPayload
) {
}
