package com.insurancehub.protocol.rest;

import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.insurancehub.interfacehub.application.execution.ExecutionRequest;
import com.insurancehub.interfacehub.application.execution.ExecutionResult;
import com.insurancehub.interfacehub.application.execution.ExecutionStepLog;
import com.insurancehub.interfacehub.application.execution.InterfaceExecutor;
import com.insurancehub.interfacehub.domain.ExecutionStepStatus;
import com.insurancehub.interfacehub.domain.ProtocolType;
import com.insurancehub.protocol.rest.application.RestEndpointConfigService;
import com.insurancehub.protocol.rest.domain.entity.RestEndpointConfig;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.stereotype.Component;
import org.springframework.util.StreamUtils;
import org.springframework.util.StringUtils;
import org.springframework.web.client.RestClient;

@Component
public class RestInterfaceExecutor implements InterfaceExecutor {

    private static final TypeReference<Map<String, Object>> HEADER_TYPE = new TypeReference<>() {
    };

    private final RestEndpointConfigService restEndpointConfigService;
    private final ObjectMapper objectMapper;

    public RestInterfaceExecutor(RestEndpointConfigService restEndpointConfigService, ObjectMapper objectMapper) {
        this.restEndpointConfigService = restEndpointConfigService;
        this.objectMapper = objectMapper;
    }

    @Override
    public ProtocolType supports() {
        return ProtocolType.REST;
    }

    @Override
    public ExecutionResult execute(ExecutionRequest request) {
        List<ExecutionStepLog> steps = new ArrayList<>();
        RestEndpointConfig config;
        LocalDateTime configStartedAt = LocalDateTime.now();
        try {
            config = restEndpointConfigService.getActiveForExecution(request.interfaceDefinition());
            steps.add(step(
                    1,
                    "Load REST endpoint configuration",
                    ExecutionStepStatus.SUCCESS,
                    "Active REST endpoint configuration loaded.",
                    configStartedAt,
                    LocalDateTime.now()
            ));
        } catch (RuntimeException exception) {
            steps.add(step(
                    1,
                    "Load REST endpoint configuration",
                    ExecutionStepStatus.FAILED,
                    readableMessage(exception),
                    configStartedAt,
                    LocalDateTime.now()
            ));
            return ExecutionResult.failure(
                    "REST_CONFIG_ERROR",
                    readableMessage(exception),
                    failurePayload("REST_CONFIG_ERROR", readableMessage(exception)),
                    steps
            );
        }

        Map<String, String> headers;
        String requestHeadersJson;
        LocalDateTime prepareStartedAt = LocalDateTime.now();
        try {
            headers = parseHeaders(config.getHeadersJson());
            requestHeadersJson = toJson(headers);
            URI.create(config.getEndpointUrl());
            steps.add(step(
                    2,
                    "Build REST request",
                    ExecutionStepStatus.SUCCESS,
                    config.getHttpMethod() + " " + config.getEndpointUrl(),
                    prepareStartedAt,
                    LocalDateTime.now()
            ));
        } catch (RuntimeException exception) {
            steps.add(step(
                    2,
                    "Build REST request",
                    ExecutionStepStatus.FAILED,
                    readableMessage(exception),
                    prepareStartedAt,
                    LocalDateTime.now()
            ));
            return ExecutionResult.failure(
                    "REST_REQUEST_BUILD_ERROR",
                    readableMessage(exception),
                    failurePayload("REST_REQUEST_BUILD_ERROR", readableMessage(exception)),
                    steps,
                    config.getEndpointUrl(),
                    config.getHttpMethod().name(),
                    config.getHeadersJson(),
                    null,
                    null,
                    null
            );
        }

        HttpMethod httpMethod = HttpMethod.valueOf(config.getHttpMethod().name());
        String requestBody = request.requestPayload();
        String requestUrl = config.getEndpointUrl();
        long startedAtNanos = System.nanoTime();
        LocalDateTime callStartedAt = LocalDateTime.now();
        try {
            RestCallResult callResult = send(config, httpMethod, requestUrl, headers, requestBody);
            long latencyMs = Duration.ofNanos(System.nanoTime() - startedAtNanos).toMillis();
            boolean success = callResult.statusCode() >= 200 && callResult.statusCode() < 300;
            steps.add(step(
                    3,
                    "Send REST request",
                    success ? ExecutionStepStatus.SUCCESS : ExecutionStepStatus.FAILED,
                    "HTTP " + callResult.statusCode() + " returned in " + latencyMs + " ms.",
                    callStartedAt,
                    LocalDateTime.now()
            ));

            if (success) {
                return ExecutionResult.success(
                        callResult.responseBody(),
                        steps,
                        requestUrl,
                        httpMethod.name(),
                        requestHeadersJson,
                        callResult.statusCode(),
                        callResult.responseHeadersJson(),
                        latencyMs
                );
            }
            return ExecutionResult.failure(
                    "REST_HTTP_ERROR",
                    "REST endpoint returned HTTP " + callResult.statusCode() + ".",
                    callResult.responseBody(),
                    steps,
                    requestUrl,
                    httpMethod.name(),
                    requestHeadersJson,
                    callResult.statusCode(),
                    callResult.responseHeadersJson(),
                    latencyMs
            );
        } catch (RuntimeException exception) {
            long latencyMs = Duration.ofNanos(System.nanoTime() - startedAtNanos).toMillis();
            steps.add(step(
                    3,
                    "Send REST request",
                    ExecutionStepStatus.FAILED,
                    readableMessage(exception),
                    callStartedAt,
                    LocalDateTime.now()
            ));
            return ExecutionResult.failure(
                    "REST_CLIENT_ERROR",
                    readableMessage(exception),
                    failurePayload("REST_CLIENT_ERROR", readableMessage(exception)),
                    steps,
                    requestUrl,
                    httpMethod.name(),
                    requestHeadersJson,
                    null,
                    null,
                    latencyMs
            );
        }
    }

