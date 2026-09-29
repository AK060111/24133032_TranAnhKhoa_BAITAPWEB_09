package vn.iotstar.dto;

import jakarta.validation.constraints.*;
import lombok.Data;

@Data
public class RegisterDTO {
    @NotBlank(message = "Username không được để trống") @Size(max = 50) private String username;
    @NotBlank(message = "Email không được để trống") @Email(message = "Email không hợp lệ") @Size(max = 120) private String email;
    @NotBlank(message = "Mật khẩu không được để trống") @Size(min = 8, max = 72, message = "Mật khẩu phải từ 8 đến 72 ký tự") private String password;
    @NotBlank(message = "Vui lòng xác nhận mật khẩu") private String confirmPassword;
    @NotBlank(message = "Họ tên không được để trống") @Size(max = 200) private String fullName;
}
