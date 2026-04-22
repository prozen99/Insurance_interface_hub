package com.insurancehub.admin.presentation;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.model;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.view;

import java.util.List;

import com.insurancehub.admin.application.DashboardService;
import com.insurancehub.monitoring.application.OperationsMonitoringService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

@WebMvcTest(DashboardController.class)
@AutoConfigureMockMvc(addFilters = false)
class DashboardControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private DashboardService dashboardService;

    @MockitoBean
    private OperationsMonitoringService operationsMonitoringService;

    @Test
    void dashboardRendersOperationsOverview() throws Exception {
        when(dashboardService.getSummaryMetrics()).thenReturn(List.of());
        when(dashboardService.getProtocolModules()).thenReturn(List.of());
        when(operationsMonitoringService.dashboardOverview()).thenReturn(emptyOverview());

        mockMvc.perform(get("/admin"))
                .andExpect(status().isOk())
                .andExpect(view().name("admin/dashboard"))
                .andExpect(model().attributeExists("overview"));
    }

    private OperationsMonitoringService.DashboardOverview emptyOverview() {
        return new OperationsMonitoringService.DashboardOverview(
                List.of(),
                List.of(),
                List.of(),
                List.of(),
                List.of(),
                List.of(),
                new OperationsMonitoringService.FileTransferSummary(0, 0, 0),
                new OperationsMonitoringService.MqSummary(0, 0, 0, 0),
                new OperationsMonitoringService.BatchSummary(0, 0, 0, 0)
        );
    }
}
