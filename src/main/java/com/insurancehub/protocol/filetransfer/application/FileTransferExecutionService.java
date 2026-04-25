package com.insurancehub.protocol.filetransfer.application;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HexFormat;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.insurancehub.interfacehub.application.execution.ExecutionRequest;
import com.insurancehub.interfacehub.application.execution.ExecutionResult;
import com.insurancehub.interfacehub.application.execution.ExecutionStepLog;
import com.insurancehub.interfacehub.domain.ExecutionStepStatus;
import com.insurancehub.interfacehub.domain.ProtocolType;
import com.insurancehub.protocol.filetransfer.domain.TransferDirection;
import com.insurancehub.protocol.filetransfer.domain.entity.FileTransferConfig;
import com.insurancehub.protocol.filetransfer.domain.entity.FileTransferHistory;
import com.insurancehub.protocol.filetransfer.infrastructure.repository.FileTransferHistoryRepository;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

@Service
public class FileTransferExecutionService {

    private final FileTransferConfigService fileTransferConfigService;
    private final FileTransferClientFactory fileTransferClientFactory;
    private final FileTransferHistoryRepository fileTransferHistoryRepository;
    private final FileTransferPayloadCodec payloadCodec;
    private final ObjectMapper objectMapper;

    public FileTransferExecutionService(
            FileTransferConfigService fileTransferConfigService,
            FileTransferClientFactory fileTransferClientFactory,
            FileTransferHistoryRepository fileTransferHistoryRepository,
            FileTransferPayloadCodec payloadCodec,
            ObjectMapper objectMapper
    ) {
        this.fileTransferConfigService = fileTransferConfigService;
        this.fileTransferClientFactory = fileTransferClientFactory;
        this.fileTransferHistoryRepository = fileTransferHistoryRepository;
        this.payloadCodec = payloadCodec;
        this.objectMapper = objectMapper;
    }

