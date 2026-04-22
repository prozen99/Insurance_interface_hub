package com.insurancehub.protocol.filetransfer.application;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

import java.util.Optional;

import com.insurancehub.interfacehub.domain.InterfaceDirection;
import com.insurancehub.interfacehub.domain.InterfaceStatus;
import com.insurancehub.interfacehub.domain.MasterStatus;
import com.insurancehub.interfacehub.domain.ProtocolType;
import com.insurancehub.interfacehub.domain.entity.InterfaceDefinition;
import com.insurancehub.interfacehub.domain.entity.InternalSystem;
import com.insurancehub.interfacehub.domain.entity.PartnerCompany;
import com.insurancehub.interfacehub.infrastructure.repository.InterfaceDefinitionRepository;
import com.insurancehub.protocol.filetransfer.domain.entity.FileTransferConfig;
import com.insurancehub.protocol.filetransfer.infrastructure.repository.FileTransferConfigRepository;
import com.insurancehub.protocol.filetransfer.presentation.form.FileTransferConfigForm;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.server.ResponseStatusException;

@ExtendWith(MockitoExtension.class)
class FileTransferConfigServiceTest {

    @Mock
    private FileTransferConfigRepository fileTransferConfigRepository;

    @Mock
    private InterfaceDefinitionRepository interfaceDefinitionRepository;

    private FileTransferConfigService service;

    @BeforeEach
    void setUp() {
        service = new FileTransferConfigService(fileTransferConfigRepository, interfaceDefinitionRepository);
    }

    @Test
    void saveCreatesSftpConfig() {
        InterfaceDefinition definition = definition(ProtocolType.SFTP);
        FileTransferConfigForm form = FileTransferConfigForm.empty(ProtocolType.SFTP);
        form.setHost("127.0.0.1");
        form.setPort(10022);
        form.setBaseRemotePath("/upload");

        when(interfaceDefinitionRepository.findDetailById(1L)).thenReturn(Optional.of(definition));
        when(fileTransferConfigRepository.findByInterfaceDefinitionId(1L)).thenReturn(Optional.empty());
        when(fileTransferConfigRepository.save(any(FileTransferConfig.class))).thenAnswer(invocation -> invocation.getArgument(0));

        FileTransferConfig saved = service.save(1L, form);

        assertThat(saved.getProtocolType()).isEqualTo(ProtocolType.SFTP);
        assertThat(saved.getHost()).isEqualTo("127.0.0.1");
        assertThat(saved.getBaseRemotePath()).isEqualTo("/upload");
        assertThat(saved.isActive()).isTrue();
    }

    @Test
    void saveRejectsProtocolMismatch() {
        InterfaceDefinition definition = definition(ProtocolType.FTP);
        FileTransferConfigForm form = FileTransferConfigForm.empty(ProtocolType.SFTP);

        when(interfaceDefinitionRepository.findDetailById(1L)).thenReturn(Optional.of(definition));

        assertThatThrownBy(() -> service.save(1L, form))
                .isInstanceOf(ResponseStatusException.class)
                .hasMessageContaining("does not match");
    }

    private InterfaceDefinition definition(ProtocolType protocolType) {
        InterfaceDefinition definition = InterfaceDefinition.create(
                "IF_" + protocolType + "_POLICY_001",
                protocolType + " policy file interface",
                protocolType,
                InterfaceDirection.BIDIRECTIONAL,
                InterfaceStatus.ACTIVE,
                PartnerCompany.create("LIFEPLUS", "Life Plus Insurance", MasterStatus.ACTIVE, null),
                InternalSystem.create("POLICY_CORE", "Policy Core System", "Insurance Platform Team", MasterStatus.ACTIVE, null),
                null
        );
        ReflectionTestUtils.setField(definition, "id", 1L);
        return definition;
    }
}
