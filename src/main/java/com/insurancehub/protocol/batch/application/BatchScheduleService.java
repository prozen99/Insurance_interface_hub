package com.insurancehub.protocol.batch.application;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

import com.insurancehub.interfacehub.application.execution.InterfaceExecutionService;
import com.insurancehub.protocol.batch.domain.entity.BatchJobConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.scheduling.support.CronExpression;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

@Service
@ConditionalOnProperty(prefix = "app.batch.scheduler", name = "enabled", havingValue = "true")
public class BatchScheduleService {

    private static final Logger log = LoggerFactory.getLogger(BatchScheduleService.class);

    private final BatchJobConfigService batchJobConfigService;
    private final InterfaceExecutionService interfaceExecutionService;
    private final Map<Long, LocalDateTime> lastRunByConfigId = new HashMap<>();

    public BatchScheduleService(
            BatchJobConfigService batchJobConfigService,
            InterfaceExecutionService interfaceExecutionService
    ) {
        this.batchJobConfigService = batchJobConfigService;
        this.interfaceExecutionService = interfaceExecutionService;
    }

    @Scheduled(
            fixedDelayString = "${app.batch.scheduler.poll-delay-ms:30000}",
            initialDelayString = "${app.batch.scheduler.initial-delay-ms:30000}"
    )
    public void launchDueBatchJobs() {
        LocalDateTime now = LocalDateTime.now();
        for (BatchJobConfig config : batchJobConfigService.findSchedulableConfigs()) {
            if (isDue(config, now)) {
                try {
                    interfaceExecutionService.executeScheduled(
                            config.getInterfaceDefinition().getId(),
                            config.getParameterTemplateJson(),
                            "batch-scheduler"
                    );
                    lastRunByConfigId.put(config.getId(), now);
                } catch (RuntimeException exception) {
                    log.warn("Scheduled batch execution failed for config {}", config.getId(), exception);
                    lastRunByConfigId.put(config.getId(), now);
                }
            }
        }
    }

    private boolean isDue(BatchJobConfig config, LocalDateTime now) {
        if (config.getId() == null || !StringUtils.hasText(config.getCronExpression())) {
            return false;
        }
        try {
            CronExpression cronExpression = CronExpression.parse(config.getCronExpression());
            LocalDateTime lastRun = lastRunByConfigId.getOrDefault(config.getId(), now.minusSeconds(1));
            LocalDateTime nextRun = cronExpression.next(lastRun);
            return nextRun != null && !nextRun.isAfter(now);
        } catch (IllegalArgumentException exception) {
            log.warn("Invalid batch cron expression for config {}: {}", config.getId(), config.getCronExpression());
            return false;
        }
    }
}
