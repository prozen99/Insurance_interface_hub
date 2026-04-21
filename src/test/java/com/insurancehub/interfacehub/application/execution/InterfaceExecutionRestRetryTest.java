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
import com.insurancehub.protocol.rest.RestInterfaceExecutor;
import com.insurancehub.protocol.rest.application.RestEndpointConfigService;
import com.insurancehub.protocol.rest.domain.RestHttpMethod;
import com.insurancehub.protocol.rest.domain.entity.RestEndpointConfig;
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
class InterfaceExecutionRestRetryTest {

    @Mock
    private InterfaceDefinitionRepository interfaceDefinitionRepository;

    @Mock
    private InterfaceExecutionRepository interfaceExecutionRepository;

    @Mock
    private InterfaceExecutionStepRepository interfaceExecutionStepRepository;

    @Mock
    private InterfaceRetryTaskRepository interfaceRetryTaskRepository;

    @Mock
    private RestEndpointConfigService restEndpointConfigService;

    private HttpServer httpServer;
    private InterfaceExecutionService service;

    @BeforeEach
    void setUp() throws IOException {
        httpServer = HttpServer.create(new InetSocketAddress(0), 0);
        httpServer.createContext("/premium/calculate", this::handlePremiumCalculate);
        httpServer.start();

        InterfaceExecutorFactory executorFactory = new InterfaceExecutorFactory(List.of(
                new RestInterfaceExecutor(restEndpointConfigService, new ObjectMapper())
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
    void retryFailedRestExecutionUsesRealHttpExecutor() {
        InterfaceDefinition definition = restDefinition();
        InterfaceExecution original = InterfaceExecution.create(
                "EXE-ORIGINAL",
                definition,
                null,
                ExecutionTriggerType.MANUAL,
                "{\"policyNo\":\"P001\"}",
                "admin"
        );
        ReflectionTestUtils.setField(original, "id", 10L);
        original.markRunning(LocalDateTime.now());
        original.markFailed("REST_CLIENT_ERROR", "first call failed", "{}", LocalDateTime.now());
        InterfaceRetryTask retryTask = InterfaceRetryTask.waitingFor(original, LocalDateTime.now());

        when(interfaceExecutionRepository.findDetailById(10L)).thenReturn(Optional.of(original));
        when(interfaceRetryTaskRepository.findFirstByExecutionIdAndRetryStatusOrderByCreatedAtDesc(10L, RetryStatus.WAITING))
                .thenReturn(Optional.of(retryTask));
        when(restEndpointConfigService.getActiveForExecution(definition)).thenReturn(config(definition));

        InterfaceExecution retryExecution = service.retryFailedExecution(10L, "admin");

        assertThat(retryExecution.getTriggerType()).isEqualTo(ExecutionTriggerType.RETRY);
        assertThat(retryExecution.getExecutionStatus()).isEqualTo(ExecutionStatus.SUCCESS);
        assertThat(retryExecution.getResponseStatusCode()).isEqualTo(200);
        assertThat(retryExecution.getRequestUrl()).endsWith("/premium/calculate");
        assertThat(retryTask.getRetryStatus()).isEqualTo(RetryStatus.DONE);
        verify(interfaceExecutionStepRepository, times(3)).save(any(InterfaceExecutionStep.class));
    }

    private void handlePremiumCalculate(HttpExchange exchange) throws IOException {
        byte[] response = "{\"status\":\"SUCCESS\",\"premiumAmount\":125000}".getBytes(StandardCharsets.UTF_8);
        exchange.getResponseHeaders().add("Content-Type", "application/json");
        exchange.sendResponseHeaders(200, response.length);
        exchange.getResponseBody().write(response);
        exchange.close();
    }

    private RestEndpointConfig config(InterfaceDefinition definition) {
        return RestEndpointConfig.create(
                definition,
                RestHttpMethod.POST,
                "http://localhost:" + httpServer.getAddress().getPort(),
                "/premium/calculate",
                3000,
                "{\"Content-Type\":\"application/json\"}",
                "{\"policyNo\":\"P001\"}",
                true
        );
    }

    private InterfaceDefinition restDefinition() {
        InterfaceDefinition definition = InterfaceDefinition.create(
                "IF_REST_POLICY_001",
                "Policy status outbound REST interface",
                ProtocolType.REST,
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
