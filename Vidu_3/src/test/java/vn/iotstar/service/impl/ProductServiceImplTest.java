package vn.iotstar.service.impl;

import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mapstruct.factory.Mappers;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.access.AccessDeniedException;
import vn.iotstar.dto.ProductDTO;
import vn.iotstar.entity.*;
import vn.iotstar.mapper.ProductMapper;
import vn.iotstar.repository.*;
import vn.iotstar.service.CloudinaryService;
import java.math.BigDecimal;
import java.util.Optional;
import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ProductServiceImplTest {
    @Mock ProductRepository products; @Mock UserRepository users; @Mock CloudinaryService cloudinary;
    ProductServiceImpl service;
    User owner;

    @BeforeEach void setUp() {
        ProductMapper mapper = Mappers.getMapper(ProductMapper.class);
        service = new ProductServiceImpl(products, users, mapper, cloudinary);
        owner = User.builder().id(7L).username("owner").build();
    }

    @Test void createAlwaysUsesAuthenticatedOwnerNotPostedUserId() {
        ProductDTO dto = new ProductDTO(); dto.setName("Sản phẩm Việt"); dto.setDescription("Mô tả"); dto.setPrice(BigDecimal.TEN); dto.setUserId(999L);
        when(users.findByUsername("owner")).thenReturn(Optional.of(owner));
        when(products.save(any())).thenAnswer(i -> { Product p=i.getArgument(0); p.setId(1L); return p; });
        ProductDTO result = service.create(dto, null, "owner");
        assertThat(result.getUserId()).isEqualTo(7L); assertThat(result.getUsername()).isEqualTo("owner");
    }

    @Test void nonOwnerCannotReadUpdateOrDelete() {
        Product product = Product.builder().id(1L).name("A").price(BigDecimal.ONE).user(owner).build();
        when(products.findById(1L)).thenReturn(Optional.of(product));
        assertThatThrownBy(() -> service.findById(1L, "other", false)).isInstanceOf(AccessDeniedException.class);
        assertThatThrownBy(() -> service.delete(1L, "other", false)).isInstanceOf(AccessDeniedException.class);
        verify(products, never()).delete(any());
    }

    @Test void adminCanAccessAnotherUsersProduct() {
        Product product = Product.builder().id(1L).name("A").price(BigDecimal.ONE).user(owner).build();
        when(products.findById(1L)).thenReturn(Optional.of(product));
        assertThat(service.findById(1L, "admin", true).getId()).isEqualTo(1L);
    }
}
