package com.podsadowski.dynamicpricemanager.controller;

import com.podsadowski.dynamicpricemanager.dto.SalonServiceFormDto;
import com.podsadowski.dynamicpricemanager.service.SaloonServiceManager;
import jakarta.validation.Valid;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.validation.ObjectError;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.stream.Collectors;

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
        model.addAttribute("services", saloonServiceManager.listAllDtos());
        if (!model.containsAttribute("saloonServiceForm")) {
            model.addAttribute("saloonServiceForm", new SalonServiceFormDto());
        }
        return "admin-services";
    }

    @PostMapping("/add")
    public String addService(@Valid @ModelAttribute("saloonServiceForm") SalonServiceFormDto saloonServiceForm,
                             BindingResult bindingResult,
                             Model model) {
        if (bindingResult.hasErrors()) {
            model.addAttribute("activeTab", "services");
            model.addAttribute("services", saloonServiceManager.listAllDtos());
            return "admin-services";
        }
        saloonServiceManager.addServiceFromForm(saloonServiceForm);
        return "redirect:/admin/services?success";
    }

    @PostMapping("/edit")
    public String editService(@Valid @ModelAttribute("saloonServiceForm") SalonServiceFormDto saloonServiceForm,
                              BindingResult bindingResult,
                              RedirectAttributes redirectAttributes) {
        if (bindingResult.hasErrors()) {
            String message = bindingResult.getAllErrors().stream()
                    .map(ObjectError::getDefaultMessage)
                    .collect(Collectors.joining(" "));
            redirectAttributes.addFlashAttribute("error", message.isBlank() ? "Check service details." : message);
            return "redirect:/admin/services";
        }
        saloonServiceManager.updateServiceFromForm(saloonServiceForm);
        return "redirect:/admin/services";
    }

    @PostMapping("/delete/{id}")
    public String deleteService(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        try {
            saloonServiceManager.deleteService(id);
        } catch (IllegalStateException ex) {
            redirectAttributes.addFlashAttribute("error", ex.getMessage());
        }
        return "redirect:/admin/services";
    }
}
