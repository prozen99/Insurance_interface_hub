package com.insurancehub.protocol.filetransfer.application;

import java.nio.file.Path;

import com.insurancehub.interfacehub.domain.ProtocolType;
import com.insurancehub.protocol.filetransfer.domain.entity.FileTransferConfig;

public interface FileTransferClient {

    ProtocolType supports();

    void upload(FileTransferConfig config, Path localFile, String remoteFilePath);

    void download(FileTransferConfig config, String remoteFilePath, Path localFile);
}
