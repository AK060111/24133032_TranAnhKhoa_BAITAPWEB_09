package vn.iotstar.mapper;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;
import org.mapstruct.factory.Mappers;
import vn.iotstar.entity.Role;
import vn.iotstar.entity.User;

class UserMapperTest {

    private final UserMapper mapper = Mappers.getMapper(UserMapper.class);

    @Test
    void mapsRoleNameAndNeverExposesPassword() {
        Role role = new Role("ADMIN");
        User user = new User();
        user.setId(7L);
        user.setUsername("admin");
        user.setEmail("admin@example.com");
        user.setPassword("secret-hash");
        user.setFullName("Quản trị viên");
        user.setImages("/images/admin.png");
        user.setEnabled(true);
        user.setRole(role);

        var dto = mapper.toDto(user);

        assertThat(dto.getUsername()).isEqualTo("admin");
        assertThat(dto.getRoleName()).isEqualTo("ADMIN");
        assertThat(dto.getFullName()).isEqualTo("Quản trị viên");
        assertThat(dto.getClass().getDeclaredFields())
                .extracting("name")
                .doesNotContain("password");
    }
}
