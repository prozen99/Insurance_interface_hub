package com.insurancehub;

import com.insurancehub.admin.infrastructure.repository.AdminUserRepository;
import com.insurancehub.interfacehub.infrastructure.repository.InterfaceDefinitionRepository;
import com.insurancehub.interfacehub.infrastructure.repository.InterfaceExecutionRepository;
import com.insurancehub.interfacehub.infrastructure.repository.InterfaceExecutionStepRepository;
import com.insurancehub.interfacehub.infrastructure.repository.InterfaceRetryTaskRepository;
import com.insurancehub.interfacehub.infrastructure.repository.InternalSystemRepository;
import com.insurancehub.interfacehub.infrastructure.repository.PartnerCompanyRepository;
import com.insurancehub.protocol.filetransfer.infrastructure.repository.FileTransferConfigRepository;
import com.insurancehub.protocol.filetransfer.infrastructure.repository.FileTransferHistoryRepository;
import com.insurancehub.protocol.mq.infrastructure.repository.MqChannelConfigRepository;
import com.insurancehub.protocol.mq.infrastructure.repository.MqMessageHistoryRepository;
import com.insurancehub.protocol.rest.infrastructure.repository.RestEndpointConfigRepository;
import com.insurancehub.protocol.soap.infrastructure.repository.SoapEndpointConfigRepository;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

@SpringBootTest(properties = {
        "spring.autoconfigure.exclude=org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration,"
                + "org.springframework.boot.autoconfigure.orm.jpa.HibernateJpaAutoConfiguration,"
                + "org.springframework.boot.autoconfigure.flyway.FlywayAutoConfiguration,"
                + "org.springframework.boot.autoconfigure.batch.BatchAutoConfiguration",
        "app.file-transfer.sftp.enabled=false",
        "app.file-transfer.ftp.enabled=false"
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

    @MockitoBean
    private MqChannelConfigRepository mqChannelConfigRepository;

    @MockitoBean
    private MqMessageHistoryRepository mqMessageHistoryRepository;

    @MockitoBean
    private FileTransferConfigRepository fileTransferConfigRepository;

    @MockitoBean
    private FileTransferHistoryRepository fileTransferHistoryRepository;

    @Test
    void contextLoads() {
    }
}
