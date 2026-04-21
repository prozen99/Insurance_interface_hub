package com.insurancehub.protocol.rest.presentation.form;

import com.insurancehub.protocol.rest.domain.RestHttpMethod;
import com.insurancehub.protocol.rest.domain.entity.RestEndpointConfig;
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
public class RestEndpointConfigForm {

    private Long id;

    @NotBlank(message = "Base URL is required.")
    @Size(max = 500, message = "Base URL must be 500 characters or less.")
    @Pattern(regexp = "https?://.+", message = "Base URL must start with http:// or https://.")
    private String baseUrl = "http://localhost:8080";

    @NotNull(message = "HTTP method is required.")
    private RestHttpMethod httpMethod = RestHttpMethod.POST;

    @NotBlank(message = "Path is required.")
    @Size(max = 300, message = "Path must be 300 characters or less.")
    private String path = "/simulator/rest/premium/calculate";

    @NotNull(message = "Timeout is required.")
    @Min(value = 100, message = "Timeout must be at least 100 ms.")
    @Max(value = 60000, message = "Timeout must be 60000 ms or less.")
    private Integer timeoutMillis = 3000;

    @Size(max = 8000, message = "Headers JSON must be 8000 characters or less.")
    private String headersJson = "{\"Content-Type\":\"application/json\"}";

    @Size(max = 8000, message = "Sample request body must be 8000 characters or less.")
    private String sampleRequestBody = "{\"policyNo\":\"P001\",\"age\":42,\"coverageAmount\":100000000}";

    private boolean active = true;

    public static RestEndpointConfigForm empty() {
        return new RestEndpointConfigForm();
    }

    public static RestEndpointConfigForm from(RestEndpointConfig config) {
        RestEndpointConfigForm form = new RestEndpointConfigForm();
        form.setId(config.getId());
        form.setBaseUrl(config.getBaseUrl());
        form.setHttpMethod(config.getHttpMethod());
        form.setPath(config.getPath());
        form.setTimeoutMillis(config.getTimeoutMillis());
        form.setHeadersJson(config.getHeadersJson());
        form.setSampleRequestBody(config.getSampleRequestBody());
        form.setActive(config.isActive());
        return form;
    }
}
