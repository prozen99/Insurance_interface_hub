package com.insurancehub.interfacehub.application.execution;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import com.insurancehub.interfacehub.domain.ExecutionStatus;
import com.insurancehub.interfacehub.domain.ExecutionTriggerType;
import com.insurancehub.interfacehub.domain.InterfaceDirection;
import com.insurancehub.interfacehub.domain.InterfaceStatus;
import com.insurancehub.interfacehub.domain.MasterStatus;
import com.insurancehub.interfacehub.domain.ProtocolType;
import com.insurancehub.interfacehub.domain.RetryStatus;
import com.insurancehub.interfacehub.domain.entity.InterfaceDefinition;
import com.insurancehub.interfacehub.domain.entity.InterfaceExecution;
import com.insurancehub.interfacehub.domain.entity.InterfaceExecutionStep;
import com.insurancehub.interfacehub.domain.entity.InterfaceRetryTask;
import com.insurancehub.interfacehub.domain.entity.InternalSystem;
import com.insurancehub.interfacehub.domain.entity.PartnerCompany;
import com.insurancehub.interfacehub.infrastructure.repository.InterfaceDefinitionRepository;
import com.insurancehub.interfacehub.infrastructure.repository.InterfaceExecutionRepository;
import com.insurancehub.interfacehub.infrastructure.repository.InterfaceExecutionStepRepository;
import com.insurancehub.interfacehub.infrastructure.repository.InterfaceRetryTaskRepository;
import com.insurancehub.protocol.rest.RestMockInterfaceExecutor;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

@ExtendWith(MockitoExtension.class)
class InterfaceExecutionServiceTest {

    @Mock
    private InterfaceDefinitionRepository interfaceDefinitionRepository;

    @Mock
    private InterfaceExecutionRepository interfaceExecutionRepository;

    @Mock
    private InterfaceExecutionStepRepository interfaceExecutionStepRepository;

    @Mock
    private InterfaceRetryTaskRepository interfaceRetryTaskRepository;

    private InterfaceExecutionService service;

    @BeforeEach
    void setUp() {
        InterfaceExecutorFactory executorFactory = new InterfaceExecutorFactory(List.of(new RestMockInterfaceExecutor()));
        service = new InterfaceExecutionService(
                interfaceDefinitionRepository,
                interfaceExecutionRepository,
                interfaceExecutionStepRepository,
                interfaceRetryTaskRepository,
                executorFactory
        );
        when(interfaceExecutionRepository.existsByExecutionNo(any())).thenReturn(false);
        when(interfaceExecutionRepository.save(any(InterfaceExecution.class))).thenAnswer(invocation -> invocation.getArgument(0));
    }

    @Test
    void executeManualCreatesSuccessfulExecutionAndSteps() {
        InterfaceDefinition definition = activeDefinition("IF_REST_POLICY_001");
        when(interfaceDefinitionRepository.findDetailById(1L)).thenReturn(Optional.of(definition));

        InterfaceExecution execution = service.executeManual(1L, "{\"policyNo\":\"P001\"}", "admin");

        assertThat(execution.getExecutionStatus()).isEqualTo(ExecutionStatus.SUCCESS);
        assertThat(execution.getTriggerType()).isEqualTo(ExecutionTriggerType.MANUAL);
        assertThat(execution.getResponsePayload()).contains("SUCCESS");
        verify(interfaceExecutionStepRepository, times(3)).save(any(InterfaceExecutionStep.class));
        verify(interfaceRetryTaskRepository, never()).save(any(InterfaceRetryTask.class));
    }

    @Test
    void executeManualCreatesFailedExecutionAndRetryTaskWhenPayloadContainsFail() {
        InterfaceDefinition definition = activeDefinition("IF_REST_POLICY_001");
        when(interfaceDefinitionRepository.findDetailById(1L)).thenReturn(Optional.of(definition));

        InterfaceExecution execution = service.executeManual(1L, "please FAIL this mock", "admin");

        assertThat(execution.getExecutionStatus()).isEqualTo(ExecutionStatus.FAILED);
        assertThat(execution.getErrorCode()).isEqualTo("MOCK_EXECUTION_FAILED");
        verify(interfaceRetryTaskRepository).save(any(InterfaceRetryTask.class));
    }

    @Test
    void retryFailedExecutionCreatesRetryExecutionAndMarksTaskDone() {
        InterfaceDefinition definition = activeDefinition("IF_REST_POLICY_001");
        InterfaceExecution original = InterfaceExecution.create(
                "EXE-ORIGINAL",
                definition,
                null,
                ExecutionTriggerType.MANUAL,
                null,
                "admin"
        );
        ReflectionTestUtils.setField(original, "id", 10L);
        original.markRunning(LocalDateTime.now());
        original.markFailed("MOCK_EXECUTION_FAILED", "first failure", "{}", LocalDateTime.now());
        InterfaceRetryTask retryTask = InterfaceRetryTask.waitingFor(original, LocalDateTime.now());

        when(interfaceExecutionRepository.findDetailById(10L)).thenReturn(Optional.of(original));
        when(interfaceRetryTaskRepository.findFirstByExecutionIdAndRetryStatusOrderByCreatedAtDesc(10L, RetryStatus.WAITING))
                .thenReturn(Optional.of(retryTask));

        InterfaceExecution retryExecution = service.retryFailedExecution(10L, "admin");

        assertThat(retryExecution.getTriggerType()).isEqualTo(ExecutionTriggerType.RETRY);
        assertThat(retryExecution.getExecutionStatus()).isEqualTo(ExecutionStatus.SUCCESS);
        assertThat(retryExecution.getRetrySourceExecution()).isEqualTo(original);
        assertThat(retryTask.getRetryStatus()).isEqualTo(RetryStatus.DONE);
        assertThat(retryTask.getRetryCount()).isEqualTo(1);
    }

    private InterfaceDefinition activeDefinition(String code) {
        return InterfaceDefinition.create(
                code,
                "Policy status outbound REST interface",
                ProtocolType.REST,
                InterfaceDirection.OUTBOUND,
                InterfaceStatus.ACTIVE,
                PartnerCompany.create("LIFEPLUS", "Life Plus Insurance", MasterStatus.ACTIVE, null),
                InternalSystem.create("POLICY_CORE", "Policy Core System", "Insurance Platform Team", MasterStatus.ACTIVE, null),
                null
        );
    }
}
