package com.insurancehub.monitoring.application;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

import java.time.LocalDateTime;
import java.util.List;

import com.insurancehub.interfacehub.domain.ExecutionStatus;
import com.insurancehub.interfacehub.domain.InterfaceStatus;
import com.insurancehub.interfacehub.domain.ProtocolType;
import com.insurancehub.interfacehub.domain.RetryStatus;
import com.insurancehub.interfacehub.infrastructure.repository.InterfaceDefinitionRepository;
import com.insurancehub.interfacehub.infrastructure.repository.InterfaceExecutionRepository;
import com.insurancehub.interfacehub.infrastructure.repository.InterfaceRetryTaskRepository;
import com.insurancehub.protocol.batch.infrastructure.repository.BatchRunHistoryRepository;
import com.insurancehub.protocol.filetransfer.infrastructure.repository.FileTransferHistoryRepository;
import com.insurancehub.protocol.mq.infrastructure.repository.MqMessageHistoryRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Pageable;

@ExtendWith(MockitoExtension.class)
class OperationsMonitoringServiceTest {

    @Mock
    private InterfaceDefinitionRepository interfaceDefinitionRepository;

    @Mock
    private InterfaceExecutionRepository interfaceExecutionRepository;

    @Mock
    private InterfaceRetryTaskRepository interfaceRetryTaskRepository;

    @Mock
    private MqMessageHistoryRepository mqMessageHistoryRepository;

    @Mock
    private FileTransferHistoryRepository fileTransferHistoryRepository;

    @Mock
    private BatchRunHistoryRepository batchRunHistoryRepository;

    private OperationsMonitoringService service;

    @BeforeEach
    void setUp() {
        service = new OperationsMonitoringService(
                interfaceDefinitionRepository,
                interfaceExecutionRepository,
                interfaceRetryTaskRepository,
                mqMessageHistoryRepository,
                fileTransferHistoryRepository,
                batchRunHistoryRepository
        );
    }

    @Test
    void dashboardOverviewAggregatesCoreMetricsAndFailureSummary() {
        when(interfaceDefinitionRepository.countByStatus(InterfaceStatus.ACTIVE)).thenReturn(8L);
        when(interfaceDefinitionRepository.count()).thenReturn(10L);
        when(interfaceExecutionRepository.countByExecutionStatusAndStartedAtBetween(eq(ExecutionStatus.SUCCESS), any(LocalDateTime.class), any(LocalDateTime.class)))
                .thenReturn(6L);
        when(interfaceExecutionRepository.countByExecutionStatusAndStartedAtBetween(eq(ExecutionStatus.FAILED), any(LocalDateTime.class), any(LocalDateTime.class)))
                .thenReturn(2L);
        when(interfaceRetryTaskRepository.countByRetryStatus(RetryStatus.WAITING)).thenReturn(3L);
        when(interfaceExecutionRepository.findTrendSource(any(LocalDateTime.class), any(LocalDateTime.class))).thenReturn(List.of());
        when(interfaceExecutionRepository.findTopFailedInterfaces(eq(ExecutionStatus.FAILED), any(LocalDateTime.class), any(LocalDateTime.class), any(Pageable.class)))
                .thenReturn(List.<Object[]>of(new Object[]{1L, "IF_REST_FAIL", "REST failure demo", ProtocolType.REST, 4L}));
        when(interfaceExecutionRepository.findTop10ByOrderByCreatedAtDesc()).thenReturn(List.of());
        when(interfaceRetryTaskRepository.findTop20ByRetryStatusOrderByCreatedAtDesc(RetryStatus.WAITING)).thenReturn(List.of());

        OperationsMonitoringService.DashboardOverview overview = service.dashboardOverview();

        assertThat(overview.primaryMetrics())
                .extracting(OperationsMonitoringService.MetricCard::value)
                .contains("8", "10", "6", "2", "3");
        assertThat(overview.protocolSummaries()).hasSize(ProtocolType.values().length);
        assertThat(overview.topFailedInterfaces()).singleElement()
                .satisfies(failed -> {
                    assertThat(failed.interfaceCode()).isEqualTo("IF_REST_FAIL");
                    assertThat(failed.failureCount()).isEqualTo(4);
                });
        assertThat(overview.executionTrend()).hasSize(7);
    }

    @Test
    void failureMonitoringReturnsConvertedTopFailureRows() {
        when(interfaceExecutionRepository.findTopFailedInterfaces(eq(ExecutionStatus.FAILED), any(LocalDateTime.class), any(LocalDateTime.class), any(Pageable.class)))
                .thenReturn(List.<Object[]>of(new Object[]{9L, "IF_SOAP_FAIL", "SOAP fault demo", ProtocolType.SOAP, 7L}));
        when(interfaceExecutionRepository.findTop20ByExecutionStatusOrderByCreatedAtDesc(ExecutionStatus.FAILED)).thenReturn(List.of());

        OperationsMonitoringService.FailureMonitoring failureMonitoring = service.failureMonitoring();

        assertThat(failureMonitoring.topFailedInterfaces()).singleElement()
                .satisfies(failed -> {
                    assertThat(failed.interfaceDefinitionId()).isEqualTo(9L);
                    assertThat(failed.protocolType()).isEqualTo(ProtocolType.SOAP);
                    assertThat(failed.failureCount()).isEqualTo(7L);
                });
        assertThat(failureMonitoring.recentFailedExecutions()).isEmpty();
    }
}
