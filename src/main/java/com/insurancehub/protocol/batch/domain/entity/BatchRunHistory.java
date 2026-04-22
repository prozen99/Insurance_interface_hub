package com.insurancehub.protocol.batch.domain.entity;

import java.time.Duration;
import java.time.LocalDateTime;

import com.insurancehub.common.entity.BaseTimeEntity;
import com.insurancehub.interfacehub.domain.entity.InterfaceDefinition;
import com.insurancehub.interfacehub.domain.entity.InterfaceExecution;
import com.insurancehub.protocol.batch.domain.BatchJobType;
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
@Table(name = "batch_run_history")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class BatchRunHistory extends BaseTimeEntity {

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
    @JoinColumn(name = "batch_job_config_id", nullable = false)
    private BatchJobConfig batchJobConfig;

    @Column(name = "spring_batch_job_execution_id")
    private Long springBatchJobExecutionId;

    @Column(name = "job_name", nullable = false, length = 160)
    private String jobName;

    @Enumerated(EnumType.STRING)
    @Column(name = "job_type", nullable = false, length = 80)
    private BatchJobType jobType;

    @Column(name = "job_parameters_json", columnDefinition = "longtext")
    private String jobParametersJson;

    @Column(name = "batch_status", nullable = false, length = 40)
    private String batchStatus;

    @Column(name = "exit_code", length = 80)
    private String exitCode;

    @Column(name = "exit_description", length = 2500)
    private String exitDescription;

    @Column(name = "read_count")
    private Long readCount;

    @Column(name = "write_count")
    private Long writeCount;

    @Column(name = "skip_count")
    private Long skipCount;

    @Column(name = "started_at")
    private LocalDateTime startedAt;

    @Column(name = "finished_at")
    private LocalDateTime finishedAt;

    @Column(name = "latency_ms")
    private Long latencyMs;

    @Column(name = "error_message", length = 2000)
    private String errorMessage;

    @Column(name = "output_summary", length = 2000)
    private String outputSummary;

    private BatchRunHistory(InterfaceExecution execution, BatchJobConfig config, String jobParametersJson) {
        this.execution = execution;
        this.interfaceDefinition = execution.getInterfaceDefinition();
        this.batchJobConfig = config;
        this.jobName = config.getJobName();
        this.jobType = config.getJobType();
        this.jobParametersJson = jobParametersJson;
        this.batchStatus = "STARTING";
    }

    public static BatchRunHistory started(InterfaceExecution execution, BatchJobConfig config, String jobParametersJson) {
        return new BatchRunHistory(execution, config, jobParametersJson);
    }

    public void recordLaunch(Long springBatchJobExecutionId, LocalDateTime startedAt) {
        this.springBatchJobExecutionId = springBatchJobExecutionId;
        this.batchStatus = "STARTED";
        this.startedAt = startedAt;
    }

    public void markCompleted(
            String batchStatus,
            String exitCode,
            String exitDescription,
            long readCount,
            long writeCount,
            long skipCount,
            LocalDateTime startedAt,
            LocalDateTime finishedAt,
            String outputSummary
    ) {
        this.batchStatus = batchStatus;
        this.exitCode = exitCode;
        this.exitDescription = trim(exitDescription, 2500);
        this.readCount = readCount;
        this.writeCount = writeCount;
        this.skipCount = skipCount;
        this.startedAt = startedAt;
        this.finishedAt = finishedAt;
        this.outputSummary = trim(outputSummary, 2000);
        this.errorMessage = null;
        recordLatency();
    }

    public void markFailed(
            String batchStatus,
            String exitCode,
            String exitDescription,
            long readCount,
            long writeCount,
            long skipCount,
            LocalDateTime startedAt,
            LocalDateTime finishedAt,
            String errorMessage,
            String outputSummary
    ) {
        markCompleted(batchStatus, exitCode, exitDescription, readCount, writeCount, skipCount, startedAt, finishedAt, outputSummary);
        this.errorMessage = trim(errorMessage, 2000);
    }

    private void recordLatency() {
        if (startedAt != null && finishedAt != null) {
            this.latencyMs = Duration.between(startedAt, finishedAt).toMillis();
        }
    }

    private String trim(String value, int maxLength) {
        if (value == null || value.length() <= maxLength) {
            return value;
        }
        return value.substring(0, maxLength);
    }
}
