package vn.iotstar.dto;

import jakarta.validation.constraints.*;
import lombok.Data;

@Data
public class UserDTO {
    private Long id;
    @NotBlank(message = "Username không được để trống")
    @Size(max = 50, message = "Username tối đa 50 ký tự")
    private String username;
    @NotBlank(message = "Email không được để trống")
    @Email(message = "Email không hợp lệ")
    @Size(max = 120, message = "Email tối đa 120 ký tự")
    private String email;
    @NotBlank(message = "Họ tên không được để trống")
    @Size(max = 200, message = "Họ tên tối đa 200 ký tự")
    private String fullName;
    private boolean enabled;
    private String roleName;
    private long productCount;
}
