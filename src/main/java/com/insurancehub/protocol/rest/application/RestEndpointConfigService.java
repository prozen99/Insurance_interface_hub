package com.insurancehub.protocol.rest.application;

import java.util.Map;
import java.util.Optional;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.insurancehub.interfacehub.domain.ProtocolType;
import com.insurancehub.interfacehub.domain.entity.InterfaceDefinition;
import com.insurancehub.interfacehub.infrastructure.repository.InterfaceDefinitionRepository;
import com.insurancehub.protocol.rest.domain.entity.RestEndpointConfig;
import com.insurancehub.protocol.rest.infrastructure.repository.RestEndpointConfigRepository;
import com.insurancehub.protocol.rest.presentation.form.RestEndpointConfigForm;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;
import org.springframework.web.server.ResponseStatusException;

@Service
public class RestEndpointConfigService {

    private static final TypeReference<Map<String, Object>> HEADER_TYPE = new TypeReference<>() {
    };

    private final RestEndpointConfigRepository restEndpointConfigRepository;
    private final InterfaceDefinitionRepository interfaceDefinitionRepository;
    private final ObjectMapper objectMapper;

    public RestEndpointConfigService(
            RestEndpointConfigRepository restEndpointConfigRepository,
            InterfaceDefinitionRepository interfaceDefinitionRepository,
            ObjectMapper objectMapper
    ) {
        this.restEndpointConfigRepository = restEndpointConfigRepository;
        this.interfaceDefinitionRepository = interfaceDefinitionRepository;
        this.objectMapper = objectMapper;
    }

    @Transactional(readOnly = true)
    public Optional<RestEndpointConfig> findByInterfaceDefinitionId(Long interfaceDefinitionId) {
        return restEndpointConfigRepository.findByInterfaceDefinitionId(interfaceDefinitionId);
    }

    @Transactional(readOnly = true)
    public RestEndpointConfigForm formForInterface(Long interfaceDefinitionId) {
        InterfaceDefinition interfaceDefinition = getRestInterface(interfaceDefinitionId);
        return restEndpointConfigRepository.findByInterfaceDefinitionId(interfaceDefinition.getId())
                .map(RestEndpointConfigForm::from)
                .orElseGet(RestEndpointConfigForm::empty);
    }

    @Transactional(readOnly = true)
    public RestEndpointConfig getActiveForExecution(InterfaceDefinition interfaceDefinition) {
        assertRestInterface(interfaceDefinition);
        return restEndpointConfigRepository.findByInterfaceDefinitionId(interfaceDefinition.getId())
                .filter(RestEndpointConfig::isActive)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.BAD_REQUEST,
                        "Active REST endpoint configuration is required before execution."
                ));
    }

    @Transactional
    public RestEndpointConfig save(Long interfaceDefinitionId, RestEndpointConfigForm form) {
        InterfaceDefinition interfaceDefinition = getRestInterface(interfaceDefinitionId);
        validateHeadersJson(form.getHeadersJson());

        RestEndpointConfig config = restEndpointConfigRepository.findByInterfaceDefinitionId(interfaceDefinitionId)
                .orElseGet(() -> RestEndpointConfig.create(
                        interfaceDefinition,
                        form.getHttpMethod(),
                        form.getBaseUrl(),
                        form.getPath(),
                        form.getTimeoutMillis(),
                        trimToNull(form.getHeadersJson()),
                        trimToNull(form.getSampleRequestBody()),
                        form.isActive()
                ));

        config.update(
                form.getHttpMethod(),
                form.getBaseUrl(),
                form.getPath(),
                form.getTimeoutMillis(),
                trimToNull(form.getHeadersJson()),
                trimToNull(form.getSampleRequestBody()),
                form.isActive()
        );
        return restEndpointConfigRepository.save(config);
    }

    private InterfaceDefinition getRestInterface(Long interfaceDefinitionId) {
        InterfaceDefinition interfaceDefinition = interfaceDefinitionRepository.findDetailById(interfaceDefinitionId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Interface definition not found"));
        assertRestInterface(interfaceDefinition);
        return interfaceDefinition;
    }

    private void assertRestInterface(InterfaceDefinition interfaceDefinition) {
        if (interfaceDefinition.getProtocolType() != ProtocolType.REST) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "REST configuration is only available for REST interfaces.");
        }
    }

    private void validateHeadersJson(String headersJson) {
        if (!StringUtils.hasText(headersJson)) {
            return;
        }
        try {
            objectMapper.readValue(headersJson, HEADER_TYPE);
        } catch (JsonProcessingException exception) {
            throw new IllegalArgumentException("Headers JSON must be a valid JSON object.");
        }
    }

    private String trimToNull(String value) {
        if (!StringUtils.hasText(value)) {
            return null;
        }
        return value.trim();
    }
}
