package com.insurancehub.admin.domain.entity;

import java.time.LocalDateTime;

import com.insurancehub.admin.domain.AdminUserStatus;
import com.insurancehub.common.entity.BaseTimeEntity;
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
@Table(name = "admin_user")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class AdminUser extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "login_id", nullable = false, unique = true, length = 80)
    private String loginId;

    @Column(name = "password_hash", length = 100)
    private String passwordHash;

    @Column(name = "display_name", nullable = false, length = 120)
    private String displayName;

    @Column(length = 180)
    private String email;

    @Column(name = "role_code", nullable = false, length = 40)
    private String roleCode;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private AdminUserStatus status;

    @Column(name = "last_login_at")
    private LocalDateTime lastLoginAt;

    @Column(length = 500)
    private String description;

    public boolean isActive() {
        return status == AdminUserStatus.ACTIVE;
    }
}
