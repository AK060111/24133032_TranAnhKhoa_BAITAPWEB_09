package vn.iotstar.service.impl;

import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import vn.iotstar.dto.RegisterDTO;
import vn.iotstar.entity.*;
import vn.iotstar.repository.*;
import vn.iotstar.service.*;

@Service @RequiredArgsConstructor
public class AuthServiceImpl implements AuthService {
    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final PasswordEncoder passwordEncoder;
    private final OtpService otpService;

    @Override @Transactional
    public void register(RegisterDTO dto) {
        if (userRepository.existsByUsername(dto.getUsername())) throw new IllegalArgumentException("Username đã tồn tại");
        if (userRepository.existsByEmail(dto.getEmail())) throw new IllegalArgumentException("Email đã tồn tại");
        if (!dto.getPassword().equals(dto.getConfirmPassword())) throw new IllegalArgumentException("Mật khẩu xác nhận không đúng");
        Role role = roleRepository.findByName("ROLE_USER").or(() -> roleRepository.findByName("USER"))
                .orElseThrow(() -> new IllegalStateException("Database chưa có ROLE_USER/USER"));
        userRepository.save(User.builder().username(dto.getUsername().trim()).email(dto.getEmail().trim().toLowerCase())
                .password(passwordEncoder.encode(dto.getPassword())).fullName(dto.getFullName().trim())
                .enabled(false).role(role).build());
        otpService.sendRegisterOtp(dto.getEmail().trim().toLowerCase());
    }

    @Override @Transactional
    public boolean verifyRegister(String email, String otp) {
        if (!otpService.verifyRegisterOtp(email, otp)) return false;
        User user = userRepository.findByEmail(email).orElseThrow(() -> new IllegalArgumentException("User không tồn tại"));
        user.setEnabled(true);
        return true;
    }

    @Override @Transactional
    public void resendRegisterOtp(String email) {
        User user = userRepository.findByEmail(email).orElseThrow(() -> new IllegalArgumentException("Email chưa đăng ký"));
        if (user.isEnabled()) throw new IllegalArgumentException("Tài khoản đã được kích hoạt");
        otpService.sendRegisterOtp(email);
    }

    @Override @Transactional
    public void forgotPassword(String email) {
        if (!userRepository.existsByEmail(email)) throw new IllegalArgumentException("Email không tồn tại");
        otpService.sendResetPasswordOtp(email);
    }

    @Override @Transactional
    public void resetPassword(String email, String otp, String password) {
        User user = userRepository.findByEmail(email).orElseThrow(() -> new IllegalArgumentException("Email không tồn tại"));
        if (!otpService.verifyResetPasswordOtp(email, otp)) throw new IllegalArgumentException("OTP không hợp lệ, đã hết hạn hoặc vượt quá số lần thử");
        user.setPassword(passwordEncoder.encode(password));
    }
}
