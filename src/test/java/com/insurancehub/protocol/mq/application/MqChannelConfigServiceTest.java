package com.insurancehub.protocol.mq.application;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

import java.util.Optional;

import com.insurancehub.interfacehub.domain.InterfaceDirection;
import com.insurancehub.interfacehub.domain.InterfaceStatus;
import com.insurancehub.interfacehub.domain.MasterStatus;
import com.insurancehub.interfacehub.domain.ProtocolType;
import com.insurancehub.interfacehub.domain.entity.InterfaceDefinition;
import com.insurancehub.interfacehub.domain.entity.InternalSystem;
import com.insurancehub.interfacehub.domain.entity.PartnerCompany;
import com.insurancehub.interfacehub.infrastructure.repository.InterfaceDefinitionRepository;
import com.insurancehub.protocol.mq.domain.MqBrokerType;
import com.insurancehub.protocol.mq.domain.MqMessageType;
import com.insurancehub.protocol.mq.domain.entity.MqChannelConfig;
import com.insurancehub.protocol.mq.infrastructure.repository.MqChannelConfigRepository;
import com.insurancehub.protocol.mq.presentation.form.MqChannelConfigForm;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.server.ResponseStatusException;

@ExtendWith(MockitoExtension.class)
class MqChannelConfigServiceTest {

    @Mock
    private MqChannelConfigRepository mqChannelConfigRepository;

    @Mock
    private InterfaceDefinitionRepository interfaceDefinitionRepository;

    private MqChannelConfigService service;

    @BeforeEach
    void setUp() {
        service = new MqChannelConfigService(mqChannelConfigRepository, interfaceDefinitionRepository);
    }

    @Test
    void saveCreatesMqConfigForMqInterface() {
        InterfaceDefinition definition = mqDefinition();
        MqChannelConfigForm form = MqChannelConfigForm.empty();
        form.setBrokerType(MqBrokerType.EMBEDDED_ARTEMIS);
        form.setDestinationName("insurancehub.demo.claim.events");
        form.setRoutingKey("claim.event");
        form.setMessageType(MqMessageType.TEXT);

        when(interfaceDefinitionRepository.findDetailById(1L)).thenReturn(Optional.of(definition));
        when(mqChannelConfigRepository.findByInterfaceDefinitionId(1L)).thenReturn(Optional.empty());
        when(mqChannelConfigRepository.save(any(MqChannelConfig.class))).thenAnswer(invocation -> invocation.getArgument(0));

        MqChannelConfig saved = service.save(1L, form);

        assertThat(saved.getDestinationName()).isEqualTo("insurancehub.demo.claim.events");
        assertThat(saved.getQueueName()).isEqualTo("insurancehub.demo.claim.events");
        assertThat(saved.getRoutingKey()).isEqualTo("claim.event");
        assertThat(saved.isActive()).isTrue();
    }

    @Test
    void getActiveForExecutionRejectsInactiveConfig() {
        InterfaceDefinition definition = mqDefinition();
        MqChannelConfig inactiveConfig = MqChannelConfig.create(
                definition,
                MqBrokerType.EMBEDDED_ARTEMIS,
                "insurancehub.demo.policy.events",
                "policy.event",
                MqMessageType.TEXT,
                "MQ-{executionNo}",
                5000,
                false
        );

        when(mqChannelConfigRepository.findByInterfaceDefinitionId(1L)).thenReturn(Optional.of(inactiveConfig));

        assertThatThrownBy(() -> service.getActiveForExecution(definition))
                .isInstanceOf(ResponseStatusException.class)
                .hasMessageContaining("Active MQ channel configuration");
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
