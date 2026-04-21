package com.insurancehub.protocol.soap.presentation.form;

import com.insurancehub.protocol.soap.domain.entity.SoapEndpointConfig;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class SoapEndpointConfigForm {

    private Long id;

    @NotBlank(message = "Endpoint URL is required.")
    @Size(max = 500, message = "Endpoint URL must be 500 characters or less.")
    @Pattern(regexp = "https?://.+", message = "Endpoint URL must start with http:// or https://.")
    private String endpointUrl = "http://localhost:8080/simulator/soap/policy-inquiry";

    @Size(max = 300, message = "SOAPAction must be 300 characters or less.")
    private String soapAction = "urn:PolicyInquiry";

    @NotBlank(message = "Operation name is required.")
    @Size(max = 160, message = "Operation name must be 160 characters or less.")
    private String operationName = "PolicyInquiry";

    @NotBlank(message = "Namespace URI is required.")
    @Size(max = 300, message = "Namespace URI must be 300 characters or less.")
    private String namespaceUri = "http://insurancehub.local/soap/policy";

    @NotBlank(message = "Request template XML is required.")
    @Size(max = 12000, message = "Request template XML must be 12000 characters or less.")
    private String requestTemplateXml = """
            <soapenv:Envelope xmlns:soapenv="http://schemas.xmlsoap.org/soap/envelope/" xmlns:pol="http://insurancehub.local/soap/policy">
              <soapenv:Header/>
              <soapenv:Body>
                <pol:PolicyInquiryRequest>
                  <pol:policyNo>POL-001</pol:policyNo>
                </pol:PolicyInquiryRequest>
              </soapenv:Body>
            </soapenv:Envelope>
            """;

    @NotNull(message = "Timeout is required.")
    @Min(value = 100, message = "Timeout must be at least 100 ms.")
    @Max(value = 60000, message = "Timeout must be 60000 ms or less.")
    private Integer timeoutMillis = 5000;

    private boolean active = true;

    public static SoapEndpointConfigForm empty() {
        return new SoapEndpointConfigForm();
    }

    public static SoapEndpointConfigForm from(SoapEndpointConfig config) {
        SoapEndpointConfigForm form = new SoapEndpointConfigForm();
        form.setId(config.getId());
        form.setEndpointUrl(config.getEndpointUrl());
        form.setSoapAction(config.getSoapAction());
        form.setOperationName(config.getOperationName());
        form.setNamespaceUri(config.getNamespaceUri());
        form.setRequestTemplateXml(config.getRequestTemplateXml());
        form.setTimeoutMillis(config.getTimeoutMillis());
        form.setActive(config.isActive());
        return form;
    }
}
