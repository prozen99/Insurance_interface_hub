package com.insurancehub.interfacehub.application;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

import com.insurancehub.interfacehub.domain.MasterStatus;
import com.insurancehub.interfacehub.domain.entity.PartnerCompany;
import com.insurancehub.interfacehub.infrastructure.repository.PartnerCompanyRepository;
import com.insurancehub.interfacehub.presentation.form.PartnerCompanyForm;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class PartnerCompanyServiceTest {

    @Mock
    private PartnerCompanyRepository partnerCompanyRepository;

    @InjectMocks
    private PartnerCompanyService partnerCompanyService;

    @Test
    void createNormalizesCodeAndSavesPartnerCompany() {
        PartnerCompanyForm form = new PartnerCompanyForm();
        form.setPartnerCode(" lifeplus ");
        form.setPartnerName("Life Plus Insurance");
        form.setStatus(MasterStatus.ACTIVE);
        form.setDescription("Demo partner");

        when(partnerCompanyRepository.existsByPartnerCode("LIFEPLUS")).thenReturn(false);
        when(partnerCompanyRepository.save(any(PartnerCompany.class))).thenAnswer(invocation -> invocation.getArgument(0));

        PartnerCompany saved = partnerCompanyService.create(form);

        assertThat(saved.getPartnerCode()).isEqualTo("LIFEPLUS");
        assertThat(saved.getPartnerName()).isEqualTo("Life Plus Insurance");
        assertThat(saved.getStatus()).isEqualTo(MasterStatus.ACTIVE);
    }

    @Test
    void createRejectsDuplicatePartnerCode() {
        PartnerCompanyForm form = new PartnerCompanyForm();
        form.setPartnerCode("LIFEPLUS");
        form.setPartnerName("Life Plus Insurance");
        form.setStatus(MasterStatus.ACTIVE);

        when(partnerCompanyRepository.existsByPartnerCode("LIFEPLUS")).thenReturn(true);

        assertThatThrownBy(() -> partnerCompanyService.create(form))
                .isInstanceOf(DuplicateCodeException.class)
                .hasMessageContaining("Partner code already exists");
    }
}
