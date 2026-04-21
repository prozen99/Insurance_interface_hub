package com.insurancehub.protocol.soap;

import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.insurancehub.interfacehub.application.execution.ExecutionRequest;
import com.insurancehub.interfacehub.application.execution.ExecutionResult;
import com.insurancehub.interfacehub.application.execution.ExecutionStepLog;
import com.insurancehub.interfacehub.application.execution.InterfaceExecutor;
import com.insurancehub.interfacehub.domain.ExecutionStepStatus;
import com.insurancehub.interfacehub.domain.ProtocolType;
import com.insurancehub.protocol.soap.application.SoapEndpointConfigService;
import com.insurancehub.protocol.soap.domain.entity.SoapEndpointConfig;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.stereotype.Component;
import org.springframework.util.StreamUtils;
import org.springframework.util.StringUtils;
import org.springframework.web.client.RestClient;

@Component
public class SoapInterfaceExecutor implements InterfaceExecutor {

    private final SoapEndpointConfigService soapEndpointConfigService;
    private final ObjectMapper objectMapper;

    public SoapInterfaceExecutor(SoapEndpointConfigService soapEndpointConfigService, ObjectMapper objectMapper) {
        this.soapEndpointConfigService = soapEndpointConfigService;
        this.objectMapper = objectMapper;
    }

    @Override
    public ProtocolType supports() {
        return ProtocolType.SOAP;
    }

    @Override
    public ExecutionResult execute(ExecutionRequest request) {
        List<ExecutionStepLog> steps = new ArrayList<>();
        SoapEndpointConfig config;
        LocalDateTime configStartedAt = LocalDateTime.now();
        try {
            config = soapEndpointConfigService.getActiveForExecution(request.interfaceDefinition());
            steps.add(step(
                    1,
                    "Load SOAP endpoint configuration",
                    ExecutionStepStatus.SUCCESS,
                    "Active SOAP endpoint configuration loaded.",
                    configStartedAt,
                    LocalDateTime.now()
            ));
        } catch (RuntimeException exception) {
            steps.add(step(
                    1,
                    "Load SOAP endpoint configuration",
                    ExecutionStepStatus.FAILED,
                    readableMessage(exception),
                    configStartedAt,
                    LocalDateTime.now()
            ));
            return ExecutionResult.failure(
                    "SOAP_CONFIG_ERROR",
                    readableMessage(exception),
                    soapFaultPayload("SOAP_CONFIG_ERROR", readableMessage(exception)),
                    steps
            );
        }

        String requestXml;
        String requestHeadersJson;
        LocalDateTime prepareStartedAt = LocalDateTime.now();
        try {
            URI.create(config.getEndpointUrl());
            requestXml = StringUtils.hasText(request.requestPayload())
                    ? request.requestPayload()
                    : config.getRequestTemplateXml();
            if (!StringUtils.hasText(requestXml)) {
                throw new IllegalArgumentException("SOAP request XML is required.");
            }
            requestHeadersJson = toJson(requestHeaders(config));
            steps.add(step(
                    2,
                    "Build SOAP request",
                    ExecutionStepStatus.SUCCESS,
                    config.getOperationName() + " request prepared for " + config.getEndpointUrl(),
                    prepareStartedAt,
                    LocalDateTime.now()
            ));
        } catch (RuntimeException exception) {
            steps.add(step(
                    2,
                    "Build SOAP request",
                    ExecutionStepStatus.FAILED,
                    readableMessage(exception),
                    prepareStartedAt,
                    LocalDateTime.now()
            ));
            return ExecutionResult.failure(
                    "SOAP_REQUEST_BUILD_ERROR",
                    readableMessage(exception),
                    soapFaultPayload("SOAP_REQUEST_BUILD_ERROR", readableMessage(exception)),
                    steps,
                    config.getEndpointUrl(),
                    "POST",
                    config.getSoapAction(),
                    null,
                    null,
                    null,
                    null
            );
        }

        long startedAtNanos = System.nanoTime();
        LocalDateTime callStartedAt = LocalDateTime.now();
        try {
            SoapCallResult callResult = send(config, requestXml);
            long latencyMs = Duration.ofNanos(System.nanoTime() - startedAtNanos).toMillis();
            boolean fault = containsSoapFault(callResult.responseBody());
            boolean success = callResult.statusCode() >= 200 && callResult.statusCode() < 300 && !fault;
            steps.add(step(
                    3,
                    "Send SOAP request",
                    success ? ExecutionStepStatus.SUCCESS : ExecutionStepStatus.FAILED,
                    "HTTP " + callResult.statusCode() + " returned in " + latencyMs + " ms.",
                    callStartedAt,
                    LocalDateTime.now()
            ));

            if (success) {
                return ExecutionResult.success(
                        callResult.responseBody(),
                        steps,
                        config.getEndpointUrl(),
                        "POST",
                        config.getSoapAction(),
                        requestHeadersJson,
                        callResult.statusCode(),
                        callResult.responseHeadersJson(),
                        latencyMs
                );
            }

            String errorCode = fault ? "SOAP_FAULT" : "SOAP_HTTP_ERROR";
            String errorMessage = fault
                    ? "SOAP endpoint returned a SOAP fault."
                    : "SOAP endpoint returned HTTP " + callResult.statusCode() + ".";
            return ExecutionResult.failure(
                    errorCode,
                    errorMessage,
                    callResult.responseBody(),
                    steps,
                    config.getEndpointUrl(),
                    "POST",
                    config.getSoapAction(),
                    requestHeadersJson,
                    callResult.statusCode(),
                    callResult.responseHeadersJson(),
                    latencyMs
            );
        } catch (RuntimeException exception) {
            long latencyMs = Duration.ofNanos(System.nanoTime() - startedAtNanos).toMillis();
            steps.add(step(
                    3,
                    "Send SOAP request",
                    ExecutionStepStatus.FAILED,
                    readableMessage(exception),
                    callStartedAt,
                    LocalDateTime.now()
            ));
            return ExecutionResult.failure(
                    "SOAP_CLIENT_ERROR",
                    readableMessage(exception),
                    soapFaultPayload("SOAP_CLIENT_ERROR", readableMessage(exception)),
                    steps,
                    config.getEndpointUrl(),
                    "POST",
                    config.getSoapAction(),
                    requestHeadersJson,
                    null,
                    null,
                    latencyMs
            );
        }
    }

