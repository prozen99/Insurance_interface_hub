package com.insurancehub.protocol.batch.config;

import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import com.insurancehub.interfacehub.domain.ExecutionStatus;
import com.insurancehub.interfacehub.domain.ProtocolType;
import com.insurancehub.interfacehub.infrastructure.repository.InterfaceExecutionRepository;
import com.insurancehub.interfacehub.infrastructure.repository.InterfaceRetryTaskRepository;
import com.insurancehub.interfacehub.domain.RetryStatus;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.JobParameters;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.StepContribution;
import org.springframework.batch.core.job.builder.JobBuilder;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.batch.core.scope.context.ChunkContext;
import org.springframework.batch.core.step.builder.StepBuilder;
import org.springframework.batch.repeat.RepeatStatus;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.util.StringUtils;

@Configuration
@EnableConfigurationProperties(BatchProperties.class)
public class BatchJobConfiguration {

    @Bean
    public Job interfaceSettlementSummaryJob(
            JobRepository jobRepository,
            Step interfaceSettlementSummaryStep
    ) {
        return new JobBuilder("interfaceSettlementSummaryJob", jobRepository)
                .start(interfaceSettlementSummaryStep)
                .build();
    }

    @Bean
    public Step interfaceSettlementSummaryStep(
            JobRepository jobRepository,
            PlatformTransactionManager transactionManager,
            InterfaceExecutionRepository interfaceExecutionRepository,
            BatchDemoStorage storage
    ) {
        return new StepBuilder("interfaceSettlementSummaryStep", jobRepository)
                .tasklet((contribution, chunkContext) -> {
                    assertNotForcedFailure(chunkContext, "Interface settlement summary forced failure.");
                    LocalDate businessDate = resolveBusinessDate(chunkContext.getStepContext().getStepExecution().getJobParameters());
                    LocalDateTime start = businessDate.atStartOfDay();
                    LocalDateTime end = businessDate.plusDays(1).atStartOfDay();

                    List<String> lines = new ArrayList<>();
                    lines.add("Interface settlement summary for " + businessDate);
                    for (ProtocolType protocolType : ProtocolType.values()) {
                        for (ExecutionStatus executionStatus : ExecutionStatus.values()) {
                            long count = interfaceExecutionRepository.countByProtocolTypeAndExecutionStatusAndStartedAtBetween(
                                    protocolType,
                                    executionStatus,
                                    start,
                                    end
                            );
                            lines.add(protocolType + "," + executionStatus + "," + count);
                            contribution.incrementReadCount();
                        }
                    }
                    contribution.incrementWriteCount(lines.size());
                    String summary = String.join(System.lineSeparator(), lines);
                    writeOutput(storage, "settlement-summary", businessDate, summary);
                    recordOutputSummary(chunkContext, summary);
                    return RepeatStatus.FINISHED;
                }, transactionManager)
                .build();
    }

    @Bean
    public Job failedExecutionRetryAggregationJob(
            JobRepository jobRepository,
            Step failedExecutionRetryAggregationStep
    ) {
        return new JobBuilder("failedExecutionRetryAggregationJob", jobRepository)
                .start(failedExecutionRetryAggregationStep)
                .build();
    }

    @Bean
    public Step failedExecutionRetryAggregationStep(
            JobRepository jobRepository,
            PlatformTransactionManager transactionManager,
            InterfaceExecutionRepository interfaceExecutionRepository,
            InterfaceRetryTaskRepository interfaceRetryTaskRepository,
            BatchDemoStorage storage
    ) {
        return new StepBuilder("failedExecutionRetryAggregationStep", jobRepository)
                .tasklet((contribution, chunkContext) -> {
                    assertNotForcedFailure(chunkContext, "Failed execution retry aggregation forced failure.");
                    LocalDate businessDate = resolveBusinessDate(chunkContext.getStepContext().getStepExecution().getJobParameters());
                    LocalDateTime start = businessDate.atStartOfDay();
                    LocalDateTime end = businessDate.plusDays(1).atStartOfDay();
                    long failedToday = interfaceExecutionRepository.countByExecutionStatusAndStartedAtBetween(
                            ExecutionStatus.FAILED,
                            start,
                            end
                    );
                    long pendingRetries = interfaceRetryTaskRepository.countByRetryStatus(RetryStatus.WAITING);

                    contribution.incrementReadCount();
                    contribution.incrementReadCount();
                    contribution.incrementWriteCount(2);
                    String summary = """
                            Failed execution retry aggregation for %s
                            failedToday=%d
                            pendingRetries=%d
                            """.formatted(businessDate, failedToday, pendingRetries).trim();
                    writeOutput(storage, "retry-aggregation", businessDate, summary);
                    recordOutputSummary(chunkContext, summary);
                    return RepeatStatus.FINISHED;
                }, transactionManager)
                .build();
    }

    private void assertNotForcedFailure(ChunkContext chunkContext, String message) {
        JobParameters parameters = chunkContext.getStepContext().getStepExecution().getJobParameters();
        String forceFail = parameters.getString("forceFail");
        if (StringUtils.hasText(forceFail) && Boolean.parseBoolean(forceFail)) {
            throw new IllegalStateException(message);
        }
    }

    private LocalDate resolveBusinessDate(JobParameters parameters) {
        String businessDate = parameters.getString("businessDate");
        if (!StringUtils.hasText(businessDate) || "TODAY".equalsIgnoreCase(businessDate)) {
            return LocalDate.now();
        }
        return LocalDate.parse(businessDate);
    }

    private void writeOutput(
            BatchDemoStorage storage,
            String prefix,
            LocalDate businessDate,
            String summary
    ) throws Exception {
        Path file = storage.outputDirectory().resolve(prefix + "-" + businessDate + "-" + System.currentTimeMillis() + ".txt");
        Files.writeString(file, summary, StandardCharsets.UTF_8);
    }

    private void recordOutputSummary(ChunkContext chunkContext, String summary) {
        String compact = summary.replace("\r", " ").replace("\n", " | ");
        if (compact.length() > 1800) {
            compact = compact.substring(0, 1800);
        }
        chunkContext.getStepContext().getStepExecution().getExecutionContext().putString("outputSummary", compact);
        chunkContext.getStepContext().getStepExecution().getJobExecution().getExecutionContext().putString("outputSummary", compact);
    }
}
