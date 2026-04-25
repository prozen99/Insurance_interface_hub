package com.insurancehub.admin.presentation;

import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestBuilders.formLogin;
import static org.springframework.security.test.web.servlet.response.SecurityMockMvcResultMatchers.authenticated;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.redirectedUrl;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.redirectedUrlPattern;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.insurancehub.admin.application.AdminUserDetailsService;
import com.insurancehub.admin.application.DashboardService;
import com.insurancehub.config.SecurityConfig;
import com.insurancehub.monitoring.application.OperationsMonitoringService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

@WebMvcTest(DashboardController.class)
@Import(SecurityConfig.class)
class AdminSecurityAccessControlTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private AdminUserDetailsService adminUserDetailsService;

    @MockitoBean
    private DashboardService dashboardService;

    @MockitoBean
    private OperationsMonitoringService operationsMonitoringService;

    @Test
    void unauthenticatedAdminRequestRedirectsToLogin() throws Exception {
        mockMvc.perform(get("/admin"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrlPattern("**/login"));
    }

    @Test
    void validAdminCredentialsAuthenticateAndRedirectToDashboard() throws Exception {
        when(adminUserDetailsService.loadUserByUsername("admin"))
                .thenReturn(User.withUsername("admin")
                        .password(new BCryptPasswordEncoder().encode("admin123!"))
                        .authorities("ADMIN")
                        .build());

        mockMvc.perform(formLogin("/login").user("admin").password("admin123!"))
                .andExpect(status().is3xxRedirection())
                .andExpect(authenticated().withUsername("admin"))
                .andExpect(redirectedUrl("/admin"));
    }
}
