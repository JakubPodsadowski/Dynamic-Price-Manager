package com.podsadowski.dynamicpricemanager.controller;

import com.podsadowski.dynamicpricemanager.dto.EmployeeSaveDto;
import com.podsadowski.dynamicpricemanager.service.EmployeeService;
import com.podsadowski.dynamicpricemanager.service.SaloonServiceManager;
import jakarta.validation.Valid;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.validation.ObjectError;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.stream.Collectors;

@Controller
@RequestMapping("/admin/employees")
public class AdminEmployeeController {

    private final EmployeeService employeeService;
    private final SaloonServiceManager saloonServiceManager;

    public AdminEmployeeController(EmployeeService employeeService, SaloonServiceManager saloonServiceManager) {
        this.employeeService = employeeService;
        this.saloonServiceManager = saloonServiceManager;
    }

    @GetMapping
    public String manageEmployees(Model model) {
        model.addAttribute("activeTab", "employees");
        model.addAttribute("employees", employeeService.getAllEmployeeDtos());
        model.addAttribute("allServices", saloonServiceManager.listAllDtos());
        model.addAttribute("openAddModal", false);
        if (!model.containsAttribute("employeeForm")) {
            model.addAttribute("employeeForm", new EmployeeSaveDto());
        }
        return "admin-employees";
    }

    @PostMapping("/save")
    public String saveEmployee(@Valid @ModelAttribute("employeeForm") EmployeeSaveDto employeeForm,
                               BindingResult bindingResult,
                               Model model,
                               RedirectAttributes redirectAttributes) {
        if (bindingResult.hasErrors()) {
            model.addAttribute("activeTab", "employees");
            model.addAttribute("employees", employeeService.getAllEmployeeDtos());
            model.addAttribute("allServices", saloonServiceManager.listAllDtos());
            model.addAttribute("employeeFormSaveErrors", bindingResult.getAllErrors().stream()
                    .map(ObjectError::getDefaultMessage)
                    .collect(Collectors.toList()));
            if (employeeForm.getId() == null) {
                model.addAttribute("openAddModal", true);
            } else {
                model.addAttribute("reopenEditEmployeeId", employeeForm.getId());
            }
            return "admin-employees";
        }
        try {
            employeeService.saveOrUpdateEmployee(employeeForm);
        } catch (IllegalArgumentException ex) {
            redirectAttributes.addFlashAttribute("error", ex.getMessage());
        }
        return "redirect:/admin/employees";
    }

    @PostMapping("/delete/{id}")
    public String deleteEmployee(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        try {
            employeeService.deleteEmployee(id);
        } catch (IllegalStateException ex) {
            redirectAttributes.addFlashAttribute("error", ex.getMessage());
        }
        return "redirect:/admin/employees";
    }
}
