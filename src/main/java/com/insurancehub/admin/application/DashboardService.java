package com.insurancehub.admin.application;

import java.util.List;

import com.insurancehub.interfacehub.domain.InterfaceStatus;
import com.insurancehub.interfacehub.domain.MasterStatus;
import com.insurancehub.interfacehub.infrastructure.repository.InterfaceDefinitionRepository;
import com.insurancehub.interfacehub.infrastructure.repository.InternalSystemRepository;
import com.insurancehub.interfacehub.infrastructure.repository.PartnerCompanyRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class DashboardService {

    private final InterfaceDefinitionRepository interfaceDefinitionRepository;
    private final PartnerCompanyRepository partnerCompanyRepository;
    private final InternalSystemRepository internalSystemRepository;

    public DashboardService(
            InterfaceDefinitionRepository interfaceDefinitionRepository,
            PartnerCompanyRepository partnerCompanyRepository,
            InternalSystemRepository internalSystemRepository
    ) {
        this.interfaceDefinitionRepository = interfaceDefinitionRepository;
        this.partnerCompanyRepository = partnerCompanyRepository;
        this.internalSystemRepository = internalSystemRepository;
    }

    @Transactional(readOnly = true)
    public List<DashboardMetric> getSummaryMetrics() {
        return List.of(
                new DashboardMetric("Total interfaces", String.valueOf(interfaceDefinitionRepository.count())),
                new DashboardMetric("Active interfaces", String.valueOf(interfaceDefinitionRepository.countByStatus(InterfaceStatus.ACTIVE))),
                new DashboardMetric("Partner companies", String.valueOf(partnerCompanyRepository.countByStatus(MasterStatus.ACTIVE))),
                new DashboardMetric("Internal systems", String.valueOf(internalSystemRepository.countByStatus(MasterStatus.ACTIVE)))
        );
    }

    public List<ProtocolModule> getProtocolModules() {
        return List.of(
                new ProtocolModule("REST", "HTTP API endpoints and partner callbacks"),
                new ProtocolModule("SOAP", "Legacy XML web service integrations"),
                new ProtocolModule("MQ", "Queue-based asynchronous interfaces"),
                new ProtocolModule("Batch", "Scheduled jobs and manual batch runs"),
                new ProtocolModule("SFTP", "Secure file transfer integrations"),
                new ProtocolModule("FTP", "Legacy file transfer integrations")
        );
    }

    public record DashboardMetric(String label, String value) {
    }

    public record ProtocolModule(String name, String description) {
    }
}
