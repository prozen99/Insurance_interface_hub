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
@Table(name = "internal_system")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class InternalSystem extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "system_code", nullable = false, unique = true, length = 60)
    private String systemCode;

    @Column(name = "system_name", nullable = false, length = 160)
    private String systemName;

    @Column(name = "owner_department", length = 120)
    private String ownerDepartment;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private MasterStatus status;

    @Column(length = 1000)
    private String description;

    @Column(name = "active_yn", nullable = false)
    private boolean active;

    private InternalSystem(
            String systemCode,
            String systemName,
            String ownerDepartment,
            MasterStatus status,
            String description
    ) {
        this.systemCode = systemCode;
        this.systemName = systemName;
        this.ownerDepartment = ownerDepartment;
        this.status = status;
        this.description = description;
        this.active = status == MasterStatus.ACTIVE;
    }

    public static InternalSystem create(
            String systemCode,
            String systemName,
            String ownerDepartment,
            MasterStatus status,
            String description
    ) {
        return new InternalSystem(systemCode, systemName, ownerDepartment, status, description);
    }

    public void update(String systemCode, String systemName, String ownerDepartment, MasterStatus status, String description) {
        this.systemCode = systemCode;
        this.systemName = systemName;
        this.ownerDepartment = ownerDepartment;
        this.status = status;
        this.description = description;
        this.active = status == MasterStatus.ACTIVE;
    }
}
