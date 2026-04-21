package com.insurancehub.protocol.rest.application;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

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
import com.insurancehub.protocol.rest.domain.RestHttpMethod;
import com.insurancehub.protocol.rest.domain.entity.RestEndpointConfig;
import com.insurancehub.protocol.rest.infrastructure.repository.RestEndpointConfigRepository;
import com.insurancehub.protocol.rest.presentation.form.RestEndpointConfigForm;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

@ExtendWith(MockitoExtension.class)
class RestEndpointConfigServiceTest {

    @Mock
    private RestEndpointConfigRepository restEndpointConfigRepository;

    @Mock
    private InterfaceDefinitionRepository interfaceDefinitionRepository;

    private RestEndpointConfigService service;

    @BeforeEach
    void setUp() {
        service = new RestEndpointConfigService(
                restEndpointConfigRepository,
                interfaceDefinitionRepository,
                new ObjectMapper()
        );
    }

    @Test
    void saveCreatesRestConfigAndNormalizesEndpointUrl() {
        InterfaceDefinition definition = restDefinition();
        RestEndpointConfigForm form = RestEndpointConfigForm.empty();
        form.setBaseUrl("http://localhost:8080/");
        form.setHttpMethod(RestHttpMethod.POST);
        form.setPath("simulator/rest/premium/calculate");

        when(interfaceDefinitionRepository.findDetailById(1L)).thenReturn(Optional.of(definition));
        when(restEndpointConfigRepository.findByInterfaceDefinitionId(1L)).thenReturn(Optional.empty());
        when(restEndpointConfigRepository.save(any(RestEndpointConfig.class))).thenAnswer(invocation -> invocation.getArgument(0));

        RestEndpointConfig saved = service.save(1L, form);

        assertThat(saved.getEndpointUrl()).isEqualTo("http://localhost:8080/simulator/rest/premium/calculate");
        assertThat(saved.getHttpMethod()).isEqualTo(RestHttpMethod.POST);
        assertThat(saved.isActive()).isTrue();
    }

    @Test
    void saveRejectsInvalidHeadersJson() {
        InterfaceDefinition definition = restDefinition();
        RestEndpointConfigForm form = RestEndpointConfigForm.empty();
        form.setHeadersJson("{bad-json");

        when(interfaceDefinitionRepository.findDetailById(1L)).thenReturn(Optional.of(definition));

        assertThatThrownBy(() -> service.save(1L, form))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Headers JSON");
    }

    private InterfaceDefinition restDefinition() {
        InterfaceDefinition definition = InterfaceDefinition.create(
                "IF_REST_POLICY_001",
                "Policy status outbound REST interface",
                ProtocolType.REST,
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
