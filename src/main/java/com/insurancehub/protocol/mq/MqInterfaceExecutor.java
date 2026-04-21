package com.insurancehub.protocol.mq;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.insurancehub.interfacehub.application.execution.ExecutionRequest;
import com.insurancehub.interfacehub.application.execution.ExecutionResult;
import com.insurancehub.interfacehub.application.execution.ExecutionStepLog;
import com.insurancehub.interfacehub.application.execution.InterfaceExecutor;
import com.insurancehub.interfacehub.domain.ExecutionStepStatus;
import com.insurancehub.interfacehub.domain.ProtocolType;
import com.insurancehub.protocol.mq.application.MqChannelConfigService;
import com.insurancehub.protocol.mq.domain.entity.MqChannelConfig;
import com.insurancehub.protocol.mq.domain.entity.MqMessageHistory;
import com.insurancehub.protocol.mq.infrastructure.LocalMqClient;
import com.insurancehub.protocol.mq.infrastructure.repository.MqMessageHistoryRepository;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

@Component
public class MqInterfaceExecutor implements InterfaceExecutor {

    private final MqChannelConfigService mqChannelConfigService;
    private final MqMessageHistoryRepository mqMessageHistoryRepository;
    private final LocalMqClient localMqClient;
    private final ObjectMapper objectMapper;

    public MqInterfaceExecutor(
            MqChannelConfigService mqChannelConfigService,
            MqMessageHistoryRepository mqMessageHistoryRepository,
            LocalMqClient localMqClient,
            ObjectMapper objectMapper
    ) {
        this.mqChannelConfigService = mqChannelConfigService;
        this.mqMessageHistoryRepository = mqMessageHistoryRepository;
        this.localMqClient = localMqClient;
        this.objectMapper = objectMapper;
    }

    @Override
    public ProtocolType supports() {
        return ProtocolType.MQ;
    }

    @Override
    public ExecutionResult execute(ExecutionRequest request) {
        List<ExecutionStepLog> steps = new ArrayList<>();
        MqChannelConfig config;
        LocalDateTime configStartedAt = LocalDateTime.now();
        try {
            config = mqChannelConfigService.getActiveForExecution(request.interfaceDefinition());
            steps.add(step(
                    1,
                    "Load MQ channel configuration",
                    ExecutionStepStatus.SUCCESS,
                    "Active MQ channel configuration loaded.",
                    configStartedAt,
                    LocalDateTime.now()
            ));
        } catch (RuntimeException exception) {
            steps.add(step(1, "Load MQ channel configuration", ExecutionStepStatus.FAILED, readableMessage(exception), configStartedAt, LocalDateTime.now()));
            return ExecutionResult.failure(
                    "MQ_CONFIG_ERROR",
                    readableMessage(exception),
                    failurePayload("MQ_CONFIG_ERROR", readableMessage(exception)),
                    steps
            );
        }

        String payload = StringUtils.hasText(request.requestPayload())
                ? request.requestPayload()
                : MqChannelConfigService.SAMPLE_PAYLOAD.trim();
        String correlationKey = resolveCorrelationKey(config, request);
        MqMessageHistory history = mqMessageHistoryRepository.save(MqMessageHistory.pending(
                request.execution(),
                config,
                correlationKey,
                payload
        ));

        LocalDateTime prepareStartedAt = LocalDateTime.now();
        steps.add(step(
                2,
                "Prepare MQ message",
                ExecutionStepStatus.SUCCESS,
                "Destination " + config.getDestinationName() + ", correlation key " + correlationKey + ".",
                prepareStartedAt,
                LocalDateTime.now()
        ));

        long startedAtNanos = System.nanoTime();
        LocalDateTime publishStartedAt = LocalDateTime.now();
        String messageId;
        try {
            LocalMqClient.PublishedMessage publishedMessage = localMqClient.publish(config.getDestinationName(), payload, correlationKey);
            messageId = publishedMessage.messageId();
            history.markPublished(messageId, LocalDateTime.now());
            steps.add(step(
                    3,
                    "Publish MQ message",
                    ExecutionStepStatus.SUCCESS,
                    "Message published to " + config.getDestinationName() + ".",
                    publishStartedAt,
                    LocalDateTime.now()
            ));
        } catch (RuntimeException exception) {
            long latencyMs = Duration.ofNanos(System.nanoTime() - startedAtNanos).toMillis();
            history.markPublishFailed("MQ_PUBLISH_ERROR", readableMessage(exception), LocalDateTime.now());
            steps.add(step(3, "Publish MQ message", ExecutionStepStatus.FAILED, readableMessage(exception), publishStartedAt, LocalDateTime.now()));
            return ExecutionResult.failure(
                    "MQ_PUBLISH_ERROR",
                    readableMessage(exception),
                    failurePayload("MQ_PUBLISH_ERROR", readableMessage(exception)),
                    steps,
                    config.getDestinationName(),
                    "PUBLISH_CONSUME",
                    correlationKey,
                    requestMetadata(config, "FAILED", "FAILED", null),
                    null,
                    responseMetadata("FAILED", "FAILED", null),
                    latencyMs
            );
        }

        LocalDateTime consumeStartedAt = LocalDateTime.now();
        try {
            LocalMqClient.ConsumedMessage consumedMessage = localMqClient.consume(
                    config.getDestinationName(),
                    correlationKey,
                    config.getTimeoutMillis()
            );
            if (containsFail(consumedMessage.payload())) {
                throw new MqMessageProcessingException("MQ consumer rejected payload because it contains FAIL.", consumedMessage.payload());
            }

            history.markConsumed(consumedMessage.payload(), "Local MQ consumer processed the message.", LocalDateTime.now());
            long latencyMs = Duration.ofNanos(System.nanoTime() - startedAtNanos).toMillis();
            steps.add(step(
                    4,
                    "Consume and process MQ message",
                    ExecutionStepStatus.SUCCESS,
                    "Message consumed and processed in " + latencyMs + " ms.",
                    consumeStartedAt,
                    LocalDateTime.now()
            ));
            return ExecutionResult.success(
                    successPayload(config, messageId, correlationKey, consumedMessage.payload(), latencyMs),
                    steps,
                    config.getDestinationName(),
                    "PUBLISH_CONSUME",
                    correlationKey,
                    requestMetadata(config, "SUCCESS", "SUCCESS", messageId),
                    null,
                    responseMetadata("SUCCESS", "SUCCESS", messageId),
                    latencyMs
            );
        } catch (MqMessageProcessingException exception) {
            long latencyMs = Duration.ofNanos(System.nanoTime() - startedAtNanos).toMillis();
            history.markConsumeFailed(exception.consumedPayload(), "MQ_CONSUME_ERROR", exception.getMessage(), LocalDateTime.now());
            steps.add(step(4, "Consume and process MQ message", ExecutionStepStatus.FAILED, exception.getMessage(), consumeStartedAt, LocalDateTime.now()));
            return ExecutionResult.failure(
                    "MQ_CONSUME_ERROR",
                    exception.getMessage(),
                    failurePayload("MQ_CONSUME_ERROR", exception.getMessage()),
                    steps,
                    config.getDestinationName(),
                    "PUBLISH_CONSUME",
                    correlationKey,
                    requestMetadata(config, "SUCCESS", "FAILED", messageId),
                    null,
                    responseMetadata("SUCCESS", "FAILED", messageId),
                    latencyMs
            );
        } catch (RuntimeException exception) {
            long latencyMs = Duration.ofNanos(System.nanoTime() - startedAtNanos).toMillis();
            history.markConsumeFailed(null, "MQ_CONSUME_ERROR", readableMessage(exception), LocalDateTime.now());
            steps.add(step(4, "Consume and process MQ message", ExecutionStepStatus.FAILED, readableMessage(exception), consumeStartedAt, LocalDateTime.now()));
            return ExecutionResult.failure(
                    "MQ_CONSUME_ERROR",
                    readableMessage(exception),
                    failurePayload("MQ_CONSUME_ERROR", readableMessage(exception)),
                    steps,
                    config.getDestinationName(),
                    "PUBLISH_CONSUME",
                    correlationKey,
                    requestMetadata(config, "SUCCESS", "FAILED", messageId),
                    null,
                    responseMetadata("SUCCESS", "FAILED", messageId),
                    latencyMs
            );
        }
    }

