package com.insurancehub.protocol.soap;

import com.insurancehub.interfacehub.application.execution.AbstractMockInterfaceExecutor;
import com.insurancehub.interfacehub.domain.ProtocolType;
import org.springframework.stereotype.Component;

@Component
public class SoapMockInterfaceExecutor extends AbstractMockInterfaceExecutor {

    @Override
    public ProtocolType supports() {
        return ProtocolType.SOAP;
    }

    @Override
    protected String mockSuccessMessage() {
        return "Mock SOAP envelope prepared and accepted.";
    }
}
