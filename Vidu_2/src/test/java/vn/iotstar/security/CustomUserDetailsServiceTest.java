package vn.iotstar.security;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.verify;
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
    void loadsSameAccountByUsernameOrEmailQuery() {
        User user = user("user", "user@example.com", true);
        when(userRepository.findByUsernameOrEmail("user", "user")).thenReturn(Optional.of(user));

        var details = (CustomUserDetails) service.loadUserByUsername("user");

        assertThat(details.getUsername()).isEqualTo("user");
        assertThat(details.getEmail()).isEqualTo("user@example.com");
        assertThat(details.getFullName()).isEqualTo("Người dùng thử nghiệm");
        verify(userRepository).findByUsernameOrEmail("user", "user");
    }

    @Test
    void preservesDisabledState() {
        User user = user("disabled", "disabled@example.com", false);
        when(userRepository.findByUsernameOrEmail("disabled@example.com", "disabled@example.com"))
                .thenReturn(Optional.of(user));

        assertThat(service.loadUserByUsername("disabled@example.com").isEnabled()).isFalse();
    }

    @Test
    void rejectsUnknownLoginWithRequiredMessage() {
        when(userRepository.findByUsernameOrEmail("missing", "missing")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.loadUserByUsername("missing"))
                .isInstanceOf(UsernameNotFoundException.class)
                .hasMessage("Không tìm thấy username/email: missing");
    }

    private User user(String username, String email, boolean enabled) {
        User user = new User();
        user.setId(1L);
        user.setUsername(username);
        user.setEmail(email);
        user.setPassword("$2a$10$encodedPasswordValue");
        user.setFullName("Người dùng thử nghiệm");
        user.setImages(null);
        user.setEnabled(enabled);
        user.setRole(new Role("USER"));
        return user;
    }
}