    private String resolveCorrelationKey(MqChannelConfig config, ExecutionRequest request) {
        String expression = config.getCorrelationKeyExpression();
        if (!StringUtils.hasText(expression) || "AUTO".equalsIgnoreCase(expression)) {
            return "MQ-" + request.execution().getExecutionNo();
        }
        return expression
                .replace("{executionNo}", request.execution().getExecutionNo())
                .replace("{interfaceCode}", request.interfaceDefinition().getInterfaceCode());
    }

    private String requestMetadata(MqChannelConfig config, String publishStatus, String consumeStatus, String messageId) {
        return toJson(Map.of(
                "brokerType", config.getBrokerType().name(),
                "messageType", config.getMessageType().name(),
                "routingKey", nullToDash(config.getRoutingKey()),
                "publishStatus", publishStatus,
                "consumeStatus", consumeStatus,
                "messageId", nullToDash(messageId)
        ));
    }

    private String responseMetadata(String publishStatus, String consumeStatus, String messageId) {
        return toJson(Map.of(
                "publishStatus", publishStatus,
                "consumeStatus", consumeStatus,
                "messageId", nullToDash(messageId)
        ));
    }

    private String successPayload(
            MqChannelConfig config,
            String messageId,
            String correlationKey,
            String consumedPayload,
            long latencyMs
    ) {
        return toJson(Map.of(
                "status", "SUCCESS",
                "protocol", "MQ",
                "destination", config.getDestinationName(),
                "messageId", nullToDash(messageId),
                "correlationKey", correlationKey,
                "consumeStatus", "SUCCESS",
                "latencyMs", latencyMs,
                "consumedPayload", consumedPayload
        ));
    }

    private String failurePayload(String code, String message) {
        return toJson(Map.of(
                "status", "FAILED",
                "protocol", "MQ",
                "code", code,
                "message", message
        ));
    }

    private String toJson(Map<String, Object> value) {
        try {
            return objectMapper.writeValueAsString(value);
        } catch (JsonProcessingException exception) {
            throw new IllegalStateException("Could not serialize MQ execution metadata.", exception);
        }
    }

    private boolean containsFail(String value) {
        return StringUtils.hasText(value) && value.toUpperCase().contains("FAIL");
    }

    private String readableMessage(RuntimeException exception) {
        if (StringUtils.hasText(exception.getMessage())) {
            return exception.getMessage();
        }
        return exception.getClass().getSimpleName();
    }

    private String nullToDash(String value) {
        return value == null ? "-" : value;
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

    private static class MqMessageProcessingException extends RuntimeException {

        private final String consumedPayload;

        private MqMessageProcessingException(String message, String consumedPayload) {
            super(message);
            this.consumedPayload = consumedPayload;
        }

        private String consumedPayload() {
            return consumedPayload;
        }
    }
}
