package com.insurancehub.protocol.batch.application;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.insurancehub.interfacehub.application.execution.ExecutionRequest;
import com.insurancehub.interfacehub.application.execution.ExecutionResult;
import com.insurancehub.interfacehub.application.execution.ExecutionStepLog;
import com.insurancehub.interfacehub.domain.ExecutionStepStatus;
import com.insurancehub.protocol.batch.domain.entity.BatchJobConfig;
import com.insurancehub.protocol.batch.domain.entity.BatchRunHistory;
import com.insurancehub.protocol.batch.domain.entity.BatchStepHistory;
import com.insurancehub.protocol.batch.infrastructure.repository.BatchRunHistoryRepository;
import com.insurancehub.protocol.batch.infrastructure.repository.BatchStepHistoryRepository;
import org.springframework.batch.core.BatchStatus;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.JobExecution;
import org.springframework.batch.core.JobParameters;
import org.springframework.batch.core.JobParametersBuilder;
import org.springframework.batch.core.StepExecution;
import org.springframework.batch.core.launch.JobLauncher;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

@Service
public class BatchExecutionService {

    private static final TypeReference<Map<String, Object>> PARAMETER_TYPE = new TypeReference<>() {
    };

    private final BatchJobConfigService batchJobConfigService;
    private final JobLauncher jobLauncher;
    private final Map<String, Job> jobsByName;
    private final BatchRunHistoryRepository batchRunHistoryRepository;
    private final BatchStepHistoryRepository batchStepHistoryRepository;
    private final ObjectMapper objectMapper;

    public BatchExecutionService(
            BatchJobConfigService batchJobConfigService,
            JobLauncher jobLauncher,
            List<Job> jobs,
            BatchRunHistoryRepository batchRunHistoryRepository,
            BatchStepHistoryRepository batchStepHistoryRepository,
            ObjectMapper objectMapper
    ) {
        this.batchJobConfigService = batchJobConfigService;
        this.jobLauncher = jobLauncher;
        this.jobsByName = jobs.stream().collect(Collectors.toMap(Job::getName, Function.identity()));
        this.batchRunHistoryRepository = batchRunHistoryRepository;
        this.batchStepHistoryRepository = batchStepHistoryRepository;
        this.objectMapper = objectMapper;
    }

