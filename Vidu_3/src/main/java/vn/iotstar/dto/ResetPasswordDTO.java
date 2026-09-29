package vn.iotstar.dto;
import jakarta.validation.constraints.*;
import lombok.Data;
@Data public class ResetPasswordDTO {
    @NotBlank @Email @Size(max = 120) private String email;
    @NotBlank @Pattern(regexp = "\\d{6}", message = "OTP phải gồm đúng 6 chữ số") private String otp;
    @NotBlank @Size(min = 8, max = 72, message = "Mật khẩu phải từ 8 đến 72 ký tự") private String password;
    @NotBlank private String confirmPassword;
}
