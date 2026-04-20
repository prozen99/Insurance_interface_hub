package com.insurancehub.interfacehub.domain.entity;

import com.insurancehub.common.entity.BaseTimeEntity;
import com.insurancehub.interfacehub.domain.MasterStatus;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Entity
@Table(name = "partner_company")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class PartnerCompany extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "partner_code", nullable = false, unique = true, length = 60)
    private String partnerCode;

    @Column(name = "partner_name", nullable = false, length = 160)
    private String partnerName;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private MasterStatus status;

    @Column(length = 1000)
    private String description;

    @Column(name = "active_yn", nullable = false)
    private boolean active;

    private PartnerCompany(String partnerCode, String partnerName, MasterStatus status, String description) {
        this.partnerCode = partnerCode;
        this.partnerName = partnerName;
        this.status = status;
        this.description = description;
        this.active = status == MasterStatus.ACTIVE;
    }

    public static PartnerCompany create(String partnerCode, String partnerName, MasterStatus status, String description) {
        return new PartnerCompany(partnerCode, partnerName, status, description);
    }

    public void update(String partnerCode, String partnerName, MasterStatus status, String description) {
        this.partnerCode = partnerCode;
        this.partnerName = partnerName;
        this.status = status;
        this.description = description;
        this.active = status == MasterStatus.ACTIVE;
    }
}
