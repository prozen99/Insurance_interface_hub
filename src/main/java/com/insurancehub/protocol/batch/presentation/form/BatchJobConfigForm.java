package com.insurancehub.protocol.batch.presentation.form;

import com.insurancehub.protocol.batch.domain.BatchJobType;
import com.insurancehub.protocol.batch.domain.entity.BatchJobConfig;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class BatchJobConfigForm {

    public static final String DEFAULT_PARAMETER_TEMPLATE = """
            {"businessDate":"TODAY","forceFail":false}
            """;

    private Long id;

    @NotNull(message = "Job type is required.")
    private BatchJobType jobType = BatchJobType.INTERFACE_SETTLEMENT_SUMMARY;

    @NotBlank(message = "Job name is required.")
    @Size(max = 160, message = "Job name must be 160 characters or less.")
    private String jobName = BatchJobType.INTERFACE_SETTLEMENT_SUMMARY.getJobName();

    @Size(max = 120, message = "Cron expression must be 120 characters or less.")
    private String cronExpression = "0/30 * * * * *";

    @Size(max = 12000, message = "Parameter template must be 12000 characters or less.")
    private String parameterTemplateJson = DEFAULT_PARAMETER_TEMPLATE.trim();

    private boolean enabled;

    @Min(value = 1, message = "Max parallel count must be at least 1.")
    @Max(value = 1, message = "Phase 7 supports one local demo run at a time.")
    private int maxParallelCount = 1;

    private boolean retryable = true;

    @NotNull(message = "Timeout is required.")
    @Min(value = 1000, message = "Timeout must be at least 1000 ms.")
    @Max(value = 3600000, message = "Timeout must be 3600000 ms or less.")
    private Integer timeoutMillis = 300000;

    private boolean active = true;

    public static BatchJobConfigForm empty() {
        return new BatchJobConfigForm();
    }

    public static BatchJobConfigForm from(BatchJobConfig config) {
        BatchJobConfigForm form = new BatchJobConfigForm();
        form.setId(config.getId());
        form.setJobType(config.getJobType());
        form.setJobName(config.getJobName());
        form.setCronExpression(config.getCronExpression());
        form.setParameterTemplateJson(config.getParameterTemplateJson());
        form.setEnabled(config.isEnabled());
        form.setMaxParallelCount(config.getMaxParallelCount());
        form.setRetryable(config.isRetryable());
        form.setTimeoutMillis(config.getTimeoutMillis());
        form.setActive(config.isActive());
        return form;
    }
}
