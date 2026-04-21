package com.insurancehub.interfacehub.application.execution;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.insurancehub.interfacehub.domain.ExecutionStatus;
import com.insurancehub.interfacehub.domain.ExecutionTriggerType;
import com.insurancehub.interfacehub.domain.InterfaceDirection;
import com.insurancehub.interfacehub.domain.InterfaceStatus;
import com.insurancehub.interfacehub.domain.MasterStatus;
import com.insurancehub.interfacehub.domain.ProtocolType;
import com.insurancehub.interfacehub.domain.RetryStatus;
import com.insurancehub.interfacehub.domain.entity.InterfaceDefinition;
import com.insurancehub.interfacehub.domain.entity.InterfaceExecution;
import com.insurancehub.interfacehub.domain.entity.InterfaceExecutionStep;
import com.insurancehub.interfacehub.domain.entity.InterfaceRetryTask;
import com.insurancehub.interfacehub.domain.entity.InternalSystem;
import com.insurancehub.interfacehub.domain.entity.PartnerCompany;
import com.insurancehub.interfacehub.infrastructure.repository.InterfaceDefinitionRepository;
import com.insurancehub.interfacehub.infrastructure.repository.InterfaceExecutionRepository;
import com.insurancehub.interfacehub.infrastructure.repository.InterfaceExecutionStepRepository;
import com.insurancehub.interfacehub.infrastructure.repository.InterfaceRetryTaskRepository;
import com.insurancehub.protocol.soap.SoapInterfaceExecutor;
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
class InterfaceExecutionSoapRetryTest {

    @Mock
    private InterfaceDefinitionRepository interfaceDefinitionRepository;

    @Mock
    private InterfaceExecutionRepository interfaceExecutionRepository;

    @Mock
    private InterfaceExecutionStepRepository interfaceExecutionStepRepository;

    @Mock
    private InterfaceRetryTaskRepository interfaceRetryTaskRepository;

    @Mock
    private SoapEndpointConfigService soapEndpointConfigService;

    private HttpServer httpServer;
    private InterfaceExecutionService service;

    @BeforeEach
    void setUp() throws IOException {
        httpServer = HttpServer.create(new InetSocketAddress(0), 0);
        httpServer.createContext("/soap/policy", this::handlePolicyInquiry);
        httpServer.start();

        InterfaceExecutorFactory executorFactory = new InterfaceExecutorFactory(List.of(
                new SoapInterfaceExecutor(soapEndpointConfigService, new ObjectMapper())
        ));
        service = new InterfaceExecutionService(
                interfaceDefinitionRepository,
                interfaceExecutionRepository,
                interfaceExecutionStepRepository,
                interfaceRetryTaskRepository,
                executorFactory
        );
        when(interfaceExecutionRepository.existsByExecutionNo(any())).thenReturn(false);
        when(interfaceExecutionRepository.save(any(InterfaceExecution.class))).thenAnswer(invocation -> invocation.getArgument(0));
    }

    @AfterEach
    void tearDown() {
        if (httpServer != null) {
            httpServer.stop(0);
        }
    }

    @Test
    void retryFailedSoapExecutionUsesRealSoapExecutor() {
        InterfaceDefinition definition = soapDefinition();
        InterfaceExecution original = InterfaceExecution.create(
                "EXE-SOAP-ORIGINAL",
                definition,
                null,
                ExecutionTriggerType.MANUAL,
                requestXml(),
                "admin"
        );
        ReflectionTestUtils.setField(original, "id", 20L);
        original.markRunning(LocalDateTime.now());
        original.markFailed("SOAP_CLIENT_ERROR", "first call failed", "{}", LocalDateTime.now());
        InterfaceRetryTask retryTask = InterfaceRetryTask.waitingFor(original, LocalDateTime.now());

        when(interfaceExecutionRepository.findDetailById(20L)).thenReturn(Optional.of(original));
        when(interfaceRetryTaskRepository.findFirstByExecutionIdAndRetryStatusOrderByCreatedAtDesc(20L, RetryStatus.WAITING))
                .thenReturn(Optional.of(retryTask));
        when(soapEndpointConfigService.getActiveForExecution(definition)).thenReturn(config(definition));

        InterfaceExecution retryExecution = service.retryFailedExecution(20L, "admin");

        assertThat(retryExecution.getTriggerType()).isEqualTo(ExecutionTriggerType.RETRY);
        assertThat(retryExecution.getExecutionStatus()).isEqualTo(ExecutionStatus.SUCCESS);
        assertThat(retryExecution.getResponseStatusCode()).isEqualTo(200);
        assertThat(retryExecution.getProtocolAction()).isEqualTo("urn:PolicyInquiry");
        assertThat(retryExecution.getRequestUrl()).endsWith("/soap/policy");
        assertThat(retryTask.getRetryStatus()).isEqualTo(RetryStatus.DONE);
        verify(interfaceExecutionStepRepository, times(3)).save(any(InterfaceExecutionStep.class));
    }

    private void handlePolicyInquiry(HttpExchange exchange) throws IOException {
        byte[] response = """
                <soapenv:Envelope xmlns:soapenv="http://schemas.xmlsoap.org/soap/envelope/">
                  <soapenv:Body><PolicyInquiryResponse><status>SUCCESS</status></PolicyInquiryResponse></soapenv:Body>
                </soapenv:Envelope>
                """.getBytes(StandardCharsets.UTF_8);
        exchange.getResponseHeaders().add("Content-Type", "text/xml");
        exchange.sendResponseHeaders(200, response.length);
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
                requestXml(),
                3000,
                true
        );
    }

    private String requestXml() {
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
