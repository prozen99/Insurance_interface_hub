package com.insurancehub.admin.presentation;

import com.insurancehub.admin.application.DashboardService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class DashboardController {

    private final DashboardService dashboardService;

    public DashboardController(DashboardService dashboardService) {
        this.dashboardService = dashboardService;
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
        return "admin/dashboard";
    }
}
