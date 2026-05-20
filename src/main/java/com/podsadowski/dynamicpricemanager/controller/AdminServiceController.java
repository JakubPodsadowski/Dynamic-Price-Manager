package com.podsadowski.dynamicpricemanager.controller;

import com.podsadowski.dynamicpricemanager.entity.SaloonService;
import com.podsadowski.dynamicpricemanager.service.SaloonServiceManager;
import jakarta.validation.Valid;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;

@Controller
@RequestMapping("/admin/services")
public class AdminServiceController {

    private final SaloonServiceManager saloonServiceManager;

    public AdminServiceController(SaloonServiceManager saloonServiceManager) {
        this.saloonServiceManager = saloonServiceManager;
    }

    @GetMapping
    public String manageServices(Model model) {
        model.addAttribute("activeTab", "services");
        model.addAttribute("services", saloonServiceManager.getAllServices());
        if (!model.containsAttribute("saloonService")) {
            model.addAttribute("saloonService", new SaloonService());
        }
        return "admin-services";
    }

    @PostMapping("/add")
    public String addService(@Valid @ModelAttribute("saloonService") SaloonService service,
                             BindingResult bindingResult,
                             Model model) {
        if (bindingResult.hasErrors()) {
            model.addAttribute("activeTab", "services");
            model.addAttribute("services", saloonServiceManager.getAllServices());
            return "admin-services";
        }

        saloonServiceManager.addService(service);
        return "redirect:/admin/services?success";
    }

    @PostMapping("/edit")
    public String editService(@ModelAttribute SaloonService service) {
        saloonServiceManager.updateService(service);
        return "redirect:/admin/services";
    }

    @PostMapping("/delete/{id}")
    public String deleteService(@PathVariable Long id) {
        saloonServiceManager.deleteService(id);
        return "redirect:/admin/services";
    }
}