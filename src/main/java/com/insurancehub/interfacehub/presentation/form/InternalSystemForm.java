package com.insurancehub.interfacehub.presentation.form;

import com.insurancehub.interfacehub.domain.MasterStatus;
import com.insurancehub.interfacehub.domain.entity.InternalSystem;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class InternalSystemForm {

    private Long id;

    @NotBlank(message = "System code is required.")
    @Size(max = 60, message = "System code must be 60 characters or less.")
    @Pattern(regexp = "^[A-Za-z0-9_-]+$", message = "System code may contain letters, numbers, underscore, and hyphen only.")
    private String systemCode;

    @NotBlank(message = "System name is required.")
    @Size(max = 160, message = "System name must be 160 characters or less.")
    private String systemName;

    @Size(max = 120, message = "Owner department must be 120 characters or less.")
    private String ownerDepartment;

    @NotNull(message = "Status is required.")
    private MasterStatus status = MasterStatus.ACTIVE;

    @Size(max = 1000, message = "Description must be 1000 characters or less.")
    private String description;

    public static InternalSystemForm empty() {
        return new InternalSystemForm();
    }

    public static InternalSystemForm from(InternalSystem internalSystem) {
        InternalSystemForm form = new InternalSystemForm();
        form.id = internalSystem.getId();
        form.systemCode = internalSystem.getSystemCode();
        form.systemName = internalSystem.getSystemName();
        form.ownerDepartment = internalSystem.getOwnerDepartment();
        form.status = internalSystem.getStatus();
        form.description = internalSystem.getDescription();
        return form;
    }
}
