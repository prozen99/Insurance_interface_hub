package com.insurancehub;

import com.insurancehub.admin.infrastructure.repository.AdminUserRepository;
import com.insurancehub.interfacehub.infrastructure.repository.InterfaceDefinitionRepository;
import com.insurancehub.interfacehub.infrastructure.repository.InterfaceExecutionRepository;
import com.insurancehub.interfacehub.infrastructure.repository.InterfaceExecutionStepRepository;
import com.insurancehub.interfacehub.infrastructure.repository.InterfaceRetryTaskRepository;
import com.insurancehub.interfacehub.infrastructure.repository.InternalSystemRepository;
import com.insurancehub.interfacehub.infrastructure.repository.PartnerCompanyRepository;
import com.insurancehub.protocol.rest.infrastructure.repository.RestEndpointConfigRepository;
import com.insurancehub.protocol.soap.infrastructure.repository.SoapEndpointConfigRepository;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

@SpringBootTest(properties = {
        "spring.autoconfigure.exclude=org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration,"
                + "org.springframework.boot.autoconfigure.orm.jpa.HibernateJpaAutoConfiguration,"
                + "org.springframework.boot.autoconfigure.flyway.FlywayAutoConfiguration,"
                + "org.springframework.boot.autoconfigure.batch.BatchAutoConfiguration"
})
class InsuranceInterfaceHubApplicationTests {

    @MockitoBean
    private AdminUserRepository adminUserRepository;

    @MockitoBean
    private InterfaceDefinitionRepository interfaceDefinitionRepository;

    @MockitoBean
    private InterfaceExecutionRepository interfaceExecutionRepository;

    @MockitoBean
    private InterfaceExecutionStepRepository interfaceExecutionStepRepository;

    @MockitoBean
    private InterfaceRetryTaskRepository interfaceRetryTaskRepository;

    @MockitoBean
    private PartnerCompanyRepository partnerCompanyRepository;

    @MockitoBean
    private InternalSystemRepository internalSystemRepository;

    @MockitoBean
    private RestEndpointConfigRepository restEndpointConfigRepository;

    @MockitoBean
    private SoapEndpointConfigRepository soapEndpointConfigRepository;

    @Test
    void contextLoads() {
    }
}
