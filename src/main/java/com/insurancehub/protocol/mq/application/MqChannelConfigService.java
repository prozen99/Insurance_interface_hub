package com.insurancehub.protocol.mq.application;

import java.util.Optional;

import com.insurancehub.interfacehub.domain.ProtocolType;
import com.insurancehub.interfacehub.domain.entity.InterfaceDefinition;
import com.insurancehub.interfacehub.infrastructure.repository.InterfaceDefinitionRepository;
import com.insurancehub.protocol.mq.domain.entity.MqChannelConfig;
import com.insurancehub.protocol.mq.infrastructure.repository.MqChannelConfigRepository;
import com.insurancehub.protocol.mq.presentation.form.MqChannelConfigForm;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

@Service
public class MqChannelConfigService {

    public static final String SAMPLE_PAYLOAD = """
            {"eventType":"POLICY_STATUS_CHANGED","policyNo":"POL-001","status":"NORMAL"}
            """;

    private final MqChannelConfigRepository mqChannelConfigRepository;
    private final InterfaceDefinitionRepository interfaceDefinitionRepository;

    public MqChannelConfigService(
            MqChannelConfigRepository mqChannelConfigRepository,
            InterfaceDefinitionRepository interfaceDefinitionRepository
    ) {
        this.mqChannelConfigRepository = mqChannelConfigRepository;
        this.interfaceDefinitionRepository = interfaceDefinitionRepository;
    }

    @Transactional(readOnly = true)
    public Optional<MqChannelConfig> findByInterfaceDefinitionId(Long interfaceDefinitionId) {
        return mqChannelConfigRepository.findByInterfaceDefinitionId(interfaceDefinitionId);
    }

    @Transactional(readOnly = true)
    public MqChannelConfigForm formForInterface(Long interfaceDefinitionId) {
        InterfaceDefinition interfaceDefinition = getMqInterface(interfaceDefinitionId);
        return mqChannelConfigRepository.findByInterfaceDefinitionId(interfaceDefinition.getId())
                .map(MqChannelConfigForm::from)
                .orElseGet(MqChannelConfigForm::empty);
    }

    @Transactional(readOnly = true)
    public MqChannelConfig getActiveForExecution(InterfaceDefinition interfaceDefinition) {
        assertMqInterface(interfaceDefinition);
        return mqChannelConfigRepository.findByInterfaceDefinitionId(interfaceDefinition.getId())
                .filter(MqChannelConfig::isActive)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.BAD_REQUEST,
                        "Active MQ channel configuration is required before execution."
                ));
    }

    @Transactional
    public MqChannelConfig save(Long interfaceDefinitionId, MqChannelConfigForm form) {
        InterfaceDefinition interfaceDefinition = getMqInterface(interfaceDefinitionId);
        MqChannelConfig config = mqChannelConfigRepository.findByInterfaceDefinitionId(interfaceDefinitionId)
                .orElseGet(() -> MqChannelConfig.create(
                        interfaceDefinition,
                        form.getBrokerType(),
                        form.getDestinationName(),
                        form.getRoutingKey(),
                        form.getMessageType(),
                        form.getCorrelationKeyExpression(),
                        form.getTimeoutMillis(),
                        form.isActive()
                ));

        config.update(
                form.getBrokerType(),
                form.getDestinationName(),
                form.getRoutingKey(),
                form.getMessageType(),
                form.getCorrelationKeyExpression(),
                form.getTimeoutMillis(),
                form.isActive()
        );
        return mqChannelConfigRepository.save(config);
    }

    private InterfaceDefinition getMqInterface(Long interfaceDefinitionId) {
        InterfaceDefinition interfaceDefinition = interfaceDefinitionRepository.findDetailById(interfaceDefinitionId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Interface definition not found"));
        assertMqInterface(interfaceDefinition);
        return interfaceDefinition;
    }

    private void assertMqInterface(InterfaceDefinition interfaceDefinition) {
        if (interfaceDefinition.getProtocolType() != ProtocolType.MQ) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "MQ configuration is only available for MQ interfaces.");
        }
    }
}
