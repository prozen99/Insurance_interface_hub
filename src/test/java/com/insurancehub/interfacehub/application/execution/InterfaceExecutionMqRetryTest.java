package com.insurancehub.interfacehub.application.execution;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import com.fasterxml.jackson.databind.ObjectMapper;
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
import com.insurancehub.protocol.mq.MqInterfaceExecutor;
import com.insurancehub.protocol.mq.application.MqChannelConfigService;
import com.insurancehub.protocol.mq.config.LocalMqConfig;
import com.insurancehub.protocol.mq.config.MqProperties;
import com.insurancehub.protocol.mq.domain.MqBrokerType;
import com.insurancehub.protocol.mq.domain.MqMessageType;
import com.insurancehub.protocol.mq.domain.entity.MqChannelConfig;
import com.insurancehub.protocol.mq.domain.entity.MqMessageHistory;
import com.insurancehub.protocol.mq.infrastructure.LocalMqClient;
import com.insurancehub.protocol.mq.infrastructure.repository.MqMessageHistoryRepository;
import com.insurancehub.testsupport.TestTransactionManager;
import org.apache.activemq.artemis.core.server.embedded.EmbeddedActiveMQ;
import org.apache.activemq.artemis.jms.client.ActiveMQConnectionFactory;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

@ExtendWith(MockitoExtension.class)
class InterfaceExecutionMqRetryTest {

    private static final int TEST_SERVER_ID = 52;

    @Mock
    private InterfaceDefinitionRepository interfaceDefinitionRepository;

    @Mock
    private InterfaceExecutionRepository interfaceExecutionRepository;

    @Mock
    private InterfaceExecutionStepRepository interfaceExecutionStepRepository;

    @Mock
    private InterfaceRetryTaskRepository interfaceRetryTaskRepository;

    @Mock
    private MqChannelConfigService mqChannelConfigService;

    @Mock
    private MqMessageHistoryRepository mqMessageHistoryRepository;

    private EmbeddedActiveMQ embeddedActiveMQ;
    private InterfaceExecutionService service;

    @BeforeEach
    void setUp() throws Exception {
        MqProperties properties = new MqProperties();
        properties.getEmbedded().setServerId(TEST_SERVER_ID);
        embeddedActiveMQ = new LocalMqConfig().embeddedActiveMQ(properties);
        LocalMqClient localMqClient = new LocalMqClient(new ActiveMQConnectionFactory("vm://" + TEST_SERVER_ID));

        InterfaceExecutorFactory executorFactory = new InterfaceExecutorFactory(List.of(
                new MqInterfaceExecutor(
                        mqChannelConfigService,
                        mqMessageHistoryRepository,
                        localMqClient,
                        new ObjectMapper()
                )
        ));
        service = new InterfaceExecutionService(
                interfaceDefinitionRepository,
                interfaceExecutionRepository,
                interfaceExecutionStepRepository,
                interfaceRetryTaskRepository,
                executorFactory,
                TestTransactionManager.noOp()
        );
        when(interfaceExecutionRepository.existsByExecutionNo(any())).thenReturn(false);
        when(interfaceExecutionRepository.saveAndFlush(any(InterfaceExecution.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(interfaceExecutionRepository.save(any(InterfaceExecution.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(mqMessageHistoryRepository.save(any(MqMessageHistory.class))).thenAnswer(invocation -> invocation.getArgument(0));
    }

    @AfterEach
    void tearDown() throws Exception {
        if (embeddedActiveMQ != null) {
            embeddedActiveMQ.stop();
        }
    }

    @Test
    void retryFailedMqExecutionPublishesAndConsumesAgain() {
        InterfaceDefinition definition = mqDefinition();
        InterfaceExecution original = InterfaceExecution.create(
                "EXE-ORIGINAL",
                definition,
                null,
                ExecutionTriggerType.MANUAL,
                "{\"policyNo\":\"POL-001\"}",
                "admin"
        );
        ReflectionTestUtils.setField(original, "id", 10L);
        original.markRunning(LocalDateTime.now());
        original.markFailed("MQ_CONSUME_ERROR", "first consumer failure", "{}", LocalDateTime.now());
        InterfaceRetryTask retryTask = InterfaceRetryTask.waitingFor(original, LocalDateTime.now());

        when(interfaceExecutionRepository.findDetailById(10L)).thenReturn(Optional.of(original));
        when(interfaceDefinitionRepository.findDetailById(1L)).thenReturn(Optional.of(definition));
        when(interfaceRetryTaskRepository.findFirstByExecutionIdAndRetryStatusOrderByCreatedAtDesc(10L, RetryStatus.WAITING))
                .thenReturn(Optional.of(retryTask));
        when(mqChannelConfigService.getActiveForExecution(definition)).thenReturn(config(definition));

        InterfaceExecution retryExecution = service.retryFailedExecution(10L, "admin");

        assertThat(retryExecution.getTriggerType()).isEqualTo(ExecutionTriggerType.RETRY);
        assertThat(retryExecution.getExecutionStatus()).isEqualTo(ExecutionStatus.SUCCESS);
        assertThat(retryExecution.getRequestMethod()).isEqualTo("PUBLISH_CONSUME");
        assertThat(retryExecution.getRequestUrl()).isEqualTo("insurancehub.test.retry.events");
        assertThat(retryExecution.getResponsePayload()).contains("\"consumeStatus\":\"SUCCESS\"");
        assertThat(retryTask.getRetryStatus()).isEqualTo(RetryStatus.DONE);
        verify(interfaceExecutionStepRepository, times(4)).save(any(InterfaceExecutionStep.class));
    }

    private MqChannelConfig config(InterfaceDefinition definition) {
        return MqChannelConfig.create(
                definition,
                MqBrokerType.EMBEDDED_ARTEMIS,
                "insurancehub.test.retry.events",
                "policy.event",
                MqMessageType.TEXT,
                "MQ-{executionNo}",
                3000,
                true
        );
    }

    private InterfaceDefinition mqDefinition() {
        InterfaceDefinition definition = InterfaceDefinition.create(
                "IF_MQ_POLICY_001",
                "Policy event outbound MQ interface",
                ProtocolType.MQ,
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
