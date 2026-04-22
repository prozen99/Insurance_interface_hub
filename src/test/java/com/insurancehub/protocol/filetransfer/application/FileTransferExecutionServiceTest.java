package com.insurancehub.protocol.filetransfer.application;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.when;

import java.net.ServerSocket;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.insurancehub.interfacehub.application.execution.ExecutionRequest;
import com.insurancehub.interfacehub.application.execution.ExecutionResult;
import com.insurancehub.interfacehub.domain.ExecutionTriggerType;
import com.insurancehub.interfacehub.domain.InterfaceDirection;
import com.insurancehub.interfacehub.domain.InterfaceStatus;
import com.insurancehub.interfacehub.domain.MasterStatus;
import com.insurancehub.interfacehub.domain.ProtocolType;
import com.insurancehub.interfacehub.domain.entity.InterfaceDefinition;
import com.insurancehub.interfacehub.domain.entity.InterfaceExecution;
import com.insurancehub.interfacehub.domain.entity.InternalSystem;
import com.insurancehub.interfacehub.domain.entity.PartnerCompany;
import com.insurancehub.protocol.filetransfer.config.FileTransferDemoStorage;
import com.insurancehub.protocol.filetransfer.config.FileTransferProperties;
import com.insurancehub.protocol.filetransfer.config.LocalFileTransferServerConfig;
import com.insurancehub.protocol.filetransfer.domain.TransferDirection;
import com.insurancehub.protocol.filetransfer.domain.entity.FileTransferConfig;
import com.insurancehub.protocol.filetransfer.domain.entity.FileTransferHistory;
import com.insurancehub.protocol.filetransfer.infrastructure.repository.FileTransferHistoryRepository;
import com.insurancehub.protocol.filetransfer.presentation.form.FileTransferConfigForm;
import com.insurancehub.protocol.ftp.FtpFileTransferClient;
import com.insurancehub.protocol.sftp.SftpFileTransferClient;
import org.apache.ftpserver.FtpServer;
import org.apache.sshd.server.SshServer;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.io.TempDir;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

@ExtendWith(MockitoExtension.class)
class FileTransferExecutionServiceTest {

    @TempDir
    private Path tempDir;

    @Mock
    private FileTransferConfigService fileTransferConfigService;

    @Mock
    private FileTransferHistoryRepository fileTransferHistoryRepository;

    private final ObjectMapper objectMapper = new ObjectMapper();

    private FileTransferDemoStorage storage;
    private SshServer sftpServer;
    private FtpServer ftpServer;
    private FileTransferExecutionService service;
    private int sftpPort;
    private int ftpPort;

    @BeforeEach
    void setUp() throws Exception {
        sftpPort = freePort();
        ftpPort = freePort();

        FileTransferProperties properties = new FileTransferProperties();
        properties.getDemo().setRootDirectory(tempDir.resolve("file-transfer-demo").toString());
        properties.getSftp().setPort(sftpPort);
        properties.getFtp().setPort(ftpPort);
        storage = new FileTransferDemoStorage(properties);
        storage.initialize();

        LocalFileTransferServerConfig serverConfig = new LocalFileTransferServerConfig();
        sftpServer = serverConfig.localSftpServer(properties, storage);
        ftpServer = serverConfig.localFtpServer(properties, storage);

        FileTransferSecretResolver secretResolver = new FileTransferSecretResolver(properties);
        service = new FileTransferExecutionService(
                fileTransferConfigService,
                new FileTransferClientFactory(List.of(
                        new SftpFileTransferClient(secretResolver),
                        new FtpFileTransferClient(secretResolver)
                )),
                fileTransferHistoryRepository,
                new FileTransferPayloadCodec(objectMapper),
                objectMapper
        );
        lenient().when(fileTransferHistoryRepository.save(any(FileTransferHistory.class))).thenAnswer(invocation -> invocation.getArgument(0));
    }

    @AfterEach
    void tearDown() throws Exception {
        if (sftpServer != null) {
            sftpServer.stop();
        }
        if (ftpServer != null) {
            ftpServer.stop();
        }
    }

