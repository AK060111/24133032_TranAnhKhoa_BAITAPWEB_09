package vn.iotstar.service.impl;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.*;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import vn.iotstar.dto.*;
import vn.iotstar.entity.*;
import vn.iotstar.mapper.UserMapper;
import vn.iotstar.repository.*;
import vn.iotstar.service.UserService;

@Service @RequiredArgsConstructor
public class UserServiceImpl implements UserService {
    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final UserMapper mapper;
    private final PasswordEncoder passwordEncoder;

    @Override @Transactional(readOnly = true)
    public Page<UserDTO> findAll(String keyword, int page, int size) {
        Pageable pageable = PageRequest.of(Math.max(page, 0), Math.min(Math.max(size, 1), 100), Sort.by(Sort.Direction.DESC, "id"));
        return userRepository.search(keyword == null ? "" : keyword.trim(), pageable).map(this::toDtoWithCount);
    }

    private UserDTO toDtoWithCount(User user) {
        UserDTO dto = mapper.toDTO(user);
        dto.setProductCount(userRepository.countProductsByUserId(user.getId()));
        return dto;
    }

    @Override @Transactional(readOnly = true)
    public UserDTO findById(Long id) { return toDtoWithCount(requireUser(id)); }

    @Override @Transactional
    public UserDTO create(AdminUserCreateDTO dto) {
        if (userRepository.existsByUsername(dto.getUsername())) throw new IllegalArgumentException("Username đã tồn tại");
        if (userRepository.existsByEmail(dto.getEmail())) throw new IllegalArgumentException("Email đã tồn tại");
        if (!dto.getPassword().equals(dto.getConfirmPassword())) throw new IllegalArgumentException("Mật khẩu xác nhận không đúng");
        Role role = requireRole(dto.getRoleName());
        User user = User.builder().username(dto.getUsername().trim()).email(dto.getEmail().trim().toLowerCase())
                .fullName(dto.getFullName().trim()).password(passwordEncoder.encode(dto.getPassword()))
                .enabled(dto.isEnabled()).role(role).build();
        return toDtoWithCount(userRepository.save(user));
    }

    @Override @Transactional
    public UserDTO update(Long id, UserDTO dto) {
        User user = requireUser(id);
        if (userRepository.existsByUsernameAndIdNot(dto.getUsername(), id)) throw new IllegalArgumentException("Username đã tồn tại");
        if (userRepository.existsByEmailAndIdNot(dto.getEmail(), id)) throw new IllegalArgumentException("Email đã tồn tại");
        user.setUsername(dto.getUsername().trim()); user.setEmail(dto.getEmail().trim().toLowerCase());
        user.setFullName(dto.getFullName().trim()); user.setEnabled(dto.isEnabled()); user.setRole(requireRole(dto.getRoleName()));
        return toDtoWithCount(user);
    }

    @Override @Transactional
    public void delete(Long id, String currentUsername) {
        User user = requireUser(id);
        if (user.getUsername().equals(currentUsername)) throw new IllegalArgumentException("Không thể xóa tài khoản đang đăng nhập");
        if (userRepository.countProductsByUserId(id) > 0) throw new IllegalArgumentException("Hãy xóa sản phẩm của user trước khi xóa user");
        userRepository.delete(user);
    }

    private User requireUser(Long id) { return userRepository.findById(id).orElseThrow(() -> new IllegalArgumentException("User không tồn tại")); }
    private Role requireRole(String roleName) {
        if (!"ROLE_USER".equals(roleName) && !"ROLE_ADMIN".equals(roleName)) throw new IllegalArgumentException("Role không hợp lệ");
        return roleRepository.findByName(roleName)
                .or(() -> roleRepository.findByName(roleName.substring("ROLE_".length())))
                .orElseThrow(() -> new IllegalArgumentException("Role không tồn tại"));
    }
    @Override public long countUsers() { return userRepository.count(); }
    @Override public long countProducts(Long userId) { return userRepository.countProductsByUserId(userId); }
}
