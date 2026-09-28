package vn.iotstar.integration;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;
import vn.iotstar.repository.UserRepository;

@SpringBootTest
class DatabaseReadSmokeTest {

    @Autowired
    private UserRepository userRepository;

    @Test
    @Transactional(readOnly = true)
    void readsExistingAccountsWithoutChangingThem() {
        var users = userRepository.findAll();

        assertThat(users).hasSizeGreaterThanOrEqualTo(3);
        assertThat(users).allSatisfy(user -> {
            assertThat(user.getUsername()).isNotBlank();
            assertThat(user.getEmail()).contains("@");
            assertThat(user.getPassword()).startsWith("$2").hasSize(60);
            assertThat(user.getFullName()).isNotBlank();
            assertThat(user.getRole().getName()).isIn("USER", "ADMIN");
        });
    }
}
