package vn.iotstar.service.impl;

import com.cloudinary.Cloudinary;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import vn.iotstar.service.*;
import java.util.Map;

@Service @RequiredArgsConstructor
public class CloudinaryServiceImpl implements CloudinaryService {
    private static final long MAX_IMAGE_BYTES = 10L * 1024 * 1024;
    private final Cloudinary cloudinary;

    @Override
    public CloudinaryUploadResult upload(MultipartFile file) {
        if (file == null || file.isEmpty()) throw new IllegalArgumentException("Chưa chọn ảnh");
        if (file.getSize() > MAX_IMAGE_BYTES) throw new IllegalArgumentException("Ảnh không được vượt quá 10 MB");
        String type = file.getContentType();
        if (type == null || !type.startsWith("image/")) throw new IllegalArgumentException("Chỉ cho phép file hình ảnh");
        try {
            Map<?, ?> result = cloudinary.uploader().upload(file.getBytes(),
                    Map.of("folder", "shop/products", "resource_type", "image"));
            return new CloudinaryUploadResult(String.valueOf(result.get("secure_url")), String.valueOf(result.get("public_id")));
        } catch (Exception e) {
            throw new IllegalStateException("Upload Cloudinary thất bại", e);
        }
    }

    @Override
    public void delete(String publicId) {
        if (publicId == null || publicId.isBlank()) return;
        try {
            cloudinary.uploader().destroy(publicId, Map.of("resource_type", "image"));
        } catch (Exception e) {
            throw new IllegalStateException("Xóa ảnh Cloudinary thất bại", e);
        }
    }
}
