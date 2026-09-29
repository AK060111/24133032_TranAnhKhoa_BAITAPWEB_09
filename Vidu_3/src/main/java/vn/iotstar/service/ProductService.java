package vn.iotstar.service;
import org.springframework.data.domain.Page;
import org.springframework.web.multipart.MultipartFile;
import vn.iotstar.dto.ProductDTO;
public interface ProductService {
    Page<ProductDTO> findAll(String keyword, int page, int size, String username, boolean admin);
    ProductDTO findById(Long id, String username, boolean admin);
    ProductDTO create(ProductDTO dto, MultipartFile image, String username);
    ProductDTO update(Long id, ProductDTO dto, MultipartFile image, String username, boolean admin);
    void delete(Long id, String username, boolean admin);
    long countProducts(String username, boolean admin);
    long countByUser(Long userId);
}
