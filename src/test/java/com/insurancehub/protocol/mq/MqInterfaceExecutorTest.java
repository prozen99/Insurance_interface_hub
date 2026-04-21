package com.insurancehub.protocol.mq;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

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
import com.insurancehub.protocol.mq.application.MqChannelConfigService;
import com.insurancehub.protocol.mq.config.LocalMqConfig;
import com.insurancehub.protocol.mq.config.MqProperties;
import com.insurancehub.protocol.mq.domain.MqBrokerType;
import com.insurancehub.protocol.mq.domain.MqMessageType;
import com.insurancehub.protocol.mq.domain.MqProcessingStatus;
import com.insurancehub.protocol.mq.domain.entity.MqChannelConfig;
import com.insurancehub.protocol.mq.domain.entity.MqMessageHistory;
import com.insurancehub.protocol.mq.infrastructure.LocalMqClient;
import com.insurancehub.protocol.mq.infrastructure.repository.MqMessageHistoryRepository;
import org.apache.activemq.artemis.core.server.embedded.EmbeddedActiveMQ;
import org.apache.activemq.artemis.jms.client.ActiveMQConnectionFactory;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

@ExtendWith(MockitoExtension.class)
class MqInterfaceExecutorTest {

    private static final int TEST_SERVER_ID = 51;

    @Mock
    private MqChannelConfigService mqChannelConfigService;

    @Mock
    private MqMessageHistoryRepository mqMessageHistoryRepository;

    private EmbeddedActiveMQ embeddedActiveMQ;
    private MqInterfaceExecutor executor;

    @BeforeEach
    void setUp() throws Exception {
        MqProperties properties = new MqProperties();
        properties.getEmbedded().setServerId(TEST_SERVER_ID);
        embeddedActiveMQ = new LocalMqConfig().embeddedActiveMQ(properties);
        LocalMqClient localMqClient = new LocalMqClient(new ActiveMQConnectionFactory("vm://" + TEST_SERVER_ID));

        executor = new MqInterfaceExecutor(
                mqChannelConfigService,
                mqMessageHistoryRepository,
                localMqClient,
                new ObjectMapper()
        );
        when(mqMessageHistoryRepository.save(any(MqMessageHistory.class))).thenAnswer(invocation -> invocation.getArgument(0));
    }

    @AfterEach
    void tearDown() throws Exception {
        if (embeddedActiveMQ != null) {
            embeddedActiveMQ.stop();
        }
    }

    @Test
    void executePublishesAndConsumesRealMqMessage() {
        InterfaceDefinition definition = mqDefinition();
        MqChannelConfig config = config(definition);
        when(mqChannelConfigService.getActiveForExecution(definition)).thenReturn(config);

        ExecutionResult result = executor.execute(new ExecutionRequest(
                definition,
                execution(definition, "EXE-MQ-001", "{\"policyNo\":\"POL-001\"}"),
                ExecutionTriggerType.MANUAL,
                "{\"policyNo\":\"POL-001\"}"
        ));

        assertThat(result.success()).isTrue();
        assertThat(result.requestUrl()).isEqualTo("insurancehub.test.policy.events");
        assertThat(result.requestMethod()).isEqualTo("PUBLISH_CONSUME");
        assertThat(result.protocolAction()).isEqualTo("MQ-EXE-MQ-001");
        assertThat(result.responsePayload()).contains("\"consumeStatus\":\"SUCCESS\"");

        MqMessageHistory history = savedHistory();
        assertThat(history.getPublishStatus()).isEqualTo(MqProcessingStatus.SUCCESS);
        assertThat(history.getConsumeStatus()).isEqualTo(MqProcessingStatus.SUCCESS);
        assertThat(history.getConsumedPayload()).contains("POL-001");
        assertThat(history.getMessageId()).isNotBlank();
    }

    @Test
    void executeReturnsFailureWhenConsumerProcessingFails() {
        InterfaceDefinition definition = mqDefinition();
        MqChannelConfig config = config(definition);
        when(mqChannelConfigService.getActiveForExecution(definition)).thenReturn(config);

        ExecutionResult result = executor.execute(new ExecutionRequest(
                definition,
                execution(definition, "EXE-MQ-FAIL", "{\"policyNo\":\"FAIL\"}"),
                ExecutionTriggerType.MANUAL,
                "{\"policyNo\":\"FAIL\"}"
        ));

        assertThat(result.success()).isFalse();
        assertThat(result.errorCode()).isEqualTo("MQ_CONSUME_ERROR");
        assertThat(result.responseHeaders()).contains("\"consumeStatus\":\"FAILED\"");

        MqMessageHistory history = savedHistory();
        assertThat(history.getPublishStatus()).isEqualTo(MqProcessingStatus.SUCCESS);
        assertThat(history.getConsumeStatus()).isEqualTo(MqProcessingStatus.FAILED);
        assertThat(history.getErrorMessage()).contains("FAIL");
    }

    private MqMessageHistory savedHistory() {
        ArgumentCaptor<MqMessageHistory> historyCaptor = ArgumentCaptor.forClass(MqMessageHistory.class);
        verify(mqMessageHistoryRepository).save(historyCaptor.capture());
        return historyCaptor.getValue();
    }

    private InterfaceExecution execution(InterfaceDefinition definition, String executionNo, String requestPayload) {
        return InterfaceExecution.create(
                executionNo,
                definition,
                null,
                ExecutionTriggerType.MANUAL,
                requestPayload,
                "admin"
        );
    }

    private MqChannelConfig config(InterfaceDefinition definition) {
        return MqChannelConfig.create(
                definition,
                MqBrokerType.EMBEDDED_ARTEMIS,
                "insurancehub.test.policy.events",
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
