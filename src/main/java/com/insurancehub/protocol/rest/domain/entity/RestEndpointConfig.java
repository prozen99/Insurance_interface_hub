package com.insurancehub.protocol.rest.domain.entity;

import com.insurancehub.common.entity.BaseTimeEntity;
import com.insurancehub.interfacehub.domain.entity.InterfaceDefinition;
import com.insurancehub.protocol.rest.domain.RestHttpMethod;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Entity
@Table(name = "rest_endpoint_config")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class RestEndpointConfig extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "interface_definition_id", nullable = false)
    private InterfaceDefinition interfaceDefinition;

    @Enumerated(EnumType.STRING)
    @Column(name = "http_method", nullable = false, length = 20)
    private RestHttpMethod httpMethod;

    @Column(name = "endpoint_url", nullable = false, length = 500)
    private String endpointUrl;

    @Column(name = "base_url", length = 500)
    private String baseUrl;

    @Column(name = "path", length = 300)
    private String path;

    @Column(name = "timeout_millis", nullable = false)
    private Integer timeoutMillis;

    @Column(name = "auth_type", nullable = false, length = 40)
    private String authType;

    @Column(name = "headers_json", columnDefinition = "longtext")
    private String headersJson;

    @Column(name = "sample_request_body", columnDefinition = "longtext")
    private String sampleRequestBody;

    @Column(name = "active_yn", nullable = false)
    private boolean active;

    private RestEndpointConfig(
            InterfaceDefinition interfaceDefinition,
            RestHttpMethod httpMethod,
            String baseUrl,
            String path,
            Integer timeoutMillis,
            String headersJson,
            String sampleRequestBody,
            boolean active
    ) {
        this.interfaceDefinition = interfaceDefinition;
        this.authType = "NONE";
        update(httpMethod, baseUrl, path, timeoutMillis, headersJson, sampleRequestBody, active);
    }

    public static RestEndpointConfig create(
            InterfaceDefinition interfaceDefinition,
            RestHttpMethod httpMethod,
            String baseUrl,
            String path,
            Integer timeoutMillis,
            String headersJson,
            String sampleRequestBody,
            boolean active
    ) {
        return new RestEndpointConfig(
                interfaceDefinition,
                httpMethod,
                baseUrl,
                path,
                timeoutMillis,
                headersJson,
                sampleRequestBody,
                active
        );
    }

    public void update(
            RestHttpMethod httpMethod,
            String baseUrl,
            String path,
            Integer timeoutMillis,
            String headersJson,
            String sampleRequestBody,
            boolean active
    ) {
        this.httpMethod = httpMethod;
        this.baseUrl = normalizeBaseUrl(baseUrl);
        this.path = normalizePath(path);
        this.endpointUrl = this.baseUrl + this.path;
        this.timeoutMillis = timeoutMillis;
        this.headersJson = trimToNull(headersJson);
        this.sampleRequestBody = trimToNull(sampleRequestBody);
        this.active = active;
        if (this.authType == null) {
            this.authType = "NONE";
        }
    }

    private String normalizeBaseUrl(String value) {
        String normalized = value == null ? "" : value.trim();
        while (normalized.endsWith("/")) {
            normalized = normalized.substring(0, normalized.length() - 1);
        }
        return normalized;
    }

    private String normalizePath(String value) {
        String normalized = value == null ? "" : value.trim();
        if (!normalized.startsWith("/")) {
            normalized = "/" + normalized;
        }
        return normalized;
    }

    private String trimToNull(String value) {
        if (value == null || value.trim().isEmpty()) {
            return null;
        }
        return value.trim();
    }
}