    private RestCallResult send(
            RestEndpointConfig config,
            HttpMethod httpMethod,
            String requestUrl,
            Map<String, String> headers,
            String requestBody
    ) {
        SimpleClientHttpRequestFactory requestFactory = new SimpleClientHttpRequestFactory();
        requestFactory.setConnectTimeout(Duration.ofMillis(config.getTimeoutMillis()));
        requestFactory.setReadTimeout(Duration.ofMillis(config.getTimeoutMillis()));

        RestClient.RequestBodySpec requestSpec = RestClient.builder()
                .requestFactory(requestFactory)
                .build()
                .method(httpMethod)
                .uri(requestUrl)
                .headers(httpHeaders -> headers.forEach(httpHeaders::add));

        if (httpMethod == HttpMethod.GET) {
            return exchange(requestSpec);
        }
        return requestSpec.body(StringUtils.hasText(requestBody) ? requestBody : "").exchange(this::readResponse);
    }

    private RestCallResult exchange(RestClient.RequestHeadersSpec<?> requestSpec) {
        return requestSpec.exchange(this::readResponse);
    }

    private RestCallResult readResponse(org.springframework.http.HttpRequest request, org.springframework.http.client.ClientHttpResponse response) {
        try {
            String responseBody = response.getBody() == null
                    ? ""
                    : StreamUtils.copyToString(response.getBody(), StandardCharsets.UTF_8);
            return new RestCallResult(
                    response.getStatusCode().value(),
                    toJson(response.getHeaders()),
                    responseBody
            );
        } catch (Exception exception) {
            throw new IllegalStateException("Could not read REST response.", exception);
        }
    }

    private Map<String, String> parseHeaders(String headersJson) {
        Map<String, String> headers = new LinkedHashMap<>();
        if (!StringUtils.hasText(headersJson)) {
            return headers;
        }

        try {
            Map<String, Object> parsedHeaders = objectMapper.readValue(headersJson, HEADER_TYPE);
            parsedHeaders.forEach((key, value) -> {
                if (StringUtils.hasText(key) && value != null) {
                    headers.put(key, String.valueOf(value));
                }
            });
            return headers;
        } catch (JsonProcessingException exception) {
            throw new IllegalArgumentException("Headers JSON must be a valid JSON object.", exception);
        }
    }

    private String toJson(Map<String, String> headers) {
        try {
            return objectMapper.writeValueAsString(headers);
        } catch (JsonProcessingException exception) {
            throw new IllegalStateException("Could not serialize REST request headers.", exception);
        }
    }

    private String toJson(HttpHeaders headers) {
        try {
            return objectMapper.writeValueAsString(headers.toSingleValueMap());
        } catch (JsonProcessingException exception) {
            throw new IllegalStateException("Could not serialize REST response headers.", exception);
        }
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

    private String failurePayload(String code, String message) {
        try {
            return objectMapper.writeValueAsString(Map.of(
                    "status", "FAILED",
                    "code", code,
                    "message", message
            ));
        } catch (JsonProcessingException exception) {
            return "{\"status\":\"FAILED\",\"code\":\"" + code + "\"}";
        }
    }

    private String readableMessage(RuntimeException exception) {
        if (StringUtils.hasText(exception.getMessage())) {
            return exception.getMessage();
        }
        return exception.getClass().getSimpleName();
    }

    private record RestCallResult(
            int statusCode,
            String responseHeadersJson,
            String responseBody
    ) {
    }
}
