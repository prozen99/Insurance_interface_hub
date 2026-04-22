package com.insurancehub.interfacehub.presentation;

import static org.mockito.Mockito.when;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.redirectedUrl;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.insurancehub.interfacehub.application.InterfaceDefinitionService;
import com.insurancehub.interfacehub.application.InternalSystemService;
import com.insurancehub.interfacehub.application.PartnerCompanyService;
import com.insurancehub.interfacehub.application.execution.InterfaceExecutionService;
import com.insurancehub.interfacehub.domain.ExecutionTriggerType;
import com.insurancehub.interfacehub.domain.InterfaceDirection;
import com.insurancehub.interfacehub.domain.InterfaceStatus;
import com.insurancehub.interfacehub.domain.MasterStatus;
import com.insurancehub.interfacehub.domain.ProtocolType;
import com.insurancehub.interfacehub.domain.entity.InterfaceDefinition;
import com.insurancehub.interfacehub.domain.entity.InterfaceExecution;
import com.insurancehub.interfacehub.domain.entity.InternalSystem;
import com.insurancehub.interfacehub.domain.entity.PartnerCompany;
import com.insurancehub.protocol.filetransfer.application.FileTransferConfigService;
import com.insurancehub.protocol.filetransfer.application.FileTransferPayloadCodec;
import com.insurancehub.protocol.mq.application.MqChannelConfigService;
import com.insurancehub.protocol.rest.application.RestEndpointConfigService;
import com.insurancehub.protocol.soap.application.SoapEndpointConfigService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.test.web.servlet.MockMvc;

@WebMvcTest(InterfaceDefinitionController.class)
@AutoConfigureMockMvc(addFilters = false)
class InterfaceDefinitionControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private InterfaceDefinitionService interfaceDefinitionService;

    @MockitoBean
    private PartnerCompanyService partnerCompanyService;

    @MockitoBean
    private InternalSystemService internalSystemService;

    @MockitoBean
    private InterfaceExecutionService interfaceExecutionService;

    @MockitoBean
    private RestEndpointConfigService restEndpointConfigService;

    @MockitoBean
    private SoapEndpointConfigService soapEndpointConfigService;

    @MockitoBean
    private MqChannelConfigService mqChannelConfigService;

    @MockitoBean
    private FileTransferConfigService fileTransferConfigService;

    @MockitoBean
    private FileTransferPayloadCodec fileTransferPayloadCodec;

    @Test
    @WithMockUser(username = "admin")
    void manualExecutionRedirectsToExecutionDetail() throws Exception {
        InterfaceDefinition definition = activeDefinition();
        InterfaceExecution execution = InterfaceExecution.create(
                "EXE-MANUAL",
                definition,
                null,
                ExecutionTriggerType.MANUAL,
                "{}",
                "admin"
        );
        ReflectionTestUtils.setField(execution, "id", 55L);
        when(interfaceDefinitionService.getDetail(1L)).thenReturn(definition);
        when(interfaceExecutionService.executeManual(eq(1L), eq("{}"), anyString())).thenReturn(execution);

        mockMvc.perform(post("/admin/interfaces/1/execute")
                        .param("requestPayload", "{}"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/admin/executions/55"));
    }

    private InterfaceDefinition activeDefinition() {
        return InterfaceDefinition.create(
                "IF_REST_POLICY_001",
                "Policy status outbound REST interface",
                ProtocolType.REST,
                InterfaceDirection.OUTBOUND,
                InterfaceStatus.ACTIVE,
                PartnerCompany.create("LIFEPLUS", "Life Plus Insurance", MasterStatus.ACTIVE, null),
                InternalSystem.create("POLICY_CORE", "Policy Core System", "Insurance Platform Team", MasterStatus.ACTIVE, null),
                null
        );
    }
}