    @Test
    void sftpUploadAndDownloadUseRealLocalServer() throws Exception {
        InterfaceDefinition definition = definition(ProtocolType.SFTP);
        FileTransferConfig config = config(definition, ProtocolType.SFTP, sftpPort);
        when(fileTransferConfigService.getActiveForExecution(definition)).thenReturn(config);

        ExecutionResult upload = service.execute(request(
                definition,
                uploadPayload("/inbox/uploaded-sftp.txt")
        ));

        assertThat(upload.success()).isTrue();
        assertThat(Files.exists(storage.sftpRemoteRoot().resolve("inbox").resolve("uploaded-sftp.txt"))).isTrue();

        ExecutionResult download = service.execute(request(
                definition,
                downloadPayload("/outbox/sample-download.txt", "downloaded-sftp.txt")
        ));

        assertThat(download.success()).isTrue();
        assertThat(Files.readString(storage.localDownloadDirectory().resolve("downloaded-sftp.txt")))
                .contains("Sample file prepared");
    }

    @Test
    void ftpUploadAndDownloadUseRealLocalServer() throws Exception {
        InterfaceDefinition definition = definition(ProtocolType.FTP);
        FileTransferConfig config = config(definition, ProtocolType.FTP, ftpPort);
        when(fileTransferConfigService.getActiveForExecution(definition)).thenReturn(config);

        ExecutionResult upload = service.execute(request(
                definition,
                uploadPayload("/inbox/uploaded-ftp.txt")
        ));

        assertThat(upload.success()).isTrue();
        assertThat(Files.exists(storage.ftpRemoteRoot().resolve("inbox").resolve("uploaded-ftp.txt"))).isTrue();

        ExecutionResult download = service.execute(request(
                definition,
                downloadPayload("/outbox/sample-download.txt", "downloaded-ftp.txt")
        ));

        assertThat(download.success()).isTrue();
        assertThat(Files.readString(storage.localDownloadDirectory().resolve("downloaded-ftp.txt")))
                .contains("Sample file prepared");
    }

    @Test
    void uploadFailsWhenLocalFileDoesNotExist() throws Exception {
        InterfaceDefinition definition = definition(ProtocolType.SFTP);
        FileTransferConfig config = config(definition, ProtocolType.SFTP, sftpPort);
        when(fileTransferConfigService.getActiveForExecution(definition)).thenReturn(config);

        String payload = objectMapper.writeValueAsString(new FileTransferExecutionPayload(
                TransferDirection.UPLOAD,
                "missing-file.txt",
                "/inbox/missing-file.txt"
        ));

        ExecutionResult result = service.execute(request(definition, payload));

        assertThat(result.success()).isFalse();
        assertThat(result.errorCode()).isEqualTo("FILE_TRANSFER_PREPARE_ERROR");
    }

    private ExecutionRequest request(InterfaceDefinition definition, String payload) {
        InterfaceExecution execution = InterfaceExecution.create(
                "EXE-" + definition.getProtocolType(),
                definition,
                null,
                ExecutionTriggerType.MANUAL,
                payload,
                "admin"
        );
        return new ExecutionRequest(definition, execution, ExecutionTriggerType.MANUAL, payload);
    }

    private String uploadPayload(String remotePath) throws Exception {
        return objectMapper.writeValueAsString(new FileTransferExecutionPayload(
                TransferDirection.UPLOAD,
                FileTransferConfigService.SAMPLE_UPLOAD_FILE_NAME,
                remotePath
        ));
    }

    private String downloadPayload(String remotePath, String localFileName) throws Exception {
        return objectMapper.writeValueAsString(new FileTransferExecutionPayload(
                TransferDirection.DOWNLOAD,
                localFileName,
                remotePath
        ));
    }

    private FileTransferConfig config(InterfaceDefinition definition, ProtocolType protocolType, int port) {
        return FileTransferConfig.create(
                definition,
                protocolType,
                "127.0.0.1",
                port,
                "demo",
                FileTransferConfigForm.LOCAL_DEMO_SECRET_REFERENCE,
                "/inbox",
                storage.localBaseDirectory().toString(),
                "*.txt",
                protocolType == ProtocolType.FTP,
                5000,
                true
        );
    }

    private InterfaceDefinition definition(ProtocolType protocolType) {
        InterfaceDefinition definition = InterfaceDefinition.create(
                "IF_" + protocolType + "_POLICY_001",
                protocolType + " policy file interface",
                protocolType,
                InterfaceDirection.BIDIRECTIONAL,
                InterfaceStatus.ACTIVE,
                PartnerCompany.create("LIFEPLUS", "Life Plus Insurance", MasterStatus.ACTIVE, null),
                InternalSystem.create("POLICY_CORE", "Policy Core System", "Insurance Platform Team", MasterStatus.ACTIVE, null),
                null
        );
        ReflectionTestUtils.setField(definition, "id", 1L);
        return definition;
    }

    private int freePort() throws Exception {
        try (ServerSocket socket = new ServerSocket(0)) {
            return socket.getLocalPort();
        }
    }
}
