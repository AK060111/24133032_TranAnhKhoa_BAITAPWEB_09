package vn.iotstar.service.impl;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.*;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import vn.iotstar.dto.ProductDTO;
import vn.iotstar.entity.*;
import vn.iotstar.mapper.ProductMapper;
import vn.iotstar.repository.*;
import vn.iotstar.service.*;

@Service @RequiredArgsConstructor
public class ProductServiceImpl implements ProductService {
    private final ProductRepository productRepository;
    private final UserRepository userRepository;
    private final ProductMapper mapper;
    private final CloudinaryService cloudinaryService;

    @Override @Transactional(readOnly = true)
    public Page<ProductDTO> findAll(String keyword, int page, int size, String username, boolean admin) {
        Pageable pageable = PageRequest.of(Math.max(page, 0), Math.min(Math.max(size, 1), 100), Sort.by(Sort.Direction.DESC, "id"));
        String q = keyword == null ? "" : keyword.trim();
        return (admin ? productRepository.search(q, pageable) : productRepository.searchOwned(username, q, pageable)).map(mapper::toDTO);
    }

    @Override @Transactional(readOnly = true)
    public ProductDTO findById(Long id, String username, boolean admin) {
        Product product = requireProduct(id);
        checkOwnership(product, username, admin);
        return mapper.toDTO(product);
    }

    @Override @Transactional
    public ProductDTO create(ProductDTO dto, MultipartFile image, String username) {
        User owner = userRepository.findByUsername(username).orElseThrow(() -> new IllegalArgumentException("User không tồn tại"));
        Product product = mapper.toEntity(dto);
        product.setUser(owner);
        CloudinaryUploadResult uploaded = null;
        try {
            if (image != null && !image.isEmpty()) {
                uploaded = cloudinaryService.upload(image);
                product.setImageUrl(uploaded.url()); product.setImagePublicId(uploaded.publicId());
            }
            return mapper.toDTO(productRepository.save(product));
        } catch (RuntimeException ex) {
            if (uploaded != null) safeDelete(uploaded.publicId());
            throw ex;
        }
    }

    @Override @Transactional
    public ProductDTO update(Long id, ProductDTO dto, MultipartFile image, String username, boolean admin) {
        Product product = requireProduct(id);
        checkOwnership(product, username, admin);
        product.setName(dto.getName().trim()); product.setDescription(dto.getDescription()); product.setPrice(dto.getPrice());
        if (image == null || image.isEmpty()) return mapper.toDTO(product);

        String oldPublicId = product.getImagePublicId();
        CloudinaryUploadResult uploaded = cloudinaryService.upload(image);
        try {
            product.setImageUrl(uploaded.url()); product.setImagePublicId(uploaded.publicId());
            ProductDTO result = mapper.toDTO(productRepository.save(product));
            safeDelete(oldPublicId);
            return result;
        } catch (RuntimeException ex) {
            safeDelete(uploaded.publicId());
            throw ex;
        }
    }

    @Override @Transactional
    public void delete(Long id, String username, boolean admin) {
        Product product = requireProduct(id);
        checkOwnership(product, username, admin);
        String publicId = product.getImagePublicId();
        productRepository.delete(product);
        productRepository.flush();
        safeDelete(publicId);
    }

    private Product requireProduct(Long id) { return productRepository.findById(id).orElseThrow(() -> new IllegalArgumentException("Product không tồn tại")); }
    private void checkOwnership(Product product, String username, boolean admin) {
        if (!admin && !product.getUser().getUsername().equals(username)) throw new AccessDeniedException("Bạn không có quyền sửa hoặc xóa sản phẩm này");
    }
    private void safeDelete(String publicId) {
        if (publicId == null || publicId.isBlank()) return;
        try { cloudinaryService.delete(publicId); } catch (RuntimeException ignored) { /* DB remains consistent; orphan can be cleaned manually. */ }
    }
    @Override public long countProducts(String username, boolean admin) {
        if (admin) return productRepository.count();
        return userRepository.findByUsername(username).map(u -> productRepository.countByUserId(u.getId())).orElse(0L);
    }
    @Override public long countByUser(Long userId) { return productRepository.countByUserId(userId); }
}
