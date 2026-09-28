package vn.iotstar.config;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;

class PasswordEncoderTest {

    @Test
    void usesBcryptInsteadOfPlaintext() {
        var encoder = new SecurityConfig().passwordEncoder();
        String encoded = encoder.encode("mat-khau-thu-nghiem");

        assertThat(encoded).isNotEqualTo("mat-khau-thu-nghiem");
        assertThat(encoded).startsWith("$2");
        assertThat(encoder.matches("mat-khau-thu-nghiem", encoded)).isTrue();
    }
}
