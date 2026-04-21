package com.insurancehub.protocol.soap.domain.entity;

import com.insurancehub.common.entity.BaseTimeEntity;
import com.insurancehub.interfacehub.domain.entity.InterfaceDefinition;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
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
@Table(name = "soap_endpoint_config")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class SoapEndpointConfig extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "interface_definition_id", nullable = false)
    private InterfaceDefinition interfaceDefinition;

    @Column(name = "service_url", nullable = false, length = 500)
    private String endpointUrl;

    @Column(name = "soap_action", length = 300)
    private String soapAction;

    @Column(name = "operation_name", length = 160)
    private String operationName;

    @Column(name = "namespace_uri", length = 300)
    private String namespaceUri;

    @Column(name = "request_template_xml", columnDefinition = "longtext")
    private String requestTemplateXml;

    @Column(name = "timeout_millis", nullable = false)
    private Integer timeoutMillis;

    @Column(name = "active_yn", nullable = false)
    private boolean active;

    private SoapEndpointConfig(
            InterfaceDefinition interfaceDefinition,
            String endpointUrl,
            String soapAction,
            String operationName,
            String namespaceUri,
            String requestTemplateXml,
            Integer timeoutMillis,
            boolean active
    ) {
        this.interfaceDefinition = interfaceDefinition;
        update(endpointUrl, soapAction, operationName, namespaceUri, requestTemplateXml, timeoutMillis, active);
    }

    public static SoapEndpointConfig create(
            InterfaceDefinition interfaceDefinition,
            String endpointUrl,
            String soapAction,
            String operationName,
            String namespaceUri,
            String requestTemplateXml,
            Integer timeoutMillis,
            boolean active
    ) {
        return new SoapEndpointConfig(
                interfaceDefinition,
                endpointUrl,
                soapAction,
                operationName,
                namespaceUri,
                requestTemplateXml,
                timeoutMillis,
                active
        );
    }

    public void update(
            String endpointUrl,
            String soapAction,
            String operationName,
            String namespaceUri,
            String requestTemplateXml,
            Integer timeoutMillis,
            boolean active
    ) {
        this.endpointUrl = endpointUrl == null ? null : endpointUrl.trim();
        this.soapAction = trimToNull(soapAction);
        this.operationName = trimToNull(operationName);
        this.namespaceUri = trimToNull(namespaceUri);
        this.requestTemplateXml = trimToNull(requestTemplateXml);
        this.timeoutMillis = timeoutMillis;
        this.active = active;
    }

    private String trimToNull(String value) {
        if (value == null || value.trim().isEmpty()) {
            return null;
        }
        return value.trim();
    }
}
