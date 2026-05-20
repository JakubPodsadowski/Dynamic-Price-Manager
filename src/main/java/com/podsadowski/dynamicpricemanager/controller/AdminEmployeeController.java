package com.podsadowski.dynamicpricemanager.controller;

import com.podsadowski.dynamicpricemanager.entity.Employee;
import com.podsadowski.dynamicpricemanager.entity.SaloonService;
import com.podsadowski.dynamicpricemanager.service.EmployeeService;
import com.podsadowski.dynamicpricemanager.service.SaloonServiceManager;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

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
        model.addAttribute("employees", employeeService.getAllEmployees());
        model.addAttribute("allServices", saloonServiceManager.getAllServices());
        return "admin-employees";
    }

    @PostMapping("/add")
    public String addEmployee(@RequestParam String firstName,
                              @RequestParam String lastName,
                              @RequestParam String specialization,
                              @RequestParam(required = false) List<Long> serviceIds) {

        Employee employee = new Employee();
        employee.setFirstName(firstName);
        employee.setLastName(lastName);
        employee.setSpecialization(specialization);

        if (serviceIds != null) {
            Set<SaloonService> services = new HashSet<>(saloonServiceManager.getServicesByIds(serviceIds));
            employee.setServices(services);
        }

        employeeService.addEmployee(employee);
        return "redirect:/admin/employees";
    }

    @PostMapping("/edit")
    public String editEmployee(@RequestParam("id") Long id,
                               @RequestParam("firstName") String firstName,
                               @RequestParam("lastName") String lastName,
                               @RequestParam("specialization") String specialization,
                               @RequestParam(value = "serviceIds", required = false) List<Long> serviceIds) {

        Employee employee = employeeService.getEmployeeById(id);
        employee.setFirstName(firstName);
        employee.setLastName(lastName);
        employee.setSpecialization(specialization);

        if (serviceIds != null && !serviceIds.isEmpty()) {
            Set<SaloonService> services = new HashSet<>(saloonServiceManager.getServicesByIds(serviceIds));
            employee.setServices(services);
        } else {
            employee.setServices(new HashSet<>());
        }

        employeeService.updateEmployee(employee);
        return "redirect:/admin/employees";
    }

    @PostMapping("/delete/{id}")
    public String deleteEmployee(@PathVariable Long id) {
        employeeService.deleteEmployee(id);
        return "redirect:/admin/employees";
    }
}