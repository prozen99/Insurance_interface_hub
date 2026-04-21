package com.insurancehub.protocol.soap.application;

import java.io.StringReader;
import java.util.Optional;

import com.insurancehub.interfacehub.domain.ProtocolType;
import com.insurancehub.interfacehub.domain.entity.InterfaceDefinition;
import com.insurancehub.interfacehub.infrastructure.repository.InterfaceDefinitionRepository;
import com.insurancehub.protocol.soap.domain.entity.SoapEndpointConfig;
import com.insurancehub.protocol.soap.infrastructure.repository.SoapEndpointConfigRepository;
import com.insurancehub.protocol.soap.presentation.form.SoapEndpointConfigForm;
import javax.xml.XMLConstants;
import javax.xml.parsers.DocumentBuilderFactory;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;
import org.springframework.web.server.ResponseStatusException;
import org.xml.sax.InputSource;

@Service
public class SoapEndpointConfigService {

    private final SoapEndpointConfigRepository soapEndpointConfigRepository;
    private final InterfaceDefinitionRepository interfaceDefinitionRepository;

    public SoapEndpointConfigService(
            SoapEndpointConfigRepository soapEndpointConfigRepository,
            InterfaceDefinitionRepository interfaceDefinitionRepository
    ) {
        this.soapEndpointConfigRepository = soapEndpointConfigRepository;
        this.interfaceDefinitionRepository = interfaceDefinitionRepository;
    }

    @Transactional(readOnly = true)
    public Optional<SoapEndpointConfig> findByInterfaceDefinitionId(Long interfaceDefinitionId) {
        return soapEndpointConfigRepository.findByInterfaceDefinitionId(interfaceDefinitionId);
    }

    @Transactional(readOnly = true)
    public SoapEndpointConfigForm formForInterface(Long interfaceDefinitionId) {
        InterfaceDefinition interfaceDefinition = getSoapInterface(interfaceDefinitionId);
        return soapEndpointConfigRepository.findByInterfaceDefinitionId(interfaceDefinition.getId())
                .map(SoapEndpointConfigForm::from)
                .orElseGet(SoapEndpointConfigForm::empty);
    }

    @Transactional(readOnly = true)
    public SoapEndpointConfig getActiveForExecution(InterfaceDefinition interfaceDefinition) {
        assertSoapInterface(interfaceDefinition);
        return soapEndpointConfigRepository.findByInterfaceDefinitionId(interfaceDefinition.getId())
                .filter(SoapEndpointConfig::isActive)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.BAD_REQUEST,
                        "Active SOAP endpoint configuration is required before execution."
                ));
    }

    @Transactional
    public SoapEndpointConfig save(Long interfaceDefinitionId, SoapEndpointConfigForm form) {
        InterfaceDefinition interfaceDefinition = getSoapInterface(interfaceDefinitionId);
        validateXml(form.getRequestTemplateXml());

        SoapEndpointConfig config = soapEndpointConfigRepository.findByInterfaceDefinitionId(interfaceDefinitionId)
                .orElseGet(() -> SoapEndpointConfig.create(
                        interfaceDefinition,
                        form.getEndpointUrl(),
                        trimToNull(form.getSoapAction()),
                        form.getOperationName(),
                        form.getNamespaceUri(),
                        form.getRequestTemplateXml(),
                        form.getTimeoutMillis(),
                        form.isActive()
                ));

        config.update(
                form.getEndpointUrl(),
                trimToNull(form.getSoapAction()),
                form.getOperationName(),
                form.getNamespaceUri(),
                form.getRequestTemplateXml(),
                form.getTimeoutMillis(),
                form.isActive()
        );
        return soapEndpointConfigRepository.save(config);
    }

    private InterfaceDefinition getSoapInterface(Long interfaceDefinitionId) {
        InterfaceDefinition interfaceDefinition = interfaceDefinitionRepository.findDetailById(interfaceDefinitionId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Interface definition not found"));
        assertSoapInterface(interfaceDefinition);
        return interfaceDefinition;
    }

    private void assertSoapInterface(InterfaceDefinition interfaceDefinition) {
        if (interfaceDefinition.getProtocolType() != ProtocolType.SOAP) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "SOAP configuration is only available for SOAP interfaces.");
        }
    }

    private void validateXml(String requestTemplateXml) {
        if (!StringUtils.hasText(requestTemplateXml)) {
            throw new IllegalArgumentException("Request template XML is required.");
        }

        try {
            DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
            factory.setFeature(XMLConstants.FEATURE_SECURE_PROCESSING, true);
            factory.setFeature("http://apache.org/xml/features/disallow-doctype-decl", true);
            factory.setNamespaceAware(true);
            factory.newDocumentBuilder().parse(new InputSource(new StringReader(requestTemplateXml)));
        } catch (Exception exception) {
            throw new IllegalArgumentException("Request template XML must be well-formed XML.");
        }
    }

    private String trimToNull(String value) {
        if (!StringUtils.hasText(value)) {
            return null;
        }
        return value.trim();
    }
}
