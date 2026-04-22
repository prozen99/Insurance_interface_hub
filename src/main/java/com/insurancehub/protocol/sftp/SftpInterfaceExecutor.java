package com.insurancehub.protocol.sftp;

import com.insurancehub.interfacehub.application.execution.ExecutionRequest;
import com.insurancehub.interfacehub.application.execution.ExecutionResult;
import com.insurancehub.interfacehub.application.execution.InterfaceExecutor;
import com.insurancehub.interfacehub.domain.ProtocolType;
import com.insurancehub.protocol.filetransfer.application.FileTransferExecutionService;
import org.springframework.stereotype.Component;

@Component
public class SftpInterfaceExecutor implements InterfaceExecutor {

    private final FileTransferExecutionService fileTransferExecutionService;

    public SftpInterfaceExecutor(FileTransferExecutionService fileTransferExecutionService) {
        this.fileTransferExecutionService = fileTransferExecutionService;
    }

    @Override
    public ProtocolType supports() {
        return ProtocolType.SFTP;
    }

    @Override
    public ExecutionResult execute(ExecutionRequest request) {
        return fileTransferExecutionService.execute(request);
    }
}
