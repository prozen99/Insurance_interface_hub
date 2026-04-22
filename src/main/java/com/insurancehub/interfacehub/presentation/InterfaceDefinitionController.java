package com.insurancehub.interfacehub.presentation;

import com.insurancehub.interfacehub.application.DuplicateCodeException;
import com.insurancehub.interfacehub.application.execution.ExecutionNotAllowedException;
import com.insurancehub.interfacehub.application.execution.InterfaceExecutionService;
import com.insurancehub.interfacehub.application.InterfaceDefinitionService;
import com.insurancehub.interfacehub.application.InternalSystemService;
import com.insurancehub.interfacehub.application.PartnerCompanyService;
import com.insurancehub.interfacehub.domain.InterfaceDirection;
import com.insurancehub.interfacehub.domain.InterfaceStatus;
import com.insurancehub.interfacehub.domain.ProtocolType;
import com.insurancehub.interfacehub.domain.entity.InterfaceDefinition;
import com.insurancehub.interfacehub.presentation.form.InterfaceDefinitionForm;
import com.insurancehub.interfacehub.presentation.form.ManualExecutionForm;
import com.insurancehub.protocol.filetransfer.application.FileTransferConfigService;
import com.insurancehub.protocol.filetransfer.application.FileTransferPayloadCodec;
import com.insurancehub.protocol.filetransfer.domain.TransferDirection;
import com.insurancehub.protocol.filetransfer.domain.entity.FileTransferConfig;
import com.insurancehub.protocol.mq.application.MqChannelConfigService;
import com.insurancehub.protocol.mq.domain.entity.MqChannelConfig;
import com.insurancehub.protocol.rest.application.RestEndpointConfigService;
import com.insurancehub.protocol.rest.domain.entity.RestEndpointConfig;
import com.insurancehub.protocol.soap.application.SoapEndpointConfigService;
import com.insurancehub.protocol.soap.domain.entity.SoapEndpointConfig;
import jakarta.validation.Valid;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/admin/interfaces")
public class InterfaceDefinitionController {

    private final InterfaceDefinitionService interfaceDefinitionService;
    private final PartnerCompanyService partnerCompanyService;
    private final InternalSystemService internalSystemService;
    private final InterfaceExecutionService interfaceExecutionService;
    private final RestEndpointConfigService restEndpointConfigService;
    private final SoapEndpointConfigService soapEndpointConfigService;
    private final MqChannelConfigService mqChannelConfigService;
    private final FileTransferConfigService fileTransferConfigService;
    private final FileTransferPayloadCodec fileTransferPayloadCodec;

    public InterfaceDefinitionController(
            InterfaceDefinitionService interfaceDefinitionService,
            PartnerCompanyService partnerCompanyService,
            InternalSystemService internalSystemService,
            InterfaceExecutionService interfaceExecutionService,
            RestEndpointConfigService restEndpointConfigService,
            SoapEndpointConfigService soapEndpointConfigService,
            MqChannelConfigService mqChannelConfigService,
            FileTransferConfigService fileTransferConfigService,
            FileTransferPayloadCodec fileTransferPayloadCodec
    ) {
        this.interfaceDefinitionService = interfaceDefinitionService;
        this.partnerCompanyService = partnerCompanyService;
        this.internalSystemService = internalSystemService;
        this.interfaceExecutionService = interfaceExecutionService;
        this.restEndpointConfigService = restEndpointConfigService;
        this.soapEndpointConfigService = soapEndpointConfigService;
        this.mqChannelConfigService = mqChannelConfigService;
        this.fileTransferConfigService = fileTransferConfigService;
        this.fileTransferPayloadCodec = fileTransferPayloadCodec;
    }

    @ModelAttribute("protocolOptions")
    public ProtocolType[] protocolOptions() {
        return ProtocolType.values();
    }

    @ModelAttribute("directionOptions")
    public InterfaceDirection[] directionOptions() {
        return InterfaceDirection.values();
    }

    @ModelAttribute("statusOptions")
    public InterfaceStatus[] statusOptions() {
        return InterfaceStatus.values();
    }

