package com.insurancehub.admin.application;

import java.util.List;

import com.insurancehub.admin.domain.entity.AdminUser;
import com.insurancehub.admin.infrastructure.repository.AdminUserRepository;
import org.springframework.security.authentication.DisabledException;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

@Service
public class AdminUserDetailsService implements UserDetailsService {

    private final AdminUserRepository adminUserRepository;

    public AdminUserDetailsService(AdminUserRepository adminUserRepository) {
        this.adminUserRepository = adminUserRepository;
    }

    @Override
    @Transactional(readOnly = true)
    public UserDetails loadUserByUsername(String username) {
        AdminUser adminUser = adminUserRepository.findByLoginId(username)
                .orElseThrow(() -> new UsernameNotFoundException("Admin user not found"));

        if (!adminUser.isActive()) {
            throw new DisabledException("Admin user is not active");
        }
        if (!StringUtils.hasText(adminUser.getPasswordHash())) {
            throw new DisabledException("Admin user has no password configured");
        }

        return User.withUsername(adminUser.getLoginId())
                .password(adminUser.getPasswordHash())
                .authorities(List.of(new SimpleGrantedAuthority(adminUser.getRoleCode())))
                .build();
    }
}
