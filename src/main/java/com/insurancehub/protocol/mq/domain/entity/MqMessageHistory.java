package com.insurancehub.protocol.mq.domain.entity;

import java.time.Duration;
import java.time.LocalDateTime;

import com.insurancehub.common.entity.BaseTimeEntity;
import com.insurancehub.interfacehub.domain.entity.InterfaceDefinition;
import com.insurancehub.interfacehub.domain.entity.InterfaceExecution;
import com.insurancehub.protocol.mq.domain.MqMessageType;
import com.insurancehub.protocol.mq.domain.MqProcessingStatus;
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
@Table(name = "mq_message_history")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class MqMessageHistory extends BaseTimeEntity {

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
    @JoinColumn(name = "mq_channel_config_id", nullable = false)
    private MqChannelConfig mqChannelConfig;

    @Column(name = "message_id", length = 160)
    private String messageId;

    @Column(name = "correlation_key", nullable = false, length = 180)
    private String correlationKey;

    @Column(name = "destination_name", nullable = false, length = 180)
    private String destinationName;

    @Enumerated(EnumType.STRING)
    @Column(name = "message_type", nullable = false, length = 60)
    private MqMessageType messageType;

    @Enumerated(EnumType.STRING)
    @Column(name = "publish_status", nullable = false, length = 40)
    private MqProcessingStatus publishStatus;

    @Enumerated(EnumType.STRING)
    @Column(name = "consume_status", nullable = false, length = 40)
    private MqProcessingStatus consumeStatus;

    @Column(name = "outbound_payload", columnDefinition = "longtext")
    private String outboundPayload;

    @Column(name = "consumed_payload", columnDefinition = "longtext")
    private String consumedPayload;

    @Column(name = "result_message", length = 1000)
    private String resultMessage;

    @Column(name = "error_code", length = 80)
    private String errorCode;

    @Column(name = "error_message", length = 2000)
    private String errorMessage;

    @Column(name = "published_at")
    private LocalDateTime publishedAt;

    @Column(name = "consumed_at")
    private LocalDateTime consumedAt;

    @Column(name = "latency_ms")
    private Long latencyMs;

    private MqMessageHistory(
            InterfaceExecution execution,
            MqChannelConfig mqChannelConfig,
            String correlationKey,
            String outboundPayload
    ) {
        this.execution = execution;
        this.interfaceDefinition = execution.getInterfaceDefinition();
        this.mqChannelConfig = mqChannelConfig;
        this.correlationKey = correlationKey;
        this.destinationName = mqChannelConfig.getDestinationName();
        this.messageType = mqChannelConfig.getMessageType();
        this.publishStatus = MqProcessingStatus.PENDING;
        this.consumeStatus = MqProcessingStatus.PENDING;
        this.outboundPayload = outboundPayload;
    }

    public static MqMessageHistory pending(
            InterfaceExecution execution,
            MqChannelConfig mqChannelConfig,
            String correlationKey,
            String outboundPayload
    ) {
        return new MqMessageHistory(execution, mqChannelConfig, correlationKey, outboundPayload);
    }

    public void markPublished(String messageId, LocalDateTime publishedAt) {
        this.messageId = messageId;
        this.publishStatus = MqProcessingStatus.SUCCESS;
        this.publishedAt = publishedAt;
    }

    public void markConsumed(String consumedPayload, String resultMessage, LocalDateTime consumedAt) {
        this.consumeStatus = MqProcessingStatus.SUCCESS;
        this.consumedPayload = consumedPayload;
        this.resultMessage = resultMessage;
        this.consumedAt = consumedAt;
        this.errorCode = null;
        this.errorMessage = null;
        recordLatency();
    }

    public void markPublishFailed(String errorCode, String errorMessage, LocalDateTime failedAt) {
        this.publishStatus = MqProcessingStatus.FAILED;
        this.consumeStatus = MqProcessingStatus.FAILED;
        this.errorCode = errorCode;
        this.errorMessage = errorMessage;
        this.publishedAt = failedAt;
        this.consumedAt = failedAt;
        recordLatency();
    }

    public void markConsumeFailed(String consumedPayload, String errorCode, String errorMessage, LocalDateTime failedAt) {
        this.consumeStatus = MqProcessingStatus.FAILED;
        this.consumedPayload = consumedPayload;
        this.errorCode = errorCode;
        this.errorMessage = errorMessage;
        this.consumedAt = failedAt;
        recordLatency();
    }

    private void recordLatency() {
        if (publishedAt != null && consumedAt != null) {
            this.latencyMs = Duration.between(publishedAt, consumedAt).toMillis();
        }
    }
}