    public ExecutionResult execute(ExecutionRequest request) {
        List<ExecutionStepLog> steps = new ArrayList<>();
        BatchJobConfig config;
        LocalDateTime configStartedAt = LocalDateTime.now();
        try {
            config = batchJobConfigService.getActiveForExecution(request.interfaceDefinition());
            steps.add(step(
                    1,
                    "Load batch job configuration",
                    ExecutionStepStatus.SUCCESS,
                    "Active batch job configuration loaded.",
                    configStartedAt,
                    LocalDateTime.now()
            ));
        } catch (RuntimeException exception) {
            steps.add(step(1, "Load batch job configuration", ExecutionStepStatus.FAILED, readableMessage(exception), configStartedAt, LocalDateTime.now()));
            return ExecutionResult.failure(
                    "BATCH_CONFIG_ERROR",
                    readableMessage(exception),
                    failurePayload("BATCH_CONFIG_ERROR", readableMessage(exception)),
                    steps
            );
        }

        Job job = jobsByName.get(config.getJobName());
        if (job == null) {
            steps.add(step(2, "Resolve Spring Batch job", ExecutionStepStatus.FAILED, "No Spring Batch job registered for " + config.getJobName() + ".", LocalDateTime.now(), LocalDateTime.now()));
            return ExecutionResult.failure(
                    "BATCH_JOB_NOT_FOUND",
                    "No Spring Batch job registered for " + config.getJobName() + ".",
                    failurePayload("BATCH_JOB_NOT_FOUND", "No Spring Batch job registered."),
                    steps,
                    config.getJobName(),
                    "BATCH_JOB",
                    config.getJobType().name(),
                    null,
                    null,
                    null,
                    null
            );
        }

        Map<String, Object> parameterMap;
        String jobParametersJson;
        JobParameters jobParameters;
        LocalDateTime parameterStartedAt = LocalDateTime.now();
        try {
            parameterMap = resolveParameterMap(request.requestPayload(), config.getParameterTemplateJson());
            jobParametersJson = toJson(parameterMap);
            jobParameters = toJobParameters(parameterMap, request);
            steps.add(step(
                    2,
                    "Build batch job parameters",
                    ExecutionStepStatus.SUCCESS,
                    "Job parameters prepared for " + config.getJobName() + ".",
                    parameterStartedAt,
                    LocalDateTime.now()
            ));
        } catch (RuntimeException exception) {
            steps.add(step(2, "Build batch job parameters", ExecutionStepStatus.FAILED, readableMessage(exception), parameterStartedAt, LocalDateTime.now()));
            return ExecutionResult.failure(
                    "BATCH_PARAMETER_ERROR",
                    readableMessage(exception),
                    failurePayload("BATCH_PARAMETER_ERROR", readableMessage(exception)),
                    steps,
                    config.getJobName(),
                    "BATCH_JOB",
                    config.getJobType().name(),
                    null,
                    null,
                    null,
                    null
            );
        }

        BatchRunHistory history = batchRunHistoryRepository.save(BatchRunHistory.started(
                request.execution(),
                config,
                jobParametersJson
        ));

        long startedAtNanos = System.nanoTime();
        LocalDateTime launchStartedAt = LocalDateTime.now();
        try {
            JobExecution jobExecution = jobLauncher.run(job, jobParameters);
            long latencyMs = Duration.ofNanos(System.nanoTime() - startedAtNanos).toMillis();
            BatchExecutionSummary summary = summarize(jobExecution);
            history.recordLaunch(jobExecution.getId(), nullSafe(jobExecution.getStartTime(), launchStartedAt));
            if (jobExecution.getStatus() == BatchStatus.COMPLETED) {
                history.markCompleted(
                        jobExecution.getStatus().name(),
                        jobExecution.getExitStatus().getExitCode(),
                        jobExecution.getExitStatus().getExitDescription(),
                        summary.readCount(),
                        summary.writeCount(),
                        summary.skipCount(),
                        nullSafe(jobExecution.getStartTime(), launchStartedAt),
                        nullSafe(jobExecution.getEndTime(), LocalDateTime.now()),
                        summary.outputSummary()
                );
            } else {
                history.markFailed(
                        jobExecution.getStatus().name(),
                        jobExecution.getExitStatus().getExitCode(),
                        jobExecution.getExitStatus().getExitDescription(),
                        summary.readCount(),
                        summary.writeCount(),
                        summary.skipCount(),
                        nullSafe(jobExecution.getStartTime(), launchStartedAt),
                        nullSafe(jobExecution.getEndTime(), LocalDateTime.now()),
                        failureMessage(jobExecution),
                        summary.outputSummary()
                );
            }
            batchRunHistoryRepository.save(history);
            saveStepHistory(history, jobExecution);

            boolean success = jobExecution.getStatus() == BatchStatus.COMPLETED;
            steps.add(step(
                    3,
                    "Launch Spring Batch job",
                    success ? ExecutionStepStatus.SUCCESS : ExecutionStepStatus.FAILED,
                    jobExecution.getStatus().name() + " with read=" + summary.readCount() + ", write=" + summary.writeCount() + ".",
                    launchStartedAt,
                    LocalDateTime.now()
            ));

            if (success) {
                return ExecutionResult.success(
                        successPayload(config, jobExecution, summary, latencyMs),
                        steps,
                        config.getJobName(),
                        "BATCH_JOB",
                        config.getJobType().name(),
                        jobParametersJson,
                        null,
                        responseMetadata(jobExecution, summary),
                        latencyMs
                );
            }
            return ExecutionResult.failure(
                    "BATCH_JOB_FAILED",
                    failureMessage(jobExecution),
                    failurePayload("BATCH_JOB_FAILED", failureMessage(jobExecution)),
                    steps,
                    config.getJobName(),
                    "BATCH_JOB",
                    config.getJobType().name(),
                    jobParametersJson,
                    null,
                    responseMetadata(jobExecution, summary),
                    latencyMs
            );
        } catch (Exception exception) {
            long latencyMs = Duration.ofNanos(System.nanoTime() - startedAtNanos).toMillis();
            history.markFailed(
                    "FAILED",
                    "FAILED",
                    readableMessage(exception),
                    0,
                    0,
                    0,
                    launchStartedAt,
                    LocalDateTime.now(),
                    readableMessage(exception),
                    null
            );
            batchRunHistoryRepository.save(history);
            steps.add(step(3, "Launch Spring Batch job", ExecutionStepStatus.FAILED, readableMessage(exception), launchStartedAt, LocalDateTime.now()));
            return ExecutionResult.failure(
                    "BATCH_LAUNCH_ERROR",
                    readableMessage(exception),
                    failurePayload("BATCH_LAUNCH_ERROR", readableMessage(exception)),
                    steps,
                    config.getJobName(),
                    "BATCH_JOB",
                    config.getJobType().name(),
                    jobParametersJson,
                    null,
                    responseMetadata("FAILED", 0, 0, 0, null),
                    latencyMs
            );
        }
    }

