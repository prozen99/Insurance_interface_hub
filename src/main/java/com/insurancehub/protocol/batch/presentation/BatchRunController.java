package com.insurancehub.protocol.batch.presentation;

import com.insurancehub.protocol.batch.application.BatchRunHistoryService;
import com.insurancehub.protocol.batch.domain.entity.BatchRunHistory;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/admin/batch-runs")
public class BatchRunController {

    private final BatchRunHistoryService batchRunHistoryService;

    public BatchRunController(BatchRunHistoryService batchRunHistoryService) {
        this.batchRunHistoryService = batchRunHistoryService;
    }

    @GetMapping
    public String list(Model model) {
        model.addAttribute("activeNav", "batchRuns");
        model.addAttribute("batchRuns", batchRunHistoryService.recentRuns());
        return "admin/batch-runs/list";
    }

    @GetMapping("/{id}")
    public String detail(@PathVariable Long id, Model model) {
        BatchRunHistory batchRun = batchRunHistoryService.getDetail(id);
        model.addAttribute("activeNav", "batchRuns");
        model.addAttribute("batchRun", batchRun);
        model.addAttribute("batchSteps", batchRunHistoryService.getSteps(id));
        return "admin/batch-runs/detail";
    }
}
