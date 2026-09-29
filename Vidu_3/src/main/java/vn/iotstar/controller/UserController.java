package vn.iotstar.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import vn.iotstar.dto.*;
import vn.iotstar.service.UserService;

@Controller @RequestMapping("/users") @RequiredArgsConstructor
public class UserController {
    private final UserService userService;
    @GetMapping public String list(@RequestParam(defaultValue = "") String keyword, @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size, Model model) {
        model.addAttribute("users", userService.findAll(keyword, page, size)); model.addAttribute("keyword", keyword); model.addAttribute("size", size); return "users/list";
    }
    @GetMapping("/create") public String create(Model model) { model.addAttribute("adminUserCreateDTO", new AdminUserCreateDTO()); return "users/create"; }
    @PostMapping("/create") public String create(@Valid @ModelAttribute("adminUserCreateDTO") AdminUserCreateDTO dto, BindingResult result, RedirectAttributes redirect) {
        if (!dto.getPassword().equals(dto.getConfirmPassword())) result.rejectValue("confirmPassword", "mismatch", "Mật khẩu xác nhận không đúng");
        if (result.hasErrors()) return "users/create";
        try { userService.create(dto); redirect.addFlashAttribute("success", "Tạo user thành công."); return "redirect:/users"; }
        catch (IllegalArgumentException e) { result.reject("user.error", e.getMessage()); return "users/create"; }
    }
    @GetMapping("/edit/{id}") public String edit(@PathVariable Long id, Model model) { model.addAttribute("userDTO", userService.findById(id)); return "users/form"; }
    @PostMapping("/edit/{id}") public String edit(@PathVariable Long id, @Valid @ModelAttribute("userDTO") UserDTO dto, BindingResult result, RedirectAttributes redirect) {
        if (result.hasErrors()) return "users/form";
        try { userService.update(id, dto); redirect.addFlashAttribute("success", "Cập nhật user thành công."); return "redirect:/users"; }
        catch (IllegalArgumentException e) { result.reject("user.error", e.getMessage()); return "users/form"; }
    }
    @PostMapping("/delete/{id}") public String delete(@PathVariable Long id, Authentication auth, RedirectAttributes redirect) {
        try { userService.delete(id, auth.getName()); redirect.addFlashAttribute("success", "Xóa user thành công."); }
        catch (IllegalArgumentException e) { redirect.addFlashAttribute("error", e.getMessage()); }
        return "redirect:/users";
    }
}
