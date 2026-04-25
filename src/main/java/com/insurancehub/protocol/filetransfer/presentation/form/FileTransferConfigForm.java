package com.insurancehub.protocol.filetransfer.presentation.form;

import com.insurancehub.interfacehub.domain.ProtocolType;
import com.insurancehub.protocol.filetransfer.domain.entity.FileTransferConfig;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class FileTransferConfigForm {

    public static final String LOCAL_DEMO_SECRET_REFERENCE = "LOCAL_DEMO_FILE_TRANSFER_PASSWORD";

    private Long id;

    @NotNull(message = "Protocol type is required.")
    private ProtocolType protocolType;

    @NotBlank(message = "Host is required.")
    @Size(max = 160, message = "Host must be 160 characters or less.")
    private String host = "127.0.0.1";

    @NotNull(message = "Port is required.")
    @Min(value = 1, message = "Port must be at least 1.")
    @Max(value = 65535, message = "Port must be 65535 or less.")
    private Integer port;

    @NotBlank(message = "Username is required.")
    @Size(max = 120, message = "Username must be 120 characters or less.")
    private String username = "demo";

    @NotBlank(message = "Secret reference is required.")
    @Size(max = 240, message = "Secret reference must be 240 characters or less.")
    private String secretReference = LOCAL_DEMO_SECRET_REFERENCE;

    @NotBlank(message = "Base remote path is required.")
    @Size(max = 500, message = "Base remote path must be 500 characters or less.")
    @Pattern(regexp = "^/.*", message = "Base remote path must start with '/'.")
    private String baseRemotePath = "/inbox";

    @NotBlank(message = "Local path is required.")
    @Size(max = 500, message = "Local path must be 500 characters or less.")
    private String localPath = "build/file-transfer-demo/local";

    @Size(max = 240, message = "File name pattern must be 240 characters or less.")
    private String fileNamePattern = "*.txt";

    private boolean passiveMode = true;

    @NotNull(message = "Timeout is required.")
    @Min(value = 100, message = "Timeout must be at least 100 ms.")
    @Max(value = 60000, message = "Timeout must be 60000 ms or less.")
    private Integer timeoutMillis = 5000;

    private boolean active = true;

    public static FileTransferConfigForm empty(ProtocolType protocolType) {
        FileTransferConfigForm form = new FileTransferConfigForm();
        form.setProtocolType(protocolType);
        form.setPort(protocolType == ProtocolType.SFTP ? 10022 : 10021);
        form.setPassiveMode(protocolType == ProtocolType.FTP);
        return form;
    }

    public static FileTransferConfigForm from(FileTransferConfig config) {
        FileTransferConfigForm form = new FileTransferConfigForm();
        form.setId(config.getId());
        form.setProtocolType(config.getProtocolType());
        form.setHost(config.getHost());
        form.setPort(config.getPort());
        form.setUsername(config.getUsername());
        form.setSecretReference(config.getSecretReference());
        form.setBaseRemotePath(config.getBaseRemotePath());
        form.setLocalPath(config.getLocalPath());
        form.setFileNamePattern(config.getFileNamePattern());
        form.setPassiveMode(config.isPassiveMode());
        form.setTimeoutMillis(config.getTimeoutMillis());
        form.setActive(config.isActive());
        return form;
    }
}
