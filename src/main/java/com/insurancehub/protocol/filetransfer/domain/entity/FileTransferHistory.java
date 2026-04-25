package com.insurancehub.protocol.filetransfer.domain.entity;

import com.insurancehub.common.entity.BaseTimeEntity;
import com.insurancehub.interfacehub.domain.ProtocolType;
import com.insurancehub.interfacehub.domain.entity.InterfaceDefinition;
import com.insurancehub.interfacehub.domain.entity.InterfaceExecution;
import com.insurancehub.protocol.filetransfer.domain.FileTransferStatus;
import com.insurancehub.protocol.filetransfer.domain.TransferDirection;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Entity
@Table(name = "file_transfer_history")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class FileTransferHistory extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "interface_execution_id", nullable = false)
    private InterfaceExecution execution;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "interface_definition_id", nullable = false)
    private InterfaceDefinition interfaceDefinition;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "file_transfer_config_id", nullable = false)
    private FileTransferConfig fileTransferConfig;

    @Enumerated(EnumType.STRING)
    @Column(name = "protocol_type", nullable = false, length = 20)
    private ProtocolType protocolType;

    @Enumerated(EnumType.STRING)
    @Column(name = "transfer_direction", nullable = false, length = 30)
    private TransferDirection transferDirection;

    @Column(name = "local_file_name", nullable = false, length = 240)
    private String localFileName;

    @Column(name = "local_file_path", nullable = false, length = 1000)
    private String localFilePath;

    @Column(name = "remote_file_path", nullable = false, length = 1000)
    private String remoteFilePath;

    @Column(name = "file_size_bytes")
    private Long fileSizeBytes;

    @Enumerated(EnumType.STRING)
    @Column(name = "transfer_status", nullable = false, length = 40)
    private FileTransferStatus transferStatus;

    @Column(name = "latency_millis")
    private Long latencyMillis;

    @Column(name = "error_message", length = 2000)
    private String errorMessage;

    @Column(name = "checksum_sha256", length = 80)
    private String checksumSha256;

    @Column(name = "content_summary", length = 500)
    private String contentSummary;

    private FileTransferHistory(
            InterfaceExecution execution,
            FileTransferConfig fileTransferConfig,
            TransferDirection transferDirection,
            String localFileName,
            String localFilePath,
            String remoteFilePath
    ) {
        this.execution = execution;
        this.interfaceDefinition = execution.getInterfaceDefinition();
        this.fileTransferConfig = fileTransferConfig;
        this.protocolType = fileTransferConfig.getProtocolType();
        this.transferDirection = transferDirection;
        this.localFileName = localFileName;
        this.localFilePath = localFilePath;
        this.remoteFilePath = remoteFilePath;
        this.transferStatus = FileTransferStatus.FAILED;
    }

    public static FileTransferHistory started(
            InterfaceExecution execution,
            FileTransferConfig fileTransferConfig,
            TransferDirection transferDirection,
            String localFileName,
            String localFilePath,
            String remoteFilePath
    ) {
        return new FileTransferHistory(
                execution,
                fileTransferConfig,
                transferDirection,
                localFileName,
                localFilePath,
                remoteFilePath
        );
    }

    public void markSuccess(
            long fileSizeBytes,
            long latencyMillis,
            String checksumSha256,
            String contentSummary
    ) {
        this.transferStatus = FileTransferStatus.SUCCESS;
        this.fileSizeBytes = fileSizeBytes;
        this.latencyMillis = latencyMillis;
        this.checksumSha256 = checksumSha256;
        this.contentSummary = contentSummary;
        this.errorMessage = null;
    }

    public void markFailed(long latencyMillis, String errorMessage) {
        this.transferStatus = FileTransferStatus.FAILED;
        this.latencyMillis = latencyMillis;
        this.errorMessage = errorMessage;
    }
}
