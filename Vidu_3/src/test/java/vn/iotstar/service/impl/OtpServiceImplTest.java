package vn.iotstar.service.impl;

import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import vn.iotstar.entity.OtpToken;
import vn.iotstar.repository.OtpTokenRepository;
import vn.iotstar.service.EmailService;
import java.time.LocalDateTime;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicReference;
import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class OtpServiceImplTest {
    @Mock OtpTokenRepository repository; @Mock EmailService emailService;
    BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();
    OtpServiceImpl service; AtomicReference<OtpToken> stored;

    @BeforeEach void setUp() {
        stored = new AtomicReference<>();
        when(repository.save(any())).thenAnswer(i -> { stored.set(i.getArgument(0)); return i.getArgument(0); });
        when(repository.findTopByEmailAndTypeAndUsedFalseOrderByCreatedAtDesc(anyString(), anyString())).thenAnswer(i -> Optional.ofNullable(stored.get()));
        service = new OtpServiceImpl(repository, encoder, emailService);
    }

    private String issueOtp() {
        service.sendRegisterOtp("codex@example.com");
        ArgumentCaptor<String> otp = ArgumentCaptor.forClass(String.class);
        verify(emailService).sendOtp(eq("codex@example.com"), otp.capture(), anyString());
        assertThat(otp.getValue()).matches("\\d{6}");
        assertThat(stored.get().getOtpHash()).doesNotContain(otp.getValue());
        return otp.getValue();
    }

    @Test void otpIsHashedValidAndOneTime() {
        String otp = issueOtp();
        assertThat(service.verifyRegisterOtp("codex@example.com", otp)).isTrue();
        assertThat(stored.get().isUsed()).isTrue();
        assertThat(service.verifyRegisterOtp("codex@example.com", otp)).isFalse();
    }

    @Test void invalidOtpIncrementsAttemptsAndStopsAtFive() {
        issueOtp();
        for (int i = 0; i < 5; i++) assertThat(service.verifyRegisterOtp("codex@example.com", "999999")).isFalse();
        assertThat(stored.get().getAttempts()).isEqualTo(5);
        assertThat(service.verifyRegisterOtp("codex@example.com", "999999")).isFalse();
        assertThat(stored.get().getAttempts()).isEqualTo(5);
    }

    @Test void expiredOtpIsRejected() {
        String otp = issueOtp(); stored.get().setExpiresAt(LocalDateTime.now().minusSeconds(1));
        assertThat(service.verifyRegisterOtp("codex@example.com", otp)).isFalse();
    }
}
