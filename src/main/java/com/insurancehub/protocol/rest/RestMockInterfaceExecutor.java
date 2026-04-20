package com.insurancehub.protocol.rest;

import com.insurancehub.interfacehub.application.execution.AbstractMockInterfaceExecutor;
import com.insurancehub.interfacehub.domain.ProtocolType;
import org.springframework.stereotype.Component;

@Component
public class RestMockInterfaceExecutor extends AbstractMockInterfaceExecutor {

    @Override
    public ProtocolType supports() {
        return ProtocolType.REST;
    }

    @Override
    protected String mockSuccessMessage() {
        return "Mock REST request prepared and accepted.";
    }
}
