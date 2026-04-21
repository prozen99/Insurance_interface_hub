package com.insurancehub.protocol.soap;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.charset.StandardCharsets;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.insurancehub.interfacehub.application.execution.ExecutionRequest;
import com.insurancehub.interfacehub.application.execution.ExecutionResult;
import com.insurancehub.interfacehub.domain.ExecutionTriggerType;
import com.insurancehub.interfacehub.domain.InterfaceDirection;
import com.insurancehub.interfacehub.domain.InterfaceStatus;
import com.insurancehub.interfacehub.domain.MasterStatus;
import com.insurancehub.interfacehub.domain.ProtocolType;
import com.insurancehub.interfacehub.domain.entity.InterfaceDefinition;
import com.insurancehub.interfacehub.domain.entity.InternalSystem;
import com.insurancehub.interfacehub.domain.entity.PartnerCompany;
import com.insurancehub.protocol.soap.application.SoapEndpointConfigService;
import com.insurancehub.protocol.soap.domain.entity.SoapEndpointConfig;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpServer;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

@ExtendWith(MockitoExtension.class)
class SoapInterfaceExecutorTest {

    @Mock
    private SoapEndpointConfigService soapEndpointConfigService;

    private HttpServer httpServer;
    private SoapInterfaceExecutor executor;

    @BeforeEach
    void setUp() throws IOException {
        httpServer = HttpServer.create(new InetSocketAddress(0), 0);
        httpServer.createContext("/soap/policy", this::handlePolicyInquiry);
        httpServer.start();
        executor = new SoapInterfaceExecutor(soapEndpointConfigService, new ObjectMapper());
    }

    @AfterEach
    void tearDown() {
        if (httpServer != null) {
            httpServer.stop(0);
        }
    }

    @Test
    void executeCallsSoapEndpointAndReturnsSuccess() {
        InterfaceDefinition definition = soapDefinition();
        SoapEndpointConfig config = config(definition);
        when(soapEndpointConfigService.getActiveForExecution(definition)).thenReturn(config);

        ExecutionResult result = executor.execute(new ExecutionRequest(
                definition,
                null,
                ExecutionTriggerType.MANUAL,
                successRequestXml()
        ));

        assertThat(result.success()).isTrue();
        assertThat(result.responseStatusCode()).isEqualTo(200);
        assertThat(result.protocolAction()).isEqualTo("urn:PolicyInquiry");
        assertThat(result.requestUrl()).endsWith("/soap/policy");
        assertThat(result.responsePayload()).contains("PolicyInquiryResponse");
        assertThat(result.latencyMs()).isNotNull();
    }

    @Test
    void executeReturnsFailureWhenSoapEndpointReturnsFault() {
        InterfaceDefinition definition = soapDefinition();
        SoapEndpointConfig config = config(definition);
        when(soapEndpointConfigService.getActiveForExecution(definition)).thenReturn(config);

        ExecutionResult result = executor.execute(new ExecutionRequest(
                definition,
                null,
                ExecutionTriggerType.MANUAL,
                successRequestXml().replace("POL-001", "FAIL")
        ));

        assertThat(result.success()).isFalse();
        assertThat(result.errorCode()).isEqualTo("SOAP_FAULT");
        assertThat(result.responseStatusCode()).isEqualTo(500);
        assertThat(result.responsePayload()).contains("Fault");
    }

    private void handlePolicyInquiry(HttpExchange exchange) throws IOException {
        String requestBody = new String(exchange.getRequestBody().readAllBytes(), StandardCharsets.UTF_8);
        boolean fail = requestBody.toUpperCase().contains("FAIL");
        byte[] response = (fail
                ? """
                <soapenv:Envelope xmlns:soapenv="http://schemas.xmlsoap.org/soap/envelope/">
                  <soapenv:Body><soapenv:Fault><faultcode>TEST_FAULT</faultcode><faultstring>failed</faultstring></soapenv:Fault></soapenv:Body>
                </soapenv:Envelope>
                """
                : """
                <soapenv:Envelope xmlns:soapenv="http://schemas.xmlsoap.org/soap/envelope/">
                  <soapenv:Body><PolicyInquiryResponse><status>SUCCESS</status></PolicyInquiryResponse></soapenv:Body>
                </soapenv:Envelope>
                """
        ).getBytes(StandardCharsets.UTF_8);

        exchange.getResponseHeaders().add("Content-Type", "text/xml");
        exchange.sendResponseHeaders(fail ? 500 : 200, response.length);
        exchange.getResponseBody().write(response);
        exchange.close();
    }

    private SoapEndpointConfig config(InterfaceDefinition definition) {
        return SoapEndpointConfig.create(
                definition,
                "http://localhost:" + httpServer.getAddress().getPort() + "/soap/policy",
                "urn:PolicyInquiry",
                "PolicyInquiry",
                "http://insurancehub.local/soap/policy",
                successRequestXml(),
                3000,
                true
        );
    }

    private String successRequestXml() {
        return """
                <soapenv:Envelope xmlns:soapenv="http://schemas.xmlsoap.org/soap/envelope/" xmlns:pol="http://insurancehub.local/soap/policy">
                  <soapenv:Body><pol:PolicyInquiryRequest><pol:policyNo>POL-001</pol:policyNo></pol:PolicyInquiryRequest></soapenv:Body>
                </soapenv:Envelope>
                """;
    }

    private InterfaceDefinition soapDefinition() {
        InterfaceDefinition definition = InterfaceDefinition.create(
                "IF_SOAP_POLICY_001",
                "Policy inquiry outbound SOAP interface",
                ProtocolType.SOAP,
                InterfaceDirection.OUTBOUND,
                InterfaceStatus.ACTIVE,
                PartnerCompany.create("LIFEPLUS", "Life Plus Insurance", MasterStatus.ACTIVE, null),
                InternalSystem.create("POLICY_CORE", "Policy Core System", "Insurance Platform Team", MasterStatus.ACTIVE, null),
                null
        );
        ReflectionTestUtils.setField(definition, "id", 1L);
        return definition;
    }
}
