package vn.iotstar;

import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.Page;
import org.springframework.transaction.annotation.Transactional;
import vn.iotstar.dto.*;
import vn.iotstar.entity.*;
import vn.iotstar.repository.*;
import vn.iotstar.service.UserService;
import java.math.BigDecimal;
import static org.assertj.core.api.Assertions.*;

@SpringBootTest @Transactional
class UserProductIntegrationTest {
    @Autowired UserService userService; @Autowired RoleRepository roles; @Autowired UserRepository users; @Autowired ProductRepository products;

    @BeforeEach void seedRole() { if (roles.findByName("USER").isEmpty()) roles.save(Role.builder().name("USER").build()); }

    @Test void userCrudSearchPaginationCountAndUnicodeRelationshipWork() {
        AdminUserCreateDTO create = new AdminUserCreateDTO(); create.setUsername("CODEX_TEST_user"); create.setEmail("codex_test@example.com");
        create.setFullName("Nguyễn Hải Đăng"); create.setPassword("Password123"); create.setConfirmPassword("Password123"); create.setRoleName("ROLE_USER"); create.setEnabled(true);
        UserDTO saved = userService.create(create);
        assertThat(saved.getId()).isNotNull(); assertThat(saved.getFullName()).isEqualTo("Nguyễn Hải Đăng"); assertThat(saved.getRoleName()).isEqualTo("ROLE_USER");

        User entity = users.findById(saved.getId()).orElseThrow();
        products.save(Product.builder().name("Cà phê sữa đá").description("Mô tả tiếng Việt").price(new BigDecimal("25000.00")).user(entity).build());
        Page<UserDTO> page = userService.findAll("Hải Đăng", 0, 1);
        assertThat(page.getTotalElements()).isEqualTo(1); assertThat(page.getContent().get(0).getProductCount()).isEqualTo(1);
        assertThat(products.searchOwned("CODEX_TEST_user", "Cà phê", org.springframework.data.domain.PageRequest.of(0, 10))).hasSize(1);

        saved.setFullName("Nguyễn Hải Đăng cập nhật"); saved.setEnabled(false); saved.setRoleName("ROLE_USER");
        assertThat(userService.update(saved.getId(), saved).getFullName()).contains("cập nhật");
        assertThat(userService.countUsers()).isPositive();
        assertThatThrownBy(() -> userService.delete(saved.getId(), "someone_else")).isInstanceOf(IllegalArgumentException.class).hasMessageContaining("sản phẩm");
    }

    @Test void adminCreateRejectsDuplicateUsernameAndEmail() {
        AdminUserCreateDTO dto = new AdminUserCreateDTO(); dto.setUsername("CODEX_TEST_duplicate"); dto.setEmail("duplicate@example.com");
        dto.setFullName("Trùng dữ liệu"); dto.setPassword("Password123"); dto.setConfirmPassword("Password123"); dto.setRoleName("ROLE_USER");
        userService.create(dto);
        assertThatThrownBy(() -> userService.create(dto)).isInstanceOf(IllegalArgumentException.class).hasMessageContaining("Username");
    }
}
