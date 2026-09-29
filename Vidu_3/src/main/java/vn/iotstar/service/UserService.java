package vn.iotstar.service;
import org.springframework.data.domain.Page;
import vn.iotstar.dto.*;
public interface UserService {
    Page<UserDTO> findAll(String keyword, int page, int size);
    UserDTO findById(Long id);
    UserDTO create(AdminUserCreateDTO dto);
    UserDTO update(Long id, UserDTO dto);
    void delete(Long id, String currentUsername);
    long countUsers();
    long countProducts(Long userId);
}
