package com.insurancehub.protocol.rest;

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
class RestInterfaceExecutorTest {

    @Mock
    private RestEndpointConfigService restEndpointConfigService;

    private HttpServer httpServer;
    private RestInterfaceExecutor executor;

    @BeforeEach
    void setUp() throws IOException {
        httpServer = HttpServer.create(new InetSocketAddress(0), 0);
        httpServer.createContext("/premium/calculate", this::handlePremiumCalculate);
        httpServer.start();
        executor = new RestInterfaceExecutor(restEndpointConfigService, new ObjectMapper());
    }

    @AfterEach
    void tearDown() {
        if (httpServer != null) {
            httpServer.stop(0);
        }
    }

    @Test
    void executeCallsRestEndpointAndReturnsSuccess() {
        InterfaceDefinition definition = restDefinition();
        RestEndpointConfig config = config(definition);
        when(restEndpointConfigService.getActiveForExecution(definition)).thenReturn(config);

        ExecutionResult result = executor.execute(new ExecutionRequest(
                definition,
                null,
                ExecutionTriggerType.MANUAL,
                "{\"policyNo\":\"P001\"}"
        ));

        assertThat(result.success()).isTrue();
        assertThat(result.responseStatusCode()).isEqualTo(200);
        assertThat(result.requestUrl()).endsWith("/premium/calculate");
        assertThat(result.responsePayload()).contains("SUCCESS");
        assertThat(result.latencyMs()).isNotNull();
    }

    @Test
    void executeReturnsFailureWhenRestEndpointReturnsErrorStatus() {
        InterfaceDefinition definition = restDefinition();
        RestEndpointConfig config = config(definition);
        when(restEndpointConfigService.getActiveForExecution(definition)).thenReturn(config);

        ExecutionResult result = executor.execute(new ExecutionRequest(
                definition,
                null,
                ExecutionTriggerType.MANUAL,
                "{\"policyNo\":\"FAIL\"}"
        ));

        assertThat(result.success()).isFalse();
        assertThat(result.errorCode()).isEqualTo("REST_HTTP_ERROR");
        assertThat(result.responseStatusCode()).isEqualTo(422);
        assertThat(result.responsePayload()).contains("FAILED");
    }

    private void handlePremiumCalculate(HttpExchange exchange) throws IOException {
        String requestBody = new String(exchange.getRequestBody().readAllBytes(), StandardCharsets.UTF_8);
        boolean fail = requestBody.toUpperCase().contains("FAIL");
        byte[] response = (fail
                ? "{\"status\":\"FAILED\",\"code\":\"TEST_FAILURE\"}"
                : "{\"status\":\"SUCCESS\",\"premiumAmount\":125000}"
        ).getBytes(StandardCharsets.UTF_8);

        exchange.getResponseHeaders().add("Content-Type", "application/json");
        exchange.sendResponseHeaders(fail ? 422 : 200, response.length);
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
