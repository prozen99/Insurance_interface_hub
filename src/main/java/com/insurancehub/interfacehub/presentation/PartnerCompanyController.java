package com.insurancehub.interfacehub.presentation;

import com.insurancehub.interfacehub.application.DuplicateCodeException;
import com.insurancehub.interfacehub.application.PartnerCompanyService;
import com.insurancehub.interfacehub.domain.MasterStatus;
import com.insurancehub.interfacehub.domain.entity.PartnerCompany;
import com.insurancehub.interfacehub.presentation.form.PartnerCompanyForm;
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
@RequestMapping("/admin/partners")
public class PartnerCompanyController {

    private final PartnerCompanyService partnerCompanyService;

    public PartnerCompanyController(PartnerCompanyService partnerCompanyService) {
        this.partnerCompanyService = partnerCompanyService;
    }

    @ModelAttribute("statusOptions")
    public MasterStatus[] statusOptions() {
        return MasterStatus.values();
    }

    @GetMapping
    public String list(Model model) {
        model.addAttribute("activeNav", "partners");
        model.addAttribute("partners", partnerCompanyService.findAll());
        return "admin/partners/list";
    }

    @GetMapping("/new")
    public String newForm(Model model) {
        model.addAttribute("activeNav", "partners");
        model.addAttribute("formTitle", "Create partner company");
        model.addAttribute("formAction", "/admin/partners");
        model.addAttribute("form", PartnerCompanyForm.empty());
        return "admin/partners/form";
    }

    @PostMapping
    public String create(
            @Valid @ModelAttribute("form") PartnerCompanyForm form,
            BindingResult bindingResult,
            Model model,
            RedirectAttributes redirectAttributes
    ) {
        if (bindingResult.hasErrors()) {
            return partnerForm(model, "Create partner company", "/admin/partners");
        }

        try {
            PartnerCompany partnerCompany = partnerCompanyService.create(form);
            redirectAttributes.addFlashAttribute("successMessage", "Partner company created: " + partnerCompany.getPartnerCode());
            return "redirect:/admin/partners";
        } catch (DuplicateCodeException exception) {
            bindingResult.rejectValue("partnerCode", "duplicate", exception.getMessage());
            return partnerForm(model, "Create partner company", "/admin/partners");
        }
    }

    @GetMapping("/{id}/edit")
    public String editForm(@PathVariable Long id, Model model) {
        PartnerCompany partnerCompany = partnerCompanyService.get(id);
        model.addAttribute("activeNav", "partners");
        model.addAttribute("formTitle", "Edit partner company");
        model.addAttribute("formAction", "/admin/partners/" + id);
        model.addAttribute("form", PartnerCompanyForm.from(partnerCompany));
        return "admin/partners/form";
    }

    @PostMapping("/{id}")
    public String update(
            @PathVariable Long id,
            @Valid @ModelAttribute("form") PartnerCompanyForm form,
            BindingResult bindingResult,
            Model model,
            RedirectAttributes redirectAttributes
    ) {
        if (bindingResult.hasErrors()) {
            return partnerForm(model, "Edit partner company", "/admin/partners/" + id);
        }

        try {
            PartnerCompany partnerCompany = partnerCompanyService.update(id, form);
            redirectAttributes.addFlashAttribute("successMessage", "Partner company updated: " + partnerCompany.getPartnerCode());
            return "redirect:/admin/partners";
        } catch (DuplicateCodeException exception) {
            bindingResult.rejectValue("partnerCode", "duplicate", exception.getMessage());
            return partnerForm(model, "Edit partner company", "/admin/partners/" + id);
        }
    }

    private String partnerForm(Model model, String title, String action) {
        model.addAttribute("activeNav", "partners");
        model.addAttribute("formTitle", title);
        model.addAttribute("formAction", action);
        return "admin/partners/form";
    }
}
