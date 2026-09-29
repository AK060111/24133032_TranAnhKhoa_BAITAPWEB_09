package vn.iotstar.dto;
import jakarta.validation.constraints.*;
import lombok.Data;
@Data public class ForgotPasswordDTO { @NotBlank @Email @Size(max = 120) private String email; }
