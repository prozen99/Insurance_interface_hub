package com.insurancehub.protocol.batch.application;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.LocalDateTime;
import java.util.List;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.insurancehub.interfacehub.application.execution.ExecutionRequest;
import com.insurancehub.interfacehub.application.execution.ExecutionResult;
import com.insurancehub.interfacehub.domain.ExecutionTriggerType;
import com.insurancehub.interfacehub.domain.InterfaceDirection;
import com.insurancehub.interfacehub.domain.InterfaceStatus;
import com.insurancehub.interfacehub.domain.MasterStatus;
import com.insurancehub.interfacehub.domain.ProtocolType;
import com.insurancehub.interfacehub.domain.entity.InterfaceDefinition;
import com.insurancehub.interfacehub.domain.entity.InterfaceExecution;
import com.insurancehub.interfacehub.domain.entity.InternalSystem;
import com.insurancehub.interfacehub.domain.entity.PartnerCompany;
import com.insurancehub.protocol.batch.domain.BatchJobType;
import com.insurancehub.protocol.batch.domain.entity.BatchJobConfig;
import com.insurancehub.protocol.batch.domain.entity.BatchRunHistory;
import com.insurancehub.protocol.batch.domain.entity.BatchStepHistory;
import com.insurancehub.protocol.batch.infrastructure.repository.BatchRunHistoryRepository;
import com.insurancehub.protocol.batch.infrastructure.repository.BatchStepHistoryRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.batch.core.BatchStatus;
import org.springframework.batch.core.ExitStatus;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.JobExecution;
import org.springframework.batch.core.JobInstance;
import org.springframework.batch.core.JobParameters;
import org.springframework.batch.core.JobParametersBuilder;
import org.springframework.batch.core.StepExecution;
import org.springframework.batch.core.launch.JobLauncher;
import org.springframework.test.util.ReflectionTestUtils;

@ExtendWith(MockitoExtension.class)
class BatchExecutionServiceTest {

    @Mock
    private BatchJobConfigService batchJobConfigService;

    @Mock
    private JobLauncher jobLauncher;

    @Mock
    private Job settlementJob;

    @Mock
    private BatchRunHistoryRepository batchRunHistoryRepository;

    @Mock
    private BatchStepHistoryRepository batchStepHistoryRepository;

    private BatchExecutionService service;

    @BeforeEach
    void setUp() {
        when(settlementJob.getName()).thenReturn("interfaceSettlementSummaryJob");
        service = new BatchExecutionService(
                batchJobConfigService,
                jobLauncher,
                List.of(settlementJob),
                batchRunHistoryRepository,
                batchStepHistoryRepository,
                new ObjectMapper()
        );
        when(batchRunHistoryRepository.save(any(BatchRunHistory.class))).thenAnswer(invocation -> invocation.getArgument(0));
    }

    @Test
    void executeLaunchesSpringBatchJobAndRecordsStepCounts() throws Exception {
        InterfaceDefinition definition = batchDefinition();
        BatchJobConfig config = config(definition);
        when(batchJobConfigService.getActiveForExecution(definition)).thenReturn(config);
        when(jobLauncher.run(eq(settlementJob), any(JobParameters.class))).thenReturn(jobExecution(BatchStatus.COMPLETED));

        ExecutionResult result = service.execute(new ExecutionRequest(
                definition,
                execution(definition),
                ExecutionTriggerType.MANUAL,
                "{\"businessDate\":\"TODAY\",\"forceFail\":false}"
        ));

        assertThat(result.success()).isTrue();
        assertThat(result.responsePayload()).contains("\"protocol\":\"BATCH\"");
        assertThat(result.responseHeaders()).contains("\"readCount\":24");
        verify(batchStepHistoryRepository).save(any(BatchStepHistory.class));
        ArgumentCaptor<JobParameters> parametersCaptor = ArgumentCaptor.forClass(JobParameters.class);
        verify(jobLauncher).run(eq(settlementJob), parametersCaptor.capture());
        assertThat(parametersCaptor.getValue().getString("forceFail")).isEqualTo("false");

        BatchRunHistory history = savedHistory();
        assertThat(history.getBatchStatus()).isEqualTo("COMPLETED");
        assertThat(history.getReadCount()).isEqualTo(24);
        assertThat(history.getWriteCount()).isEqualTo(6);
    }