    private Map<String, Object> resolveParameterMap(String requestPayload, String templateJson) {
        String raw = StringUtils.hasText(requestPayload) ? requestPayload : templateJson;
        if (!StringUtils.hasText(raw)) {
            raw = "{}";
        }

        Map<String, Object> parameters = new LinkedHashMap<>();
        boolean forceFail;
        String trimmed = raw.trim();
        if (trimmed.startsWith("{")) {
            try {
                parameters.putAll(objectMapper.readValue(trimmed, PARAMETER_TYPE));
            } catch (JsonProcessingException exception) {
                throw new IllegalArgumentException("Batch parameters must be a valid JSON object.", exception);
            }
            forceFail = hasForcedFailure(parameters);
        } else {
            parameters.put("rawPayload", trimmed);
            forceFail = containsFailureToken(trimmed);
        }
        if (forceFail) {
            parameters.put("forceFail", true);
        }
        parameters.putIfAbsent("businessDate", "TODAY");
        return parameters;
    }

    private boolean hasForcedFailure(Map<String, Object> parameters) {
        Object explicitForceFail = parameters.get("forceFail");
        if (explicitForceFail instanceof Boolean booleanValue) {
            return booleanValue;
        }
        if (explicitForceFail instanceof String stringValue && "true".equalsIgnoreCase(stringValue.trim())) {
            return true;
        }
        return containsFailureToken(parameters);
    }

    private boolean containsFailureToken(Object value) {
        if (value == null) {
            return false;
        }
        if (value instanceof Map<?, ?> map) {
            return map.values().stream().anyMatch(this::containsFailureToken);
        }
        if (value instanceof Collection<?> collection) {
            return collection.stream().anyMatch(this::containsFailureToken);
        }
        if (value instanceof String stringValue) {
            return stringValue.toUpperCase().contains("FAIL");
        }
        return false;
    }

    private JobParameters toJobParameters(Map<String, Object> parameterMap, ExecutionRequest request) {
        JobParametersBuilder builder = new JobParametersBuilder()
                .addLong("run.id", System.currentTimeMillis())
                .addString("hubExecutionNo", request.execution().getExecutionNo())
                .addString("interfaceCode", request.interfaceDefinition().getInterfaceCode());

        parameterMap.forEach((key, value) -> {
            if (StringUtils.hasText(key) && value != null) {
                builder.addString(normalizeParameterName(key), String.valueOf(value));
            }
        });
        return builder.toJobParameters();
    }

    private String normalizeParameterName(String key) {
        String normalized = key.trim().replaceAll("[^A-Za-z0-9_.-]", "_");
        if (normalized.length() <= 90) {
            return normalized;
        }
        return normalized.substring(0, 90);
    }

    private void saveStepHistory(BatchRunHistory history, JobExecution jobExecution) {
        for (StepExecution stepExecution : jobExecution.getStepExecutions()) {
            batchStepHistoryRepository.save(BatchStepHistory.create(
                    history,
                    stepExecution.getStepName(),
                    stepExecution.getStatus().name(),
                    stepExecution.getReadCount(),
                    stepExecution.getWriteCount(),
                    stepExecution.getCommitCount(),
                    stepExecution.getRollbackCount(),
                    stepExecution.getReadSkipCount() + stepExecution.getProcessSkipCount() + stepExecution.getWriteSkipCount(),
                    stepExecution.getStartTime(),
                    stepExecution.getEndTime(),
                    stepExecution.getExitStatus().getExitCode(),
                    stepExecution.getExitStatus().getExitDescription()
            ));
        }
    }

