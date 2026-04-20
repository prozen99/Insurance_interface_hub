package com.insurancehub.interfacehub.application;

import java.util.List;

import com.insurancehub.interfacehub.domain.InterfaceStatus;
import com.insurancehub.interfacehub.domain.ProtocolType;
import com.insurancehub.interfacehub.domain.entity.InterfaceDefinition;
import com.insurancehub.interfacehub.domain.entity.InternalSystem;
import com.insurancehub.interfacehub.domain.entity.PartnerCompany;
import com.insurancehub.interfacehub.infrastructure.repository.InterfaceDefinitionRepository;
import com.insurancehub.interfacehub.presentation.form.InterfaceDefinitionForm;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;
import org.springframework.web.server.ResponseStatusException;

@Service
public class InterfaceDefinitionService {

    private final InterfaceDefinitionRepository interfaceDefinitionRepository;
    private final PartnerCompanyService partnerCompanyService;
    private final InternalSystemService internalSystemService;

    public InterfaceDefinitionService(
            InterfaceDefinitionRepository interfaceDefinitionRepository,
            PartnerCompanyService partnerCompanyService,
            InternalSystemService internalSystemService
    ) {
        this.interfaceDefinitionRepository = interfaceDefinitionRepository;
        this.partnerCompanyService = partnerCompanyService;
        this.internalSystemService = internalSystemService;
    }

    @Transactional(readOnly = true)
    public List<InterfaceDefinition> search(String keyword, ProtocolType protocolType, InterfaceStatus status) {
        return interfaceDefinitionRepository.search(normalizeKeyword(keyword), protocolType, status);
    }

    @Transactional(readOnly = true)
    public InterfaceDefinition getDetail(Long id) {
        return interfaceDefinitionRepository.findDetailById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Interface definition not found"));
    }

    @Transactional(readOnly = true)
    public InterfaceDefinition get(Long id) {
        return interfaceDefinitionRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Interface definition not found"));
    }

    @Transactional
    public InterfaceDefinition create(InterfaceDefinitionForm form) {
        String code = normalizeCode(form.getInterfaceCode());
        if (interfaceDefinitionRepository.existsByInterfaceCode(code)) {
            throw new DuplicateCodeException("Interface code already exists.");
        }

        PartnerCompany partnerCompany = partnerCompanyService.get(form.getPartnerCompanyId());
        InternalSystem internalSystem = internalSystemService.get(form.getInternalSystemId());

        return interfaceDefinitionRepository.save(InterfaceDefinition.create(
                code,
                form.getInterfaceName().trim(),
                form.getProtocolType(),
                form.getDirection(),
                form.getStatus(),
                partnerCompany,
                internalSystem,
                trimToNull(form.getDescription())
        ));
    }

    @Transactional
    public InterfaceDefinition update(Long id, InterfaceDefinitionForm form) {
        InterfaceDefinition interfaceDefinition = get(id);
        String code = normalizeCode(form.getInterfaceCode());
        if (interfaceDefinitionRepository.existsByInterfaceCodeAndIdNot(code, id)) {
            throw new DuplicateCodeException("Interface code already exists.");
        }

        PartnerCompany partnerCompany = partnerCompanyService.get(form.getPartnerCompanyId());
        InternalSystem internalSystem = internalSystemService.get(form.getInternalSystemId());

        interfaceDefinition.update(
                code,
                form.getInterfaceName().trim(),
                form.getProtocolType(),
                form.getDirection(),
                form.getStatus(),
                partnerCompany,
                internalSystem,
                trimToNull(form.getDescription())
        );
        return interfaceDefinition;
    }

    @Transactional
    public void activate(Long id) {
        get(id).activate();
    }

    @Transactional
    public void deactivate(Long id) {
        get(id).deactivate();
    }

    private String normalizeCode(String code) {
        return code == null ? null : code.trim().toUpperCase();
    }

    private String normalizeKeyword(String keyword) {
        if (!StringUtils.hasText(keyword)) {
            return null;
        }
        return keyword.trim();
    }

    private String trimToNull(String value) {
        if (!StringUtils.hasText(value)) {
            return null;
        }
        return value.trim();
    }
}
