package com.insurancehub.protocol.filetransfer.domain.entity;

import com.insurancehub.common.entity.BaseTimeEntity;
import com.insurancehub.interfacehub.domain.ProtocolType;
import com.insurancehub.interfacehub.domain.entity.InterfaceDefinition;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Entity
@Table(name = "file_transfer_config")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class FileTransferConfig extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "interface_definition_id", nullable = false)
    private InterfaceDefinition interfaceDefinition;

    @Enumerated(EnumType.STRING)
    @Column(name = "transfer_protocol", nullable = false, length = 20)
    private ProtocolType protocolType;

    @Column(name = "host_alias", nullable = false, length = 120)
    private String hostAlias;

    @Column(name = "host", length = 160)
    private String host;

    @Column(name = "port")
    private Integer port;

    @Column(name = "username", length = 120)
    private String username;

    @Column(name = "secret_reference", length = 240)
    private String secretReference;

    @Column(name = "remote_path", nullable = false, length = 500)
    private String remotePath;

    @Column(name = "base_remote_path", length = 500)
    private String baseRemotePath;

    @Column(name = "local_path", nullable = false, length = 500)
    private String localPath;

    @Column(name = "file_name_pattern", length = 240)
    private String fileNamePattern;

    @Column(name = "passive_mode_yn", nullable = false)
    private boolean passiveMode;

    @Column(name = "timeout_millis", nullable = false)
    private Integer timeoutMillis;

    @Column(name = "active_yn", nullable = false)
    private boolean active;

    private FileTransferConfig(
            InterfaceDefinition interfaceDefinition,
            ProtocolType protocolType,
            String host,
            Integer port,
            String username,
            String secretReference,
            String baseRemotePath,
            String localPath,
            String fileNamePattern,
            boolean passiveMode,
            Integer timeoutMillis,
            boolean active
    ) {
        this.interfaceDefinition = interfaceDefinition;
        update(
                protocolType,
                host,
                port,
                username,
                secretReference,
                baseRemotePath,
                localPath,
                fileNamePattern,
                passiveMode,
                timeoutMillis,
                active
        );
    }

    public static FileTransferConfig create(
            InterfaceDefinition interfaceDefinition,
            ProtocolType protocolType,
            String host,
            Integer port,
            String username,
            String secretReference,
            String baseRemotePath,
            String localPath,
            String fileNamePattern,
            boolean passiveMode,
            Integer timeoutMillis,
            boolean active
    ) {
        return new FileTransferConfig(
                interfaceDefinition,
                protocolType,
                host,
                port,
                username,
                secretReference,
                baseRemotePath,
                localPath,
                fileNamePattern,
                passiveMode,
                timeoutMillis,
                active
        );
    }

    public void update(
            ProtocolType protocolType,
            String host,
            Integer port,
            String username,
            String secretReference,
            String baseRemotePath,
            String localPath,
            String fileNamePattern,
            boolean passiveMode,
            Integer timeoutMillis,
            boolean active
    ) {
        this.protocolType = protocolType;
        this.host = trimToNull(host);
        this.hostAlias = this.host == null ? "local-file-transfer" : this.host;
        this.port = port;
        this.username = trimToNull(username);
        this.secretReference = trimToNull(secretReference);
        this.baseRemotePath = normalizeRemotePath(baseRemotePath);
        this.remotePath = this.baseRemotePath;
        this.localPath = normalizeLocalPath(localPath);
        this.fileNamePattern = trimToNull(fileNamePattern);
        this.passiveMode = passiveMode;
        this.timeoutMillis = timeoutMillis;
        this.active = active;
    }

    public String getBaseRemotePath() {
        return baseRemotePath == null ? remotePath : baseRemotePath;
    }

    private String normalizeRemotePath(String value) {
        String trimmed = trimToNull(value);
        if (trimmed == null) {
            return "/";
        }
        String normalized = trimmed.replace('\\', '/');
        return normalized.startsWith("/") ? normalized : "/" + normalized;
    }

    private String normalizeLocalPath(String value) {
        String trimmed = trimToNull(value);
        return trimmed == null ? "build/file-transfer-demo/local" : trimmed;
    }

    private String trimToNull(String value) {
        if (value == null || value.trim().isEmpty()) {
            return null;
        }
        return value.trim();
    }
}
