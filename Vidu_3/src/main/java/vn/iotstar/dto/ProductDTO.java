package vn.iotstar.dto;

import jakarta.validation.constraints.*;
import lombok.Data;
import org.springframework.web.multipart.MultipartFile;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
public class ProductDTO {
    private Long id;
    @NotBlank(message = "Tên sản phẩm không được để trống")
    @Size(max = 500, message = "Tên sản phẩm tối đa 500 ký tự")
    private String name;
    @Size(max = 2000, message = "Mô tả tối đa 2000 ký tự")
    private String description;
    @NotNull(message = "Giá không được để trống")
    @DecimalMin(value = "0.0", inclusive = true, message = "Giá phải lớn hơn hoặc bằng 0")
    private BigDecimal price;
    private String imageUrl;
    private Long userId;
    private String username;
    private LocalDateTime createdAt;
    private MultipartFile image;
}
