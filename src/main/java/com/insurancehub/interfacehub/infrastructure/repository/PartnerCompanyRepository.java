package com.insurancehub.interfacehub.infrastructure.repository;

import java.util.List;

import com.insurancehub.interfacehub.domain.MasterStatus;
import com.insurancehub.interfacehub.domain.entity.PartnerCompany;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PartnerCompanyRepository extends JpaRepository<PartnerCompany, Long> {

    List<PartnerCompany> findAllByOrderByPartnerCodeAsc();

    List<PartnerCompany> findByStatusOrderByPartnerCodeAsc(MasterStatus status);

    boolean existsByPartnerCode(String partnerCode);

    boolean existsByPartnerCodeAndIdNot(String partnerCode, Long id);

    long countByStatus(MasterStatus status);
}
