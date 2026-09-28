package vn.iotstar.integration;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.redirectedUrl;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.test.web.servlet.MockMvc;
import vn.iotstar.security.CustomUserDetails;

@SpringBootTest
@AutoConfigureMockMvc
class SecurityWebTest {

    @Autowired
    private MockMvc mockMvc;

    @Test
    void anonymousUserIsRedirectedToLogin() throws Exception {
        mockMvc.perform(get("/"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/login"));
    }

    @Test
    void userCanRenderHeaderWithUnicodeNameAndNullImage() throws Exception {
        mockMvc.perform(get("/").with(user(principal("USER", null))))
                .andExpect(status().isOk())
                .andExpect(content().string(org.hamcrest.Matchers.containsString("Người dùng thử nghiệm")))
                .andExpect(content().string(org.hamcrest.Matchers.containsString("avatar-placeholder")));
    }

    @Test
    void userCannotAccessAdminButAdminCan() throws Exception {
        mockMvc.perform(get("/admin").with(user(principal("USER", null))))
                .andExpect(status().isForbidden());

        mockMvc.perform(get("/admin").with(user(principal("ADMIN", "/images/admin.png"))))
                .andExpect(status().isOk())
                .andExpect(content().string(org.hamcrest.Matchers.containsString("Khu vực quản trị")));
    }

    @Test
    void loginPostRequiresCsrf() throws Exception {
        mockMvc.perform(post("/login").param("login", "user").param("password", "wrong"))
                .andExpect(status().isForbidden());
    }

    @Test
    void logoutUsesPostAndRedirects() throws Exception {
        mockMvc.perform(post("/logout")
                        .with(user(principal("USER", null)))
                        .with(csrf()))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/login?logout=true"));
    }

    private CustomUserDetails principal(String role, String images) {
        return new CustomUserDetails(
                1L, role.toLowerCase(), role.toLowerCase() + "@example.com", "$2a$10$hash",
                role.equals("ADMIN") ? "Quản trị viên" : "Người dùng thử nghiệm",
                images, role, true);
    }
}
