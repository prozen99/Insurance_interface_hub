package com.insurancehub.interfacehub.domain.entity;

import java.time.Duration;
import java.time.LocalDateTime;

import com.insurancehub.common.entity.BaseTimeEntity;
import com.insurancehub.interfacehub.domain.ExecutionStatus;
import com.insurancehub.interfacehub.domain.ExecutionTriggerType;
import com.insurancehub.interfacehub.domain.ProtocolType;
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
@Table(name = "interface_execution")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class InterfaceExecution extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "execution_no", nullable = false, unique = true, length = 120)
    private String executionNo;

    @Column(name = "execution_key", nullable = false, unique = true, length = 120)
    private String executionKey;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "interface_definition_id", nullable = false)
    private InterfaceDefinition interfaceDefinition;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "retry_source_execution_id")
    private InterfaceExecution retrySourceExecution;

    @Enumerated(EnumType.STRING)
    @Column(name = "protocol_type", nullable = false, length = 30)
    private ProtocolType protocolType;

    @Enumerated(EnumType.STRING)
    @Column(name = "trigger_type", nullable = false, length = 40)
    private ExecutionTriggerType triggerType;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 40)
    private ExecutionStatus executionStatus;

    @Column(name = "requested_by", length = 120)
    private String requestedBy;

    @Column(name = "request_payload", columnDefinition = "longtext")
    private String requestPayload;

    @Column(name = "request_url", length = 1000)
    private String requestUrl;

    @Column(name = "request_method", length = 20)
    private String requestMethod;

    @Column(name = "protocol_action", length = 300)
    private String protocolAction;

    @Column(name = "request_headers", columnDefinition = "longtext")
    private String requestHeaders;

    @Column(name = "response_payload", columnDefinition = "longtext")
    private String responsePayload;

    @Column(name = "response_status_code")
    private Integer responseStatusCode;

    @Column(name = "response_headers", columnDefinition = "longtext")
    private String responseHeaders;

    @Column(name = "latency_ms")
    private Long latencyMs;

    @Column(name = "started_at")
    private LocalDateTime startedAt;

    @Column(name = "finished_at")
    private LocalDateTime finishedAt;

    @Column(name = "duration_ms")
    private Long durationMs;

    @Column(name = "error_code", length = 80)
    private String errorCode;

    @Column(name = "error_message", length = 2000)
    private String errorMessage;

    private InterfaceExecution(
            String executionNo,
            InterfaceDefinition interfaceDefinition,
            InterfaceExecution retrySourceExecution,
            ExecutionTriggerType triggerType,
            String requestPayload,
            String requestedBy
    ) {
        this.executionNo = executionNo;
        this.executionKey = executionNo;
        this.interfaceDefinition = interfaceDefinition;
        this.retrySourceExecution = retrySourceExecution;
        this.protocolType = interfaceDefinition.getProtocolType();
        this.triggerType = triggerType;
        this.executionStatus = ExecutionStatus.PENDING;
        this.requestPayload = requestPayload;
        this.requestedBy = requestedBy;
    }

    public static InterfaceExecution create(
            String executionNo,
            InterfaceDefinition interfaceDefinition,
            InterfaceExecution retrySourceExecution,
            ExecutionTriggerType triggerType,
            String requestPayload,
            String requestedBy
    ) {
        return new InterfaceExecution(
                executionNo,
                interfaceDefinition,
                retrySourceExecution,
                triggerType,
                requestPayload,
                requestedBy
        );
    }

    public void markRunning(LocalDateTime startedAt) {
        this.executionStatus = ExecutionStatus.RUNNING;
        this.startedAt = startedAt;
    }

    public void recordHttpExchange(
            String requestUrl,
            String requestMethod,
            String requestHeaders,
            Integer responseStatusCode,
            String responseHeaders,
            Long latencyMs
    ) {
        recordHttpExchange(requestUrl, requestMethod, null, requestHeaders, responseStatusCode, responseHeaders, latencyMs);
    }

    public void recordHttpExchange(
            String requestUrl,
            String requestMethod,
            String protocolAction,
            String requestHeaders,
            Integer responseStatusCode,
            String responseHeaders,
            Long latencyMs
    ) {
        this.requestUrl = requestUrl;
        this.requestMethod = requestMethod;
        this.protocolAction = protocolAction;
        this.requestHeaders = requestHeaders;
        this.responseStatusCode = responseStatusCode;
        this.responseHeaders = responseHeaders;
        this.latencyMs = latencyMs;
    }

    public void markSuccess(String responsePayload, LocalDateTime finishedAt) {
        this.executionStatus = ExecutionStatus.SUCCESS;
        this.responsePayload = responsePayload;
        this.errorCode = null;
        this.errorMessage = null;
        finish(finishedAt);
    }

    public void markFailed(String errorCode, String errorMessage, String responsePayload, LocalDateTime finishedAt) {
        this.executionStatus = ExecutionStatus.FAILED;
        this.errorCode = errorCode;
        this.errorMessage = errorMessage;
        this.responsePayload = responsePayload;
        finish(finishedAt);
    }

    public boolean isFailed() {
        return executionStatus == ExecutionStatus.FAILED;
    }

    private void finish(LocalDateTime finishedAt) {
        this.finishedAt = finishedAt;
        if (startedAt != null && finishedAt != null) {
            this.durationMs = Duration.between(startedAt, finishedAt).toMillis();
        }
    }
}
