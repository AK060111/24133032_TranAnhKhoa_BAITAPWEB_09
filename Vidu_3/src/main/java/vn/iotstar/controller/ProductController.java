package vn.iotstar.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import vn.iotstar.dto.ProductDTO;
import vn.iotstar.service.ProductService;

@Controller @RequestMapping("/products") @RequiredArgsConstructor
public class ProductController {
    private final ProductService productService;
    private boolean isAdmin(Authentication auth) { return auth.getAuthorities().stream().anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN")); }

    @GetMapping public String list(@RequestParam(defaultValue = "") String keyword, @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size, Authentication auth, Model model) {
        model.addAttribute("products", productService.findAll(keyword, page, size, auth.getName(), isAdmin(auth)));
        model.addAttribute("keyword", keyword); model.addAttribute("size", size); return "products/list";
    }
    @GetMapping("/create") public String create(Model model) { model.addAttribute("productDTO", new ProductDTO()); model.addAttribute("mode", "create"); return "products/form"; }
    @PostMapping("/create") public String create(@Valid @ModelAttribute("productDTO") ProductDTO dto, BindingResult result,
            @RequestParam(name = "image", required = false) MultipartFile image, Authentication auth, Model model, RedirectAttributes redirect) {
        if (result.hasErrors()) { model.addAttribute("mode", "create"); return "products/form"; }
        try { productService.create(dto, image, auth.getName()); redirect.addFlashAttribute("success", "Tạo sản phẩm thành công."); return "redirect:/products"; }
        catch (IllegalArgumentException | IllegalStateException e) { result.reject("product.error", e.getMessage()); model.addAttribute("mode", "create"); return "products/form"; }
    }
    @GetMapping("/edit/{id}") public String edit(@PathVariable Long id, Authentication auth, Model model) {
        model.addAttribute("productDTO", productService.findById(id, auth.getName(), isAdmin(auth))); model.addAttribute("mode", "edit"); return "products/form";
    }
    @PostMapping("/edit/{id}") public String edit(@PathVariable Long id, @Valid @ModelAttribute("productDTO") ProductDTO dto, BindingResult result,
            @RequestParam(name = "image", required = false) MultipartFile image, Authentication auth, Model model, RedirectAttributes redirect) {
        if (result.hasErrors()) { model.addAttribute("mode", "edit"); return "products/form"; }
        try { productService.update(id, dto, image, auth.getName(), isAdmin(auth)); redirect.addFlashAttribute("success", "Cập nhật sản phẩm thành công."); return "redirect:/products"; }
        catch (IllegalArgumentException | IllegalStateException e) { result.reject("product.error", e.getMessage()); model.addAttribute("mode", "edit"); return "products/form"; }
    }
    @PostMapping("/delete/{id}") public String delete(@PathVariable Long id, Authentication auth, RedirectAttributes redirect) {
        productService.delete(id, auth.getName(), isAdmin(auth)); redirect.addFlashAttribute("success", "Xóa sản phẩm thành công."); return "redirect:/products";
    }
}