    @Test
    void executeReturnsFailureWhenSpringBatchJobFails() throws Exception {
        InterfaceDefinition definition = batchDefinition();
        BatchJobConfig config = config(definition);
        when(batchJobConfigService.getActiveForExecution(definition)).thenReturn(config);
        when(jobLauncher.run(eq(settlementJob), any(JobParameters.class))).thenReturn(jobExecution(BatchStatus.FAILED));

        ExecutionResult result = service.execute(new ExecutionRequest(
                definition,
                execution(definition),
                ExecutionTriggerType.MANUAL,
                "{\"businessDate\":\"TODAY\",\"forceFail\":true}"
        ));

        assertThat(result.success()).isFalse();
        assertThat(result.errorCode()).isEqualTo("BATCH_JOB_FAILED");

        BatchRunHistory history = savedHistory();
        assertThat(history.getBatchStatus()).isEqualTo("FAILED");
        assertThat(history.getErrorMessage()).contains("forced failure");
    }

    private BatchRunHistory savedHistory() {
        ArgumentCaptor<BatchRunHistory> historyCaptor = ArgumentCaptor.forClass(BatchRunHistory.class);
        verify(batchRunHistoryRepository, org.mockito.Mockito.atLeastOnce()).save(historyCaptor.capture());
        return historyCaptor.getAllValues().get(historyCaptor.getAllValues().size() - 1);
    }

    private JobExecution jobExecution(BatchStatus status) {
        JobParameters parameters = new JobParametersBuilder()
                .addLong("run.id", System.currentTimeMillis())
                .toJobParameters();
        JobExecution jobExecution = new JobExecution(new JobInstance(1L, "interfaceSettlementSummaryJob"), 100L, parameters);
        jobExecution.setStatus(status);
        jobExecution.setExitStatus(status == BatchStatus.COMPLETED ? ExitStatus.COMPLETED : ExitStatus.FAILED);
        jobExecution.setStartTime(LocalDateTime.now());
        jobExecution.setEndTime(LocalDateTime.now().plusSeconds(1));
        if (status == BatchStatus.FAILED) {
            jobExecution.addFailureException(new IllegalStateException("forced failure"));
        }

        StepExecution stepExecution = new StepExecution("interfaceSettlementSummaryStep", jobExecution, 101L);
        stepExecution.setStatus(status);
        stepExecution.setExitStatus(jobExecution.getExitStatus());
        stepExecution.setReadCount(24);
        stepExecution.setWriteCount(6);
        stepExecution.setCommitCount(1);
        stepExecution.setStartTime(LocalDateTime.now());
        stepExecution.setEndTime(LocalDateTime.now().plusSeconds(1));
        stepExecution.getExecutionContext().putString("outputSummary", "summary");
        jobExecution.addStepExecutions(List.of(stepExecution));
        return jobExecution;
    }

    private BatchJobConfig config(InterfaceDefinition definition) {
        return BatchJobConfig.create(
                definition,
                BatchJobType.INTERFACE_SETTLEMENT_SUMMARY,
                "interfaceSettlementSummaryJob",
                "0/30 * * * * *",
                "{\"businessDate\":\"TODAY\",\"forceFail\":false}",
                false,
                1,
                true,
                300000,
                true
        );
    }

    private InterfaceExecution execution(InterfaceDefinition definition) {
        InterfaceExecution execution = InterfaceExecution.create(
                "EXE-BATCH-001",
                definition,
                null,
                ExecutionTriggerType.MANUAL,
                "{}",
                "admin"
        );
        ReflectionTestUtils.setField(execution, "id", 10L);
        return execution;
    }

    private InterfaceDefinition batchDefinition() {
        InterfaceDefinition definition = InterfaceDefinition.create(
                "IF_BATCH_SETTLEMENT_001",
                "Daily interface settlement summary batch",
                ProtocolType.BATCH,
                InterfaceDirection.OUTBOUND,
                InterfaceStatus.ACTIVE,
                PartnerCompany.create("LIFEPLUS", "Life Plus Insurance", MasterStatus.ACTIVE, null),
                InternalSystem.create("POLICY_CORE", "Policy Core System", "Insurance Platform Team", MasterStatus.ACTIVE, null),
                null
        );
        ReflectionTestUtils.setField(definition, "id", 1L);
        return definition;
    }
}
