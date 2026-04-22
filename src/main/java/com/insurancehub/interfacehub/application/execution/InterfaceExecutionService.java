package com.insurancehub.interfacehub.application.execution;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.ThreadLocalRandom;

import com.insurancehub.interfacehub.domain.ExecutionStatus;
import com.insurancehub.interfacehub.domain.ExecutionStepStatus;
import com.insurancehub.interfacehub.domain.ExecutionTriggerType;
import com.insurancehub.interfacehub.domain.InterfaceStatus;
import com.insurancehub.interfacehub.domain.ProtocolType;
import com.insurancehub.interfacehub.domain.RetryStatus;
import com.insurancehub.interfacehub.domain.entity.InterfaceDefinition;
import com.insurancehub.interfacehub.domain.entity.InterfaceExecution;
import com.insurancehub.interfacehub.domain.entity.InterfaceExecutionStep;
import com.insurancehub.interfacehub.domain.entity.InterfaceRetryTask;
import com.insurancehub.interfacehub.infrastructure.repository.InterfaceDefinitionRepository;
import com.insurancehub.interfacehub.infrastructure.repository.InterfaceExecutionRepository;
import com.insurancehub.interfacehub.infrastructure.repository.InterfaceExecutionStepRepository;
import com.insurancehub.interfacehub.infrastructure.repository.InterfaceRetryTaskRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionTemplate;
import org.springframework.util.StringUtils;
import org.springframework.web.server.ResponseStatusException;

@Service
public class InterfaceExecutionService {

    private static final DateTimeFormatter EXECUTION_NO_FORMATTER = DateTimeFormatter.ofPattern("yyyyMMddHHmmssSSS");

    private final InterfaceDefinitionRepository interfaceDefinitionRepository;
    private final InterfaceExecutionRepository interfaceExecutionRepository;
    private final InterfaceExecutionStepRepository interfaceExecutionStepRepository;
    private final InterfaceRetryTaskRepository interfaceRetryTaskRepository;
    private final InterfaceExecutorFactory interfaceExecutorFactory;
    private final TransactionTemplate transactionTemplate;

    public InterfaceExecutionService(
            InterfaceDefinitionRepository interfaceDefinitionRepository,
            InterfaceExecutionRepository interfaceExecutionRepository,
            InterfaceExecutionStepRepository interfaceExecutionStepRepository,
            InterfaceRetryTaskRepository interfaceRetryTaskRepository,
            InterfaceExecutorFactory interfaceExecutorFactory,
            PlatformTransactionManager transactionManager
    ) {
        this.interfaceDefinitionRepository = interfaceDefinitionRepository;
        this.interfaceExecutionRepository = interfaceExecutionRepository;
        this.interfaceExecutionStepRepository = interfaceExecutionStepRepository;
        this.interfaceRetryTaskRepository = interfaceRetryTaskRepository;
        this.interfaceExecutorFactory = interfaceExecutorFactory;
        this.transactionTemplate = new TransactionTemplate(transactionManager);
    }

    @Transactional(readOnly = true)
    public List<InterfaceExecution> search(String keyword, ProtocolType protocolType, ExecutionStatus executionStatus) {
        return search(keyword, protocolType, executionStatus, null, null, null);
    }

    @Transactional(readOnly = true)
    public List<InterfaceExecution> search(
            String keyword,
            ProtocolType protocolType,
            ExecutionStatus executionStatus,
            ExecutionTriggerType triggerType,
            LocalDate startedFrom,
            LocalDate startedTo
    ) {
        LocalDateTime startedFromDateTime = startedFrom == null ? null : startedFrom.atStartOfDay();
        LocalDateTime startedToDateTime = startedTo == null ? null : startedTo.plusDays(1).atStartOfDay();
        return interfaceExecutionRepository.search(
                normalizeKeyword(keyword),
                protocolType,
                executionStatus,
                triggerType,
                startedFromDateTime,
                startedToDateTime
        );
    }

