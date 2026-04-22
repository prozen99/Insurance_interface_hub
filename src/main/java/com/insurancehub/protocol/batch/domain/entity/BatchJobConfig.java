package com.insurancehub.protocol.batch.domain.entity;

import com.insurancehub.common.entity.BaseTimeEntity;
import com.insurancehub.interfacehub.domain.entity.InterfaceDefinition;
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
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Entity
@Table(name = "batch_job_config")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class BatchJobConfig extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "interface_definition_id", nullable = false)
    private InterfaceDefinition interfaceDefinition;

    @Column(name = "job_name", nullable = false, length = 160)
    private String jobName;

    @Enumerated(EnumType.STRING)
    @Column(name = "job_type", nullable = false, length = 80)
    private BatchJobType jobType;

    @Column(name = "cron_expression", length = 120)
    private String cronExpression;

    @Column(name = "parameter_template_json", columnDefinition = "longtext")
    private String parameterTemplateJson;

    @Column(name = "enabled_yn", nullable = false)
    private boolean enabled;

    @Column(name = "max_parallel_count", nullable = false)
    private int maxParallelCount;

    @Column(name = "retryable_yn", nullable = false)
    private boolean retryable;

    @Column(name = "timeout_millis", nullable = false)
    private Integer timeoutMillis;

    @Column(name = "active_yn", nullable = false)
    private boolean active;

    private BatchJobConfig(
            InterfaceDefinition interfaceDefinition,
            BatchJobType jobType,
            String jobName,
            String cronExpression,
            String parameterTemplateJson,
            boolean enabled,
            int maxParallelCount,
            boolean retryable,
            Integer timeoutMillis,
            boolean active
    ) {
        this.interfaceDefinition = interfaceDefinition;
        update(jobType, jobName, cronExpression, parameterTemplateJson, enabled, maxParallelCount, retryable, timeoutMillis, active);
    }

    public static BatchJobConfig create(
            InterfaceDefinition interfaceDefinition,
            BatchJobType jobType,
            String jobName,
            String cronExpression,
            String parameterTemplateJson,
            boolean enabled,
            int maxParallelCount,
            boolean retryable,
            Integer timeoutMillis,
            boolean active
    ) {
        return new BatchJobConfig(
                interfaceDefinition,
                jobType,
                jobName,
                cronExpression,
                parameterTemplateJson,
                enabled,
                maxParallelCount,
                retryable,
                timeoutMillis,
                active
        );
    }

    public void update(
            BatchJobType jobType,
            String jobName,
            String cronExpression,
            String parameterTemplateJson,
            boolean enabled,
            int maxParallelCount,
            boolean retryable,
            Integer timeoutMillis,
            boolean active
    ) {
        this.jobType = jobType;
        this.jobName = trimToDefault(jobName, jobType.getJobName());
        this.cronExpression = trimToNull(cronExpression);
        this.parameterTemplateJson = trimToDefault(parameterTemplateJson, "{}");
        this.enabled = enabled;
        this.maxParallelCount = maxParallelCount;
        this.retryable = retryable;
        this.timeoutMillis = timeoutMillis;
        this.active = active;
    }

    private String trimToDefault(String value, String defaultValue) {
        String trimmed = trimToNull(value);
        return trimmed == null ? defaultValue : trimmed;
    }

    private String trimToNull(String value) {
        if (value == null || value.trim().isEmpty()) {
            return null;
        }
        return value.trim();
    }
}
