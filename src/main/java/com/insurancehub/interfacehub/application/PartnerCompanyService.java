package com.insurancehub.interfacehub.application;

import java.util.List;

import com.insurancehub.interfacehub.domain.MasterStatus;
import com.insurancehub.interfacehub.domain.entity.PartnerCompany;
import com.insurancehub.interfacehub.infrastructure.repository.PartnerCompanyRepository;
import com.insurancehub.interfacehub.presentation.form.PartnerCompanyForm;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

@Service
public class PartnerCompanyService {

    private final PartnerCompanyRepository partnerCompanyRepository;

    public PartnerCompanyService(PartnerCompanyRepository partnerCompanyRepository) {
        this.partnerCompanyRepository = partnerCompanyRepository;
    }

    @Transactional(readOnly = true)
    public List<PartnerCompany> findAll() {
        return partnerCompanyRepository.findAllByOrderByPartnerCodeAsc();
    }

    @Transactional(readOnly = true)
    public List<PartnerCompany> findActive() {
        return partnerCompanyRepository.findByStatusOrderByPartnerCodeAsc(MasterStatus.ACTIVE);
    }

    @Transactional(readOnly = true)
    public PartnerCompany get(Long id) {
        return partnerCompanyRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Partner company not found"));
    }

    @Transactional
    public PartnerCompany create(PartnerCompanyForm form) {
        String code = normalizeCode(form.getPartnerCode());
        if (partnerCompanyRepository.existsByPartnerCode(code)) {
            throw new DuplicateCodeException("Partner code already exists.");
        }

        return partnerCompanyRepository.save(PartnerCompany.create(
                code,
                form.getPartnerName().trim(),
                form.getStatus(),
                trimToNull(form.getDescription())
        ));
    }

    @Transactional
    public PartnerCompany update(Long id, PartnerCompanyForm form) {
        PartnerCompany partnerCompany = get(id);
        String code = normalizeCode(form.getPartnerCode());
        if (partnerCompanyRepository.existsByPartnerCodeAndIdNot(code, id)) {
            throw new DuplicateCodeException("Partner code already exists.");
        }

        partnerCompany.update(
                code,
                form.getPartnerName().trim(),
                form.getStatus(),
                trimToNull(form.getDescription())
        );
        return partnerCompany;
    }

    private String normalizeCode(String code) {
        return code == null ? null : code.trim().toUpperCase();
    }

    private String trimToNull(String value) {
        if (value == null || value.trim().isEmpty()) {
            return null;
        }
        return value.trim();
    }
}
