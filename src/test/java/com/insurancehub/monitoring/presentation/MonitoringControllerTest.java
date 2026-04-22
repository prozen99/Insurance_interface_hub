package com.insurancehub.monitoring.presentation;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.model;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.view;

import java.util.List;

import com.insurancehub.monitoring.application.OperationsMonitoringService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

@WebMvcTest(MonitoringController.class)
@AutoConfigureMockMvc(addFilters = false)
class MonitoringControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private OperationsMonitoringService operationsMonitoringService;

    @Test
    void overviewRendersMonitoringPage() throws Exception {
        when(operationsMonitoringService.dashboardOverview()).thenReturn(emptyOverview());

        mockMvc.perform(get("/admin/monitoring"))
                .andExpect(status().isOk())
                .andExpect(view().name("admin/monitoring/overview"))
                .andExpect(model().attributeExists("overview"));
    }

    @Test
    void failuresRendersFailureMonitoringPage() throws Exception {
        when(operationsMonitoringService.failureMonitoring())
                .thenReturn(new OperationsMonitoringService.FailureMonitoring(List.of(), List.of()));

        mockMvc.perform(get("/admin/monitoring/failures"))
                .andExpect(status().isOk())
                .andExpect(view().name("admin/monitoring/failures"))
                .andExpect(model().attributeExists("failureMonitoring"));
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
