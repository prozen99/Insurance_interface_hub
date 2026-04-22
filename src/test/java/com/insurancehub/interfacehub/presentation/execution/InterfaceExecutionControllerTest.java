package com.insurancehub.interfacehub.presentation.execution;

import static org.mockito.Mockito.when;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.redirectedUrl;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

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
import com.insurancehub.protocol.batch.application.BatchRunHistoryService;
import com.insurancehub.protocol.filetransfer.application.FileTransferHistoryService;
import com.insurancehub.protocol.mq.application.MqMessageHistoryService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.test.web.servlet.MockMvc;

@WebMvcTest(InterfaceExecutionController.class)
@AutoConfigureMockMvc(addFilters = false)
class InterfaceExecutionControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private InterfaceExecutionService interfaceExecutionService;

    @MockitoBean
    private MqMessageHistoryService mqMessageHistoryService;

    @MockitoBean
    private FileTransferHistoryService fileTransferHistoryService;

    @MockitoBean
    private BatchRunHistoryService batchRunHistoryService;

    @Test
    @WithMockUser(username = "admin")
    void retryRedirectsToNewExecutionDetail() throws Exception {
        InterfaceExecution retryExecution = InterfaceExecution.create(
                "EXE-RETRY",
                activeDefinition(),
                null,
                ExecutionTriggerType.RETRY,
                null,
                "admin"
        );
        ReflectionTestUtils.setField(retryExecution, "id", 77L);
        when(interfaceExecutionService.retryFailedExecution(eq(10L), anyString())).thenReturn(retryExecution);

        mockMvc.perform(post("/admin/executions/10/retry"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/admin/executions/77"));
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
