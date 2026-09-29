package vn.iotstar.controller;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import vn.iotstar.service.*;
@Controller @RequiredArgsConstructor
public class HomeController {
    private final UserService userService; private final ProductService productService;
    @GetMapping("/") public String home(Authentication auth, Model model) {
        boolean admin = auth != null && auth.getAuthorities().stream().anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN"));
        model.addAttribute("userCount", userService.countUsers());
        model.addAttribute("productCount", auth == null ? 0 : productService.countProducts(auth.getName(), admin));
        return "home";
    }
    @GetMapping("/403") public String forbidden() { return "error/403"; }
}