    @Transactional(readOnly = true)
    public InterfaceExecution getDetail(Long executionId) {
        return interfaceExecutionRepository.findDetailById(executionId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Execution not found"));
    }

    @Transactional(readOnly = true)
    public List<InterfaceExecutionStep> getSteps(Long executionId) {
        return interfaceExecutionStepRepository.findByExecutionIdOrderByStepOrderAsc(executionId);
    }

    @Transactional(readOnly = true)
    public List<InterfaceRetryTask> getRetryTasks(Long executionId) {
        return interfaceRetryTaskRepository.findByExecutionIdOrderByCreatedAtDesc(executionId);
    }

    @Transactional(readOnly = true)
    public List<InterfaceExecution> recentExecutionsForInterface(Long interfaceDefinitionId) {
        return interfaceExecutionRepository.findTop5ByInterfaceDefinitionIdOrderByCreatedAtDesc(interfaceDefinitionId);
    }

    @Transactional(readOnly = true)
    public long countToday(ExecutionStatus executionStatus) {
        LocalDate today = LocalDate.now();
        return interfaceExecutionRepository.countByExecutionStatusAndStartedAtBetween(
                executionStatus,
                today.atStartOfDay(),
                today.plusDays(1).atStartOfDay()
        );
    }

    @Transactional(readOnly = true)
    public long countPendingRetries() {
        return interfaceRetryTaskRepository.countByRetryStatus(RetryStatus.WAITING);
    }

    public InterfaceExecution executeManual(Long interfaceDefinitionId, String requestPayload, String requestedBy) {
        return execute(interfaceDefinitionId, null, ExecutionTriggerType.MANUAL, trimToNull(requestPayload), requestedBy);
    }

    public InterfaceExecution executeScheduled(Long interfaceDefinitionId, String requestPayload, String requestedBy) {
        return execute(interfaceDefinitionId, null, ExecutionTriggerType.SCHEDULED, trimToNull(requestPayload), requestedBy);
    }

    public InterfaceExecution retryFailedExecution(Long executionId, String requestedBy) {
        RetryAttempt retryAttempt = prepareRetryAttempt(executionId);
        InterfaceExecution retryExecution = execute(
                retryAttempt.interfaceDefinitionId(),
                retryAttempt.retrySourceExecutionId(),
                ExecutionTriggerType.RETRY,
                retryAttempt.requestPayload(),
                requestedBy
        );
        markRetryTaskDone(retryAttempt.retryTask());
        return retryExecution;
    }

    private InterfaceExecution execute(
            Long interfaceDefinitionId,
            Long retrySourceExecutionId,
            ExecutionTriggerType triggerType,
            String requestPayload,
            String requestedBy
    ) {
        StartedExecution startedExecution = startExecution(
                interfaceDefinitionId,
                retrySourceExecutionId,
                triggerType,
                requestPayload,
                requestedBy
        );

        ExecutionResult result;
        try {
            InterfaceExecutor executor = interfaceExecutorFactory.getExecutor(startedExecution.interfaceDefinition().getProtocolType());
            result = executor.execute(new ExecutionRequest(
                    startedExecution.interfaceDefinition(),
                    startedExecution.execution(),
                    triggerType,
                    requestPayload
            ));
        } catch (Exception exception) {
            result = ExecutionResult.failure(
                    "EXECUTOR_ERROR",
                    exception.getMessage(),
                    "{\"status\":\"FAILED\",\"message\":\"Executor error\"}",
                    List.of(new ExecutionStepLog(
                            1,
                            "Execute protocol strategy",
                            ExecutionStepStatus.FAILED,
                            exception.getMessage(),
                            LocalDateTime.now(),
                            LocalDateTime.now()
                    ))
            );
        }

        return completeExecution(startedExecution.execution(), result);
    }

    private StartedExecution startExecution(
            Long interfaceDefinitionId,
            Long retrySourceExecutionId,
            ExecutionTriggerType triggerType,
            String requestPayload,
            String requestedBy
    ) {
        return Objects.requireNonNull(transactionTemplate.execute(status -> {
            InterfaceDefinition interfaceDefinition = getInterfaceDefinition(interfaceDefinitionId);
            assertInterfaceActive(interfaceDefinition);
            InterfaceExecution retrySourceExecution = null;
            if (retrySourceExecutionId != null) {
                retrySourceExecution = interfaceExecutionRepository.findDetailById(retrySourceExecutionId)
                        .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Retry source execution not found"));
            }

            InterfaceExecution execution = InterfaceExecution.create(
                    generateExecutionNo(),
                    interfaceDefinition,
                    retrySourceExecution,
                    triggerType,
                    requestPayload,
                    requestedBy
            );
            execution.markRunning(LocalDateTime.now());
            return new StartedExecution(interfaceDefinition, interfaceExecutionRepository.saveAndFlush(execution));
        }));
    }

    private InterfaceExecution completeExecution(InterfaceExecution execution, ExecutionResult result) {
        return Objects.requireNonNull(transactionTemplate.execute(status -> {
            LocalDateTime finishedAt = LocalDateTime.now();

            execution.recordHttpExchange(
                    result.requestUrl(),
                    result.requestMethod(),
                    result.protocolAction(),
                    result.requestHeaders(),
                    result.responseStatusCode(),
                    result.responseHeaders(),
                    result.latencyMs()
            );

            for (ExecutionStepLog step : result.steps()) {
                interfaceExecutionStepRepository.save(InterfaceExecutionStep.create(
                        execution,
                        step.stepOrder(),
                        step.stepName(),
                        step.stepStatus(),
                        step.message(),
                        step.startedAt(),
                        step.finishedAt()
                ));
            }

            if (result.success()) {
                execution.markSuccess(result.responsePayload(), finishedAt);
            } else {
                execution.markFailed(result.errorCode(), result.errorMessage(), result.responsePayload(), finishedAt);
                interfaceRetryTaskRepository.save(InterfaceRetryTask.waitingFor(
                        execution,
                        finishedAt.plusMinutes(5)
                ));
            }

            return interfaceExecutionRepository.save(execution);
        }));
    }

    private RetryAttempt prepareRetryAttempt(Long executionId) {
        return Objects.requireNonNull(transactionTemplate.execute(status -> {
            InterfaceExecution original = interfaceExecutionRepository.findDetailById(executionId)
                    .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Execution not found"));

            if (!original.isFailed()) {
                throw new ExecutionNotAllowedException("Only failed executions can be retried.");
            }

            InterfaceDefinition interfaceDefinition = original.getInterfaceDefinition();
            assertInterfaceActive(interfaceDefinition);

            InterfaceRetryTask retryTask = interfaceRetryTaskRepository
                    .findFirstByExecutionIdAndRetryStatusOrderByCreatedAtDesc(original.getId(), RetryStatus.WAITING)
                    .orElseGet(() -> interfaceRetryTaskRepository.save(
                            InterfaceRetryTask.waitingFor(original, LocalDateTime.now())
                    ));

            return new RetryAttempt(
                    interfaceDefinition.getId(),
                    original.getId(),
                    original.getRequestPayload(),
                    retryTask
            );
        }));
    }

    private void markRetryTaskDone(InterfaceRetryTask retryTask) {
        transactionTemplate.executeWithoutResult(status -> {
            retryTask.markDone(LocalDateTime.now());
            interfaceRetryTaskRepository.save(retryTask);
        });
    }

    private InterfaceDefinition getInterfaceDefinition(Long interfaceDefinitionId) {
        return interfaceDefinitionRepository.findDetailById(interfaceDefinitionId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Interface definition not found"));
    }

    private void assertInterfaceActive(InterfaceDefinition interfaceDefinition) {
        if (interfaceDefinition.getStatus() != InterfaceStatus.ACTIVE) {
            throw new ExecutionNotAllowedException("Inactive interfaces cannot be executed.");
        }
    }

    private String generateExecutionNo() {
        for (int i = 0; i < 10; i++) {
            String executionNo = "EXE-" + LocalDateTime.now().format(EXECUTION_NO_FORMATTER)
                    + "-" + ThreadLocalRandom.current().nextInt(1000, 10000);
            if (!interfaceExecutionRepository.existsByExecutionNo(executionNo)) {
                return executionNo;
            }
        }
        throw new IllegalStateException("Could not generate unique execution number.");
    }

    private String normalizeKeyword(String keyword) {
        if (!StringUtils.hasText(keyword)) {
            return null;
        }
        return keyword.trim();
    }

    private String trimToNull(String value) {
        if (!StringUtils.hasText(value)) {
            return null;
        }
        return value.trim();
    }

    private record StartedExecution(
            InterfaceDefinition interfaceDefinition,
            InterfaceExecution execution
    ) {
    }

    private record RetryAttempt(
            Long interfaceDefinitionId,
            Long retrySourceExecutionId,
            String requestPayload,
            InterfaceRetryTask retryTask
    ) {
    }
}
