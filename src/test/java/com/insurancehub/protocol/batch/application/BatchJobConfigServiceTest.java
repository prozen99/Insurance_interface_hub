package com.insurancehub.protocol.batch.application;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;
import static org.mockito.ArgumentMatchers.any;

import java.util.Optional;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.insurancehub.interfacehub.domain.InterfaceDirection;
import com.insurancehub.interfacehub.domain.InterfaceStatus;
import com.insurancehub.interfacehub.domain.MasterStatus;
import com.insurancehub.interfacehub.domain.ProtocolType;
import com.insurancehub.interfacehub.domain.entity.InterfaceDefinition;
import com.insurancehub.interfacehub.domain.entity.InternalSystem;
import com.insurancehub.interfacehub.domain.entity.PartnerCompany;
import com.insurancehub.interfacehub.infrastructure.repository.InterfaceDefinitionRepository;
import com.insurancehub.protocol.batch.domain.BatchJobType;
import com.insurancehub.protocol.batch.domain.entity.BatchJobConfig;
import com.insurancehub.protocol.batch.infrastructure.repository.BatchJobConfigRepository;
import com.insurancehub.protocol.batch.presentation.form.BatchJobConfigForm;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

@ExtendWith(MockitoExtension.class)
class BatchJobConfigServiceTest {

    @Mock
    private BatchJobConfigRepository batchJobConfigRepository;

    @Mock
    private InterfaceDefinitionRepository interfaceDefinitionRepository;

    private BatchJobConfigService service;

    @BeforeEach
    void setUp() {
        service = new BatchJobConfigService(batchJobConfigRepository, interfaceDefinitionRepository, new ObjectMapper());
    }

    @Test
    void saveCreatesBatchJobConfigForBatchInterface() {
        InterfaceDefinition definition = batchDefinition();
        when(interfaceDefinitionRepository.findDetailById(1L)).thenReturn(Optional.of(definition));
        when(batchJobConfigRepository.findByInterfaceDefinitionId(1L)).thenReturn(Optional.empty());
        when(batchJobConfigRepository.save(any(BatchJobConfig.class))).thenAnswer(invocation -> invocation.getArgument(0));

        BatchJobConfigForm form = BatchJobConfigForm.empty();
        form.setJobType(BatchJobType.FAILED_RETRY_AGGREGATION);
        form.setJobName(BatchJobType.FAILED_RETRY_AGGREGATION.getJobName());
        form.setParameterTemplateJson("{\"businessDate\":\"TODAY\",\"forceFail\":false}");
        form.setEnabled(true);

        BatchJobConfig saved = service.save(1L, form);

        assertThat(saved.getJobType()).isEqualTo(BatchJobType.FAILED_RETRY_AGGREGATION);
        assertThat(saved.getJobName()).isEqualTo("failedExecutionRetryAggregationJob");
        assertThat(saved.isEnabled()).isTrue();
        assertThat(saved.isActive()).isTrue();
    }

    private InterfaceDefinition batchDefinition() {
        InterfaceDefinition definition = InterfaceDefinition.create(
                "IF_BATCH_SETTLEMENT_001",
                "Daily interface settlement summary batch",
                ProtocolType.BATCH,
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
