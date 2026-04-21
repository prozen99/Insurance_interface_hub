package com.insurancehub.protocol.rest.presentation;

import com.insurancehub.interfacehub.application.InterfaceDefinitionService;
import com.insurancehub.interfacehub.domain.entity.InterfaceDefinition;
import com.insurancehub.protocol.rest.application.RestEndpointConfigService;
import com.insurancehub.protocol.rest.domain.RestHttpMethod;
import com.insurancehub.protocol.rest.presentation.form.RestEndpointConfigForm;
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
@RequestMapping("/admin/interfaces/{interfaceId}/rest-config")
public class RestEndpointConfigController {

    private final RestEndpointConfigService restEndpointConfigService;
    private final InterfaceDefinitionService interfaceDefinitionService;

    public RestEndpointConfigController(
            RestEndpointConfigService restEndpointConfigService,
            InterfaceDefinitionService interfaceDefinitionService
    ) {
        this.restEndpointConfigService = restEndpointConfigService;
        this.interfaceDefinitionService = interfaceDefinitionService;
    }

    @ModelAttribute("httpMethodOptions")
    public RestHttpMethod[] httpMethodOptions() {
        return RestHttpMethod.values();
    }

    @GetMapping
    public String form(@PathVariable Long interfaceId, Model model) {
        InterfaceDefinition interfaceDefinition = interfaceDefinitionService.getDetail(interfaceId);
        model.addAttribute("form", restEndpointConfigService.formForInterface(interfaceId));
        return restConfigForm(model, interfaceDefinition);
    }

    @PostMapping
    public String save(
            @PathVariable Long interfaceId,
            @Valid @ModelAttribute("form") RestEndpointConfigForm form,
            BindingResult bindingResult,
            Model model,
            RedirectAttributes redirectAttributes
    ) {
        InterfaceDefinition interfaceDefinition = interfaceDefinitionService.getDetail(interfaceId);
        if (bindingResult.hasErrors()) {
            return restConfigForm(model, interfaceDefinition);
        }

        try {
            restEndpointConfigService.save(interfaceId, form);
            redirectAttributes.addFlashAttribute("successMessage", "REST endpoint configuration saved.");
            return "redirect:/admin/interfaces/" + interfaceId;
        } catch (IllegalArgumentException exception) {
            bindingResult.rejectValue("headersJson", "invalidJson", exception.getMessage());
            return restConfigForm(model, interfaceDefinition);
        }
    }

    private String restConfigForm(Model model, InterfaceDefinition interfaceDefinition) {
        model.addAttribute("activeNav", "interfaces");
        model.addAttribute("interfaceDefinition", interfaceDefinition);
        return "admin/interfaces/rest-config";
    }
}
