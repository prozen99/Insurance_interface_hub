package com.insurancehub.interfacehub.application;

import java.util.List;

import com.insurancehub.interfacehub.domain.MasterStatus;
import com.insurancehub.interfacehub.domain.entity.InternalSystem;
import com.insurancehub.interfacehub.infrastructure.repository.InternalSystemRepository;
import com.insurancehub.interfacehub.presentation.form.InternalSystemForm;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

@Service
public class InternalSystemService {

    private final InternalSystemRepository internalSystemRepository;

    public InternalSystemService(InternalSystemRepository internalSystemRepository) {
        this.internalSystemRepository = internalSystemRepository;
    }

    @Transactional(readOnly = true)
    public List<InternalSystem> findAll() {
        return internalSystemRepository.findAllByOrderBySystemCodeAsc();
    }

    @Transactional(readOnly = true)
    public List<InternalSystem> findActive() {
        return internalSystemRepository.findByStatusOrderBySystemCodeAsc(MasterStatus.ACTIVE);
    }

    @Transactional(readOnly = true)
    public InternalSystem get(Long id) {
        return internalSystemRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Internal system not found"));
    }

    @Transactional
    public InternalSystem create(InternalSystemForm form) {
        String code = normalizeCode(form.getSystemCode());
        if (internalSystemRepository.existsBySystemCode(code)) {
            throw new DuplicateCodeException("System code already exists.");
        }

        return internalSystemRepository.save(InternalSystem.create(
                code,
                form.getSystemName().trim(),
                trimToNull(form.getOwnerDepartment()),
                form.getStatus(),
                trimToNull(form.getDescription())
        ));
    }

    @Transactional
    public InternalSystem update(Long id, InternalSystemForm form) {
        InternalSystem internalSystem = get(id);
        String code = normalizeCode(form.getSystemCode());
        if (internalSystemRepository.existsBySystemCodeAndIdNot(code, id)) {
            throw new DuplicateCodeException("System code already exists.");
        }

        internalSystem.update(
                code,
                form.getSystemName().trim(),
                trimToNull(form.getOwnerDepartment()),
                form.getStatus(),
                trimToNull(form.getDescription())
        );
        return internalSystem;
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
