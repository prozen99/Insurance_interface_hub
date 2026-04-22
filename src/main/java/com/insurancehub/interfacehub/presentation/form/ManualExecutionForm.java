package com.insurancehub.interfacehub.presentation.form;

import com.insurancehub.protocol.filetransfer.domain.TransferDirection;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ManualExecutionForm {

    @Size(max = 12000, message = "Request payload must be 12000 characters or less.")
    private String requestPayload;

    private TransferDirection transferDirection = TransferDirection.UPLOAD;

    @Size(max = 240, message = "Local file name must be 240 characters or less.")
    private String localFileName;

    @Size(max = 1000, message = "Remote file path must be 1000 characters or less.")
    private String remoteFilePath;
}
