package com.insurancehub.protocol.sftp;

import com.insurancehub.interfacehub.application.execution.AbstractMockInterfaceExecutor;
import com.insurancehub.interfacehub.domain.ProtocolType;
import org.springframework.stereotype.Component;

@Component
public class SftpMockInterfaceExecutor extends AbstractMockInterfaceExecutor {

    @Override
    public ProtocolType supports() {
        return ProtocolType.SFTP;
    }

    @Override
    protected String mockSuccessMessage() {
        return "Mock SFTP transfer validated without opening a session.";
    }
}
