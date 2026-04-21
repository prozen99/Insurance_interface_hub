package com.insurancehub.protocol.soap.presentation;

import com.insurancehub.interfacehub.application.InterfaceDefinitionService;
import com.insurancehub.interfacehub.domain.entity.InterfaceDefinition;
import com.insurancehub.protocol.soap.application.SoapEndpointConfigService;
import com.insurancehub.protocol.soap.presentation.form.SoapEndpointConfigForm;
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
@RequestMapping("/admin/interfaces/{interfaceId}/soap-config")
public class SoapEndpointConfigController {

    private final SoapEndpointConfigService soapEndpointConfigService;
    private final InterfaceDefinitionService interfaceDefinitionService;

    public SoapEndpointConfigController(
            SoapEndpointConfigService soapEndpointConfigService,
            InterfaceDefinitionService interfaceDefinitionService
    ) {
        this.soapEndpointConfigService = soapEndpointConfigService;
        this.interfaceDefinitionService = interfaceDefinitionService;
    }

    @GetMapping
    public String form(@PathVariable Long interfaceId, Model model) {
        InterfaceDefinition interfaceDefinition = interfaceDefinitionService.getDetail(interfaceId);
        model.addAttribute("form", soapEndpointConfigService.formForInterface(interfaceId));
        return soapConfigForm(model, interfaceDefinition);
    }

    @PostMapping
    public String save(
            @PathVariable Long interfaceId,
            @Valid @ModelAttribute("form") SoapEndpointConfigForm form,
            BindingResult bindingResult,
            Model model,
            RedirectAttributes redirectAttributes
    ) {
        InterfaceDefinition interfaceDefinition = interfaceDefinitionService.getDetail(interfaceId);
        if (bindingResult.hasErrors()) {
            return soapConfigForm(model, interfaceDefinition);
        }

        try {
            soapEndpointConfigService.save(interfaceId, form);
            redirectAttributes.addFlashAttribute("successMessage", "SOAP endpoint configuration saved.");
            return "redirect:/admin/interfaces/" + interfaceId;
        } catch (IllegalArgumentException exception) {
            bindingResult.rejectValue("requestTemplateXml", "invalidXml", exception.getMessage());
            return soapConfigForm(model, interfaceDefinition);
        }
    }

    private String soapConfigForm(Model model, InterfaceDefinition interfaceDefinition) {
        model.addAttribute("activeNav", "interfaces");
        model.addAttribute("interfaceDefinition", interfaceDefinition);
        return "admin/interfaces/soap-config";
    }
}
