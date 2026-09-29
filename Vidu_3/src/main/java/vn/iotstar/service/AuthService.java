package vn.iotstar.service;
import vn.iotstar.dto.RegisterDTO;
public interface AuthService {
    void register(RegisterDTO dto);
    boolean verifyRegister(String email, String otp);
    void resendRegisterOtp(String email);
    void forgotPassword(String email);
    void resetPassword(String email, String otp, String password);
}
