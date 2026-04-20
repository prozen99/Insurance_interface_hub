package com.insurancehub.admin.infrastructure.repository;

import java.util.Optional;

import com.insurancehub.admin.domain.entity.AdminUser;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AdminUserRepository extends JpaRepository<AdminUser, Long> {

    Optional<AdminUser> findByLoginId(String loginId);
}
