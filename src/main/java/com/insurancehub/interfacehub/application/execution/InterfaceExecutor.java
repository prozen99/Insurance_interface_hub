package com.insurancehub.interfacehub.application.execution;

import com.insurancehub.interfacehub.domain.ProtocolType;

public interface InterfaceExecutor {

    ProtocolType supports();

    ExecutionResult execute(ExecutionRequest request);
}