    public ExecutionResult execute(ExecutionRequest request) {
        List<ExecutionStepLog> steps = new ArrayList<>();
        FileTransferConfig config;
        LocalDateTime configStartedAt = LocalDateTime.now();
        try {
            config = fileTransferConfigService.getActiveForExecution(request.interfaceDefinition());
            steps.add(step(
                    1,
                    "Load file transfer configuration",
                    ExecutionStepStatus.SUCCESS,
                    "Active " + request.interfaceDefinition().getProtocolType() + " configuration loaded.",
                    configStartedAt,
                    LocalDateTime.now()
            ));
        } catch (RuntimeException exception) {
            steps.add(step(1, "Load file transfer configuration", ExecutionStepStatus.FAILED, readableMessage(exception), configStartedAt, LocalDateTime.now()));
            return ExecutionResult.failure(
                    "FILE_TRANSFER_CONFIG_ERROR",
                    readableMessage(exception),
                    failurePayload(request.interfaceDefinition().getProtocolType(), "FILE_TRANSFER_CONFIG_ERROR", readableMessage(exception)),
                    steps
            );
        }

        FileTransferExecutionPayload payload = payloadCodec.decode(
                request.requestPayload(),
                defaultRemoteFilePath(config)
        );
        LocalDateTime prepareStartedAt = LocalDateTime.now();
        PreparedTransfer preparedTransfer;
        try {
            preparedTransfer = prepare(config, payload);
            steps.add(step(
                    2,
                    "Prepare file transfer",
                    ExecutionStepStatus.SUCCESS,
                    preparedTransfer.direction() + " " + preparedTransfer.localFileName() + " <-> " + preparedTransfer.remoteFilePath(),
                    prepareStartedAt,
                    LocalDateTime.now()
            ));
        } catch (RuntimeException exception) {
            steps.add(step(2, "Prepare file transfer", ExecutionStepStatus.FAILED, readableMessage(exception), prepareStartedAt, LocalDateTime.now()));
            return ExecutionResult.failure(
                    "FILE_TRANSFER_PREPARE_ERROR",
                    readableMessage(exception),
                    failurePayload(config.getProtocolType(), "FILE_TRANSFER_PREPARE_ERROR", readableMessage(exception)),
                    steps,
                    requestTarget(config, payload.remoteFilePath()),
                    payload.transferDirection().name(),
                    payload.localFileName(),
                    requestMetadata(config),
                    null,
                    null,
                    null
            );
        }

        FileTransferHistory history = fileTransferHistoryRepository.save(FileTransferHistory.started(
                request.execution(),
                config,
                preparedTransfer.direction(),
                preparedTransfer.localFileName(),
                preparedTransfer.localFile().toString(),
                preparedTransfer.remoteFilePath()
        ));

        long startedAtNanos = System.nanoTime();
        LocalDateTime transferStartedAt = LocalDateTime.now();
        try {
            FileTransferClient client = fileTransferClientFactory.getClient(config.getProtocolType());
            if (preparedTransfer.direction() == TransferDirection.UPLOAD) {
                client.upload(config, preparedTransfer.localFile(), preparedTransfer.remoteFilePath());
            } else {
                client.download(config, preparedTransfer.remoteFilePath(), preparedTransfer.localFile());
            }

            long latencyMs = Duration.ofNanos(System.nanoTime() - startedAtNanos).toMillis();
            long fileSize = Files.size(preparedTransfer.localFile());
            String checksum = checksum(preparedTransfer.localFile());
            String summary = contentSummary(preparedTransfer.localFile());
            history.markSuccess(fileSize, latencyMs, checksum, summary);
            steps.add(step(
                    3,
                    "Transfer file via " + config.getProtocolType(),
                    ExecutionStepStatus.SUCCESS,
                    "File transfer completed in " + latencyMs + " ms.",
                    transferStartedAt,
                    LocalDateTime.now()
            ));

            return ExecutionResult.success(
                    successPayload(config, preparedTransfer, fileSize, latencyMs, checksum, summary),
                    steps,
                    requestTarget(config, preparedTransfer.remoteFilePath()),
                    preparedTransfer.direction().name(),
                    preparedTransfer.localFileName(),
                    requestMetadata(config),
                    null,
                    responseMetadata("SUCCESS", fileSize, checksum),
                    latencyMs
            );
        } catch (RuntimeException | IOException exception) {
            long latencyMs = Duration.ofNanos(System.nanoTime() - startedAtNanos).toMillis();
            String message = readableMessage(exception);
            history.markFailed(latencyMs, message);
            steps.add(step(
                    3,
                    "Transfer file via " + config.getProtocolType(),
                    ExecutionStepStatus.FAILED,
                    message,
                    transferStartedAt,
                    LocalDateTime.now()
            ));

            return ExecutionResult.failure(
                    "FILE_TRANSFER_ERROR",
                    message,
                    failurePayload(config.getProtocolType(), "FILE_TRANSFER_ERROR", message),
                    steps,
                    requestTarget(config, preparedTransfer.remoteFilePath()),
                    preparedTransfer.direction().name(),
                    preparedTransfer.localFileName(),
                    requestMetadata(config),
                    null,
                    responseMetadata("FAILED", null, null),
                    latencyMs
            );
        }
    }

    private PreparedTransfer prepare(FileTransferConfig config, FileTransferExecutionPayload payload) {
        TransferDirection direction = payload.transferDirection() == null ? TransferDirection.UPLOAD : payload.transferDirection();
        String localFileName = sanitizeFileName(payload.localFileName());
        String remoteFilePath = normalizeRemoteFilePath(payload.remoteFilePath(), config, localFileName);
        Path localBase = Path.of(config.getLocalPath()).toAbsolutePath().normalize();
        Path localFile = direction == TransferDirection.UPLOAD
                ? localBase.resolve("input").resolve(localFileName).normalize()
                : localBase.resolve("download").resolve(localFileName).normalize();

        assertInside(localBase, localFile);
        if (direction == TransferDirection.UPLOAD && Files.notExists(localFile)) {
            throw new IllegalArgumentException("Local upload file does not exist: " + localFile);
        }
        return new PreparedTransfer(direction, localFileName, localFile, remoteFilePath);
    }

    private String defaultRemoteFilePath(FileTransferConfig config) {
        return joinRemote(config.getBaseRemotePath(), FileTransferConfigService.SAMPLE_UPLOAD_FILE_NAME);
    }

    private String normalizeRemoteFilePath(String remoteFilePath, FileTransferConfig config, String localFileName) {
        String value = StringUtils.hasText(remoteFilePath)
                ? remoteFilePath.trim()
                : joinRemote(config.getBaseRemotePath(), localFileName);
        String normalized = value.replace('\\', '/');
        if (!normalized.startsWith("/")) {
            normalized = joinRemote(config.getBaseRemotePath(), normalized);
        }
        if (normalized.contains("..")) {
            throw new IllegalArgumentException("Remote file path cannot contain '..'.");
        }
        return normalized;
    }

