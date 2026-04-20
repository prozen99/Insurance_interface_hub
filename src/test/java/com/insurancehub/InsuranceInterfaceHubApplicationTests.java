package com.insurancehub;

import com.insurancehub.admin.infrastructure.repository.AdminUserRepository;
import com.insurancehub.interfacehub.infrastructure.repository.InterfaceDefinitionRepository;
import com.insurancehub.interfacehub.infrastructure.repository.InternalSystemRepository;
import com.insurancehub.interfacehub.infrastructure.repository.PartnerCompanyRepository;
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
    private PartnerCompanyRepository partnerCompanyRepository;

    @MockitoBean
    private InternalSystemRepository internalSystemRepository;

    @Test
    void contextLoads() {
    }
}
