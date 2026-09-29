package vn.iotstar.dto;

import jakarta.validation.constraints.*;
import lombok.Data;

@Data
public class AdminUserCreateDTO {
    @NotBlank @Size(max = 50) private String username;
    @NotBlank @Email @Size(max = 120) private String email;
    @NotBlank @Size(max = 200) private String fullName;
    @NotBlank @Size(min = 8, max = 72, message = "Mật khẩu phải từ 8 đến 72 ký tự") private String password;
    @NotBlank private String confirmPassword;
    @Pattern(regexp = "ROLE_USER|ROLE_ADMIN", message = "Role không hợp lệ")
    private String roleName = "ROLE_USER";
    private boolean enabled = true;
}
