package com.insurancehub.protocol.mq;

import com.insurancehub.interfacehub.application.execution.AbstractMockInterfaceExecutor;
import com.insurancehub.interfacehub.domain.ProtocolType;
import org.springframework.stereotype.Component;

@Component
public class MqMockInterfaceExecutor extends AbstractMockInterfaceExecutor {

    @Override
    public ProtocolType supports() {
        return ProtocolType.MQ;
    }

    @Override
    protected String mockSuccessMessage() {
        return "Mock MQ message staged without sending to a broker.";
    }
}
