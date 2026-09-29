package vn.iotstar;

import org.junit.jupiter.api.*;
import org.mockito.ArgumentCaptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;
import vn.iotstar.entity.Role;
import vn.iotstar.repository.RoleRepository;
import vn.iotstar.service.EmailService;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest @AutoConfigureMockMvc @Transactional
class AuthFlowIntegrationTest {
    @Autowired MockMvc mvc;
    @Autowired RoleRepository roles;
    @MockitoBean EmailService emailService;

    @BeforeEach void seedRole() {
        if (roles.findByName("USER").isEmpty()) roles.save(Role.builder().name("USER").build());
    }

    @Test void registerResendVerifyLoginForgotResetAndLoginAgain() throws Exception {
        String email = "codex.flow@example.com";
        mvc.perform(post("/register").with(csrf())
                .param("username", "CODEX_TEST_AUTH_FLOW").param("email", email).param("fullName", "Luồng xác thực")
                .param("password", "Password123").param("confirmPassword", "Password123"))
                .andExpect(status().is3xxRedirection());

        mvc.perform(post("/resend-register-otp").with(csrf()).param("email", email))
                .andExpect(status().is3xxRedirection());
        ArgumentCaptor<String> codes = ArgumentCaptor.forClass(String.class);
        verify(emailService, times(2)).sendOtp(eq(email), codes.capture(), anyString());
        String registerOtp = codes.getAllValues().get(1);

        mvc.perform(post("/verify-otp").with(csrf()).param("email", email).param("otp", registerOtp))
                .andExpect(status().is3xxRedirection()).andExpect(redirectedUrl("/login"));
        mvc.perform(post("/login").with(csrf()).param("username", "CODEX_TEST_AUTH_FLOW").param("password", "Password123"))
                .andExpect(status().is3xxRedirection()).andExpect(redirectedUrl("/"));

        clearInvocations(emailService);
        mvc.perform(post("/forgot-password").with(csrf()).param("email", email))
                .andExpect(status().is3xxRedirection()).andExpect(redirectedUrl("/reset-password"));
        ArgumentCaptor<String> resetCode = ArgumentCaptor.forClass(String.class);
        verify(emailService).sendOtp(eq(email), resetCode.capture(), anyString());
        String resetOtp = resetCode.getValue();

        mvc.perform(post("/reset-password").with(csrf()).param("email", email).param("otp", resetOtp)
                .param("password", "ChangedPassword123").param("confirmPassword", "ChangedPassword123"))
                .andExpect(status().is3xxRedirection()).andExpect(redirectedUrl("/login"));
        mvc.perform(post("/login").with(csrf()).param("username", "CODEX_TEST_AUTH_FLOW").param("password", "ChangedPassword123"))
                .andExpect(status().is3xxRedirection()).andExpect(redirectedUrl("/"));
    }
}
