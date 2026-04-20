package com.insurancehub.protocol.ftp;

import com.insurancehub.interfacehub.application.execution.AbstractMockInterfaceExecutor;
import com.insurancehub.interfacehub.domain.ProtocolType;
import org.springframework.stereotype.Component;

@Component
public class FtpMockInterfaceExecutor extends AbstractMockInterfaceExecutor {

    @Override
    public ProtocolType supports() {
        return ProtocolType.FTP;
    }

    @Override
    protected String mockSuccessMessage() {
        return "Mock FTP transfer validated without opening a session.";
    }
}
