package vn.iotstar.security;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.when;

import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import vn.iotstar.entity.Role;
import vn.iotstar.entity.User;
import vn.iotstar.repository.UserRepository;

@ExtendWith(MockitoExtension.class)
class CustomUserDetailsServiceTest {

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private CustomUserDetailsService service;

    @Test
    void loadsEnabledUserByEmailWithRole() {
        Role role = new Role("ADMIN");
        User user = new User();
        user.setEmail("admin@example.com");
        user.setPassword("$2a$10$encodedPasswordValue");
        user.setEnabled(true);
        user.setRole(role);
        when(userRepository.findByEmailWithRole("admin@example.com")).thenReturn(Optional.of(user));

        var details = service.loadUserByUsername("admin@example.com");

        assertThat(details.getUsername()).isEqualTo("admin@example.com");
        assertThat(details.isEnabled()).isTrue();
        assertThat(details.getAuthorities()).extracting("authority").containsExactly("ROLE_ADMIN");
    }

    @Test
    void preservesDisabledState() {
        Role role = new Role("USER");
        User user = new User();
        user.setEmail("disabled@example.com");
        user.setPassword("$2a$10$encodedPasswordValue");
        user.setEnabled(false);
        user.setRole(role);
        when(userRepository.findByEmailWithRole("disabled@example.com")).thenReturn(Optional.of(user));

        assertThat(service.loadUserByUsername("disabled@example.com").isEnabled()).isFalse();
    }

    @Test
    void rejectsUnknownEmail() {
        when(userRepository.findByEmailWithRole("missing@example.com")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.loadUserByUsername("missing@example.com"))
                .isInstanceOf(UsernameNotFoundException.class)
                .hasMessage("Không tìm thấy tài khoản.");
    }
}