    @GetMapping
    public String list(
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) ProtocolType protocolType,
            @RequestParam(required = false) InterfaceStatus status,
            Model model
    ) {
        model.addAttribute("activeNav", "interfaces");
        model.addAttribute("interfaces", interfaceDefinitionService.search(keyword, protocolType, status));
        model.addAttribute("keyword", keyword);
        model.addAttribute("selectedProtocolType", protocolType);
        model.addAttribute("selectedStatus", status);
        return "admin/interfaces/list";
    }

    @GetMapping("/new")
    public String newForm(Model model) {
        model.addAttribute("form", InterfaceDefinitionForm.empty());
        return interfaceForm(model, "Create interface definition", "/admin/interfaces");
    }

    @PostMapping
    public String create(
            @Valid @ModelAttribute("form") InterfaceDefinitionForm form,
            BindingResult bindingResult,
            Model model,
            RedirectAttributes redirectAttributes
    ) {
        if (bindingResult.hasErrors()) {
            return interfaceForm(model, "Create interface definition", "/admin/interfaces");
        }

        try {
            InterfaceDefinition interfaceDefinition = interfaceDefinitionService.create(form);
            redirectAttributes.addFlashAttribute("successMessage", "Interface created: " + interfaceDefinition.getInterfaceCode());
            return "redirect:/admin/interfaces/" + interfaceDefinition.getId();
        } catch (DuplicateCodeException exception) {
            bindingResult.rejectValue("interfaceCode", "duplicate", exception.getMessage());
            return interfaceForm(model, "Create interface definition", "/admin/interfaces");
        }
    }

    @GetMapping("/{id}")
    public String detail(@PathVariable Long id, Model model) {
        InterfaceDefinition interfaceDefinition = addDetailModel(id, model);
        ManualExecutionForm manualExecutionForm = new ManualExecutionForm();
        RestEndpointConfig restConfig = (RestEndpointConfig) model.asMap().get("restConfig");
        if (interfaceDefinition.getProtocolType() == ProtocolType.REST && restConfig != null) {
            manualExecutionForm.setRequestPayload(restConfig.getSampleRequestBody());
        }
        SoapEndpointConfig soapConfig = (SoapEndpointConfig) model.asMap().get("soapConfig");
        if (interfaceDefinition.getProtocolType() == ProtocolType.SOAP && soapConfig != null) {
            manualExecutionForm.setRequestPayload(soapConfig.getRequestTemplateXml());
        }
        if (interfaceDefinition.getProtocolType() == ProtocolType.MQ) {
            manualExecutionForm.setRequestPayload(MqChannelConfigService.SAMPLE_PAYLOAD.trim());
        }
        if (isFileTransferProtocol(interfaceDefinition.getProtocolType())) {
            manualExecutionForm.setTransferDirection(TransferDirection.UPLOAD);
            manualExecutionForm.setLocalFileName(FileTransferConfigService.SAMPLE_UPLOAD_FILE_NAME);
            manualExecutionForm.setRemoteFilePath("/inbox/" + FileTransferConfigService.SAMPLE_UPLOAD_FILE_NAME);
        }
        model.addAttribute("manualExecutionForm", manualExecutionForm);
        return "admin/interfaces/detail";
    }

    @PostMapping("/{id}/execute")
    public String executeManual(
            @PathVariable Long id,
            @Valid @ModelAttribute("manualExecutionForm") ManualExecutionForm form,
            BindingResult bindingResult,
            Model model,
            Authentication authentication,
            RedirectAttributes redirectAttributes
    ) {
        if (bindingResult.hasErrors()) {
            addDetailModel(id, model);
            return "admin/interfaces/detail";
        }

        InterfaceDefinition interfaceDefinition = interfaceDefinitionService.getDetail(id);
        String executionPayload = form.getRequestPayload();
        if (isFileTransferProtocol(interfaceDefinition.getProtocolType())) {
            validateFileTransferExecutionForm(form, bindingResult);
            if (bindingResult.hasErrors()) {
                addDetailModel(id, model);
                return "admin/interfaces/detail";
            }
            executionPayload = fileTransferPayloadCodec.encode(form);
        }

        try {
            Long executionId = interfaceExecutionService.executeManual(
                    id,
                    executionPayload,
                    authentication == null ? "anonymous" : authentication.getName()
            ).getId();
            redirectAttributes.addFlashAttribute("successMessage", "Manual execution finished.");
            return "redirect:/admin/executions/" + executionId;
        } catch (ExecutionNotAllowedException exception) {
            redirectAttributes.addFlashAttribute("errorMessage", exception.getMessage());
            return "redirect:/admin/interfaces/" + id;
        }
    }

    @GetMapping("/{id}/edit")
    public String editForm(@PathVariable Long id, Model model) {
        InterfaceDefinition interfaceDefinition = interfaceDefinitionService.getDetail(id);
        model.addAttribute("form", InterfaceDefinitionForm.from(interfaceDefinition));
        return interfaceForm(model, "Edit interface definition", "/admin/interfaces/" + id);
    }

    @PostMapping("/{id}")
    public String update(
            @PathVariable Long id,
            @Valid @ModelAttribute("form") InterfaceDefinitionForm form,
            BindingResult bindingResult,
            Model model,
            RedirectAttributes redirectAttributes
    ) {
        if (bindingResult.hasErrors()) {
            return interfaceForm(model, "Edit interface definition", "/admin/interfaces/" + id);
        }

        try {
            InterfaceDefinition interfaceDefinition = interfaceDefinitionService.update(id, form);
            redirectAttributes.addFlashAttribute("successMessage", "Interface updated: " + interfaceDefinition.getInterfaceCode());
            return "redirect:/admin/interfaces/" + id;
        } catch (DuplicateCodeException exception) {
            bindingResult.rejectValue("interfaceCode", "duplicate", exception.getMessage());
            return interfaceForm(model, "Edit interface definition", "/admin/interfaces/" + id);
        }
    }

    @PostMapping("/{id}/activate")
    public String activate(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        interfaceDefinitionService.activate(id);
        redirectAttributes.addFlashAttribute("successMessage", "Interface activated.");
        return "redirect:/admin/interfaces/" + id;
    }

    @PostMapping("/{id}/deactivate")
    public String deactivate(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        interfaceDefinitionService.deactivate(id);
        redirectAttributes.addFlashAttribute("successMessage", "Interface deactivated.");
        return "redirect:/admin/interfaces/" + id;
    }

    private String interfaceForm(Model model, String title, String action) {
        model.addAttribute("activeNav", "interfaces");
        model.addAttribute("formTitle", title);
        model.addAttribute("formAction", action);
        model.addAttribute("partnerOptions", partnerCompanyService.findActive());
        model.addAttribute("systemOptions", internalSystemService.findActive());
        return "admin/interfaces/form";
    }

    private InterfaceDefinition addDetailModel(Long id, Model model) {
        InterfaceDefinition interfaceDefinition = interfaceDefinitionService.getDetail(id);
        model.addAttribute("activeNav", "interfaces");
        model.addAttribute("interfaceDefinition", interfaceDefinition);
        model.addAttribute("recentExecutions", interfaceExecutionService.recentExecutionsForInterface(id));
        if (interfaceDefinition.getProtocolType() == ProtocolType.REST) {
            model.addAttribute("restConfig", restEndpointConfigService.findByInterfaceDefinitionId(id).orElse(null));
        }
        if (interfaceDefinition.getProtocolType() == ProtocolType.SOAP) {
            model.addAttribute("soapConfig", soapEndpointConfigService.findByInterfaceDefinitionId(id).orElse(null));
        }
        if (interfaceDefinition.getProtocolType() == ProtocolType.MQ) {
            MqChannelConfig mqConfig = mqChannelConfigService.findByInterfaceDefinitionId(id).orElse(null);
            model.addAttribute("mqConfig", mqConfig);
        }
        if (isFileTransferProtocol(interfaceDefinition.getProtocolType())) {
            FileTransferConfig fileTransferConfig = fileTransferConfigService.findByInterfaceDefinitionId(id).orElse(null);
            model.addAttribute("fileTransferConfig", fileTransferConfig);
            model.addAttribute("transferDirectionOptions", TransferDirection.values());
        }
        return interfaceDefinition;
    }

    private boolean isFileTransferProtocol(ProtocolType protocolType) {
        return protocolType == ProtocolType.SFTP || protocolType == ProtocolType.FTP;
    }

    private void validateFileTransferExecutionForm(ManualExecutionForm form, BindingResult bindingResult) {
        if (form.getTransferDirection() == null) {
            bindingResult.rejectValue("transferDirection", "required", "Transfer direction is required.");
        }
        if (form.getLocalFileName() == null || form.getLocalFileName().trim().isEmpty()) {
            bindingResult.rejectValue("localFileName", "required", "Local file name is required.");
        }
        if (form.getRemoteFilePath() == null || form.getRemoteFilePath().trim().isEmpty()) {
            bindingResult.rejectValue("remoteFilePath", "required", "Remote file path is required.");
        }
    }
}
