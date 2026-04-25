package com.insurancehub.protocol.filetransfer.application;

import com.insurancehub.protocol.filetransfer.domain.TransferDirection;

public record FileTransferExecutionPayload(
        TransferDirection transferDirection,
        String localFileName,
        String remoteFilePath
) {
}
