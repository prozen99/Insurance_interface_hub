package com.insurancehub.protocol.filetransfer.presentation;

import com.insurancehub.interfacehub.application.InterfaceDefinitionService;
import com.insurancehub.interfacehub.domain.entity.InterfaceDefinition;
import com.insurancehub.protocol.filetransfer.application.FileTransferConfigService;
import com.insurancehub.protocol.filetransfer.presentation.form.FileTransferConfigForm;
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
@RequestMapping("/admin/interfaces/{interfaceId}/file-transfer-config")
public class FileTransferConfigController {

    private final FileTransferConfigService fileTransferConfigService;
    private final InterfaceDefinitionService interfaceDefinitionService;

    public FileTransferConfigController(
            FileTransferConfigService fileTransferConfigService,
            InterfaceDefinitionService interfaceDefinitionService
    ) {
        this.fileTransferConfigService = fileTransferConfigService;
        this.interfaceDefinitionService = interfaceDefinitionService;
    }

    @GetMapping
    public String form(@PathVariable Long interfaceId, Model model) {
        InterfaceDefinition interfaceDefinition = interfaceDefinitionService.getDetail(interfaceId);
        model.addAttribute("form", fileTransferConfigService.formForInterface(interfaceId));
        return fileTransferConfigForm(model, interfaceDefinition);
    }

    @PostMapping
    public String save(
            @PathVariable Long interfaceId,
            @Valid @ModelAttribute("form") FileTransferConfigForm form,
            BindingResult bindingResult,
            Model model,
            RedirectAttributes redirectAttributes
    ) {
        InterfaceDefinition interfaceDefinition = interfaceDefinitionService.getDetail(interfaceId);
        if (bindingResult.hasErrors()) {
            return fileTransferConfigForm(model, interfaceDefinition);
        }

        fileTransferConfigService.save(interfaceId, form);
        redirectAttributes.addFlashAttribute("successMessage", interfaceDefinition.getProtocolType() + " file transfer configuration saved.");
        return "redirect:/admin/interfaces/" + interfaceId;
    }

    private String fileTransferConfigForm(Model model, InterfaceDefinition interfaceDefinition) {
        model.addAttribute("activeNav", "interfaces");
        model.addAttribute("interfaceDefinition", interfaceDefinition);
        return "admin/interfaces/file-transfer-config";
    }
}
