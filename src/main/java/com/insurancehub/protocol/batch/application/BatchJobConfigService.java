package com.insurancehub.protocol.batch.application;

import java.util.List;
import java.util.Optional;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.insurancehub.interfacehub.domain.ProtocolType;
import com.insurancehub.interfacehub.domain.entity.InterfaceDefinition;
import com.insurancehub.interfacehub.infrastructure.repository.InterfaceDefinitionRepository;
import com.insurancehub.protocol.batch.domain.entity.BatchJobConfig;
import com.insurancehub.protocol.batch.infrastructure.repository.BatchJobConfigRepository;
import com.insurancehub.protocol.batch.presentation.form.BatchJobConfigForm;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;
import org.springframework.web.server.ResponseStatusException;

@Service
public class BatchJobConfigService {

    private final BatchJobConfigRepository batchJobConfigRepository;
    private final InterfaceDefinitionRepository interfaceDefinitionRepository;
    private final ObjectMapper objectMapper;

    public BatchJobConfigService(
            BatchJobConfigRepository batchJobConfigRepository,
            InterfaceDefinitionRepository interfaceDefinitionRepository,
            ObjectMapper objectMapper
    ) {
        this.batchJobConfigRepository = batchJobConfigRepository;
        this.interfaceDefinitionRepository = interfaceDefinitionRepository;
        this.objectMapper = objectMapper;
    }

    @Transactional(readOnly = true)
    public Optional<BatchJobConfig> findByInterfaceDefinitionId(Long interfaceDefinitionId) {
        return batchJobConfigRepository.findByInterfaceDefinitionId(interfaceDefinitionId);
    }

    @Transactional(readOnly = true)
    public BatchJobConfigForm formForInterface(Long interfaceDefinitionId) {
        InterfaceDefinition interfaceDefinition = getBatchInterface(interfaceDefinitionId);
        return batchJobConfigRepository.findByInterfaceDefinitionId(interfaceDefinition.getId())
                .map(BatchJobConfigForm::from)
                .orElseGet(BatchJobConfigForm::empty);
    }

    @Transactional(readOnly = true)
    public BatchJobConfig getActiveForExecution(InterfaceDefinition interfaceDefinition) {
        assertBatchInterface(interfaceDefinition);
        return batchJobConfigRepository.findByInterfaceDefinitionId(interfaceDefinition.getId())
                .filter(BatchJobConfig::isActive)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.BAD_REQUEST,
                        "Active batch job configuration is required before execution."
                ));
    }

    @Transactional(readOnly = true)
    public List<BatchJobConfig> findSchedulableConfigs() {
        return batchJobConfigRepository.findByActiveTrueAndEnabledTrueOrderByUpdatedAtAsc();
    }

    @Transactional
    public BatchJobConfig save(Long interfaceDefinitionId, BatchJobConfigForm form) {
        InterfaceDefinition interfaceDefinition = getBatchInterface(interfaceDefinitionId);
        validateParameterTemplate(form.getParameterTemplateJson());

        BatchJobConfig config = batchJobConfigRepository.findByInterfaceDefinitionId(interfaceDefinitionId)
                .orElseGet(() -> BatchJobConfig.create(
                        interfaceDefinition,
                        form.getJobType(),
                        form.getJobName(),
                        form.getCronExpression(),
                        form.getParameterTemplateJson(),
                        form.isEnabled(),
                        form.getMaxParallelCount(),
                        form.isRetryable(),
                        form.getTimeoutMillis(),
                        form.isActive()
                ));

        config.update(
                form.getJobType(),
                form.getJobName(),
                form.getCronExpression(),
                form.getParameterTemplateJson(),
                form.isEnabled(),
                form.getMaxParallelCount(),
                form.isRetryable(),
                form.getTimeoutMillis(),
                form.isActive()
        );
        return batchJobConfigRepository.save(config);
    }

    private InterfaceDefinition getBatchInterface(Long interfaceDefinitionId) {
        InterfaceDefinition interfaceDefinition = interfaceDefinitionRepository.findDetailById(interfaceDefinitionId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Interface definition not found"));
        assertBatchInterface(interfaceDefinition);
        return interfaceDefinition;
    }

    private void assertBatchInterface(InterfaceDefinition interfaceDefinition) {
        if (interfaceDefinition.getProtocolType() != ProtocolType.BATCH) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Batch configuration is only available for BATCH interfaces.");
        }
    }

    private void validateParameterTemplate(String value) {
        if (!StringUtils.hasText(value)) {
            return;
        }
        try {
            objectMapper.readTree(value);
        } catch (Exception exception) {
            throw new IllegalArgumentException("Parameter template JSON must be valid JSON.");
        }
    }
}
