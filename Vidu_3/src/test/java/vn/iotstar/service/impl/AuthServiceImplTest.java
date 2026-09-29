package vn.iotstar.service.impl;

import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import vn.iotstar.dto.RegisterDTO;
import vn.iotstar.entity.*;
import vn.iotstar.repository.*;
import vn.iotstar.service.OtpService;
import java.util.Optional;
import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AuthServiceImplTest {
    @Mock UserRepository users; @Mock RoleRepository roles; @Mock OtpService otp;
    @Captor ArgumentCaptor<User> userCaptor;
    BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();
    AuthServiceImpl service;

    @BeforeEach void setUp() { service = new AuthServiceImpl(users, roles, encoder, otp); }

    private RegisterDTO validDto() {
        RegisterDTO d = new RegisterDTO(); d.setUsername("codex_user"); d.setEmail("codex@example.com");
        d.setFullName("Người dùng thử nghiệm"); d.setPassword("SafePass123"); d.setConfirmPassword("SafePass123"); return d;
    }

    @Test void registerHashesPasswordAssignsUserRoleAndStartsDisabled() {
        when(roles.findByName("ROLE_USER")).thenReturn(Optional.empty());
        when(roles.findByName("USER")).thenReturn(Optional.of(Role.builder().id(1L).name("USER").build()));
        when(users.save(any())).thenAnswer(i -> i.getArgument(0));
        service.register(validDto());
        verify(users).save(userCaptor.capture()); User saved = userCaptor.getValue();
        assertThat(saved.isEnabled()).isFalse(); assertThat(saved.getRole().getName()).isEqualTo("USER");
        assertThat(saved.getPassword()).isNotEqualTo("SafePass123"); assertThat(encoder.matches("SafePass123", saved.getPassword())).isTrue();
        verify(otp).sendRegisterOtp("codex@example.com");
    }

    @Test void registerRejectsDuplicateUsernameAndEmail() {
        RegisterDTO dto = validDto(); when(users.existsByUsername(dto.getUsername())).thenReturn(true);
        assertThatThrownBy(() -> service.register(dto)).isInstanceOf(IllegalArgumentException.class).hasMessageContaining("Username");
        reset(users); when(users.existsByEmail(dto.getEmail())).thenReturn(true);
        assertThatThrownBy(() -> service.register(dto)).isInstanceOf(IllegalArgumentException.class).hasMessageContaining("Email");
    }

    @Test void registerRejectsPasswordConfirmation() {
        RegisterDTO dto = validDto(); dto.setConfirmPassword("different");
        assertThatThrownBy(() -> service.register(dto)).isInstanceOf(IllegalArgumentException.class).hasMessageContaining("xác nhận");
    }

    @Test void successfulVerificationEnablesUser() {
        User user = User.builder().email("codex@example.com").enabled(false).build();
        when(otp.verifyRegisterOtp(user.getEmail(), "123456")).thenReturn(true); when(users.findByEmail(user.getEmail())).thenReturn(Optional.of(user));
        assertThat(service.verifyRegister(user.getEmail(), "123456")).isTrue(); assertThat(user.isEnabled()).isTrue();
    }

    @Test void forgotPasswordRequiresExistingEmailAndSendsResetOtp() {
        when(users.existsByEmail("codex@example.com")).thenReturn(true);
        service.forgotPassword("codex@example.com");
        verify(otp).sendResetPasswordOtp("codex@example.com");
        assertThatThrownBy(() -> service.forgotPassword("missing@example.com")).isInstanceOf(IllegalArgumentException.class);
    }

    @Test void resetPasswordRequiresValidOtpAndStoresBcrypt() {
        User user = User.builder().email("codex@example.com").password("old").build();
        when(users.findByEmail(user.getEmail())).thenReturn(Optional.of(user));
        when(otp.verifyResetPasswordOtp(user.getEmail(), "123456")).thenReturn(true);
        service.resetPassword(user.getEmail(), "123456", "NewPassword123");
        assertThat(encoder.matches("NewPassword123", user.getPassword())).isTrue();
        when(otp.verifyResetPasswordOtp(user.getEmail(), "000000")).thenReturn(false);
        assertThatThrownBy(() -> service.resetPassword(user.getEmail(), "000000", "AnotherPassword123"))
                .isInstanceOf(IllegalArgumentException.class).hasMessageContaining("OTP");
    }
}
