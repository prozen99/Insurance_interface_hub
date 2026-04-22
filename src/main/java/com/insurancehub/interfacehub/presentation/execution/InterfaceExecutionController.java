package com.insurancehub.interfacehub.presentation.execution;

import com.insurancehub.interfacehub.application.execution.ExecutionNotAllowedException;
import com.insurancehub.interfacehub.application.execution.InterfaceExecutionService;
import com.insurancehub.interfacehub.domain.ExecutionStatus;
import com.insurancehub.interfacehub.domain.ProtocolType;
import com.insurancehub.interfacehub.domain.entity.InterfaceExecution;
import com.insurancehub.protocol.batch.application.BatchRunHistoryService;
import com.insurancehub.protocol.filetransfer.application.FileTransferHistoryService;
import com.insurancehub.protocol.mq.application.MqMessageHistoryService;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/admin/executions")
public class InterfaceExecutionController {

    private final InterfaceExecutionService interfaceExecutionService;
    private final MqMessageHistoryService mqMessageHistoryService;
    private final FileTransferHistoryService fileTransferHistoryService;
    private final BatchRunHistoryService batchRunHistoryService;

    public InterfaceExecutionController(
            InterfaceExecutionService interfaceExecutionService,
            MqMessageHistoryService mqMessageHistoryService,
            FileTransferHistoryService fileTransferHistoryService,
            BatchRunHistoryService batchRunHistoryService
    ) {
        this.interfaceExecutionService = interfaceExecutionService;
        this.mqMessageHistoryService = mqMessageHistoryService;
        this.fileTransferHistoryService = fileTransferHistoryService;
        this.batchRunHistoryService = batchRunHistoryService;
    }

    @ModelAttribute("protocolOptions")
    public ProtocolType[] protocolOptions() {
        return ProtocolType.values();
    }

    @ModelAttribute("executionStatusOptions")
    public ExecutionStatus[] executionStatusOptions() {
        return ExecutionStatus.values();
    }

    @GetMapping
    public String list(
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) ProtocolType protocolType,
            @RequestParam(required = false) ExecutionStatus executionStatus,
            Model model
    ) {
        model.addAttribute("activeNav", "executions");
        model.addAttribute("executions", interfaceExecutionService.search(keyword, protocolType, executionStatus));
        model.addAttribute("keyword", keyword);
        model.addAttribute("selectedProtocolType", protocolType);
        model.addAttribute("selectedExecutionStatus", executionStatus);
        return "admin/executions/list";
    }

    @GetMapping("/failed")
    public String failed(Model model) {
        model.addAttribute("activeNav", "executions");
        model.addAttribute("executions", interfaceExecutionService.search(null, null, ExecutionStatus.FAILED));
        model.addAttribute("selectedExecutionStatus", ExecutionStatus.FAILED);
        return "admin/executions/list";
    }

    @GetMapping("/{id}")
    public String detail(@PathVariable Long id, Model model) {
        addDetailModel(id, model);
        return "admin/executions/detail";
    }

    @PostMapping("/{id}/retry")
    public String retry(
            @PathVariable Long id,
            Authentication authentication,
            RedirectAttributes redirectAttributes
    ) {
        try {
            InterfaceExecution retryExecution = interfaceExecutionService.retryFailedExecution(
                    id,
                    authentication == null ? "anonymous" : authentication.getName()
            );
            redirectAttributes.addFlashAttribute("successMessage", "Retry execution finished.");
            return "redirect:/admin/executions/" + retryExecution.getId();
        } catch (ExecutionNotAllowedException exception) {
            redirectAttributes.addFlashAttribute("errorMessage", exception.getMessage());
            return "redirect:/admin/executions/" + id;
        }
    }

    private void addDetailModel(Long id, Model model) {
        model.addAttribute("activeNav", "executions");
        InterfaceExecution execution = interfaceExecutionService.getDetail(id);
        model.addAttribute("execution", execution);
        model.addAttribute("steps", interfaceExecutionService.getSteps(id));
        model.addAttribute("retryTasks", interfaceExecutionService.getRetryTasks(id));
        if (execution.getProtocolType() == ProtocolType.MQ) {
            model.addAttribute("mqMessages", mqMessageHistoryService.findByExecutionId(id));
        }
        if (execution.getProtocolType() == ProtocolType.SFTP || execution.getProtocolType() == ProtocolType.FTP) {
            model.addAttribute("fileTransfers", fileTransferHistoryService.findByExecutionId(id));
        }
        if (execution.getProtocolType() == ProtocolType.BATCH) {
            model.addAttribute("batchRuns", batchRunHistoryService.findByExecutionId(id));
        }
    }
}
