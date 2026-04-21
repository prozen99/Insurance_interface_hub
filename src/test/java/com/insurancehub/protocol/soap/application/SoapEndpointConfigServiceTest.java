package com.insurancehub.protocol.soap.application;

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
import com.insurancehub.protocol.soap.domain.entity.SoapEndpointConfig;
import com.insurancehub.protocol.soap.infrastructure.repository.SoapEndpointConfigRepository;
import com.insurancehub.protocol.soap.presentation.form.SoapEndpointConfigForm;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

@ExtendWith(MockitoExtension.class)
class SoapEndpointConfigServiceTest {

    @Mock
    private SoapEndpointConfigRepository soapEndpointConfigRepository;

    @Mock
    private InterfaceDefinitionRepository interfaceDefinitionRepository;

    private SoapEndpointConfigService service;

    @BeforeEach
    void setUp() {
        service = new SoapEndpointConfigService(soapEndpointConfigRepository, interfaceDefinitionRepository);
    }

    @Test
    void saveCreatesSoapConfig() {
        InterfaceDefinition definition = soapDefinition();
        SoapEndpointConfigForm form = SoapEndpointConfigForm.empty();
        form.setEndpointUrl("http://localhost:8080/simulator/soap/policy-inquiry");

        when(interfaceDefinitionRepository.findDetailById(1L)).thenReturn(Optional.of(definition));
        when(soapEndpointConfigRepository.findByInterfaceDefinitionId(1L)).thenReturn(Optional.empty());
        when(soapEndpointConfigRepository.save(any(SoapEndpointConfig.class))).thenAnswer(invocation -> invocation.getArgument(0));

        SoapEndpointConfig saved = service.save(1L, form);

        assertThat(saved.getEndpointUrl()).isEqualTo("http://localhost:8080/simulator/soap/policy-inquiry");
        assertThat(saved.getSoapAction()).isEqualTo("urn:PolicyInquiry");
        assertThat(saved.isActive()).isTrue();
    }

    @Test
    void saveRejectsMalformedRequestTemplateXml() {
        InterfaceDefinition definition = soapDefinition();
        SoapEndpointConfigForm form = SoapEndpointConfigForm.empty();
        form.setRequestTemplateXml("<soapenv:Envelope>");

        when(interfaceDefinitionRepository.findDetailById(1L)).thenReturn(Optional.of(definition));

        assertThatThrownBy(() -> service.save(1L, form))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("well-formed XML");
    }

    private InterfaceDefinition soapDefinition() {
        InterfaceDefinition definition = InterfaceDefinition.create(
                "IF_SOAP_POLICY_001",
                "Policy inquiry outbound SOAP interface",
                ProtocolType.SOAP,
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
