package com.insurancehub.interfacehub.application.execution;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

import java.net.ServerSocket;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.insurancehub.interfacehub.domain.ExecutionStatus;
import com.insurancehub.interfacehub.domain.ExecutionTriggerType;
import com.insurancehub.interfacehub.domain.InterfaceDirection;
import com.insurancehub.interfacehub.domain.InterfaceStatus;
import com.insurancehub.interfacehub.domain.MasterStatus;
import com.insurancehub.interfacehub.domain.ProtocolType;
import com.insurancehub.interfacehub.domain.RetryStatus;
import com.insurancehub.interfacehub.domain.entity.InterfaceDefinition;
import com.insurancehub.interfacehub.domain.entity.InterfaceExecution;
import com.insurancehub.interfacehub.domain.entity.InterfaceRetryTask;
import com.insurancehub.interfacehub.domain.entity.InternalSystem;
import com.insurancehub.interfacehub.domain.entity.PartnerCompany;
import com.insurancehub.interfacehub.infrastructure.repository.InterfaceDefinitionRepository;
import com.insurancehub.interfacehub.infrastructure.repository.InterfaceExecutionRepository;
import com.insurancehub.interfacehub.infrastructure.repository.InterfaceExecutionStepRepository;
import com.insurancehub.interfacehub.infrastructure.repository.InterfaceRetryTaskRepository;
import com.insurancehub.protocol.filetransfer.application.FileTransferClientFactory;
import com.insurancehub.protocol.filetransfer.application.FileTransferConfigService;
import com.insurancehub.protocol.filetransfer.application.FileTransferExecutionPayload;
import com.insurancehub.protocol.filetransfer.application.FileTransferExecutionService;
import com.insurancehub.protocol.filetransfer.application.FileTransferPayloadCodec;
import com.insurancehub.protocol.filetransfer.application.FileTransferSecretResolver;
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
import com.insurancehub.protocol.sftp.SftpInterfaceExecutor;
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
class InterfaceExecutionFileTransferRetryTest {

    @TempDir
    private Path tempDir;

    @Mock
    private InterfaceDefinitionRepository interfaceDefinitionRepository;

    @Mock
    private InterfaceExecutionRepository interfaceExecutionRepository;

    @Mock
    private InterfaceExecutionStepRepository interfaceExecutionStepRepository;

    @Mock
    private InterfaceRetryTaskRepository interfaceRetryTaskRepository;

    @Mock
    private FileTransferConfigService fileTransferConfigService;

    @Mock
    private FileTransferHistoryRepository fileTransferHistoryRepository;

    private final ObjectMapper objectMapper = new ObjectMapper();

    private FileTransferDemoStorage storage;
    private SshServer sftpServer;
    private FtpServer ftpServer;
    private InterfaceExecutionService service;
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
        FileTransferExecutionService fileTransferExecutionService = new FileTransferExecutionService(
                fileTransferConfigService,
                new FileTransferClientFactory(List.of(
                        new SftpFileTransferClient(secretResolver),
                        new FtpFileTransferClient(secretResolver)
                )),
                fileTransferHistoryRepository,
                new FileTransferPayloadCodec(objectMapper),
                objectMapper
        );

        service = new InterfaceExecutionService(
                interfaceDefinitionRepository,
                interfaceExecutionRepository,
                interfaceExecutionStepRepository,
                interfaceRetryTaskRepository,
                new InterfaceExecutorFactory(List.of(new SftpInterfaceExecutor(fileTransferExecutionService)))
        );
        when(interfaceExecutionRepository.existsByExecutionNo(any())).thenReturn(false);
        when(interfaceExecutionRepository.save(any(InterfaceExecution.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(fileTransferHistoryRepository.save(any(FileTransferHistory.class))).thenAnswer(invocation -> invocation.getArgument(0));
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
    void retryFailedSftpExecutionTransfersFileAgain() throws Exception {
        InterfaceDefinition definition = definition();
        String payload = objectMapper.writeValueAsString(new FileTransferExecutionPayload(
                TransferDirection.UPLOAD,
                FileTransferConfigService.SAMPLE_UPLOAD_FILE_NAME,
                "/inbox/retry-upload.txt"
        ));
        InterfaceExecution original = InterfaceExecution.create(
                "EXE-ORIGINAL",
                definition,
                null,
                ExecutionTriggerType.MANUAL,
                payload,
                "admin"
        );
        ReflectionTestUtils.setField(original, "id", 10L);
        original.markRunning(LocalDateTime.now());
        original.markFailed("FILE_TRANSFER_ERROR", "first transfer failed", "{}", LocalDateTime.now());
        InterfaceRetryTask retryTask = InterfaceRetryTask.waitingFor(original, LocalDateTime.now());

        when(interfaceExecutionRepository.findDetailById(10L)).thenReturn(Optional.of(original));
        when(interfaceRetryTaskRepository.findFirstByExecutionIdAndRetryStatusOrderByCreatedAtDesc(10L, RetryStatus.WAITING))
                .thenReturn(Optional.of(retryTask));
        when(fileTransferConfigService.getActiveForExecution(definition)).thenReturn(config(definition));

        InterfaceExecution retryExecution = service.retryFailedExecution(10L, "admin");

        assertThat(retryExecution.getTriggerType()).isEqualTo(ExecutionTriggerType.RETRY);
        assertThat(retryExecution.getExecutionStatus()).isEqualTo(ExecutionStatus.SUCCESS);
        assertThat(retryExecution.getRequestMethod()).isEqualTo("UPLOAD");
        assertThat(Files.exists(storage.sftpRemoteRoot().resolve("inbox").resolve("retry-upload.txt"))).isTrue();
        assertThat(retryTask.getRetryStatus()).isEqualTo(RetryStatus.DONE);
    }

    private FileTransferConfig config(InterfaceDefinition definition) {
        return FileTransferConfig.create(
                definition,
                ProtocolType.SFTP,
                "127.0.0.1",
                sftpPort,
                "demo",
                FileTransferConfigForm.LOCAL_DEMO_SECRET_REFERENCE,
                "/inbox",
                storage.localBaseDirectory().toString(),
                "*.txt",
                false,
                5000,
                true
        );
    }

    private InterfaceDefinition definition() {
        InterfaceDefinition definition = InterfaceDefinition.create(
                "IF_SFTP_POLICY_001",
                "Policy document SFTP transfer interface",
                ProtocolType.SFTP,
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
