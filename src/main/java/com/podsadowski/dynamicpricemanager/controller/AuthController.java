package com.podsadowski.dynamicpricemanager.controller;

import com.podsadowski.dynamicpricemanager.dto.RegisterDto;
import com.podsadowski.dynamicpricemanager.service.UserService;
import jakarta.validation.Valid;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;

@Controller
public class AuthController {

    private final UserService userService;

    public AuthController(UserService userService) {
        this.userService = userService;
    }

    @GetMapping("/login")
    public String login() {
        return "login";
    }

    @GetMapping("/register")
    public String register(Model model) {
        model.addAttribute("registerDto", new RegisterDto());
        return "register";
    }

    @PostMapping("/register")
    public String registerUser(@Valid @ModelAttribute("registerDto") RegisterDto registerDto,
                               BindingResult bindingResult,
                               Model model) {
        if (bindingResult.hasErrors()) {
            return "register";
        }
        if (!registerDto.getPassword().equals(registerDto.getConfirmPassword())) {
            model.addAttribute("passwordMismatch", true);
            return "register";
        }
        try {
            userService.registerUser(registerDto, "CLIENT");
        } catch (IllegalArgumentException ex) {
            model.addAttribute("error", ex.getMessage());
            return "register";
        }
        return "redirect:/login?registered";
    }

    @GetMapping("/admin")
    public String adminDashboard(Model model) {
        model.addAttribute("activeTab", "dashboard");
        return "admin";
    }
}
