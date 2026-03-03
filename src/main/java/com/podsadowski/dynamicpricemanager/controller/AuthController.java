package com.podsadowski.dynamicpricemanager.controller;

import com.podsadowski.dynamicpricemanager.service.UserService;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
public class AuthController {

    private final UserService userService;

    public AuthController(UserService userService) {
        this.userService = userService;
    }

    @GetMapping("/login")
    public String showLoginForm() {
        return "login";
    }

    @GetMapping("/register")
    public String showRegisterForm() {
        return "register";
    }

    @PostMapping("/register")
    public String registerUser(@RequestParam("email") String email,
                               @RequestParam("password") String password) {
        userService.registerUser(email, password, "CLIENT");

        return "redirect:/login?registered";
    }

    @GetMapping("/admin")
    public String showAdminPanel() {
        return "admin";
    }

    @GetMapping("/client")
    public String showClientPanel() {
        return "client";
    }
}