    private SoapCallResult send(SoapEndpointConfig config, String requestXml) {
        SimpleClientHttpRequestFactory requestFactory = new SimpleClientHttpRequestFactory();
        requestFactory.setConnectTimeout(Duration.ofMillis(config.getTimeoutMillis()));
        requestFactory.setReadTimeout(Duration.ofMillis(config.getTimeoutMillis()));

        return RestClient.builder()
                .requestFactory(requestFactory)
                .build()
                .method(HttpMethod.POST)
                .uri(config.getEndpointUrl())
                .contentType(MediaType.TEXT_XML)
                .headers(headers -> {
                    headers.set(HttpHeaders.ACCEPT, MediaType.TEXT_XML_VALUE + ", " + MediaType.APPLICATION_XML_VALUE);
                    if (StringUtils.hasText(config.getSoapAction())) {
                        headers.set("SOAPAction", config.getSoapAction());
                    }
                })
                .body(requestXml)
                .exchange(this::readResponse);
    }

    private SoapCallResult readResponse(org.springframework.http.HttpRequest request, org.springframework.http.client.ClientHttpResponse response) {
        try {
            String responseBody = response.getBody() == null
                    ? ""
                    : StreamUtils.copyToString(response.getBody(), StandardCharsets.UTF_8);
            return new SoapCallResult(
                    response.getStatusCode().value(),
                    toJson(response.getHeaders()),
                    responseBody
            );
        } catch (Exception exception) {
            throw new IllegalStateException("Could not read SOAP response.", exception);
        }
    }

    private Map<String, String> requestHeaders(SoapEndpointConfig config) {
        Map<String, String> headers = new LinkedHashMap<>();
        headers.put(HttpHeaders.CONTENT_TYPE, MediaType.TEXT_XML_VALUE);
        headers.put(HttpHeaders.ACCEPT, MediaType.TEXT_XML_VALUE + ", " + MediaType.APPLICATION_XML_VALUE);
        if (StringUtils.hasText(config.getSoapAction())) {
            headers.put("SOAPAction", config.getSoapAction());
        }
        return headers;
    }

    private String toJson(Map<String, String> headers) {
        try {
            return objectMapper.writeValueAsString(headers);
        } catch (JsonProcessingException exception) {
            throw new IllegalStateException("Could not serialize SOAP request headers.", exception);
        }
    }

    private String toJson(HttpHeaders headers) {
        try {
            return objectMapper.writeValueAsString(headers.toSingleValueMap());
        } catch (JsonProcessingException exception) {
            throw new IllegalStateException("Could not serialize SOAP response headers.", exception);
        }
    }

    private boolean containsSoapFault(String responseBody) {
        if (!StringUtils.hasText(responseBody)) {
            return false;
        }
        return responseBody.contains(":Fault") || responseBody.contains("<Fault");
    }

    private ExecutionStepLog step(
            int order,
            String name,
            ExecutionStepStatus status,
            String message,
            LocalDateTime startedAt,
            LocalDateTime finishedAt
    ) {
        return new ExecutionStepLog(order, name, status, message, startedAt, finishedAt);
    }

    private String soapFaultPayload(String code, String message) {
        return """
                <soapenv:Envelope xmlns:soapenv="http://schemas.xmlsoap.org/soap/envelope/">
                  <soapenv:Body>
                    <soapenv:Fault>
                      <faultcode>%s</faultcode>
                      <faultstring>%s</faultstring>
                    </soapenv:Fault>
                  </soapenv:Body>
                </soapenv:Envelope>
                """.formatted(escapeXml(code), escapeXml(message));
    }

    private String escapeXml(String value) {
        if (value == null) {
            return "";
        }
        return value
                .replace("&", "&amp;")
                .replace("<", "&lt;")
                .replace(">", "&gt;")
                .replace("\"", "&quot;")
                .replace("'", "&apos;");
    }

    private String readableMessage(RuntimeException exception) {
        if (StringUtils.hasText(exception.getMessage())) {
            return exception.getMessage();
        }
        return exception.getClass().getSimpleName();
    }

    private record SoapCallResult(
            int statusCode,
            String responseHeadersJson,
            String responseBody
    ) {
    }
}
