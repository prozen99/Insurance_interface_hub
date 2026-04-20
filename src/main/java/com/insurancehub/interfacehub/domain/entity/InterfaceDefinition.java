package com.insurancehub.interfacehub.domain.entity;

import com.insurancehub.common.entity.BaseTimeEntity;
import com.insurancehub.interfacehub.domain.InterfaceDirection;
import com.insurancehub.interfacehub.domain.InterfaceStatus;
import com.insurancehub.interfacehub.domain.ProtocolType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Entity
@Table(name = "interface_definition")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class InterfaceDefinition extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "interface_code", nullable = false, unique = true, length = 80)
    private String interfaceCode;

    @Column(name = "interface_name", nullable = false, length = 180)
    private String interfaceName;

    @Enumerated(EnumType.STRING)
    @Column(name = "protocol_type", nullable = false, length = 30)
    private ProtocolType protocolType;

    @Enumerated(EnumType.STRING)
    @Column(name = "direction_type", nullable = false, length = 30)
    private InterfaceDirection direction;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private InterfaceStatus status;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "partner_company_id")
    private PartnerCompany partnerCompany;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "internal_system_id")
    private InternalSystem internalSystem;

    @Column(length = 1000)
    private String description;

    @Column(name = "enabled_yn", nullable = false)
    private boolean enabled;

    private InterfaceDefinition(
            String interfaceCode,
            String interfaceName,
            ProtocolType protocolType,
            InterfaceDirection direction,
            InterfaceStatus status,
            PartnerCompany partnerCompany,
            InternalSystem internalSystem,
            String description
    ) {
        this.interfaceCode = interfaceCode;
        this.interfaceName = interfaceName;
        this.protocolType = protocolType;
        this.direction = direction;
        this.status = status;
        this.partnerCompany = partnerCompany;
        this.internalSystem = internalSystem;
        this.description = description;
        this.enabled = status == InterfaceStatus.ACTIVE;
    }

    public static InterfaceDefinition create(
            String interfaceCode,
            String interfaceName,
            ProtocolType protocolType,
            InterfaceDirection direction,
            InterfaceStatus status,
            PartnerCompany partnerCompany,
            InternalSystem internalSystem,
            String description
    ) {
        return new InterfaceDefinition(
                interfaceCode,
                interfaceName,
                protocolType,
                direction,
                status,
                partnerCompany,
                internalSystem,
                description
        );
    }

    public void update(
            String interfaceCode,
            String interfaceName,
            ProtocolType protocolType,
            InterfaceDirection direction,
            InterfaceStatus status,
            PartnerCompany partnerCompany,
            InternalSystem internalSystem,
            String description
    ) {
        this.interfaceCode = interfaceCode;
        this.interfaceName = interfaceName;
        this.protocolType = protocolType;
        this.direction = direction;
        this.status = status;
        this.partnerCompany = partnerCompany;
        this.internalSystem = internalSystem;
        this.description = description;
        this.enabled = status == InterfaceStatus.ACTIVE;
    }

    public void activate() {
        this.status = InterfaceStatus.ACTIVE;
        this.enabled = true;
    }

    public void deactivate() {
        this.status = InterfaceStatus.INACTIVE;
        this.enabled = false;
    }
}
