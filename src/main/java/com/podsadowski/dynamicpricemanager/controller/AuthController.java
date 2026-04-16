package com.podsadowski.dynamicpricemanager.controller;

import com.podsadowski.dynamicpricemanager.entity.SaloonService;
import com.podsadowski.dynamicpricemanager.service.SaloonServiceManager;
import com.podsadowski.dynamicpricemanager.service.UserService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Controller
public class AuthController {

    private final UserService userService;
    private final SaloonServiceManager saloonServiceManager;

    public AuthController(UserService userService, SaloonServiceManager saloonServiceManager) {
        this.userService = userService;
        this.saloonServiceManager = saloonServiceManager;
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
    public String showAdminPanel(Model model) {
        model.addAttribute("services", saloonServiceManager.getAllServices());
        return "admin";
    }

    @GetMapping("/client")
    public String showClientPanel() {
        return "client";
    }

    @PostMapping("/admin/addservice")
    @ResponseBody
    public ResponseEntity<SaloonService> addService(@RequestBody SaloonService service) {
        SaloonService saved = saloonServiceManager.addService(service);
        return ResponseEntity.status(201).body(saved);
    }

    @PostMapping("/admin/addservice-ui")
    public String addServiceFromUI(@RequestParam String name,
                                   @RequestParam Double price,
                                   @RequestParam String description,
                                   @RequestParam Integer duration) {

        SaloonService service = new SaloonService(name, price, description, duration);

        saloonServiceManager.addService(service);

        return "redirect:/admin";
    }
}
