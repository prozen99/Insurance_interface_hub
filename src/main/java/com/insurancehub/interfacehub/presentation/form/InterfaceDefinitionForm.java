package com.insurancehub.interfacehub.presentation.form;

import com.insurancehub.interfacehub.domain.InterfaceDirection;
import com.insurancehub.interfacehub.domain.InterfaceStatus;
import com.insurancehub.interfacehub.domain.ProtocolType;
import com.insurancehub.interfacehub.domain.entity.InterfaceDefinition;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class InterfaceDefinitionForm {

    private Long id;

    @NotBlank(message = "Interface code is required.")
    @Size(max = 80, message = "Interface code must be 80 characters or less.")
    @Pattern(regexp = "^[A-Za-z0-9_-]+$", message = "Interface code may contain letters, numbers, underscore, and hyphen only.")
    private String interfaceCode;

    @NotBlank(message = "Interface name is required.")
    @Size(max = 180, message = "Interface name must be 180 characters or less.")
    private String interfaceName;

    @NotNull(message = "Protocol type is required.")
    private ProtocolType protocolType = ProtocolType.REST;

    @NotNull(message = "Direction is required.")
    private InterfaceDirection direction = InterfaceDirection.OUTBOUND;

    @NotNull(message = "Status is required.")
    private InterfaceStatus status = InterfaceStatus.ACTIVE;

    @NotNull(message = "Partner company is required.")
    private Long partnerCompanyId;

    @NotNull(message = "Internal system is required.")
    private Long internalSystemId;

    @Size(max = 1000, message = "Description must be 1000 characters or less.")
    private String description;

    public static InterfaceDefinitionForm empty() {
        return new InterfaceDefinitionForm();
    }

    public static InterfaceDefinitionForm from(InterfaceDefinition interfaceDefinition) {
        InterfaceDefinitionForm form = new InterfaceDefinitionForm();
        form.id = interfaceDefinition.getId();
        form.interfaceCode = interfaceDefinition.getInterfaceCode();
        form.interfaceName = interfaceDefinition.getInterfaceName();
        form.protocolType = interfaceDefinition.getProtocolType();
        form.direction = interfaceDefinition.getDirection();
        form.status = interfaceDefinition.getStatus();
        form.partnerCompanyId = interfaceDefinition.getPartnerCompany() == null ? null : interfaceDefinition.getPartnerCompany().getId();
        form.internalSystemId = interfaceDefinition.getInternalSystem() == null ? null : interfaceDefinition.getInternalSystem().getId();
        form.description = interfaceDefinition.getDescription();
        return form;
    }
}
