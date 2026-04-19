package com.podsadowski.dynamicpricemanager.controller;

import com.podsadowski.dynamicpricemanager.entity.Employee;
import com.podsadowski.dynamicpricemanager.entity.Reservation;
import com.podsadowski.dynamicpricemanager.entity.SaloonService;
import com.podsadowski.dynamicpricemanager.service.EmployeeService;
import com.podsadowski.dynamicpricemanager.service.ReservationService;
import com.podsadowski.dynamicpricemanager.service.SaloonServiceManager;
import com.podsadowski.dynamicpricemanager.service.UserService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Controller
public class AuthController {

    private final UserService userService;
    private final SaloonServiceManager saloonServiceManager;
    private final ReservationService reservationService;
    private final EmployeeService employeeService;

    public AuthController(UserService userService, SaloonServiceManager saloonServiceManager,
                          ReservationService reservationService, EmployeeService employeeService) {
        this.userService = userService;
        this.saloonServiceManager = saloonServiceManager;
        this.reservationService = reservationService;
        this.employeeService = employeeService;
    }

    @GetMapping("/login")
    public String login() {
        return "login";
    }

    @GetMapping("/register")
    public String register() {
        return "register";
    }

    @PostMapping("/register")
    public String registerUser(@RequestParam String email,
                               @RequestParam String password,
                               @RequestParam String confirmPassword,
                               Model model) {
        if (!password.equals(confirmPassword)) {
            model.addAttribute("error", "Hasła nie są identyczne!");
            return "register";
        }
        userService.registerUser(email, password, "CLIENT");
        return "redirect:/login?registered";
    }

    @GetMapping("/client")
    public String clientPanel(Model model) {
        model.addAttribute("services", saloonServiceManager.getAllServices());
        return "client";
    }

    @PostMapping("/client/reserve")
    public String processReservation(@RequestParam String serviceName, Principal principal) {
        String userEmail = principal.getName();
        Reservation res = new Reservation();
        res.setService(serviceName);
        res.setName(userEmail);
        reservationService.addReservation(res);
        return "redirect:/client?success";
    }

    @GetMapping("/admin")
    public String adminDashboard(Model model) {
        model.addAttribute("activeTab", "dashboard");
        return "admin";
    }

    @GetMapping("/admin/services")
    public String manageServices(Model model) {
        model.addAttribute("activeTab", "services");
        model.addAttribute("services", saloonServiceManager.getAllServices());
        return "admin-services";
    }

    @PostMapping("/admin/addservice-ui")
    public String addService(@ModelAttribute SaloonService service) {
        saloonServiceManager.addService(service);
        return "redirect:/admin/services";
    }

    @PostMapping("/admin/services/edit")
    public String editService(@ModelAttribute SaloonService service) {
        saloonServiceManager.updateService(service);
        return "redirect:/admin/services";
    }

    @PostMapping("/admin/services/delete/{id}")
    public String deleteService(@PathVariable Long id) {
        saloonServiceManager.deleteService(id);
        return "redirect:/admin/services";
    }

    @GetMapping("/admin/employees")
    public String manageEmployees(Model model) {
        model.addAttribute("activeTab", "employees");
        model.addAttribute("employees", employeeService.getAllEmployees());
        model.addAttribute("allServices", saloonServiceManager.getAllServices());
        return "admin-employees";
    }

    @PostMapping("/admin/addemployee")
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

    @PostMapping("/admin/employees/edit")
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

    @PostMapping("/admin/employees/delete/{id}")
    public String deleteEmployee(@PathVariable Long id) {
        employeeService.deleteEmployee(id);
        return "redirect:/admin/employees";
    }
}