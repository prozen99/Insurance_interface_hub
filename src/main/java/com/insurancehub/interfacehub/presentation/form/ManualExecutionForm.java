package com.insurancehub.interfacehub.presentation.form;

import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ManualExecutionForm {

    @Size(max = 4000, message = "Request payload must be 4000 characters or less.")
    private String requestPayload;
}
