package com.insurancehub.protocol.batch.presentation;

import com.insurancehub.interfacehub.application.InterfaceDefinitionService;
import com.insurancehub.interfacehub.domain.entity.InterfaceDefinition;
import com.insurancehub.protocol.batch.application.BatchJobConfigService;
import com.insurancehub.protocol.batch.domain.BatchJobType;
import com.insurancehub.protocol.batch.presentation.form.BatchJobConfigForm;
import jakarta.validation.Valid;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/admin/interfaces/{interfaceId}/batch-config")
public class BatchJobConfigController {

    private final BatchJobConfigService batchJobConfigService;
    private final InterfaceDefinitionService interfaceDefinitionService;

    public BatchJobConfigController(
            BatchJobConfigService batchJobConfigService,
            InterfaceDefinitionService interfaceDefinitionService
    ) {
        this.batchJobConfigService = batchJobConfigService;
        this.interfaceDefinitionService = interfaceDefinitionService;
    }

    @ModelAttribute("jobTypeOptions")
    public BatchJobType[] jobTypeOptions() {
        return BatchJobType.values();
    }

    @GetMapping
    public String form(@PathVariable Long interfaceId, Model model) {
        InterfaceDefinition interfaceDefinition = interfaceDefinitionService.getDetail(interfaceId);
        model.addAttribute("form", batchJobConfigService.formForInterface(interfaceId));
        return batchConfigForm(model, interfaceDefinition);
    }

    @PostMapping
    public String save(
            @PathVariable Long interfaceId,
            @Valid @ModelAttribute("form") BatchJobConfigForm form,
            BindingResult bindingResult,
            Model model,
            RedirectAttributes redirectAttributes
    ) {
        InterfaceDefinition interfaceDefinition = interfaceDefinitionService.getDetail(interfaceId);
        if (bindingResult.hasErrors()) {
            return batchConfigForm(model, interfaceDefinition);
        }

        try {
            batchJobConfigService.save(interfaceId, form);
            redirectAttributes.addFlashAttribute("successMessage", "Batch job configuration saved.");
            return "redirect:/admin/interfaces/" + interfaceId;
        } catch (IllegalArgumentException exception) {
            bindingResult.rejectValue("parameterTemplateJson", "invalidJson", exception.getMessage());
            return batchConfigForm(model, interfaceDefinition);
        }
    }

    private String batchConfigForm(Model model, InterfaceDefinition interfaceDefinition) {
        model.addAttribute("activeNav", "interfaces");
        model.addAttribute("interfaceDefinition", interfaceDefinition);
        return "admin/interfaces/batch-config";
    }
}
