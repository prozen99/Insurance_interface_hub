package com.insurancehub.monitoring.application;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.EnumMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import com.insurancehub.interfacehub.domain.ExecutionStatus;
import com.insurancehub.interfacehub.domain.InterfaceStatus;
import com.insurancehub.interfacehub.domain.ProtocolType;
import com.insurancehub.interfacehub.domain.RetryStatus;
import com.insurancehub.interfacehub.domain.entity.InterfaceExecution;
import com.insurancehub.interfacehub.domain.entity.InterfaceRetryTask;
import com.insurancehub.interfacehub.infrastructure.repository.InterfaceDefinitionRepository;
import com.insurancehub.interfacehub.infrastructure.repository.InterfaceExecutionRepository;
import com.insurancehub.interfacehub.infrastructure.repository.InterfaceRetryTaskRepository;
import com.insurancehub.protocol.batch.domain.entity.BatchRunHistory;
import com.insurancehub.protocol.batch.infrastructure.repository.BatchRunHistoryRepository;
import com.insurancehub.protocol.filetransfer.domain.FileTransferStatus;
import com.insurancehub.protocol.filetransfer.domain.entity.FileTransferHistory;
import com.insurancehub.protocol.filetransfer.infrastructure.repository.FileTransferHistoryRepository;
import com.insurancehub.protocol.mq.domain.MqProcessingStatus;
import com.insurancehub.protocol.mq.domain.entity.MqMessageHistory;
import com.insurancehub.protocol.mq.infrastructure.repository.MqMessageHistoryRepository;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class OperationsMonitoringService {

    private static final int TREND_DAYS = 7;

    private final InterfaceDefinitionRepository interfaceDefinitionRepository;
    private final InterfaceExecutionRepository interfaceExecutionRepository;
    private final InterfaceRetryTaskRepository interfaceRetryTaskRepository;
    private final MqMessageHistoryRepository mqMessageHistoryRepository;
    private final FileTransferHistoryRepository fileTransferHistoryRepository;
    private final BatchRunHistoryRepository batchRunHistoryRepository;

    public OperationsMonitoringService(
            InterfaceDefinitionRepository interfaceDefinitionRepository,
            InterfaceExecutionRepository interfaceExecutionRepository,
            InterfaceRetryTaskRepository interfaceRetryTaskRepository,
            MqMessageHistoryRepository mqMessageHistoryRepository,
            FileTransferHistoryRepository fileTransferHistoryRepository,
            BatchRunHistoryRepository batchRunHistoryRepository
    ) {
        this.interfaceDefinitionRepository = interfaceDefinitionRepository;
        this.interfaceExecutionRepository = interfaceExecutionRepository;
        this.interfaceRetryTaskRepository = interfaceRetryTaskRepository;
        this.mqMessageHistoryRepository = mqMessageHistoryRepository;
        this.fileTransferHistoryRepository = fileTransferHistoryRepository;
        this.batchRunHistoryRepository = batchRunHistoryRepository;
    }

    @Transactional(readOnly = true)
    public DashboardOverview dashboardOverview() {
        Window today = today();
        Window lastSevenDays = lastSevenDays();

        long activeInterfaces = interfaceDefinitionRepository.countByStatus(InterfaceStatus.ACTIVE);
        long totalInterfaces = interfaceDefinitionRepository.count();
        long todaySuccess = interfaceExecutionRepository.countByExecutionStatusAndStartedAtBetween(
                ExecutionStatus.SUCCESS,
                today.start(),
                today.end()
        );
        long todayFailure = interfaceExecutionRepository.countByExecutionStatusAndStartedAtBetween(
                ExecutionStatus.FAILED,
                today.start(),
                today.end()
        );
        long pendingRetries = interfaceRetryTaskRepository.countByRetryStatus(RetryStatus.WAITING);
        long recentRetryDone = interfaceRetryTaskRepository.countByRetryStatusAndLastRetriedAtBetween(
                RetryStatus.DONE,
                lastSevenDays.start(),
                lastSevenDays.end()
        );

        List<MetricCard> metrics = List.of(
                new MetricCard("Active interfaces", String.valueOf(activeInterfaces), "Ready for execution", "normal", "/admin/interfaces?status=ACTIVE"),
                new MetricCard("Total interfaces", String.valueOf(totalInterfaces), "Registered master definitions", "normal", "/admin/interfaces"),
                new MetricCard("Today success", String.valueOf(todaySuccess), "Completed executions", "success", "/admin/executions?executionStatus=SUCCESS"),
                new MetricCard("Today failure", String.valueOf(todayFailure), "Needs operator review", "danger", "/admin/monitoring/failures"),
                new MetricCard("Pending retries", String.valueOf(pendingRetries), "Waiting retry tasks", "warning", "/admin/monitoring/retries"),
                new MetricCard("Recent retry done", String.valueOf(recentRetryDone), "Retried in the last 7 days", "success", "/admin/monitoring/retries")
        );

        return new DashboardOverview(
                metrics,
                protocolSummaries(),
                executionTrend(),
                topFailedInterfaces(5),
                interfaceExecutionRepository.findTop10ByOrderByCreatedAtDesc(),
                interfaceRetryTaskRepository.findTop20ByRetryStatusOrderByCreatedAtDesc(RetryStatus.WAITING),
                fileTransferSummary(),
                mqSummary(),
                batchSummary()
        );
    }

    @Transactional(readOnly = true)
    public List<ProtocolSummary> protocolSummaries() {
        Window today = today();
        Window lastSevenDays = lastSevenDays();
        Map<ProtocolType, InterfaceProtocolCounter> interfaceCounters = interfaceProtocolCounters();
        Map<ProtocolType, ExecutionProtocolCounter> todayExecutionCounters = executionProtocolCounters(today);
        Map<ProtocolType, ExecutionProtocolCounter> recentExecutionCounters = executionProtocolCounters(lastSevenDays);

        List<ProtocolSummary> summaries = new ArrayList<>();
        for (ProtocolType protocolType : ProtocolType.values()) {
            InterfaceProtocolCounter interfaceCounter = interfaceCounters.getOrDefault(protocolType, new InterfaceProtocolCounter());
            ExecutionProtocolCounter todayCounter = todayExecutionCounters.getOrDefault(protocolType, new ExecutionProtocolCounter());
            ExecutionProtocolCounter recentCounter = recentExecutionCounters.getOrDefault(protocolType, new ExecutionProtocolCounter());
            summaries.add(new ProtocolSummary(
                    protocolType,
                    interfaceCounter.total,
                    interfaceCounter.active,
                    todayCounter.success,
                    todayCounter.failure,
                    recentCounter.total
            ));
        }
        return summaries;
    }

    @Transactional(readOnly = true)
    public FailureMonitoring failureMonitoring() {
        return new FailureMonitoring(
                topFailedInterfaces(10),
                interfaceExecutionRepository.findTop20ByExecutionStatusOrderByCreatedAtDesc(ExecutionStatus.FAILED)
        );
    }

    @Transactional(readOnly = true)
    public RetryMonitoring retryMonitoring() {
        Window today = today();
        Window lastSevenDays = lastSevenDays();
        return new RetryMonitoring(
                interfaceRetryTaskRepository.countByRetryStatus(RetryStatus.WAITING),
                interfaceRetryTaskRepository.countByRetryStatusAndCreatedAtBetween(RetryStatus.WAITING, today.start(), today.end()),
                interfaceRetryTaskRepository.countByRetryStatusAndLastRetriedAtBetween(RetryStatus.DONE, lastSevenDays.start(), lastSevenDays.end()),
                interfaceRetryTaskRepository.findTop20ByRetryStatusOrderByCreatedAtDesc(RetryStatus.WAITING),
                interfaceRetryTaskRepository.findTop10ByOrderByCreatedAtDesc()
        );
    }

    @Transactional(readOnly = true)
    public List<TrendPoint> executionTrend() {
        Window window = lastSevenDays();
        Map<LocalDate, TrendCounter> counters = new LinkedHashMap<>();
        for (int i = TREND_DAYS - 1; i >= 0; i--) {
            counters.put(LocalDate.now().minusDays(i), new TrendCounter());
        }

        for (InterfaceExecution execution : interfaceExecutionRepository.findTrendSource(window.start(), window.end())) {
            if (execution.getStartedAt() == null) {
                continue;
            }
            TrendCounter counter = counters.get(execution.getStartedAt().toLocalDate());
            if (counter != null) {
                counter.add(execution.getExecutionStatus());
            }
        }

        return counters.entrySet().stream()
                .map(entry -> new TrendPoint(
                        entry.getKey(),
                        entry.getValue().success,
                        entry.getValue().failure,
                        entry.getValue().total()
                ))
                .toList();
    }

    @Transactional(readOnly = true)
    public FileTransferMonitoring fileTransferMonitoring() {
        return new FileTransferMonitoring(fileTransferSummary(), fileTransferHistoryRepository.findTop30ByOrderByCreatedAtDesc());
    }

    @Transactional(readOnly = true)
    public MqMonitoring mqMonitoring() {
        return new MqMonitoring(mqSummary(), mqMessageHistoryRepository.findTop20ByOrderByCreatedAtDesc());
    }

    @Transactional(readOnly = true)
    public BatchMonitoring batchMonitoring() {
        return new BatchMonitoring(batchSummary(), batchRunHistoryRepository.findTop50ByOrderByCreatedAtDesc());
    }

    private Map<ProtocolType, InterfaceProtocolCounter> interfaceProtocolCounters() {
        Map<ProtocolType, InterfaceProtocolCounter> counters = new EnumMap<>(ProtocolType.class);
        for (Object[] row : safeRows(interfaceDefinitionRepository.countByProtocolTypeAndStatusGroup())) {
            ProtocolType protocolType = (ProtocolType) row[0];
            InterfaceStatus status = (InterfaceStatus) row[1];
            long count = ((Number) row[2]).longValue();

            InterfaceProtocolCounter counter = counters.computeIfAbsent(protocolType, ignored -> new InterfaceProtocolCounter());
            counter.total += count;
            if (status == InterfaceStatus.ACTIVE) {
                counter.active += count;
            }
        }
        return counters;
    }

    private Map<ProtocolType, ExecutionProtocolCounter> executionProtocolCounters(Window window) {
        Map<ProtocolType, ExecutionProtocolCounter> counters = new EnumMap<>(ProtocolType.class);
        for (Object[] row : safeRows(interfaceExecutionRepository.countByProtocolTypeAndStatusBetweenGroup(window.start(), window.end()))) {
            ProtocolType protocolType = (ProtocolType) row[0];
            ExecutionStatus status = (ExecutionStatus) row[1];
            long count = ((Number) row[2]).longValue();

            ExecutionProtocolCounter counter = counters.computeIfAbsent(protocolType, ignored -> new ExecutionProtocolCounter());
            counter.total += count;
            if (status == ExecutionStatus.SUCCESS) {
                counter.success += count;
            } else if (status == ExecutionStatus.FAILED) {
                counter.failure += count;
            }
        }
        return counters;
    }

    private List<Object[]> safeRows(List<Object[]> rows) {
        return rows == null ? Collections.emptyList() : rows;
    }

    private List<TopFailedInterface> topFailedInterfaces(int limit) {
        Window lastSevenDays = lastSevenDays();
        return interfaceExecutionRepository.findTopFailedInterfaces(
                        ExecutionStatus.FAILED,
                        lastSevenDays.start(),
                        lastSevenDays.end(),
                        PageRequest.of(0, limit)
                )
                .stream()
                .map(this::toTopFailedInterface)
                .toList();
    }

    private TopFailedInterface toTopFailedInterface(Object[] row) {
        return new TopFailedInterface(
                ((Number) row[0]).longValue(),
                String.valueOf(row[1]),
                String.valueOf(row[2]),
                (ProtocolType) row[3],
                ((Number) row[4]).longValue()
        );
    }

    private FileTransferSummary fileTransferSummary() {
        Window today = today();
        long success = fileTransferHistoryRepository.countByTransferStatusAndCreatedAtBetween(
                FileTransferStatus.SUCCESS,
                today.start(),
                today.end()
        );
        long failed = fileTransferHistoryRepository.countByTransferStatusAndCreatedAtBetween(
                FileTransferStatus.FAILED,
                today.start(),
                today.end()
        );
        return new FileTransferSummary(success + failed, success, failed);
    }

    private MqSummary mqSummary() {
        Window today = today();
        long publishSuccess = mqMessageHistoryRepository.countByPublishStatusAndCreatedAtBetween(
                MqProcessingStatus.SUCCESS,
                today.start(),
                today.end()
        );
        long publishFailed = mqMessageHistoryRepository.countByPublishStatusAndCreatedAtBetween(
                MqProcessingStatus.FAILED,
                today.start(),
                today.end()
        );
        long consumeSuccess = mqMessageHistoryRepository.countByConsumeStatusAndCreatedAtBetween(
                MqProcessingStatus.SUCCESS,
                today.start(),
                today.end()
        );
        long consumeFailed = mqMessageHistoryRepository.countByConsumeStatusAndCreatedAtBetween(
                MqProcessingStatus.FAILED,
                today.start(),
                today.end()
        );
        return new MqSummary(publishSuccess, publishFailed, consumeSuccess, consumeFailed);
    }

    private BatchSummary batchSummary() {
        Window today = today();
        long completed = batchRunHistoryRepository.countByBatchStatusAndCreatedAtBetween("COMPLETED", today.start(), today.end());
        long failed = batchRunHistoryRepository.countByBatchStatusAndCreatedAtBetween("FAILED", today.start(), today.end());
        long running = batchRunHistoryRepository.countByBatchStatusAndCreatedAtBetween("STARTING", today.start(), today.end())
                + batchRunHistoryRepository.countByBatchStatusAndCreatedAtBetween("STARTED", today.start(), today.end());
        return new BatchSummary(completed + failed + running, completed, failed, running);
    }

    private Window today() {
        LocalDate today = LocalDate.now();
        return new Window(today.atStartOfDay(), today.plusDays(1).atStartOfDay());
    }

    private Window lastSevenDays() {
        LocalDate today = LocalDate.now();
        return new Window(today.minusDays(TREND_DAYS - 1).atStartOfDay(), today.plusDays(1).atStartOfDay());
    }

    public record DashboardOverview(
            List<MetricCard> primaryMetrics,
            List<ProtocolSummary> protocolSummaries,
            List<TrendPoint> executionTrend,
            List<TopFailedInterface> topFailedInterfaces,
            List<InterfaceExecution> recentExecutions,
            List<InterfaceRetryTask> pendingRetries,
            FileTransferSummary fileTransferSummary,
            MqSummary mqSummary,
            BatchSummary batchSummary
    ) {
    }

    public record MetricCard(String label, String value, String caption, String tone, String href) {
    }

    public record ProtocolSummary(
            ProtocolType protocolType,
            long totalInterfaces,
            long activeInterfaces,
            long todaySuccess,
            long todayFailure,
            long recentTotal
    ) {
    }

    public record TrendPoint(LocalDate date, long success, long failure, long total) {
    }

    public record TopFailedInterface(
            Long interfaceDefinitionId,
            String interfaceCode,
            String interfaceName,
            ProtocolType protocolType,
            long failureCount
    ) {
    }

    public record FailureMonitoring(
            List<TopFailedInterface> topFailedInterfaces,
            List<InterfaceExecution> recentFailedExecutions
    ) {
    }

    public record RetryMonitoring(
            long pendingRetryCount,
            long newWaitingToday,
            long doneLastSevenDays,
            List<InterfaceRetryTask> waitingRetries,
            List<InterfaceRetryTask> recentRetryTasks
    ) {
    }

    public record FileTransferSummary(long todayTotal, long todaySuccess, long todayFailed) {
    }

    public record FileTransferMonitoring(
            FileTransferSummary summary,
            List<FileTransferHistory> recentTransfers
    ) {
    }

    public record MqSummary(long publishSuccess, long publishFailed, long consumeSuccess, long consumeFailed) {
    }

    public record MqMonitoring(
            MqSummary summary,
            List<MqMessageHistory> recentMessages
    ) {
    }

    public record BatchSummary(long todayTotal, long todayCompleted, long todayFailed, long todayRunning) {
    }

    public record BatchMonitoring(
            BatchSummary summary,
            List<BatchRunHistory> recentRuns
    ) {
    }

    private record Window(LocalDateTime start, LocalDateTime end) {
    }

    private static final class TrendCounter {

        private long success;
        private long failure;
        private long other;

        private void add(ExecutionStatus status) {
            if (status == ExecutionStatus.SUCCESS) {
                success++;
            } else if (status == ExecutionStatus.FAILED) {
                failure++;
            } else {
                other++;
            }
        }

        private long total() {
            return success + failure + other;
        }
    }

    private static final class InterfaceProtocolCounter {

        private long total;
        private long active;
    }

    private static final class ExecutionProtocolCounter {

        private long total;
        private long success;
        private long failure;
    }
}
