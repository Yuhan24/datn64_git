package com.poly.shopquanao.controller.client;

import com.poly.shopquanao.dto.client.RegisterDTO;
import com.poly.shopquanao.service.AuthService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;

@Controller
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

    @GetMapping("/login")
    public String login() {
        return "client/login";
    }

    @GetMapping("/register")
    public String register() {
        return "client/register";
    }

    @PostMapping("/register")
    public String doRegister(RegisterDTO dto) {
        try {
            authService.register(dto);
            return "redirect:/login?registered";
        } catch (RuntimeException e) {
            return "redirect:/register?error";
        }
    }

    @GetMapping("/redirect-by-role")
    public String redirectAfterLogin(Authentication auth) {

        if (auth.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN")
                        || a.getAuthority().equals("ROLE_STAFF"))) {
            return "redirect:/admin/dashboard";
        }

        return "redirect:/shop/home";
    }
}