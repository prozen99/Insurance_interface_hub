package com.insurancehub.interfacehub.domain.entity;

import java.time.LocalDateTime;

import com.insurancehub.common.entity.BaseTimeEntity;
import com.insurancehub.interfacehub.domain.RetryStatus;
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
@Table(name = "interface_retry_task")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class InterfaceRetryTask extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "interface_execution_id", nullable = false)
    private InterfaceExecution execution;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "interface_definition_id", nullable = false)
    private InterfaceDefinition interfaceDefinition;

    @Enumerated(EnumType.STRING)
    @Column(name = "retry_status", nullable = false, length = 40)
    private RetryStatus retryStatus;

    @Column(name = "retry_count", nullable = false)
    private int retryCount;

    @Column(name = "max_retry_count", nullable = false)
    private int maxRetryCount;

    @Column(name = "next_retry_at")
    private LocalDateTime nextRetryAt;

    @Column(name = "last_retried_at")
    private LocalDateTime lastRetriedAt;

    @Column(name = "last_error_message", length = 2000)
    private String lastErrorMessage;

    private InterfaceRetryTask(InterfaceExecution execution, LocalDateTime nextRetryAt) {
        this.execution = execution;
        this.interfaceDefinition = execution.getInterfaceDefinition();
        this.retryStatus = RetryStatus.WAITING;
        this.retryCount = 0;
        this.maxRetryCount = 3;
        this.nextRetryAt = nextRetryAt;
        this.lastErrorMessage = execution.getErrorMessage();
    }

    public static InterfaceRetryTask waitingFor(InterfaceExecution execution, LocalDateTime nextRetryAt) {
        return new InterfaceRetryTask(execution, nextRetryAt);
    }

    public void markDone(LocalDateTime retriedAt) {
        this.retryStatus = RetryStatus.DONE;
        this.retryCount++;
        this.lastRetriedAt = retriedAt;
        this.nextRetryAt = null;
    }

    public void cancel() {
        this.retryStatus = RetryStatus.CANCELLED;
        this.nextRetryAt = null;
    }
}
