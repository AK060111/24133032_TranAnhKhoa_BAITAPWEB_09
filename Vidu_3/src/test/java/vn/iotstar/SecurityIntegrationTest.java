package vn.iotstar;

import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.mock.web.MockHttpSession;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;
import vn.iotstar.entity.*;
import vn.iotstar.repository.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest @AutoConfigureMockMvc @Transactional
class SecurityIntegrationTest {
    @Autowired MockMvc mvc; @Autowired RoleRepository roles; @Autowired UserRepository users; @Autowired PasswordEncoder encoder;

    @BeforeEach void seed() {
        if (users.findByUsername("security_user").isPresent()) return;
        Role userRole = roles.save(Role.builder().name("USER").build());
        Role adminRole = roles.save(Role.builder().name("ADMIN").build());
        users.save(User.builder().username("security_user").email("user@test.local").fullName("Người dùng").password(encoder.encode("Password123")).enabled(true).role(userRole).build());
        users.save(User.builder().username("security_admin").email("admin@test.local").fullName("Quản trị").password(encoder.encode("Password123")).enabled(true).role(adminRole).build());
        users.save(User.builder().username("disabled_user").email("disabled@test.local").fullName("Chưa kích hoạt").password(encoder.encode("Password123")).enabled(false).role(userRole).build());
    }

    @Test void publicPagesAreAccessibleAndProtectedPagesRedirect() throws Exception {
        mvc.perform(get("/login")).andExpect(status().isOk()).andExpect(content().contentType("text/html;charset=UTF-8"));
        mvc.perform(get("/register")).andExpect(status().isOk());
        mvc.perform(get("/products")).andExpect(status().is3xxRedirection()).andExpect(redirectedUrl("/login"));
        mvc.perform(get("/users")).andExpect(status().is3xxRedirection()).andExpect(redirectedUrl("/login"));
    }

    @Test void roleUserCanUseProductsButCannotUseUsers() throws Exception {
        mvc.perform(get("/products").with(user("security_user").roles("USER"))).andExpect(status().isOk());
        mvc.perform(get("/users").with(user("security_user").roles("USER"))).andExpect(status().isForbidden());
    }

    @Test void roleAdminCanUseUsersAndProducts() throws Exception {
        mvc.perform(get("/users").with(user("security_admin").roles("ADMIN"))).andExpect(status().isOk());
        mvc.perform(get("/products").with(user("security_admin").roles("ADMIN"))).andExpect(status().isOk());
    }

    @Test void csrfRejectsMissingTokenAndAcceptsValidLogout() throws Exception {
        mvc.perform(post("/logout").with(user("security_user").roles("USER"))).andExpect(status().isForbidden());
        mvc.perform(post("/logout").with(user("security_user").roles("USER")).with(csrf())).andExpect(status().is3xxRedirection()).andExpect(redirectedUrl("/login?logout=true"));
    }

    @Test void databaseLoginCreatesSessionAndLogoutInvalidatesAccess() throws Exception {
        MockHttpSession session = (MockHttpSession) mvc.perform(post("/login").with(csrf()).param("username", "security_user").param("password", "Password123"))
                .andExpect(status().is3xxRedirection()).andExpect(redirectedUrl("/")).andReturn().getRequest().getSession(false);
        mvc.perform(get("/products").session(session)).andExpect(status().isOk());
        mvc.perform(post("/logout").session(session).with(csrf())).andExpect(status().is3xxRedirection());
        mvc.perform(get("/products").session(session)).andExpect(status().is3xxRedirection());
    }

    @Test void disabledUserCannotLogin() throws Exception {
        mvc.perform(post("/login").with(csrf()).param("username", "disabled_user").param("password", "Password123"))
                .andExpect(status().is3xxRedirection()).andExpect(redirectedUrl("/login?error=true"));
    }
}
