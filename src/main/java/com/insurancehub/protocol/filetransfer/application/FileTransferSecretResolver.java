package com.insurancehub.protocol.filetransfer.application;

import com.insurancehub.protocol.filetransfer.config.FileTransferProperties;
import com.insurancehub.protocol.filetransfer.presentation.form.FileTransferConfigForm;
import org.springframework.stereotype.Component;

@Component
public class FileTransferSecretResolver {

    private final FileTransferProperties properties;

    public FileTransferSecretResolver(FileTransferProperties properties) {
        this.properties = properties;
    }

    public String resolve(String secretReference) {
        if (FileTransferConfigForm.LOCAL_DEMO_SECRET_REFERENCE.equals(secretReference)) {
            return properties.getDemo().getPassword();
        }
        throw new IllegalArgumentException("Unsupported file transfer secret reference for local demo: " + secretReference);
    }
}
