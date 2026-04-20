package com.insurancehub.interfacehub.presentation.form;

import com.insurancehub.interfacehub.domain.MasterStatus;
import com.insurancehub.interfacehub.domain.entity.PartnerCompany;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class PartnerCompanyForm {

    private Long id;

    @NotBlank(message = "Partner code is required.")
    @Size(max = 60, message = "Partner code must be 60 characters or less.")
    @Pattern(regexp = "^[A-Za-z0-9_-]+$", message = "Partner code may contain letters, numbers, underscore, and hyphen only.")
    private String partnerCode;

    @NotBlank(message = "Partner name is required.")
    @Size(max = 160, message = "Partner name must be 160 characters or less.")
    private String partnerName;

    @NotNull(message = "Status is required.")
    private MasterStatus status = MasterStatus.ACTIVE;

    @Size(max = 1000, message = "Description must be 1000 characters or less.")
    private String description;

    public static PartnerCompanyForm empty() {
        return new PartnerCompanyForm();
    }

    public static PartnerCompanyForm from(PartnerCompany partnerCompany) {
        PartnerCompanyForm form = new PartnerCompanyForm();
        form.id = partnerCompany.getId();
        form.partnerCode = partnerCompany.getPartnerCode();
        form.partnerName = partnerCompany.getPartnerName();
        form.status = partnerCompany.getStatus();
        form.description = partnerCompany.getDescription();
        return form;
    }
}
