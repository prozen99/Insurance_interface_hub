package com.insurancehub.protocol.mq.presentation;

import com.insurancehub.interfacehub.application.InterfaceDefinitionService;
import com.insurancehub.interfacehub.domain.entity.InterfaceDefinition;
import com.insurancehub.protocol.mq.application.MqChannelConfigService;
import com.insurancehub.protocol.mq.domain.MqBrokerType;
import com.insurancehub.protocol.mq.domain.MqMessageType;
import com.insurancehub.protocol.mq.presentation.form.MqChannelConfigForm;
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
@RequestMapping("/admin/interfaces/{interfaceId}/mq-config")
public class MqChannelConfigController {

    private final MqChannelConfigService mqChannelConfigService;
    private final InterfaceDefinitionService interfaceDefinitionService;

    public MqChannelConfigController(
            MqChannelConfigService mqChannelConfigService,
            InterfaceDefinitionService interfaceDefinitionService
    ) {
        this.mqChannelConfigService = mqChannelConfigService;
        this.interfaceDefinitionService = interfaceDefinitionService;
    }

    @ModelAttribute("brokerTypeOptions")
    public MqBrokerType[] brokerTypeOptions() {
        return MqBrokerType.values();
    }

    @ModelAttribute("messageTypeOptions")
    public MqMessageType[] messageTypeOptions() {
        return MqMessageType.values();
    }

    @GetMapping
    public String form(@PathVariable Long interfaceId, Model model) {
        InterfaceDefinition interfaceDefinition = interfaceDefinitionService.getDetail(interfaceId);
        model.addAttribute("form", mqChannelConfigService.formForInterface(interfaceId));
        return mqConfigForm(model, interfaceDefinition);
    }

    @PostMapping
    public String save(
            @PathVariable Long interfaceId,
            @Valid @ModelAttribute("form") MqChannelConfigForm form,
            BindingResult bindingResult,
            Model model,
            RedirectAttributes redirectAttributes
    ) {
        InterfaceDefinition interfaceDefinition = interfaceDefinitionService.getDetail(interfaceId);
        if (bindingResult.hasErrors()) {
            return mqConfigForm(model, interfaceDefinition);
        }

        mqChannelConfigService.save(interfaceId, form);
        redirectAttributes.addFlashAttribute("successMessage", "MQ channel configuration saved.");
        return "redirect:/admin/interfaces/" + interfaceId;
    }

    private String mqConfigForm(Model model, InterfaceDefinition interfaceDefinition) {
        model.addAttribute("activeNav", "interfaces");
        model.addAttribute("interfaceDefinition", interfaceDefinition);
        return "admin/interfaces/mq-config";
    }
}
