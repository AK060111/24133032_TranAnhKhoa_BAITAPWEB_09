package vn.iotstar;

import jakarta.validation.*;
import org.junit.jupiter.api.*;
import vn.iotstar.dto.RegisterDTO;
import static org.assertj.core.api.Assertions.*;

class DtoValidationTest {
    Validator validator;
    @BeforeEach void setUp() { validator = Validation.buildDefaultValidatorFactory().getValidator(); }

    @Test void registerValidationRejectsBlankAndMalformedValues() {
        RegisterDTO dto = new RegisterDTO(); dto.setUsername(""); dto.setEmail("not-email"); dto.setFullName(""); dto.setPassword("short"); dto.setConfirmPassword("");
        assertThat(validator.validate(dto)).extracting(ConstraintViolation::getPropertyPath).extracting(Object::toString)
                .contains("username", "email", "fullName", "password", "confirmPassword");
    }
}
