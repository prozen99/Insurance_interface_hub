package com.insurancehub.protocol.rest.presentation;

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
import com.insurancehub.protocol.rest.application.RestEndpointConfigService;
import com.insurancehub.protocol.rest.presentation.form.RestEndpointConfigForm;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.security.web.csrf.DefaultCsrfToken;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.test.web.servlet.MockMvc;

@WebMvcTest(RestEndpointConfigController.class)
@AutoConfigureMockMvc(addFilters = false)
class RestEndpointConfigControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private RestEndpointConfigService restEndpointConfigService;

    @MockitoBean
    private InterfaceDefinitionService interfaceDefinitionService;

    @Test
    @WithMockUser(username = "admin")
    void formRendersForRestInterface() throws Exception {
        when(interfaceDefinitionService.getDetail(1L)).thenReturn(restDefinition());
        when(restEndpointConfigService.formForInterface(1L)).thenReturn(RestEndpointConfigForm.empty());

        mockMvc.perform(get("/admin/interfaces/1/rest-config")
                        .requestAttr("_csrf", new DefaultCsrfToken("X-CSRF-TOKEN", "_csrf", "test-token")))
                .andExpect(status().isOk())
                .andExpect(view().name("admin/interfaces/rest-config"))
                .andExpect(model().attributeExists("form"))
                .andExpect(model().attributeExists("interfaceDefinition"));
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
