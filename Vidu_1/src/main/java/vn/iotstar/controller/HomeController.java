package vn.iotstar.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class HomeController {

    @GetMapping("/")
    String home() {
        return "home";
    }

    @GetMapping({"/admin", "/dashboard"})
    String admin() {
        return "admin";
    }
}
