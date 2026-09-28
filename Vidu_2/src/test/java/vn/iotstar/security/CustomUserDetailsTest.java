package vn.iotstar.security;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;

class CustomUserDetailsTest {

    @Test
    void exposesProfileAndPrefixesPlainRoleOnce() {
        var details = new CustomUserDetails(
                1L, "user", "user@example.com", "$2a$10$hash",
                "Người dùng thử nghiệm", null, "USER", true);

        assertThat(details.getFullName()).isEqualTo("Người dùng thử nghiệm");
        assertThat(details.getImages()).isNull();
        assertThat(details.getEmail()).isEqualTo("user@example.com");
        assertThat(details.getAuthorities()).extracting("authority").containsExactly("ROLE_USER");
    }

    @Test
    void doesNotDoublePrefixRole() {
        var details = new CustomUserDetails(
                2L, "admin", "admin@example.com", "$2a$10$hash",
                "Quản trị viên", "/avatar.png", "ROLE_ADMIN", true);

        assertThat(details.getAuthorities()).extracting("authority").containsExactly("ROLE_ADMIN");
    }
}
