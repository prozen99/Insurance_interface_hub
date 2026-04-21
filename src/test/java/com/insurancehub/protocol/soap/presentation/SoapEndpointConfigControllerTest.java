package com.insurancehub.protocol.soap.presentation;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.model;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.view;

import com.insurancehub.interfacehub.application.InterfaceDefinitionService;
import com.insurancehub.interfacehub.domain.InterfaceDirection;
import com.insurancehub.interfacehub.domain.InterfaceStatus;
import com.insurancehub.interfacehub.domain.MasterStatus;
import com.insurancehub.interfacehub.domain.ProtocolType;
import com.insurancehub.interfacehub.domain.entity.InterfaceDefinition;
import com.insurancehub.interfacehub.domain.entity.InternalSystem;
import com.insurancehub.interfacehub.domain.entity.PartnerCompany;
import com.insurancehub.protocol.soap.application.SoapEndpointConfigService;
import com.insurancehub.protocol.soap.presentation.form.SoapEndpointConfigForm;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.security.web.csrf.DefaultCsrfToken;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.test.web.servlet.MockMvc;

@WebMvcTest(SoapEndpointConfigController.class)
@AutoConfigureMockMvc(addFilters = false)
class SoapEndpointConfigControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private SoapEndpointConfigService soapEndpointConfigService;

    @MockitoBean
    private InterfaceDefinitionService interfaceDefinitionService;

    @Test
    @WithMockUser(username = "admin")
    void formRendersForSoapInterface() throws Exception {
        when(interfaceDefinitionService.getDetail(1L)).thenReturn(soapDefinition());
        when(soapEndpointConfigService.formForInterface(1L)).thenReturn(SoapEndpointConfigForm.empty());

        mockMvc.perform(get("/admin/interfaces/1/soap-config")
                        .requestAttr("_csrf", new DefaultCsrfToken("X-CSRF-TOKEN", "_csrf", "test-token")))
                .andExpect(status().isOk())
                .andExpect(view().name("admin/interfaces/soap-config"))
                .andExpect(model().attributeExists("form"))
                .andExpect(model().attributeExists("interfaceDefinition"));
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
