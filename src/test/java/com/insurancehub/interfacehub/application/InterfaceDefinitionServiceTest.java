package com.insurancehub.interfacehub.application;

import static org.assertj.core.api.Assertions.assertThat;
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
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class InterfaceDefinitionServiceTest {

    @Mock
    private InterfaceDefinitionRepository interfaceDefinitionRepository;

    @Mock
    private PartnerCompanyService partnerCompanyService;

    @Mock
    private InternalSystemService internalSystemService;

    @Test
    void deactivateChangesInterfaceStatusToInactive() {
        InterfaceDefinitionService service = new InterfaceDefinitionService(
                interfaceDefinitionRepository,
                partnerCompanyService,
                internalSystemService
        );
        InterfaceDefinition interfaceDefinition = InterfaceDefinition.create(
                "IF_REST_POLICY_001",
                "Policy status outbound REST interface",
                ProtocolType.REST,
                InterfaceDirection.OUTBOUND,
                InterfaceStatus.ACTIVE,
                PartnerCompany.create("LIFEPLUS", "Life Plus Insurance", MasterStatus.ACTIVE, null),
                InternalSystem.create("POLICY_CORE", "Policy Core System", "Insurance Platform Team", MasterStatus.ACTIVE, null),
                null
        );
        when(interfaceDefinitionRepository.findById(1L)).thenReturn(Optional.of(interfaceDefinition));

        service.deactivate(1L);

        assertThat(interfaceDefinition.getStatus()).isEqualTo(InterfaceStatus.INACTIVE);
    }
}
