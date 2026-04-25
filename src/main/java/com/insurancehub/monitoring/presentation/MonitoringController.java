package com.insurancehub.monitoring.presentation;

import com.insurancehub.monitoring.application.OperationsMonitoringService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/admin/monitoring")
public class MonitoringController {

    private final OperationsMonitoringService operationsMonitoringService;

    public MonitoringController(OperationsMonitoringService operationsMonitoringService) {
        this.operationsMonitoringService = operationsMonitoringService;
    }

    @GetMapping
    public String overview(Model model) {
        model.addAttribute("activeNav", "monitoring");
        model.addAttribute("overview", operationsMonitoringService.dashboardOverview());
        return "admin/monitoring/overview";
    }

    @GetMapping("/failures")
    public String failures(Model model) {
        model.addAttribute("activeNav", "monitoring");
        model.addAttribute("failureMonitoring", operationsMonitoringService.failureMonitoring());
        return "admin/monitoring/failures";
    }

    @GetMapping("/retries")
    public String retries(Model model) {
        model.addAttribute("activeNav", "monitoring");
        model.addAttribute("retryMonitoring", operationsMonitoringService.retryMonitoring());
        return "admin/monitoring/retries";
    }

    @GetMapping("/protocols")
    public String protocols(Model model) {
        model.addAttribute("activeNav", "monitoring");
        model.addAttribute("protocolSummaries", operationsMonitoringService.protocolSummaries());
        model.addAttribute("trend", operationsMonitoringService.executionTrend());
        return "admin/monitoring/protocols";
    }

    @GetMapping("/files")
    public String fileTransfers(Model model) {
        model.addAttribute("activeNav", "monitoring");
        model.addAttribute("fileTransferMonitoring", operationsMonitoringService.fileTransferMonitoring());
        return "admin/monitoring/files";
    }

    @GetMapping("/mq")
    public String mq(Model model) {
        model.addAttribute("activeNav", "monitoring");
        model.addAttribute("mqMonitoring", operationsMonitoringService.mqMonitoring());
        return "admin/monitoring/mq";
    }

    @GetMapping("/batch")
    public String batch(Model model) {
        model.addAttribute("activeNav", "monitoring");
        model.addAttribute("batchMonitoring", operationsMonitoringService.batchMonitoring());
        return "admin/monitoring/batch";
    }
}
