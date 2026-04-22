package com.insurancehub.protocol.ftp;

import com.insurancehub.interfacehub.application.execution.ExecutionRequest;
import com.insurancehub.interfacehub.application.execution.ExecutionResult;
import com.insurancehub.interfacehub.application.execution.InterfaceExecutor;
import com.insurancehub.interfacehub.domain.ProtocolType;
import com.insurancehub.protocol.filetransfer.application.FileTransferExecutionService;
import org.springframework.stereotype.Component;

@Component
public class FtpInterfaceExecutor implements InterfaceExecutor {

    private final FileTransferExecutionService fileTransferExecutionService;

    public FtpInterfaceExecutor(FileTransferExecutionService fileTransferExecutionService) {
        this.fileTransferExecutionService = fileTransferExecutionService;
    }

    @Override
    public ProtocolType supports() {
        return ProtocolType.FTP;
    }

    @Override
    public ExecutionResult execute(ExecutionRequest request) {
        return fileTransferExecutionService.execute(request);
    }
}
