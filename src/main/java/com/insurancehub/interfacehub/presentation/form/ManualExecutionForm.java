package com.insurancehub.interfacehub.presentation.form;

import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ManualExecutionForm {

    @Size(max = 12000, message = "Request payload must be 12000 characters or less.")
    private String requestPayload;
}