    private String joinRemote(String basePath, String fileName) {
        String base = StringUtils.hasText(basePath) ? basePath.replace('\\', '/') : "/";
        String normalizedBase = base.startsWith("/") ? base : "/" + base;
        if (normalizedBase.endsWith("/")) {
            return normalizedBase + fileName;
        }
        return normalizedBase + "/" + fileName;
    }

    private String sanitizeFileName(String value) {
        if (!StringUtils.hasText(value)) {
            throw new IllegalArgumentException("Local file name is required.");
        }
        String fileName = value.trim();
        if (fileName.contains("..") || fileName.contains("/") || fileName.contains("\\") || Path.of(fileName).isAbsolute()) {
            throw new IllegalArgumentException("Local file name must be a simple file name without path traversal.");
        }
        return fileName;
    }

    private void assertInside(Path base, Path child) {
        if (!child.startsWith(base)) {
            throw new IllegalArgumentException("Local file path must stay inside the configured local directory.");
        }
    }

    private String requestTarget(FileTransferConfig config, String remoteFilePath) {
        return config.getProtocolType().name().toLowerCase() + "://" + config.getHost() + ":" + config.getPort() + remoteFilePath;
    }

    private String successPayload(
            FileTransferConfig config,
            PreparedTransfer preparedTransfer,
            long fileSize,
            long latencyMs,
            String checksum,
            String summary
    ) {
        Map<String, Object> value = new LinkedHashMap<>();
        value.put("status", "SUCCESS");
        value.put("protocol", config.getProtocolType().name());
        value.put("direction", preparedTransfer.direction().name());
        value.put("localFileName", preparedTransfer.localFileName());
        value.put("localFilePath", preparedTransfer.localFile().toString());
        value.put("remoteFilePath", preparedTransfer.remoteFilePath());
        value.put("fileSizeBytes", fileSize);
        value.put("latencyMillis", latencyMs);
        value.put("checksumSha256", checksum);
        value.put("contentSummary", summary);
        return toJson(value);
    }

    private String failurePayload(ProtocolType protocolType, String code, String message) {
        Map<String, Object> value = new LinkedHashMap<>();
        value.put("status", "FAILED");
        value.put("protocol", protocolType.name());
        value.put("code", code);
        value.put("message", message);
        return toJson(value);
    }

    private String requestMetadata(FileTransferConfig config) {
        Map<String, Object> value = new LinkedHashMap<>();
        value.put("host", config.getHost());
        value.put("port", config.getPort());
        value.put("username", config.getUsername());
        value.put("protocol", config.getProtocolType().name());
        value.put("passiveMode", config.isPassiveMode());
        value.put("timeoutMillis", config.getTimeoutMillis());
        return toJson(value);
    }

    private String responseMetadata(String status, Long fileSize, String checksum) {
        Map<String, Object> value = new LinkedHashMap<>();
        value.put("transferStatus", status);
        value.put("fileSizeBytes", fileSize);
        value.put("checksumSha256", checksum);
        return toJson(value);
    }

    private String checksum(Path file) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(Files.readAllBytes(file));
            return HexFormat.of().formatHex(hash);
        } catch (IOException | NoSuchAlgorithmException exception) {
            throw new IllegalStateException("Could not calculate file checksum.", exception);
        }
    }

    private String contentSummary(Path file) throws IOException {
        String content = Files.readString(file, StandardCharsets.UTF_8);
        String compact = content.replace("\r", " ").replace("\n", " ").trim();
        if (compact.length() <= 160) {
            return compact;
        }
        return compact.substring(0, 160);
    }

    private String toJson(Map<String, Object> value) {
        try {
            return objectMapper.writeValueAsString(value);
        } catch (JsonProcessingException exception) {
            throw new IllegalStateException("Could not serialize file transfer metadata.", exception);
        }
    }

    private String readableMessage(Exception exception) {
        if (StringUtils.hasText(exception.getMessage())) {
            return exception.getMessage();
        }
        return exception.getClass().getSimpleName();
    }

    private ExecutionStepLog step(
            int order,
            String name,
            ExecutionStepStatus status,
            String message,
            LocalDateTime startedAt,
            LocalDateTime finishedAt
    ) {
        return new ExecutionStepLog(order, name, status, message, startedAt, finishedAt);
    }

    private record PreparedTransfer(
            TransferDirection direction,
            String localFileName,
            Path localFile,
            String remoteFilePath
    ) {
    }
}
