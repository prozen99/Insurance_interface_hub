package com.insurancehub.interfacehub.presentation;

import com.insurancehub.interfacehub.application.DuplicateCodeException;
import com.insurancehub.interfacehub.application.InternalSystemService;
import com.insurancehub.interfacehub.domain.MasterStatus;
import com.insurancehub.interfacehub.domain.entity.InternalSystem;
import com.insurancehub.interfacehub.presentation.form.InternalSystemForm;
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
@RequestMapping("/admin/systems")
public class InternalSystemController {

    private final InternalSystemService internalSystemService;

    public InternalSystemController(InternalSystemService internalSystemService) {
        this.internalSystemService = internalSystemService;
    }

    @ModelAttribute("statusOptions")
    public MasterStatus[] statusOptions() {
        return MasterStatus.values();
    }

    @GetMapping
    public String list(Model model) {
        model.addAttribute("activeNav", "systems");
        model.addAttribute("systems", internalSystemService.findAll());
        return "admin/systems/list";
    }

    @GetMapping("/new")
    public String newForm(Model model) {
        model.addAttribute("activeNav", "systems");
        model.addAttribute("formTitle", "Create internal system");
        model.addAttribute("formAction", "/admin/systems");
        model.addAttribute("form", InternalSystemForm.empty());
        return "admin/systems/form";
    }

    @PostMapping
    public String create(
            @Valid @ModelAttribute("form") InternalSystemForm form,
            BindingResult bindingResult,
            Model model,
            RedirectAttributes redirectAttributes
    ) {
        if (bindingResult.hasErrors()) {
            return systemForm(model, "Create internal system", "/admin/systems");
        }

        try {
            InternalSystem internalSystem = internalSystemService.create(form);
            redirectAttributes.addFlashAttribute("successMessage", "Internal system created: " + internalSystem.getSystemCode());
            return "redirect:/admin/systems";
        } catch (DuplicateCodeException exception) {
            bindingResult.rejectValue("systemCode", "duplicate", exception.getMessage());
            return systemForm(model, "Create internal system", "/admin/systems");
        }
    }

    @GetMapping("/{id}/edit")
    public String editForm(@PathVariable Long id, Model model) {
        InternalSystem internalSystem = internalSystemService.get(id);
        model.addAttribute("activeNav", "systems");
        model.addAttribute("formTitle", "Edit internal system");
        model.addAttribute("formAction", "/admin/systems/" + id);
        model.addAttribute("form", InternalSystemForm.from(internalSystem));
        return "admin/systems/form";
    }

    @PostMapping("/{id}")
    public String update(
            @PathVariable Long id,
            @Valid @ModelAttribute("form") InternalSystemForm form,
            BindingResult bindingResult,
            Model model,
            RedirectAttributes redirectAttributes
    ) {
        if (bindingResult.hasErrors()) {
            return systemForm(model, "Edit internal system", "/admin/systems/" + id);
        }

        try {
            InternalSystem internalSystem = internalSystemService.update(id, form);
            redirectAttributes.addFlashAttribute("successMessage", "Internal system updated: " + internalSystem.getSystemCode());
            return "redirect:/admin/systems";
        } catch (DuplicateCodeException exception) {
            bindingResult.rejectValue("systemCode", "duplicate", exception.getMessage());
            return systemForm(model, "Edit internal system", "/admin/systems/" + id);
        }
    }

    private String systemForm(Model model, String title, String action) {
        model.addAttribute("activeNav", "systems");
        model.addAttribute("formTitle", title);
        model.addAttribute("formAction", action);
        return "admin/systems/form";
    }
}