    private BatchExecutionSummary summarize(JobExecution jobExecution) {
        long readCount = 0;
        long writeCount = 0;
        long skipCount = 0;
        String outputSummary = null;
        for (StepExecution stepExecution : jobExecution.getStepExecutions()) {
            readCount += stepExecution.getReadCount();
            writeCount += stepExecution.getWriteCount();
            skipCount += stepExecution.getReadSkipCount() + stepExecution.getProcessSkipCount() + stepExecution.getWriteSkipCount();
            if (outputSummary == null && stepExecution.getExecutionContext().containsKey("outputSummary")) {
                outputSummary = stepExecution.getExecutionContext().getString("outputSummary");
            }
        }
        if (outputSummary == null && jobExecution.getExecutionContext().containsKey("outputSummary")) {
            outputSummary = jobExecution.getExecutionContext().getString("outputSummary");
        }
        return new BatchExecutionSummary(readCount, writeCount, skipCount, outputSummary);
    }

    private String successPayload(
            BatchJobConfig config,
            JobExecution jobExecution,
            BatchExecutionSummary summary,
            long latencyMs
    ) {
        Map<String, Object> value = new LinkedHashMap<>();
        value.put("status", "SUCCESS");
        value.put("protocol", "BATCH");
        value.put("jobName", config.getJobName());
        value.put("jobType", config.getJobType().name());
        value.put("springBatchJobExecutionId", nullToDash(jobExecution.getId()));
        value.put("batchStatus", jobExecution.getStatus().name());
        value.put("readCount", summary.readCount());
        value.put("writeCount", summary.writeCount());
        value.put("skipCount", summary.skipCount());
        value.put("latencyMs", latencyMs);
        value.put("outputSummary", nullToDash(summary.outputSummary()));
        return toJson(value);
    }

    private String failurePayload(String code, String message) {
        return toJson(Map.of(
                "status", "FAILED",
                "protocol", "BATCH",
                "code", code,
                "message", nullToDash(message)
        ));
    }

    private String responseMetadata(JobExecution jobExecution, BatchExecutionSummary summary) {
        return responseMetadata(
                jobExecution.getStatus().name(),
                summary.readCount(),
                summary.writeCount(),
                summary.skipCount(),
                jobExecution.getExitStatus().getExitCode()
        );
    }

    private String responseMetadata(String status, long readCount, long writeCount, long skipCount, String exitCode) {
        return toJson(Map.of(
                "batchStatus", status,
                "exitCode", nullToDash(exitCode),
                "readCount", readCount,
                "writeCount", writeCount,
                "skipCount", skipCount
        ));
    }

    private String failureMessage(JobExecution jobExecution) {
        if (!jobExecution.getAllFailureExceptions().isEmpty()) {
            return readableMessage(jobExecution.getAllFailureExceptions().get(0));
        }
        if (StringUtils.hasText(jobExecution.getExitStatus().getExitDescription())) {
            return jobExecution.getExitStatus().getExitDescription();
        }
        return "Batch job finished with status " + jobExecution.getStatus().name() + ".";
    }

    private String toJson(Map<String, Object> value) {
        try {
            return objectMapper.writeValueAsString(value);
        } catch (JsonProcessingException exception) {
            throw new IllegalStateException("Could not serialize batch execution metadata.", exception);
        }
    }

    private String readableMessage(Throwable exception) {
        if (exception != null && StringUtils.hasText(exception.getMessage())) {
            return exception.getMessage();
        }
        return exception == null ? "Unknown batch error." : exception.getClass().getSimpleName();
    }

    private LocalDateTime nullSafe(LocalDateTime value, LocalDateTime fallback) {
        return value == null ? fallback : value;
    }

    private String nullToDash(Object value) {
        return value == null ? "-" : String.valueOf(value);
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

    private record BatchExecutionSummary(
            long readCount,
            long writeCount,
            long skipCount,
            String outputSummary
    ) {
    }
}
