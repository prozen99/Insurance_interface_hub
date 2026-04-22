package com.insurancehub.admin.presentation;

import com.insurancehub.admin.application.DashboardService;
import com.insurancehub.monitoring.application.OperationsMonitoringService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class DashboardController {

    private final DashboardService dashboardService;
    private final OperationsMonitoringService operationsMonitoringService;

    public DashboardController(
            DashboardService dashboardService,
            OperationsMonitoringService operationsMonitoringService
    ) {
        this.dashboardService = dashboardService;
        this.operationsMonitoringService = operationsMonitoringService;
    }

    @GetMapping("/")
    public String home() {
        return "redirect:/admin";
    }

    @GetMapping("/admin")
    public String dashboard(Model model) {
        model.addAttribute("activeNav", "dashboard");
        model.addAttribute("summaryMetrics", dashboardService.getSummaryMetrics());
        model.addAttribute("protocolModules", dashboardService.getProtocolModules());
        model.addAttribute("overview", operationsMonitoringService.dashboardOverview());
        return "admin/dashboard";
    }
}
