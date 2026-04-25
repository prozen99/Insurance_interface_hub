package com.insurancehub.protocol.filetransfer.application;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.insurancehub.interfacehub.presentation.form.ManualExecutionForm;
import com.insurancehub.protocol.filetransfer.domain.TransferDirection;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

@Component
public class FileTransferPayloadCodec {

    private final ObjectMapper objectMapper;

    public FileTransferPayloadCodec(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    public String encode(ManualExecutionForm form) {
        FileTransferExecutionPayload payload = new FileTransferExecutionPayload(
                form.getTransferDirection(),
                trimToNull(form.getLocalFileName()),
                trimToNull(form.getRemoteFilePath())
        );
        try {
            return objectMapper.writeValueAsString(payload);
        } catch (JsonProcessingException exception) {
            throw new IllegalStateException("Could not serialize file transfer execution payload.", exception);
        }
    }

    public FileTransferExecutionPayload decode(String requestPayload, String defaultRemotePath) {
        if (!StringUtils.hasText(requestPayload)) {
            return defaultPayload(defaultRemotePath);
        }

        try {
            FileTransferExecutionPayload payload = objectMapper.readValue(requestPayload, FileTransferExecutionPayload.class);
            return new FileTransferExecutionPayload(
                    payload.transferDirection() == null ? TransferDirection.UPLOAD : payload.transferDirection(),
                    trimToDefault(payload.localFileName(), FileTransferConfigService.SAMPLE_UPLOAD_FILE_NAME),
                    trimToDefault(payload.remoteFilePath(), defaultRemotePath)
            );
        } catch (JsonProcessingException exception) {
            return new FileTransferExecutionPayload(
                    TransferDirection.UPLOAD,
                    FileTransferConfigService.SAMPLE_UPLOAD_FILE_NAME,
                    defaultRemotePath
            );
        }
    }

    public FileTransferExecutionPayload defaultPayload(String defaultRemotePath) {
        return new FileTransferExecutionPayload(
                TransferDirection.UPLOAD,
                FileTransferConfigService.SAMPLE_UPLOAD_FILE_NAME,
                defaultRemotePath
        );
    }

    private String trimToDefault(String value, String defaultValue) {
        String trimmed = trimToNull(value);
        return trimmed == null ? defaultValue : trimmed;
    }

    private String trimToNull(String value) {
        if (!StringUtils.hasText(value)) {
            return null;
        }
        return value.trim();
    }
}
